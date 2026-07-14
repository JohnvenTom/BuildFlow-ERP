package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.FinPaymentReceiveItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 回款核销明细Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供FinPaymentReceiveItem实体的基础CRUD操作。
 */
@Mapper
public interface FinPaymentReceiveItemMapper extends BaseMapper<FinPaymentReceiveItem> {
}
