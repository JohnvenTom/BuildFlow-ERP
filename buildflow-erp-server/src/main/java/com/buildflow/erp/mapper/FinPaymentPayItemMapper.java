package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.FinPaymentPayItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 付款核销明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供FinPaymentPayItem实体的基础CRUD操作。
 */
@Mapper
public interface FinPaymentPayItemMapper extends BaseMapper<FinPaymentPayItem> {
}
