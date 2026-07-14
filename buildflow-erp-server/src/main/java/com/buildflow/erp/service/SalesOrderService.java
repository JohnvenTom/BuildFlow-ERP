package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SalesOrder;
import com.buildflow.erp.entity.SalesOrderItem;

import java.util.List;

/**
 * 销售订单服务接口
 * 定义销售订单管理的核心业务方法，包括分页查询、订单详情、新增销售订单（含明细）、审核销售订单、作废销售订单等操作。
 */
public interface SalesOrderService {

    /**
     * 分页查询销售订单列表
     * 支持按销售订单号模糊搜索、客户、业务员、单据状态过滤
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param orderNo       销售订单号（模糊查询），可为null
     * @param customerId    客户ID，可为null表示不过滤
     * @param salespersonId 业务员ID，可为null表示不过滤
     * @param status        单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页销售订单列表
     */
    R<PageResult<SalesOrder>> page(Integer pageNum, Integer pageSize, String orderNo, Long customerId, Long salespersonId, String status);

    /**
     * 查询销售订单详情（含明细列表）
     * 根据订单ID查询主表信息，并查询关联的明细列表一并返回
     *
     * @param id 销售订单ID
     * @return 销售订单主表对象（明细列表暂存于明细Mapper查询，此处返回主表）
     */
    R<SalesOrder> detail(Long id);

    /**
     * 新增销售订单（含明细）
     * 自动生成销售订单号（SO+日期+流水号），设置初始状态为草稿，
     * 自动计算每条明细金额（unitPrice × quantity × discount）和订单总金额及提成金额，插入主表和明细表
     *
     * @param order 销售订单主表对象，需包含customerId、salespersonId、commissionRate等字段
     * @param items 销售订单明细列表，每条需包含productId、unitPrice、quantity、discount
     * @return 操作结果
     */
    R<Void> add(SalesOrder order, List<SalesOrderItem> items);

    /**
     * 审核销售订单
     * 校验单据状态为草稿，更新状态为已审核，记录审核人和审核时间
     *
     * @param id      销售订单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废销售订单
     * 仅草稿状态的销售订单可作废
     *
     * @param id 销售订单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
