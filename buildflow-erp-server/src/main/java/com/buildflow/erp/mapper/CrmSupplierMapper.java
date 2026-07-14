package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.CrmSupplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供CrmSupplier实体的基础CRUD操作。
 */
@Mapper
public interface CrmSupplierMapper extends BaseMapper<CrmSupplier> {
}
