package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品档案Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsProduct实体的基础CRUD操作。
 */
@Mapper
public interface WmsProductMapper extends BaseMapper<WmsProduct> {
}
