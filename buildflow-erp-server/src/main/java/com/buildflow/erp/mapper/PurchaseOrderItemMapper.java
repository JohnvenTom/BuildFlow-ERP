package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供PurchaseOrderItem实体的基础CRUD操作。
 */
@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
