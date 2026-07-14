package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysOperationLog;
import com.buildflow.erp.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器
 * 提供操作日志的分页查询接口，用于日志审计与问题排查
 */
@RestController
@RequestMapping("/system/log")
public class SysOperationLogController {

    @Autowired
    private SysOperationLogService sysOperationLogService;

    /**
     * 分页查询操作日志
     * 支持按操作模块模糊搜索和时间范围过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param module    操作模块（模糊查询），可为空
     * @param startTime 操作起始时间，格式yyyy-MM-dd，可为空
     * @param endTime   操作结束时间，格式yyyy-MM-dd，可为空
     * @return 分页结果，包含总记录数和当前页日志列表
     */
    @GetMapping("/page")
    public R<PageResult<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return sysOperationLogService.page(pageNum, pageSize, module, startTime, endTime);
    }
}
