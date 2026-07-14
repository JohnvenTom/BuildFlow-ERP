package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购退货单主表实体
 * 对应数据库表 purchase_return，映射采购退货单基本信息，包括供应商、关联入库单、退货金额、状态等。
 * 审核退货单时通过事务调用WmsInventoryService.decreaseStock扣减库存，并减少供应商应付台账。
 *
 * @TableName purchase_return
 */
@Data
@TableName("purchase_return")
public class PurchaseReturn {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 退货单号，自动生成，格式如 PR202607140001 */
    private String returnNo;

    /** 供应商ID，关联 crm_supplier.id */
    private Long supplierId;

    /** 关联入库单ID，关联 purchase_stock_in.id */
    private Long stockInId;

    /** 退货总金额 */
    private BigDecimal totalAmount;

    /** 单据状态（0-草稿 1-已审核 2-已作废） */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建人ID，关联 sys_user.id */
    private Long createBy;

    /** 审核人ID，关联 sys_user.id */
    private Long auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 逻辑删除标识（0-未删除 1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间，新增时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，新增和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
