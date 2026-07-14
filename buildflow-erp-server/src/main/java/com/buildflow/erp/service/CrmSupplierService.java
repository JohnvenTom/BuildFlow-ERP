package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmSupplier;

import java.util.List;

/**
 * 供应商服务接口
 * 定义供应商档案管理的核心业务方法，包括分页查询、增删改查、全部供应商列表查询等操作。
 */
public interface CrmSupplierService {

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
    R<PageResult<CrmSupplier>> page(Integer pageNum, Integer pageSize, String name,
                                    String mainCategory, Integer cooperationStatus);

    /**
     * 新增供应商
     * 校验供应商名称唯一性后插入数据库
     *
     * @param supplier 供应商实体对象，需包含name、mainCategory、contact等字段
     * @return 操作结果，供应商名称已存在时返回R.fail()
     */
    R<Void> add(CrmSupplier supplier);

    /**
     * 编辑供应商信息
     *
     * @param supplier 供应商实体对象，id字段必填
     * @return 操作结果，供应商不存在时返回R.fail()
     */
    R<Void> update(CrmSupplier supplier);

    /**
     * 删除供应商
     * 仅未产生订单的供应商可删除，有业务单据的供应商仅逻辑删除
     *
     * @param id 供应商ID
     * @return 操作结果，供应商不存在时返回R.fail()
     */
    R<Void> delete(Long id);

    /**
     * 查询全部供应商列表
     * 用于下拉选择场景，不进行分页，返回所有正常合作状态的供应商
     *
     * @return 全部供应商列表
     */
    R<List<CrmSupplier>> listAll();
}
