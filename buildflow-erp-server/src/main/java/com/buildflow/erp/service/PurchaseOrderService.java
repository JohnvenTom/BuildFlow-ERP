package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.PurchaseOrder;
import com.buildflow.erp.entity.PurchaseOrderItem;

import java.util.List;

/**
 * 采购订单服务接口
 * 定义采购订单管理的核心业务方法，包括分页查询、新增采购订单（含明细）、审核采购订单、作废采购订单等操作。
 */
public interface PurchaseOrderService {

    /**
     * 分页查询采购订单列表
     * 支持按采购订单号模糊搜索、供应商过滤、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param orderNo     采购订单号（模糊查询），可为null
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页采购订单列表
     */
    R<PageResult<PurchaseOrder>> page(Integer pageNum, Integer pageSize, String orderNo, Long supplierId, String status);

    /**
     * 新增采购订单（含明细）
     * 自动生成采购订单号（PO+日期+流水号），设置初始状态为草稿，自动计算明细金额和订单总金额，插入主表和明细表
     *
     * @param order 采购订单主表对象，需包含supplierId等字段
     * @param items 采购订单明细列表，每条需包含productId、unitPrice、quantity
     * @return 操作结果
     */
    R<Void> add(PurchaseOrder order, List<PurchaseOrderItem> items);

    /**
     * 审核采购订单
     * 校验单据状态为草稿，更新状态为已审核
     *
     * @param id      采购订单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废采购订单
     * 仅草稿状态的采购订单可作废
     *
     * @param id 采购订单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
