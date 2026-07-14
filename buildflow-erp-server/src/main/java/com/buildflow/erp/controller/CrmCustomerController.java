package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.service.CrmCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理控制器
 * 提供客户档案的分页查询、新增、编辑、删除及全部客户列表查询等接口
 */
@RestController
@RequestMapping("/crm/customer")
public class CrmCustomerController {

    @Autowired
    private CrmCustomerService crmCustomerService;

    /**
     * 分页查询客户列表
     * 支持按客户名称模糊搜索、客户类型过滤、业务员过滤及欠款状态过滤
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param name          客户名称（模糊查询），可为空
     * @param type          客户类型，可为空
     * @param salespersonId 业务员ID，可为空
     * @param debtStatus    欠款状态（overdue-逾期/normal-正常），可为空
     * @return 分页结果，包含总记录数和当前页客户列表
     */
    @GetMapping("/page")
    public R<PageResult<CrmCustomer>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) String debtStatus) {
        return crmCustomerService.page(pageNum, pageSize, name, type, salespersonId, debtStatus);
    }

    /**
     * 新增客户
     * 校验客户名称唯一性后插入数据库
     *
     * @param customer 客户实体对象，需包含name、type、contact等字段
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody CrmCustomer customer) {
        return crmCustomerService.add(customer);
    }

    /**
     * 编辑客户信息
     *
     * @param customer 客户实体对象，id字段必填
     * @return 操作结果
     */
    @PutMapping
    public R<Void> update(@RequestBody CrmCustomer customer) {
        return crmCustomerService.update(customer);
    }

    /**
     * 删除客户
     * 仅未产生订单的空白客户可物理删除，有业务单据的客户仅逻辑作废
     *
     * @param id 客户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return crmCustomerService.delete(id);
    }

    /**
     * 查询全部客户列表
     * 用于下拉选择场景，返回所有正常状态的客户
     *
     * @return 全部客户列表
     */
    @GetMapping("/list")
    public R<List<CrmCustomer>> listAll() {
        return crmCustomerService.listAll();
    }
}
