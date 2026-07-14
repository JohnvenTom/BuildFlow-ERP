package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.WmsStockTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调拨单主表Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供WmsStockTransfer实体的基础CRUD操作。
 */
@Mapper
public interface WmsStockTransferMapper extends BaseMapper<WmsStockTransfer> {
}
