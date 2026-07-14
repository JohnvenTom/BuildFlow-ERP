import { get, post, put, del } from '@/utils/request'

// ==================== 销售订单 ====================

/**
 * 分页查询销售订单列表
 * @param params 分页查询参数（含订单号、客户ID、业务员ID、状态等筛选条件）
 * @returns 销售订单分页数据
 */
export const salesOrderPage = (params: any) => get('/sales/order/page', params)

/**
 * 查询销售订单详情
 * @param id 销售订单ID
 * @returns 销售订单详情数据（含明细列表）
 */
export const salesOrderDetail = (id: number) => get(`/sales/order/${id}`)

/**
 * 新增销售订单
 * @param data 销售订单表单数据（含客户ID、业务员ID、提成比例、明细列表）
 * @returns void
 */
export const salesOrderAdd = (data: any) => post('/sales/order', data)

/**
 * 审核销售订单
 * @param id 销售订单ID
 * @returns void
 */
export const salesOrderAudit = (id: number) => put(`/sales/order/audit/${id}`)

/**
 * 作废销售订单
 * @param id 销售订单ID
 * @returns void
 */
export const salesOrderVoid = (id: number) => put(`/sales/order/void/${id}`)

// ==================== 工地出库 ====================

/**
 * 分页查询工地出库单列表
 * @param params 分页查询参数（含出库单号、销售订单号、状态等筛选条件）
 * @returns 出库单分页数据
 */
export const deliveryPage = (params: any) => get('/sales/delivery/page', params)

/**
 * 新增工地出库单
 * @param data 出库单表单数据（含销售订单ID、仓库ID、明细列表）
 * @returns void
 */
export const deliveryAdd = (data: any) => post('/sales/delivery', data)

/**
 * 审核工地出库单
 * @param id 出库单ID
 * @returns void
 */
export const deliveryAudit = (id: number) => put(`/sales/delivery/audit/${id}`)

/**
 * 作废工地出库单
 * @param id 出库单ID
 * @returns void
 */
export const deliveryVoid = (id: number) => put(`/sales/delivery/void/${id}`)

// ==================== 销售退货 ====================

/**
 * 分页查询销售退货单列表
 * @param params 分页查询参数（含退货单号、客户ID、状态等筛选条件）
 * @returns 销售退货单分页数据
 */
export const salesReturnPage = (params: any) => get('/sales/return/page', params)

/**
 * 新增销售退货单
 * @param data 销售退货单表单数据（含客户ID、关联出库单ID、明细列表）
 * @returns void
 */
export const salesReturnAdd = (data: any) => post('/sales/return', data)

/**
 * 审核销售退货单
 * @param id 销售退货单ID
 * @returns void
 */
export const salesReturnAudit = (id: number) => put(`/sales/return/audit/${id}`)

/**
 * 作废销售退货单
 * @param id 销售退货单ID
 * @returns void
 */
export const salesReturnVoid = (id: number) => put(`/sales/return/void/${id}`)

// ==================== 提成报表 ====================

/**
 * 查询业务员提成核算报表
 * @param params 查询参数（含起止日期等筛选条件）
 * @returns 提成报表数据列表
 */
export const commissionReport = (params: any) => get('/sales/commission/report', params)
