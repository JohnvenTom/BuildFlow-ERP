package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 付款核销明细实体
 * 对应数据库表 fin_payment_pay_item，映射付款单与应付台账的核销对应关系。
 * 一张付款单可核销多条应付台账记录，审核时按明细金额更新应付台账的已付款和剩余金额。
 *
 * @TableName fin_payment_pay_item
 */
@Data
@TableName("fin_payment_pay_item")
public class FinPaymentPayItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 付款单ID，关联 fin_payment_pay.id */
    private Long payId;

    /** 应付台账ID，关联 fin_payable.id */
    private Long payableId;

    /** 本次核销金额 */
    private BigDecimal amount;
}
