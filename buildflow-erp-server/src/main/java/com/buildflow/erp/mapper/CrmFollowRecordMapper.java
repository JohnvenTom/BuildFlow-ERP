package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.CrmFollowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户跟进记录Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供CrmFollowRecord实体的基础CRUD操作。
 */
@Mapper
public interface CrmFollowRecordMapper extends BaseMapper<CrmFollowRecord> {
}
