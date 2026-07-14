package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmFollowRecord;

/**
 * 客户跟进记录服务接口
 * 定义客户跟进记录的核心业务方法，包括分页查询、新增、编辑、删除等操作。
 */
public interface CrmFollowRecordService {

    /**
     * 分页查询跟进记录列表
     * 支持按客户ID、跟进类型、业务员ID及时间范围过滤
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param customerId    客户ID，可为null表示不过滤
     * @param followType    跟进类型，可为null表示不过滤
     * @param salespersonId 业务员ID，可为null表示不过滤
     * @param startTime     开始时间（yyyy-MM-dd HH:mm:ss），可为null
     * @param endTime       结束时间（yyyy-MM-dd HH:mm:ss），可为null
     * @return 分页结果，包含总记录数和当前页跟进记录列表
     */
    R<PageResult<CrmFollowRecord>> page(Integer pageNum, Integer pageSize, Long customerId,
                                        String followType, Long salespersonId,
                                        String startTime, String endTime);

    /**
     * 新增跟进记录
     *
     * @param record 跟进记录实体对象，需包含customerId、followType、content等字段
     * @return 操作结果
     */
    R<Void> add(CrmFollowRecord record);

    /**
     * 编辑跟进记录
     *
     * @param record 跟进记录实体对象，id字段必填
     * @return 操作结果，记录不存在时返回R.fail()
     */
    R<Void> update(CrmFollowRecord record);

    /**
     * 删除跟进记录（逻辑删除）
     *
     * @param id 跟进记录ID
     * @return 操作结果，记录不存在时返回R.fail()
     */
    R<Void> delete(Long id);
}
