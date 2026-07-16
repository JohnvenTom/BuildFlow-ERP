package com.buildflow.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.PurchaseStatementVO;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.entity.PurchaseOrder;
import com.buildflow.erp.entity.PurchaseReturn;
import com.buildflow.erp.mapper.CrmSupplierMapper;
import com.buildflow.erp.mapper.PurchaseOrderMapper;
import com.buildflow.erp.mapper.PurchaseReturnMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 月度采购对账控制器
 * 按供应商汇总指定月份的采购金额、退货金额及应付净额，用于供应商月度对账
 */
@RestController
@RequestMapping("/purchase/statement")
public class PurchaseStatementController {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseReturnMapper purchaseReturnMapper;

    @Autowired
    private CrmSupplierMapper crmSupplierMapper;

    /**
     * 查询月度采购对账单
     * 按供应商汇总指定月份的采购总金额、退货总金额及应付净额。
     * 不指定月份时统计全部数据，不指定供应商时统计全部供应商。
     *
     * @param supplierId 供应商ID，可为空（统计全部供应商）
     * @param month      月份，格式 YYYY-MM（如 2026-07），可为空（统计全部月份）
     * @return 对账单列表，按供应商汇总
     */
    @PreAuthorize("@ps.hasPermission('purchase:statement:list')")
    @GetMapping
    public R<List<PurchaseStatementVO>> list(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String month) {

        // 计算月份起止时间
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (month != null && !month.isEmpty()) {
            YearMonth ym = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
            startTime = ym.atDay(1).atStartOfDay();
            endTime = ym.plusMonths(1).atDay(1).atStartOfDay();
        }

        // 查询采购订单（排除已作废），按供应商分组汇总
        LambdaQueryWrapper<PurchaseOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ne(PurchaseOrder::getStatus, "已作废");
        if (supplierId != null) {
            orderWrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (startTime != null) {
            orderWrapper.ge(PurchaseOrder::getCreateTime, startTime);
            orderWrapper.lt(PurchaseOrder::getCreateTime, endTime);
        }
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(orderWrapper);
        Map<Long, BigDecimal> purchaseMap = orders.stream()
                .collect(Collectors.groupingBy(
                        PurchaseOrder::getSupplierId,
                        Collectors.reducing(BigDecimal.ZERO, PurchaseOrder::getTotalAmount, BigDecimal::add)));

        // 查询采购退货（排除已作废），按供应商分组汇总
        LambdaQueryWrapper<PurchaseReturn> returnWrapper = new LambdaQueryWrapper<>();
        returnWrapper.ne(PurchaseReturn::getStatus, "已作废");
        if (supplierId != null) {
            returnWrapper.eq(PurchaseReturn::getSupplierId, supplierId);
        }
        if (startTime != null) {
            returnWrapper.ge(PurchaseReturn::getCreateTime, startTime);
            returnWrapper.lt(PurchaseReturn::getCreateTime, endTime);
        }
        List<PurchaseReturn> returns = purchaseReturnMapper.selectList(returnWrapper);
        Map<Long, BigDecimal> returnMap = returns.stream()
                .collect(Collectors.groupingBy(
                        PurchaseReturn::getSupplierId,
                        Collectors.reducing(BigDecimal.ZERO, PurchaseReturn::getTotalAmount, BigDecimal::add)));

        // 合并所有涉及的供应商ID
        Map<Long, BigDecimal> allSupplierIds = new LinkedHashMap<>();
        allSupplierIds.putAll(purchaseMap);
        allSupplierIds.putAll(returnMap);

        // 批量查询供应商名称
        Map<Long, String> supplierNameMap = new java.util.HashMap<>();
        if (!allSupplierIds.isEmpty()) {
            List<CrmSupplier> suppliers = crmSupplierMapper.selectBatchIds(allSupplierIds.keySet());
            supplierNameMap = suppliers.stream()
                    .collect(Collectors.toMap(CrmSupplier::getId, CrmSupplier::getName));
        }

        // 构建对账单结果
        List<PurchaseStatementVO> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : allSupplierIds.entrySet()) {
            Long sid = entry.getKey();
            PurchaseStatementVO vo = new PurchaseStatementVO();
            vo.setSupplierName(supplierNameMap.getOrDefault(sid, "未知供应商"));
            BigDecimal purchaseAmount = purchaseMap.getOrDefault(sid, BigDecimal.ZERO);
            BigDecimal returnAmount = returnMap.getOrDefault(sid, BigDecimal.ZERO);
            vo.setPurchaseAmount(purchaseAmount);
            vo.setReturnAmount(returnAmount);
            vo.setNetAmount(purchaseAmount.subtract(returnAmount));
            result.add(vo);
        }

        return R.ok(result);
    }
}
