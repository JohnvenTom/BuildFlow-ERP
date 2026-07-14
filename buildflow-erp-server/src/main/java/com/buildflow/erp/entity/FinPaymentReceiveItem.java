package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 回款核销明细实体
 * 对应数据库表 fin_payment_receive_item，映射回款单与应收台账的核销对应关系。
 * 一张回款单可核销多条应收台账记录，审核时按明细金额更新应收台账的已回款和剩余金额。
 *
 * @TableName fin_payment_receive_item
 */
@Data
@TableName("fin_payment_receive_item")
public class FinPaymentReceiveItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 回款单ID，关联 fin_payment_receive.id */
    private Long receiveId;

    /** 应收台账ID，关联 fin_receivable.id */
    private Long receivableId;

    /** 本次核销金额 */
    private BigDecimal amount;
}
