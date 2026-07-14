import { get, post, put, del } from '@/utils/request'
import type { PageParams, PageResult, UserForm, RoleForm, MenuItem, LogItem } from '@/types'

// ==================== 用户管理 ====================

/**
 * 分页查询用户列表
 * @param params 分页查询参数
 * @returns 用户分页数据
 */
export function userPage(params: PageParams) {
  return get<PageResult<any>>('/system/user/page', params)
}

/**
 * 新增用户
 * @param data 用户表单数据
 * @returns void
 */
export function userAdd(data: UserForm) {
  return post('/system/user', data)
}

/**
 * 修改用户
 * @param data 用户表单数据（含id）
 * @returns void
 */
export function userUpdate(data: UserForm) {
  return put('/system/user', data)
}

/**
 * 删除用户
 * @param id 用户ID
 * @returns void
 */
export function userDelete(id: number) {
  return del(`/system/user/${id}`)
}

/**
 * 重置用户密码
 * @param id 用户ID
 * @returns void
 */
export function resetPassword(id: number) {
  return put(`/system/user/${id}/reset-password`)
}

/**
 * 更新用户状态
 * @param id 用户ID
 * @param status 状态值：0-禁用 1-启用
 * @returns void
 */
export function updateStatus(id: number, status: number) {
  return put(`/system/user/${id}/status`, { status })
}

// ==================== 角色管理 ====================

/**
 * 查询角色列表
 * @returns 角色列表
 */
export function roleList() {
  return get<RoleForm[]>('/system/role/list')
}

/**
 * 新增角色
 * @param data 角色表单数据
 * @returns void
 */
export function roleAdd(data: RoleForm) {
  return post('/system/role', data)
}

/**
 * 修改角色
 * @param data 角色表单数据（含id）
 * @returns void
 */
export function roleUpdate(data: RoleForm) {
  return put('/system/role', data)
}

/**
 * 删除角色
 * @param id 角色ID
 * @returns void
 */
export function roleDelete(id: number) {
  return del(`/system/role/${id}`)
}

/**
 * 获取角色已分配的菜单ID列表
 * @param roleId 角色ID
 * @returns 菜单ID列表
 */
export function getRoleMenus(roleId: number) {
  return get<number[]>(`/system/role/${roleId}/menus`)
}

/**
 * 给角色分配菜单权限
 * @param roleId 角色ID
 * @param menuIds 菜单ID列表
 * @returns void
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return put(`/system/role/${roleId}/menus`, { menuIds })
}

// ==================== 菜单管理 ====================

/**
 * 获取菜单树形结构
 * @returns 菜单树
 */
export function menuTree() {
  return get<MenuItem[]>('/system/menu/tree')
}

// ==================== 日志管理 ====================

/**
 * 分页查询操作日志
 * @param params 分页查询参数
 * @returns 日志分页数据
 */
export function logPage(params: PageParams) {
  return get<PageResult<LogItem>>('/system/log/page', params)
}
