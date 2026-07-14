package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SalesOrder实体的基础CRUD操作。
 */
@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
}
