package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmCustomer;

import java.util.List;

/**
 * 客户服务接口
 * 定义客户档案管理的核心业务方法，包括分页查询、增删改查、全部客户列表查询等操作。
 */
public interface CrmCustomerService {

    /**
     * 分页查询客户列表
     * 支持按客户名称模糊搜索、客户类型过滤、业务员过滤及欠款状态过滤
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param name          客户名称（模糊查询），可为null
     * @param type          客户类型，可为null表示不过滤
     * @param salespersonId 业务员ID，可为null表示不过滤
     * @param debtStatus    欠款状态（如：overdue-逾期、normal-正常），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页客户列表
     */
    R<PageResult<CrmCustomer>> page(Integer pageNum, Integer pageSize, String name, String type,
                                    Long salespersonId, String debtStatus);

    /**
     * 新增客户
     * 校验客户名称唯一性，设置默认状态后插入数据库
     *
     * @param customer 客户实体对象，需包含name、type、contact等字段
     * @return 操作结果，客户名称已存在时返回R.fail()
     */
    R<Void> add(CrmCustomer customer);

    /**
     * 编辑客户信息
     *
     * @param customer 客户实体对象，id字段必填
     * @return 操作结果，客户不存在时返回R.fail()
     */
    R<Void> update(CrmCustomer customer);

    /**
     * 删除客户
     * 仅未产生订单的空白客户可物理删除，有业务单据的客户仅逻辑作废（设置status=1）
     *
     * @param id 客户ID
     * @return 操作结果，客户不存在时返回R.fail()
     */
    R<Void> delete(Long id);

    /**
     * 查询全部客户列表
     * 用于下拉选择场景，不进行分页，返回所有正常状态的客户
     *
     * @return 全部客户列表
     */
    R<List<CrmCustomer>> listAll();
}
