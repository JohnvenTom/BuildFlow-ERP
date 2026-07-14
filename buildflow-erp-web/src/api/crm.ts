import { get, post, put, del } from '@/utils/request'

// ==================== 客户管理 ====================

/**
 * 分页查询客户列表
 * @param params 分页查询参数（含客户名称、客户类型、欠款状态等筛选条件）
 * @returns 客户分页数据
 */
export const customerPage = (params: any) => get('/crm/customer/page', params)

/**
 * 新增客户
 * @param data 客户表单数据
 * @returns void
 */
export const customerAdd = (data: any) => post('/crm/customer', data)

/**
 * 修改客户
 * @param data 客户表单数据（含id）
 * @returns void
 */
export const customerUpdate = (data: any) => put('/crm/customer', data)

/**
 * 删除客户
 * @param id 客户ID
 * @returns void
 */
export const customerDelete = (id: number) => del(`/crm/customer/${id}`)

/**
 * 查询全部客户列表（不分页）
 * @returns 客户列表
 */
export const customerList = () => get('/crm/customer/list')

// ==================== 供应商管理 ====================

/**
 * 分页查询供应商列表
 * @param params 分页查询参数（含供应商名称、主营品类、合作状态等筛选条件）
 * @returns 供应商分页数据
 */
export const supplierPage = (params: any) => get('/crm/supplier/page', params)

/**
 * 新增供应商
 * @param data 供应商表单数据
 * @returns void
 */
export const supplierAdd = (data: any) => post('/crm/supplier', data)

/**
 * 修改供应商
 * @param data 供应商表单数据（含id）
 * @returns void
 */
export const supplierUpdate = (data: any) => put('/crm/supplier', data)

/**
 * 删除供应商
 * @param id 供应商ID
 * @returns void
 */
export const supplierDelete = (id: number) => del(`/crm/supplier/${id}`)

/**
 * 查询全部供应商列表（不分页）
 * @returns 供应商列表
 */
export const supplierList = () => get('/crm/supplier/list')

// ==================== 跟进记录 ====================

/**
 * 分页查询跟进记录列表
 * @param params 分页查询参数（含客户ID、跟进类型、时间段等筛选条件）
 * @returns 跟进记录分页数据
 */
export const followPage = (params: any) => get('/crm/follow/page', params)

/**
 * 新增跟进记录
 * @param data 跟进记录表单数据
 * @returns void
 */
export const followAdd = (data: any) => post('/crm/follow', data)

/**
 * 修改跟进记录
 * @param data 跟进记录表单数据（含id）
 * @returns void
 */
export const followUpdate = (data: any) => put('/crm/follow', data)

/**
 * 删除跟进记录
 * @param id 跟进记录ID
 * @returns void
 */
export const followDelete = (id: number) => del(`/crm/follow/${id}`)

// ==================== 信用查询 ====================

/**
 * 分页查询客户信用与欠款信息
 * @param params 分页查询参数（含客户名称、欠款状态等筛选条件）
 * @returns 客户信用分页数据
 */
export const creditPage = (params: any) => get('/crm/credit/page', params)
