package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmFollowRecord;
import com.buildflow.erp.mapper.CrmFollowRecordMapper;
import com.buildflow.erp.service.CrmFollowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 客户跟进记录服务实现类
 * 实现客户跟进记录的核心业务逻辑，包括分页查询、新增、编辑、删除等操作。
 */
@Service
public class CrmFollowRecordServiceImpl implements CrmFollowRecordService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CrmFollowRecordMapper crmFollowRecordMapper;

    /**
     * 分页查询跟进记录列表
     * 支持按客户ID、跟进类型、业务员ID及时间范围过滤，按创建时间倒序排列
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
    @Override
    public R<PageResult<CrmFollowRecord>> page(Integer pageNum, Integer pageSize, Long customerId,
                                               String followType, Long salespersonId,
                                               String startTime, String endTime) {
        Page<CrmFollowRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CrmFollowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customerId != null, CrmFollowRecord::getCustomerId, customerId)
                .eq(StringUtils.hasText(followType), CrmFollowRecord::getFollowType, followType)
                .eq(salespersonId != null, CrmFollowRecord::getSalespersonId, salespersonId)
                .ge(StringUtils.hasText(startTime), CrmFollowRecord::getCreateTime,
                        StringUtils.hasText(startTime) ? LocalDateTime.parse(startTime, FORMATTER) : null)
                .le(StringUtils.hasText(endTime), CrmFollowRecord::getCreateTime,
                        StringUtils.hasText(endTime) ? LocalDateTime.parse(endTime, FORMATTER) : null)
                .orderByDesc(CrmFollowRecord::getCreateTime);
        Page<CrmFollowRecord> result = crmFollowRecordMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增跟进记录
     *
     * @param record 跟进记录实体对象，需包含customerId、followType、content等字段
     * @return 操作结果
     */
    @Override
    public R<Void> add(CrmFollowRecord record) {
        crmFollowRecordMapper.insert(record);
        return R.ok();
    }

    /**
     * 编辑跟进记录
     *
     * @param record 跟进记录实体对象，id字段必填
     * @return 操作结果，记录不存在时返回R.fail()
     */
    @Override
    public R<Void> update(CrmFollowRecord record) {
        CrmFollowRecord existing = crmFollowRecordMapper.selectById(record.getId());
        if (existing == null) {
            return R.fail("跟进记录不存在");
        }
        crmFollowRecordMapper.updateById(record);
        return R.ok();
    }

    /**
     * 删除跟进记录（逻辑删除）
     *
     * @param id 跟进记录ID
     * @return 操作结果，记录不存在时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        CrmFollowRecord existing = crmFollowRecordMapper.selectById(id);
        if (existing == null) {
            return R.fail("跟进记录不存在");
        }
        crmFollowRecordMapper.deleteById(id);
        return R.ok();
    }
}
