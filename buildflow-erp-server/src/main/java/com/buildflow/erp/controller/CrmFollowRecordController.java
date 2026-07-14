package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmFollowRecord;
import com.buildflow.erp.service.CrmFollowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 客户跟进记录控制器
 * 提供客户跟进记录的分页查询、新增、编辑、删除等接口
 */
@RestController
@RequestMapping("/crm/follow")
public class CrmFollowRecordController {

    @Autowired
    private CrmFollowRecordService crmFollowRecordService;

    /**
     * 分页查询跟进记录列表
     * 支持按客户ID、跟进类型、业务员ID及时间范围过滤
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param customerId    客户ID，可为空
     * @param followType    跟进类型，可为空
     * @param salespersonId 业务员ID，可为空
     * @param startTime     开始时间（yyyy-MM-dd HH:mm:ss），可为空
     * @param endTime       结束时间（yyyy-MM-dd HH:mm:ss），可为空
     * @return 分页结果，包含总记录数和当前页跟进记录列表
     */
    @PreAuthorize("@ps.hasPermission('crm:follow:list')")
    @GetMapping("/page")
    public R<PageResult<CrmFollowRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String followType,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return crmFollowRecordService.page(pageNum, pageSize, customerId, followType,
                salespersonId, startTime, endTime);
    }

    /**
     * 新增跟进记录
     *
     * @param record 跟进记录实体对象，需包含customerId、followType、content等字段
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('crm:follow:add')")
    @PostMapping
    public R<Void> add(@RequestBody CrmFollowRecord record) {
        return crmFollowRecordService.add(record);
    }

    /**
     * 编辑跟进记录
     *
     * @param record 跟进记录实体对象，id字段必填
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('crm:follow:edit')")
    @PutMapping
    public R<Void> update(@RequestBody CrmFollowRecord record) {
        return crmFollowRecordService.update(record);
    }

    /**
     * 删除跟进记录（逻辑删除）
     *
     * @param id 跟进记录ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('crm:follow:delete')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return crmFollowRecordService.delete(id);
    }
}
