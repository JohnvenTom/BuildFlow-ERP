package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.FinPayable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商应付台账Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供FinPayable实体的基础CRUD操作。
 */
@Mapper
public interface FinPayableMapper extends BaseMapper<FinPayable> {
}
