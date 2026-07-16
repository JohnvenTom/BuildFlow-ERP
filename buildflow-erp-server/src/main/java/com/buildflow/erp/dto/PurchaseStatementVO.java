package com.buildflow.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度采购对账单视图对象
 * 用于按供应商汇总指定月份的采购金额、退货金额及应付净额
 */
@Data
public class PurchaseStatementVO {

    /** 供应商名称 */
    private String supplierName;

    /** 采购总金额 */
    private BigDecimal purchaseAmount;

    /** 退货总金额 */
    private BigDecimal returnAmount;

    /** 应付净额（采购总额 - 退货总额） */
    private BigDecimal netAmount;
}
