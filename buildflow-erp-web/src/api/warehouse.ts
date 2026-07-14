import { get, post, put, del } from '@/utils/request'

// ==================== 商品档案 ====================

/**
 * 分页查询商品列表
 * @param params 分页查询参数（含商品名称、商品分类等筛选条件）
 * @returns 商品分页数据
 */
export const productPage = (params: any) => get('/warehouse/product/page', params)

/**
 * 新增商品
 * @param data 商品表单数据
 * @returns void
 */
export const productAdd = (data: any) => post('/warehouse/product', data)

/**
 * 修改商品
 * @param data 商品表单数据（含id）
 * @returns void
 */
export const productUpdate = (data: any) => put('/warehouse/product', data)

/**
 * 删除商品
 * @param id 商品ID
 * @returns void
 */
export const productDelete = (id: number) => del(`/warehouse/product/${id}`)

/**
 * 查询全部商品列表（不分页）
 * @returns 商品列表
 */
export const productList = () => get('/warehouse/product/list')

// ==================== 仓库档案 ====================

/**
 * 分页查询仓库列表
 * @param params 分页查询参数（含仓库名称、仓库类型等筛选条件）
 * @returns 仓库分页数据
 */
export const warehousePage = (params: any) => get('/warehouse/warehouse/page', params)

/**
 * 新增仓库
 * @param data 仓库表单数据
 * @returns void
 */
export const warehouseAdd = (data: any) => post('/warehouse/warehouse', data)

/**
 * 修改仓库
 * @param data 仓库表单数据（含id）
 * @returns void
 */
export const warehouseUpdate = (data: any) => put('/warehouse/warehouse', data)

/**
 * 删除仓库
 * @param id 仓库ID
 * @returns void
 */
export const warehouseDelete = (id: number) => del(`/warehouse/warehouse/${id}`)

/**
 * 查询全部仓库列表（不分页）
 * @returns 仓库列表
 */
export const warehouseList = () => get('/warehouse/warehouse/list')

// ==================== 库存查询 ====================

/**
 * 分页查询库存列表
 * @param params 分页查询参数（含仓库ID、商品名称、批次号等筛选条件）
 * @returns 库存分页数据
 */
export const inventoryPage = (params: any) => get('/warehouse/inventory/page', params)

// ==================== 调拨单 ====================

/**
 * 分页查询调拨单列表
 * @param params 分页查询参数（含调拨单号、状态等筛选条件）
 * @returns 调拨单分页数据
 */
export const transferPage = (params: any) => get('/warehouse/transfer/page', params)

/**
 * 新增调拨单
 * @param data 调拨单表单数据（含明细列表）
 * @returns void
 */
export const transferAdd = (data: any) => post('/warehouse/transfer', data)

/**
 * 审核调拨单
 * @param id 调拨单ID
 * @param auditBy 审核人ID
 * @returns void
 */
export const transferAudit = (id: number, auditBy: number) => put(`/warehouse/transfer/audit/${id}`, { auditBy })

/**
 * 作废调拨单
 * @param id 调拨单ID
 * @returns void
 */
export const transferVoid = (id: number) => put(`/warehouse/transfer/void/${id}`)

// ==================== 盘点单 ====================

/**
 * 分页查询盘点单列表
 * @param params 分页查询参数（含盘点单号、状态等筛选条件）
 * @returns 盘点单分页数据
 */
export const checkPage = (params: any) => get('/warehouse/check/page', params)

/**
 * 新增盘点单
 * @param data 盘点单表单数据（含明细列表）
 * @returns void
 */
export const checkAdd = (data: any) => post('/warehouse/check', data)

/**
 * 审核盘点单
 * @param id 盘点单ID
 * @param auditBy 审核人ID
 * @returns void
 */
export const checkAudit = (id: number, auditBy: number) => put(`/warehouse/check/audit/${id}`, { auditBy })

/**
 * 作废盘点单
 * @param id 盘点单ID
 * @returns void
 */
export const checkVoid = (id: number) => put(`/warehouse/check/void/${id}`)

// ==================== 库存预警 ====================

/**
 * 查询低库存预警列表
 * @returns 低库存预警数据列表
 */
export const lowStockAlert = () => get('/warehouse/alert/lowStock')

/**
 * 查询呆滞库存预警列表
 * @returns 呆滞库存预警数据列表
 */
export const dormantStockAlert = () => get('/warehouse/alert/dormant')
