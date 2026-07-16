import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { R } from '@/types'

/** Token 在 localStorage 中的存储键名（从环境变量读取，默认 buildflow_token） */
const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY || 'buildflow_token'

/** 创建Axios实例 */
const service: AxiosInstance = axios.create({
  // 从环境变量读取 API 基础路径，默认 /api
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

/**
 * 请求拦截器
 * @description 在请求头中添加Bearer Token鉴权信息
 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * @description 统一处理响应错误：401跳转登录页，其他错误弹出提示
 */
service.interceptors.response.use(
  (response: AxiosResponse<R>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem(TOKEN_KEY)
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.removeItem(TOKEN_KEY)
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有操作权限')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

/**
 * GET请求
 * @param url 请求地址
 * @param params 查询参数
 * @returns Promise包装的响应数据
 */
export function get<T = any>(url: string, params?: object): Promise<R<T>> {
  return service.get(url, { params }).then((res) => res.data)
}

/**
 * POST请求
 * @param url 请求地址
 * @param data 请求体数据
 * @returns Promise包装的响应数据
 */
export function post<T = any>(url: string, data?: object): Promise<R<T>> {
  return service.post(url, data).then((res) => res.data)
}

/**
 * PUT请求
 * @param url 请求地址
 * @param data 请求体数据
 * @returns Promise包装的响应数据
 */
export function put<T = any>(url: string, data?: object): Promise<R<T>> {
  return service.put(url, data).then((res) => res.data)
}

/**
 * DELETE请求
 * @param url 请求地址
 * @param params 查询参数
 * @returns Promise包装的响应数据
 */
export function del<T = any>(url: string, params?: object): Promise<R<T>> {
  return service.delete(url, { params }).then((res) => res.data)
}

export default service
