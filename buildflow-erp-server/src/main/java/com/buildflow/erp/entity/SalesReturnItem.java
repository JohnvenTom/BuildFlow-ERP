package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售退货明细实体
 * 对应数据库表 sales_return_item，映射退货单中每个商品的数量和单价。
 * 一个退货单可包含多条明细记录，审核时按明细逐条调用increaseStock返还库存。
 *
 * @TableName sales_return_item
 */
@Data
@TableName("sales_return_item")
public class SalesReturnItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 退货单ID，关联 sales_return.id */
    private Long returnId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 退货数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;
}
