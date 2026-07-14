package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.mapper.CrmSupplierMapper;
import com.buildflow.erp.service.CrmSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 供应商服务实现类
 * 实现供应商档案管理的核心业务逻辑，包括分页查询、新增、编辑、删除、全部列表查询等操作。
 */
@Service
public class CrmSupplierServiceImpl implements CrmSupplierService {

    @Autowired
    private CrmSupplierMapper crmSupplierMapper;

    /**
     * 分页查询供应商列表
     * 支持按供应商名称模糊搜索、主营类目过滤及合作状态过滤
     *
     * @param pageNum            当前页码，从1开始
     * @param pageSize           每页记录数
     * @param name               供应商名称（模糊查询），可为null
     * @param mainCategory       主营类目，可为null表示不过滤
     * @param cooperationStatus  合作状态（0-正常 1-暂停 2-终止），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页供应商列表
     */
    @Override
    public R<PageResult<CrmSupplier>> page(Integer pageNum, Integer pageSize, String name,
                                           String mainCategory, Integer cooperationStatus) {
        Page<CrmSupplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CrmSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), CrmSupplier::getName, name)
                .eq(StringUtils.hasText(mainCategory), CrmSupplier::getMainCategory, mainCategory)
                .eq(cooperationStatus != null, CrmSupplier::getCooperationStatus, cooperationStatus)
                .orderByDesc(CrmSupplier::getCreateTime);
        Page<CrmSupplier> result = crmSupplierMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增供应商
     * 校验供应商名称唯一性后插入数据库
     *
     * @param supplier 供应商实体对象，需包含name、mainCategory、contact等字段
     * @return 操作结果，供应商名称已存在时返回R.fail()
     */
    @Override
    public R<Void> add(CrmSupplier supplier) {
        // 校验供应商名称是否已存在
        LambdaQueryWrapper<CrmSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrmSupplier::getName, supplier.getName());
        if (crmSupplierMapper.selectCount(wrapper) > 0) {
            return R.fail("供应商名称已存在");
        }
        // 设置默认合作状态为正常
        if (supplier.getCooperationStatus() == null) {
            supplier.setCooperationStatus(0);
        }
        crmSupplierMapper.insert(supplier);
        return R.ok();
    }

    /**
     * 编辑供应商信息
     *
     * @param supplier 供应商实体对象，id字段必填
     * @return 操作结果，供应商不存在时返回R.fail()
     */
    @Override
    public R<Void> update(CrmSupplier supplier) {
        CrmSupplier existing = crmSupplierMapper.selectById(supplier.getId());
        if (existing == null) {
            return R.fail("供应商不存在");
        }
        crmSupplierMapper.updateById(supplier);
        return R.ok();
    }

    /**
     * 删除供应商
     * 仅未产生订单的供应商可删除，有业务单据的供应商仅逻辑删除。
     * 注意：当前版本暂无采购订单关联检查，默认执行逻辑删除
     *
     * @param id 供应商ID
     * @return 操作结果，供应商不存在时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        CrmSupplier existing = crmSupplierMapper.selectById(id);
        if (existing == null) {
            return R.fail("供应商不存在");
        }
        // TODO: 待集成采购订单模块后，增加是否有关联采购订单的校验
        // 执行逻辑删除（由@TableLogic自动处理）
        crmSupplierMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 查询全部供应商列表
     * 用于下拉选择场景，返回所有正常合作状态（cooperationStatus=0）的供应商，按创建时间倒序排列
     *
     * @return 全部供应商列表
     */
    @Override
    public R<List<CrmSupplier>> listAll() {
        LambdaQueryWrapper<CrmSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrmSupplier::getCooperationStatus, 0)
                .orderByDesc(CrmSupplier::getCreateTime);
        List<CrmSupplier> list = crmSupplierMapper.selectList(wrapper);
        return R.ok(list);
    }
}
