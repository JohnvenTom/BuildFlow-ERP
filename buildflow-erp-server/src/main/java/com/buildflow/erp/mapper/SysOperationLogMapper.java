package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SysOperationLog实体的基础CRUD操作，
 * 用于系统操作日志的持久化存储与查询。
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
