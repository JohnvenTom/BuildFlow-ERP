package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SalesReturnItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售退货明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SalesReturnItem实体的基础CRUD操作。
 */
@Mapper
public interface SalesReturnItemMapper extends BaseMapper<SalesReturnItem> {
}
