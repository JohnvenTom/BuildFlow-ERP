package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.service.CrmSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商管理控制器
 * 提供供应商档案的分页查询、新增、编辑、删除及全部供应商列表查询等接口
 */
@RestController
@RequestMapping("/crm/supplier")
public class CrmSupplierController {

    @Autowired
    private CrmSupplierService crmSupplierService;

    /**
     * 分页查询供应商列表
     * 支持按供应商名称模糊搜索、主营类目过滤及合作状态过滤
     *
     * @param pageNum            当前页码，从1开始
     * @param pageSize           每页记录数
     * @param name               供应商名称（模糊查询），可为空
     * @param mainCategory       主营类目，可为空
     * @param cooperationStatus  合作状态（0-正常 1-暂停 2-终止），可为空
     * @return 分页结果，包含总记录数和当前页供应商列表
     */
    @PreAuthorize("@ps.hasPermission('crm:supplier:list')")
    @GetMapping("/page")
    public R<PageResult<CrmSupplier>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) Integer cooperationStatus) {
        return crmSupplierService.page(pageNum, pageSize, name, mainCategory, cooperationStatus);
    }

    /**
     * 新增供应商
     * 校验供应商名称唯一性后插入数据库
     *
     * @param supplier 供应商实体对象，需包含name、mainCategory、contact等字段
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('crm:supplier:add')")
    @PostMapping
    public R<Void> add(@RequestBody CrmSupplier supplier) {
        return crmSupplierService.add(supplier);
    }

    /**
     * 编辑供应商信息
     *
     * @param supplier 供应商实体对象，id字段必填
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('crm:supplier:edit')")
    @PutMapping
    public R<Void> update(@RequestBody CrmSupplier supplier) {
        return crmSupplierService.update(supplier);
    }

    /**
     * 删除供应商
     * 仅未产生订单的供应商可删除
     *
     * @param id 供应商ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('crm:supplier:delete')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return crmSupplierService.delete(id);
    }

    /**
     * 查询全部供应商列表
     * 用于下拉选择场景，返回所有正常合作状态的供应商
     *
     * @return 全部供应商列表
     */
    @PreAuthorize("@ps.hasPermission('crm:supplier:list')")
    @GetMapping("/list")
    public R<List<CrmSupplier>> listAll() {
        return crmSupplierService.listAll();
    }
}
