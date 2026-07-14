package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.PurchaseReturnItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购退货单明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供PurchaseReturnItem实体的基础CRUD操作。
 */
@Mapper
public interface PurchaseReturnItemMapper extends BaseMapper<PurchaseReturnItem> {
}
