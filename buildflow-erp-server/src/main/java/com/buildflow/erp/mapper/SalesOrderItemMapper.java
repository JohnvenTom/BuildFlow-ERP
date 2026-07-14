package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SalesOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SalesOrderItem实体的基础CRUD操作。
 */
@Mapper
public interface SalesOrderItemMapper extends BaseMapper<SalesOrderItem> {
}
