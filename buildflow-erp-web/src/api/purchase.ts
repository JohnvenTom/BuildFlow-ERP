import { get, post, put, del } from '@/utils/request'

// ==================== 采购订单 ====================

/**
 * 分页查询采购订单列表
 * @param params 分页查询参数（含订单号、供应商ID、状态等筛选条件）
 * @returns 采购订单分页数据
 */
export const purchaseOrderPage = (params: any) => get('/purchase/order/page', params)

/**
 * 新增采购订单
 * @param data 采购订单表单数据（含供应商ID、备注、明细列表）
 * @returns void
 */
export const purchaseOrderAdd = (data: any) => post('/purchase/order', data)

/**
 * 审核采购订单
 * @param id 采购订单ID
 * @returns void
 */
export const purchaseOrderAudit = (id: number) => put(`/purchase/order/audit/${id}`)

/**
 * 作废采购订单
 * @param id 采购订单ID
 * @returns void
 */
export const purchaseOrderVoid = (id: number) => put(`/purchase/order/void/${id}`)

// ==================== 采购入库 ====================

/**
 * 分页查询采购入库单列表
 * @param params 分页查询参数（含入库单号、供应商ID、状态等筛选条件）
 * @returns 采购入库单分页数据
 */
export const stockInPage = (params: any) => get('/purchase/stock-in/page', params)

/**
 * 新增采购入库单
 * @param data 采购入库单表单数据（含供应商ID、仓库ID、关联订单ID、明细列表）
 * @returns void
 */
export const stockInAdd = (data: any) => post('/purchase/stock-in', data)

/**
 * 审核采购入库单
 * @param id 采购入库单ID
 * @returns void
 */
export const stockInAudit = (id: number) => put(`/purchase/stock-in/audit/${id}`)

/**
 * 作废采购入库单
 * @param id 采购入库单ID
 * @returns void
 */
export const stockInVoid = (id: number) => put(`/purchase/stock-in/void/${id}`)

// ==================== 采购退货 ====================

/**
 * 分页查询采购退货单列表
 * @param params 分页查询参数（含退货单号、供应商ID、状态等筛选条件）
 * @returns 采购退货单分页数据
 */
export const purchaseReturnPage = (params: any) => get('/purchase/return/page', params)

/**
 * 新增采购退货单
 * @param data 采购退货单表单数据（含供应商ID、关联入库单ID、明细列表）
 * @returns void
 */
export const purchaseReturnAdd = (data: any) => post('/purchase/return', data)

/**
 * 审核采购退货单
 * @param id 采购退货单ID
 * @returns void
 */
export const purchaseReturnAudit = (id: number) => put(`/purchase/return/audit/${id}`)

/**
 * 作废采购退货单
 * @param id 采购退货单ID
 * @returns void
 */
export const purchaseReturnVoid = (id: number) => put(`/purchase/return/void/${id}`)

// ==================== 采购对账 ====================

/**
 * 查询月度采购对账单数据
 * @param params 查询参数（含供应商ID、月份等筛选条件）
 * @returns 采购对账数据列表
 */
export const purchaseStatement = (params: any) => get('/purchase/statement', params)
