package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.FinPaymentPay;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商付款单主表Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供FinPaymentPay实体的基础CRUD操作。
 */
@Mapper
public interface FinPaymentPayMapper extends BaseMapper<FinPaymentPay> {
}
