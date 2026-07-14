package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.DeliveryOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工地出库明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供DeliveryOrderItem实体的基础CRUD操作。
 */
@Mapper
public interface DeliveryOrderItemMapper extends BaseMapper<DeliveryOrderItem> {
}
