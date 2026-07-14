package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsWarehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库档案Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsWarehouse实体的基础CRUD操作。
 */
@Mapper
public interface WmsWarehouseMapper extends BaseMapper<WmsWarehouse> {
}
