package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.PurchaseReturn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购退货单主表Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供PurchaseReturn实体的基础CRUD操作。
 */
@Mapper
public interface PurchaseReturnMapper extends BaseMapper<PurchaseReturn> {
}
