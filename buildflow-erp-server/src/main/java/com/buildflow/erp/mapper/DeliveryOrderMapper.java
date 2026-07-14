package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.DeliveryOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工地出库单Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供DeliveryOrder实体的基础CRUD操作。
 */
@Mapper
public interface DeliveryOrderMapper extends BaseMapper<DeliveryOrder> {
}
