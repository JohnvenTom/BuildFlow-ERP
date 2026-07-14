package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsStockTransferItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调拨单明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsStockTransferItem实体的基础CRUD操作。
 */
@Mapper
public interface WmsStockTransferItemMapper extends BaseMapper<WmsStockTransferItem> {
}
