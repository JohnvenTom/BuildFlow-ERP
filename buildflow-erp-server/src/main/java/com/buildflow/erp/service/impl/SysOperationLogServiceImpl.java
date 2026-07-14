package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysOperationLog;
import com.buildflow.erp.mapper.SysOperationLogMapper;
import com.buildflow.erp.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志服务实现类
 * 实现操作日志的分页查询与保存逻辑，分页查询支持按模块和时间范围过滤。
 */
@Service
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private SysOperationLogMapper sysOperationLogMapper;

    /**
     * 分页查询操作日志
     * 支持按操作模块模糊查询和时间范围过滤，按操作时间降序排列
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param module    操作模块（模糊查询），可为null
     * @param startTime 操作起始时间，格式yyyy-MM-dd，可为null
     * @param endTime   操作结束时间，格式yyyy-MM-dd，可为null
     * @return 分页结果，包含总记录数和当前页日志列表
     */
    @Override
    public R<PageResult<SysOperationLog>> page(Integer pageNum, Integer pageSize, String module, String startTime, String endTime) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(module), SysOperationLog::getModule, module)
                .ge(StringUtils.hasText(startTime), SysOperationLog::getCreateTime,
                        StringUtils.hasText(startTime) ? LocalDateTime.parse(startTime + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .le(StringUtils.hasText(endTime), SysOperationLog::getCreateTime,
                        StringUtils.hasText(endTime) ? LocalDateTime.parse(endTime + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> result = sysOperationLogMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 保存操作日志
     * 将操作日志实体持久化到数据库，通常由AOP切面异步调用
     *
     * @param log 操作日志实体对象，需包含userId、module、operation、method、params、ip等字段
     */
    @Override
    public void save(SysOperationLog log) {
        sysOperationLogMapper.insert(log);
    }
}
