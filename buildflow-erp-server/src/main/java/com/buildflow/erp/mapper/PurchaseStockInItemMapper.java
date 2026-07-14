package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.PurchaseStockInItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购入库单明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供PurchaseStockInItem实体的基础CRUD操作。
 */
@Mapper
public interface PurchaseStockInItemMapper extends BaseMapper<PurchaseStockInItem> {
}
