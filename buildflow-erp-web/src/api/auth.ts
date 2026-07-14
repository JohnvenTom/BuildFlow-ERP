import { post } from '@/utils/request'
import type { LoginRequest, LoginResponse } from '@/types'

/**
 * 用户登录
 * @param data 登录请求参数（用户名+密码）
 * @returns 登录响应数据（含token）
 */
export function login(data: LoginRequest) {
  return post<LoginResponse>('/auth/login', data)
}

/**
 * 用户登出
 * @returns void
 */
export function logout() {
  return post('/auth/logout')
}
