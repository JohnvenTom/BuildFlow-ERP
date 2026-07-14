package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.entity.FinPayable;
import com.buildflow.erp.mapper.CrmSupplierMapper;
import com.buildflow.erp.mapper.FinPayableMapper;
import com.buildflow.erp.service.FinPayableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 供应商应付台账服务实现类
 * 实现应付账款管理的核心业务逻辑，包括分页查询、自动生成应付记录、按供应商汇总应付数据等操作。
 */
@Service
public class FinPayableServiceImpl implements FinPayableService {

    @Autowired
    private FinPayableMapper finPayableMapper;

    @Autowired
    private CrmSupplierMapper crmSupplierMapper;

    /**
     * 按供应商分页查询应付台账
     * 支持按供应商ID和状态过滤，按创建时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      状态（0-正常 1-逾期 2-已结清），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页应付台账列表
     */
    @Override
    public R<PageResult<FinPayable>> page(Integer pageNum, Integer pageSize, Long supplierId, String status) {
        Page<FinPayable> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinPayable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(supplierId != null, FinPayable::getSupplierId, supplierId)
                .eq(StringUtils.hasText(status), FinPayable::getStatus, status)
                .orderByDesc(FinPayable::getCreateTime);
        Page<FinPayable> result = finPayableMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 入库审核后自动生成应付记录
     * 根据供应商ID、来源单据类型和ID、应付金额、到期日创建应付台账，
     * 初始已付款金额为0，剩余金额等于应付金额，状态为正常。
     *
     * @param supplierId 供应商ID
     * @param sourceType 来源单据类型（如：STOCK_IN-入库单）
     * @param sourceId   来源单据ID
     * @param amount     应付金额
     * @param dueDate    到期日
     */
    @Override
    public void addPayable(Long supplierId, String sourceType, Long sourceId, BigDecimal amount, LocalDate dueDate) {
        FinPayable payable = new FinPayable();
        payable.setSupplierId(supplierId);
        payable.setSourceType(sourceType);
        payable.setSourceId(sourceId);
        payable.setAmount(amount);
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setRemainAmount(amount);
        payable.setDueDate(dueDate);
        payable.setStatus(Constants.FIN_STATUS_NORMAL);
        finPayableMapper.insert(payable);
    }

    /**
     * 按供应商汇总应付总金额、已付款、剩余
     * 汇总所有未删除供应商的应付数据
     *
     * @return 汇总列表，每条包含supplierId、supplierName、totalAmount、paidAmount、remainAmount
     */
    @Override
    public R<List<Map<String, Object>>> summaryBySupplier() {
        // 查询全部应付记录
        LambdaQueryWrapper<FinPayable> wrapper = new LambdaQueryWrapper<>();
        List<FinPayable> allPayables = finPayableMapper.selectList(wrapper);

        // 按供应商ID分组
        Map<Long, List<FinPayable>> groupBySupplier = allPayables.stream()
                .collect(Collectors.groupingBy(FinPayable::getSupplierId));

        // 批量查询供应商名称
        LambdaQueryWrapper<CrmSupplier> supplierWrapper = new LambdaQueryWrapper<>();
        List<CrmSupplier> suppliers = crmSupplierMapper.selectList(supplierWrapper);
        Map<Long, String> supplierNameMap = suppliers.stream()
                .collect(Collectors.toMap(CrmSupplier::getId, CrmSupplier::getName, (a, b) -> a));

        // 按供应商汇总
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<FinPayable>> entry : groupBySupplier.entrySet()) {
            Long supplierId = entry.getKey();
            List<FinPayable> payables = entry.getValue();

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal paidAmount = BigDecimal.ZERO;
            BigDecimal remainAmount = BigDecimal.ZERO;

            for (FinPayable p : payables) {
                totalAmount = totalAmount.add(p.getAmount() == null ? BigDecimal.ZERO : p.getAmount());
                paidAmount = paidAmount.add(p.getPaidAmount() == null ? BigDecimal.ZERO : p.getPaidAmount());
                remainAmount = remainAmount.add(p.getRemainAmount() == null ? BigDecimal.ZERO : p.getRemainAmount());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("supplierId", supplierId);
            map.put("supplierName", supplierNameMap.getOrDefault(supplierId, "未知供应商"));
            map.put("totalAmount", totalAmount);
            map.put("paidAmount", paidAmount);
            map.put("remainAmount", remainAmount);
            result.add(map);
        }
        return R.ok(result);
    }
}
