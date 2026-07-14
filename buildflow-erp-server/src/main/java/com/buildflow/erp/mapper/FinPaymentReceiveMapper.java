package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.FinPaymentReceive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户回款单主表Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供FinPaymentReceive实体的基础CRUD操作。
 */
@Mapper
public interface FinPaymentReceiveMapper extends BaseMapper<FinPaymentReceive> {
}
