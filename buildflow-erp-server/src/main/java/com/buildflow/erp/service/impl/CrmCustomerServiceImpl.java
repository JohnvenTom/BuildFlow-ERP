package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.service.CrmCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户服务实现类
 * 实现客户档案管理的核心业务逻辑，包括分页查询、新增、编辑、删除、全部列表查询等操作。
 */
@Service
public class CrmCustomerServiceImpl implements CrmCustomerService {

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    /**
     * 分页查询客户列表
     * 支持按客户名称模糊搜索、客户类型过滤、业务员过滤及欠款状态过滤。
     * 欠款状态过滤逻辑：overdue-当前欠款大于0，normal-当前欠款为0或null
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param name          客户名称（模糊查询），可为null
     * @param type          客户类型，可为null表示不过滤
     * @param salespersonId 业务员ID，可为null表示不过滤
     * @param debtStatus    欠款状态（overdue-逾期/normal-正常），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页客户列表
     */
    @Override
    public R<PageResult<CrmCustomer>> page(Integer pageNum, Integer pageSize, String name, String type,
                                           Long salespersonId, String debtStatus) {
        Page<CrmCustomer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), CrmCustomer::getName, name)
                .eq(StringUtils.hasText(type), CrmCustomer::getType, type)
                .eq(salespersonId != null, CrmCustomer::getSalespersonId, salespersonId)
                .gt("overdue".equals(debtStatus), CrmCustomer::getCurrentDebt, BigDecimal.ZERO)
                .and("normal".equals(debtStatus), w -> w.le(CrmCustomer::getCurrentDebt, BigDecimal.ZERO)
                        .or().isNull(CrmCustomer::getCurrentDebt))
                .orderByDesc(CrmCustomer::getCreateTime);
        Page<CrmCustomer> result = crmCustomerMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增客户
     * 校验客户名称唯一性，设置默认状态为0（正常），初始化当前欠款为0
     *
     * @param customer 客户实体对象，需包含name、type、contact等字段
     * @return 操作结果，客户名称已存在时返回R.fail()
     */
    @Override
    public R<Void> add(CrmCustomer customer) {
        // 校验客户名称是否已存在
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrmCustomer::getName, customer.getName());
        if (crmCustomerMapper.selectCount(wrapper) > 0) {
            return R.fail("客户名称已存在");
        }
        // 设置默认值
        customer.setStatus(0);
        if (customer.getCurrentDebt() == null) {
            customer.setCurrentDebt(BigDecimal.ZERO);
        }
        crmCustomerMapper.insert(customer);
        return R.ok();
    }

    /**
     * 编辑客户信息
     *
     * @param customer 客户实体对象，id字段必填
     * @return 操作结果，客户不存在时返回R.fail()
     */
    @Override
    public R<Void> update(CrmCustomer customer) {
        CrmCustomer existing = crmCustomerMapper.selectById(customer.getId());
        if (existing == null) {
            return R.fail("客户不存在");
        }
        crmCustomerMapper.updateById(customer);
        return R.ok();
    }

    /**
     * 删除客户
     * 仅未产生订单的空白客户可物理删除，有业务单据的客户仅逻辑作废（设置status=1）
     * 注意：当前版本暂无订单关联检查，默认检查currentDebt是否大于0判断是否有业务单据
     *
     * @param id 客户ID
     * @return 操作结果，客户不存在时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        CrmCustomer existing = crmCustomerMapper.selectById(id);
        if (existing == null) {
            return R.fail("客户不存在");
        }
        // 判断客户是否有业务单据：当前欠款大于0视为有业务单据
        if (existing.getCurrentDebt() != null && existing.getCurrentDebt().compareTo(BigDecimal.ZERO) > 0) {
            // 有业务单据，仅逻辑作废
            LambdaUpdateWrapper<CrmCustomer> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CrmCustomer::getId, id).set(CrmCustomer::getStatus, 1);
            crmCustomerMapper.update(null, updateWrapper);
            return R.ok("客户存在业务单据，已作废处理", null);
        }
        // 无业务单据，执行逻辑删除（由@TableLogic自动处理）
        crmCustomerMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 查询全部客户列表
     * 用于下拉选择场景，返回所有正常状态（status=0）的客户，按创建时间倒序排列
     *
     * @return 全部客户列表
     */
    @Override
    public R<List<CrmCustomer>> listAll() {
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrmCustomer::getStatus, 0)
                .orderByDesc(CrmCustomer::getCreateTime);
        List<CrmCustomer> list = crmCustomerMapper.selectList(wrapper);
        return R.ok(list);
    }
}
