package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.FinReceivable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户应收台账Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供FinReceivable实体的基础CRUD操作。
 */
@Mapper
public interface FinReceivableMapper extends BaseMapper<FinReceivable> {
}
