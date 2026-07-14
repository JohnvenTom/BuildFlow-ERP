package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysOperationLog;

/**
 * 操作日志服务接口
 * 定义操作日志的查询与保存方法，日志通常由AOP切面自动采集写入。
 */
public interface SysOperationLogService {

    /**
     * 分页查询操作日志
     * 支持按操作模块、时间范围进行过滤查询
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param module    操作模块（模糊查询），可为null
     * @param startTime 操作起始时间，格式yyyy-MM-dd，可为null
     * @param endTime   操作结束时间，格式yyyy-MM-dd，可为null
     * @return 分页结果，包含总记录数和当前页日志列表
     */
    R<PageResult<SysOperationLog>> page(Integer pageNum, Integer pageSize, String module, String startTime, String endTime);

    /**
     * 保存操作日志
     * 通常由AOP切面异步调用，将操作记录持久化到数据库
     *
     * @param log 操作日志实体对象，需包含userId、module、operation、method、params、ip等字段
     */
    void save(SysOperationLog log);
}
