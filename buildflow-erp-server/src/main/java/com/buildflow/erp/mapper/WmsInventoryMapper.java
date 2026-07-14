package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsInventory实体的基础CRUD操作。
 */
@Mapper
public interface WmsInventoryMapper extends BaseMapper<WmsInventory> {
}
