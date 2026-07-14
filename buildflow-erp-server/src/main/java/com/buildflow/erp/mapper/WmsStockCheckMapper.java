package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsStockCheck;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点单主表Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsStockCheck实体的基础CRUD操作。
 */
@Mapper
public interface WmsStockCheckMapper extends BaseMapper<WmsStockCheck> {
}
