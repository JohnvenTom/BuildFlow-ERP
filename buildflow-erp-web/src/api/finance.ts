import { get, post, put } from '@/utils/request'

// ==================== 客户应收台账 ====================

/**
 * 分页查询客户应收台账列表
 * @param params 分页查询参数（含客户ID、状态等筛选条件）
 * @returns 应收台账分页数据
 */
export const receivablePage = (params: any) => get('/finance/receivable/page', params)

/**
 * 查询应收台账汇总数据
 * @description 返回应收总额、已收总额、剩余总额
 * @returns 应收汇总数据
 */
export const receivableSummary = () => get('/finance/receivable/summary')

// ==================== 供应商应付台账 ====================

/**
 * 分页查询供应商应付台账列表
 * @param params 分页查询参数（含供应商ID、状态等筛选条件）
 * @returns 应付台账分页数据
 */
export const payablePage = (params: any) => get('/finance/payable/page', params)

/**
 * 查询应付台账汇总数据
 * @description 返回应付总额、已付总额、剩余总额
 * @returns 应付汇总数据
 */
export const payableSummary = () => get('/finance/payable/summary')

// ==================== 客户回款单 ====================

/**
 * 分页查询客户回款单列表
 * @param params 分页查询参数（含回款单号、客户ID、状态等筛选条件）
 * @returns 回款单分页数据
 */
export const paymentReceivePage = (params: any) => get('/finance/payment-receive/page', params)

/**
 * 新增客户回款单
 * @param data 回款单表单数据（含客户ID、付款方式、备注、核销明细列表）
 * @returns void
 */
export const paymentReceiveAdd = (data: any) => post('/finance/payment-receive', data)

/**
 * 审核客户回款单
 * @param id 回款单ID
 * @returns void
 */
export const paymentReceiveAudit = (id: number) => put(`/finance/payment-receive/audit/${id}`)

/**
 * 作废客户回款单
 * @param id 回款单ID
 * @returns void
 */
export const paymentReceiveVoid = (id: number) => put(`/finance/payment-receive/void/${id}`)

// ==================== 供应商付款单 ====================

/**
 * 分页查询供应商付款单列表
 * @param params 分页查询参数（含付款单号、供应商ID、状态等筛选条件）
 * @returns 付款单分页数据
 */
export const paymentPayPage = (params: any) => get('/finance/payment-pay/page', params)

/**
 * 新增供应商付款单
 * @param data 付款单表单数据（含供应商ID、付款方式、备注、核销明细列表）
 * @returns void
 */
export const paymentPayAdd = (data: any) => post('/finance/payment-pay', data)

/**
 * 审核供应商付款单
 * @param id 付款单ID
 * @returns void
 */
export const paymentPayAudit = (id: number) => put(`/finance/payment-pay/audit/${id}`)

/**
 * 作废供应商付款单
 * @param id 付款单ID
 * @returns void
 */
export const paymentPayVoid = (id: number) => put(`/finance/payment-pay/void/${id}`)

// ==================== 逾期预警 ====================

/**
 * 查询逾期欠款列表
 * @description 返回所有逾期客户及其逾期金额、逾期天数
 * @returns 逾期欠款列表
 */
export const overdueList = () => get('/finance/overdue/list')

/**
 * 查询逾期欠款汇总数据
 * @description 返回逾期客户数、逾期总金额
 * @returns 逾期汇总数据
 */
export const overdueSummary = () => get('/finance/overdue/summary')
