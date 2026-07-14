package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsStockCheckItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点单明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsStockCheckItem实体的基础CRUD操作。
 */
@Mapper
public interface WmsStockCheckItemMapper extends BaseMapper<WmsStockCheckItem> {
}
