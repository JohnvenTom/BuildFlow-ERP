package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.DashboardData;
import com.buildflow.erp.entity.*;
import com.buildflow.erp.mapper.*;
import com.buildflow.erp.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据大屏服务实现类
 * 聚合统计所有大屏数据，使用LambdaQueryWrapper + selectList + Java Stream进行聚合，
 * 避免复杂SQL，保持代码可维护性。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private WmsProductMapper wmsProductMapper;

    /**
     * 聚合统计所有大屏数据
     * 一次性查询所有已审核销售订单，通过Java Stream进行多维聚合统计，
     * 避免多次数据库查询和复杂SQL。
     *
     * @return 大屏聚合数据
     */
    @Override
    public R<DashboardData> getDashboardData() {
        DashboardData data = new DashboardData();

        // 查询所有已审核销售订单
        LambdaQueryWrapper<SalesOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(SalesOrder::getStatus, "1");
        List<SalesOrder> auditedOrders = salesOrderMapper.selectList(orderWrapper);

        // 查询所有已审核销售订单明细
        List<Long> orderIds = auditedOrders.stream().map(SalesOrder::getId).collect(Collectors.toList());
        List<SalesOrderItem> allItems = new ArrayList<>();
        if (!orderIds.isEmpty()) {
            // 分批查询，防止IN列表过长
            int batchSize = 500;
            for (int i = 0; i < orderIds.size(); i += batchSize) {
                List<Long> batch = orderIds.subList(i, Math.min(i + batchSize, orderIds.size()));
                LambdaQueryWrapper<SalesOrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.in(SalesOrderItem::getOrderId, batch);
                allItems.addAll(salesOrderItemMapper.selectList(itemWrapper));
            }
        }

        // ===== 1. 今日销售额 =====
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal todaySales = auditedOrders.stream()
                .filter(o -> o.getAuditTime() != null
                        && !o.getAuditTime().isBefore(todayStart)
                        && !o.getAuditTime().isAfter(todayEnd))
                .map(SalesOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.setTodaySales(todaySales);

        // ===== 2. 本月总销量 =====
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        BigDecimal monthSales = auditedOrders.stream()
                .filter(o -> o.getAuditTime() != null && !o.getAuditTime().isBefore(monthStart))
                .map(SalesOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.setMonthSales(monthSales);

        // ===== 3. 总应收欠款 =====
        LambdaQueryWrapper<FinReceivable> receivableWrapper = new LambdaQueryWrapper<>();
        receivableWrapper.ne(FinReceivable::getStatus, "2"); // 未结清（状态非2-已结清）
        List<FinReceivable> unsettledReceivables = finReceivableMapper.selectList(receivableWrapper);
        BigDecimal totalReceivable = unsettledReceivables.stream()
                .map(FinReceivable::getRemainAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.setTotalReceivable(totalReceivable);

        // ===== 4. 呆滞库存数量（简化实现：库存商品总数） =====
        LambdaQueryWrapper<WmsInventory> inventoryWrapper = new LambdaQueryWrapper<>();
        inventoryWrapper.gt(WmsInventory::getQuantity, 0);
        Long dormantCount = wmsInventoryMapper.selectCount(inventoryWrapper);
        data.setDormantStockCount(dormantCount.intValue());

        // ===== 5. 客户类型销量分布 =====
        // 查询所有客户，建立id->客户映射
        List<CrmCustomer> allCustomers = crmCustomerMapper.selectList(null);
        Map<Long, CrmCustomer> customerMap = allCustomers.stream()
                .collect(Collectors.toMap(CrmCustomer::getId, c -> c, (a, b) -> a));
        // 按客户类型分组统计已审核销售订单金额
        Map<String, BigDecimal> typeAmountMap = new LinkedHashMap<>();
        for (SalesOrder order : auditedOrders) {
            CrmCustomer customer = customerMap.get(order.getCustomerId());
            String type = (customer != null && customer.getType() != null) ? customer.getType() : "未知";
            BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            typeAmountMap.merge(type, amount, BigDecimal::add);
        }
        List<Map<String, Object>> customerTypeSales = typeAmountMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("type", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        data.setCustomerTypeSales(customerTypeSales);

        // ===== 6. 商品热销排行TOP10 =====
        // 查询所有商品，建立id->商品映射
        List<WmsProduct> allProducts = wmsProductMapper.selectList(null);
        Map<Long, WmsProduct> productMap = allProducts.stream()
                .collect(Collectors.toMap(WmsProduct::getId, p -> p, (a, b) -> a));
        // 按商品分组统计已审核销售订单明细金额
        Map<Long, BigDecimal> productAmountMap = new LinkedHashMap<>();
        for (SalesOrderItem item : allItems) {
            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            productAmountMap.merge(item.getProductId(), amount, BigDecimal::add);
        }
        List<Map<String, Object>> hotProducts = productAmountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    WmsProduct product = productMap.get(e.getKey());
                    m.put("name", product != null ? product.getName() : "未知商品");
                    m.put("amount", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        data.setHotProducts(hotProducts);

        // ===== 7. 近30日销售额趋势 =====
        LocalDate today = LocalDate.now();
        Map<LocalDate, BigDecimal> dailyAmountMap = new TreeMap<>();
        // 初始化近30天每天为0
        for (int i = 29; i >= 0; i--) {
            dailyAmountMap.put(today.minusDays(i), BigDecimal.ZERO);
        }
        for (SalesOrder order : auditedOrders) {
            if (order.getAuditTime() != null) {
                LocalDate auditDate = order.getAuditTime().toLocalDate();
                if (dailyAmountMap.containsKey(auditDate)) {
                    BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                    dailyAmountMap.merge(auditDate, amount, BigDecimal::add);
                }
            }
        }
        List<Map<String, Object>> salesTrend = dailyAmountMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("date", e.getKey().toString());
                    m.put("amount", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        data.setSalesTrend(salesTrend);

        // ===== 8. 各仓库库存总量 =====
        // 查询所有仓库，建立id->仓库映射
        List<WmsWarehouse> allWarehouses = wmsWarehouseMapper.selectList(null);
        Map<Long, WmsWarehouse> warehouseMap = allWarehouses.stream()
                .collect(Collectors.toMap(WmsWarehouse::getId, w -> w, (a, b) -> a));
        // 查询所有库存记录
        List<WmsInventory> allInventory = wmsInventoryMapper.selectList(null);
        // 按仓库分组统计库存总量
        Map<Long, Integer> warehouseQuantityMap = new LinkedHashMap<>();
        for (WmsInventory inv : allInventory) {
            int qty = inv.getQuantity() != null ? inv.getQuantity() : 0;
            warehouseQuantityMap.merge(inv.getWarehouseId(), qty, Integer::sum);
        }
        List<Map<String, Object>> warehouseStock = warehouseQuantityMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    WmsWarehouse wh = warehouseMap.get(e.getKey());
                    m.put("name", wh != null ? wh.getName() : "未知仓库");
                    m.put("quantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        data.setWarehouseStock(warehouseStock);

        // ===== 9. 逾期欠款客户榜单 =====
        // 过滤逾期应收记录（状态为1-逾期，或到期日<今天且未结清）
        LocalDate now = LocalDate.now();
        List<FinReceivable> overdueReceivables = unsettledReceivables.stream()
                .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(now))
                .collect(Collectors.toList());
        // 按客户汇总逾期金额
        Map<Long, BigDecimal> overdueAmountMap = new LinkedHashMap<>();
        Map<Long, Integer> overdueDaysMap = new LinkedHashMap<>();
        for (FinReceivable r : overdueReceivables) {
            Long customerId = r.getCustomerId();
            BigDecimal remain = r.getRemainAmount() != null ? r.getRemainAmount() : BigDecimal.ZERO;
            overdueAmountMap.merge(customerId, remain, BigDecimal::add);
            // 取最大逾期天数
            int days = (int) java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), now);
            overdueDaysMap.merge(customerId, days, Math::max);
        }
        List<Map<String, Object>> overdueCustomers = overdueAmountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    CrmCustomer customer = customerMap.get(e.getKey());
                    m.put("name", customer != null ? customer.getName() : "未知客户");
                    m.put("amount", e.getValue());
                    m.put("days", overdueDaysMap.getOrDefault(e.getKey(), 0));
                    return m;
                })
                .collect(Collectors.toList());
        data.setOverdueCustomers(overdueCustomers);

        return R.ok(data);
    }
}
