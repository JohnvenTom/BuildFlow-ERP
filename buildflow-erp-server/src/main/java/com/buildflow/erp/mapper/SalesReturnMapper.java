package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SalesReturn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售退货单Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SalesReturn实体的基础CRUD操作。
 */
@Mapper
public interface SalesReturnMapper extends BaseMapper<SalesReturn> {
}
