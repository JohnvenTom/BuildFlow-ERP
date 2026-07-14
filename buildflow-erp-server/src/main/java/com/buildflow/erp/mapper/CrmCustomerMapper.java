package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.CrmCustomer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供CrmCustomer实体的基础CRUD操作。
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapper<CrmCustomer> {
}
