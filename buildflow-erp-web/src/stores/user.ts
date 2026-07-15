import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { menuTree } from '@/api/system'
import type { LoginRequest, UserInfo, MenuItem } from '@/types'
import router from '@/router'

/**
 * 用户状态管理
 * @description 管理用户token、用户信息、菜单列表，提供登录/登出/获取用户信息等操作
 */
export const useUserStore = defineStore('user', () => {
  /** Token 在 localStorage 中的存储键名（从环境变量读取，默认 buildflow_token） */
  const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY || 'buildflow_token'

  /** 访问令牌 */
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(null)
  /** 菜单列表 */
  const menuList = ref<MenuItem[]>([])

  /**
   * 用户登录
   * @param loginData 登录请求参数（用户名+密码）
   */
  async function login(loginData: LoginRequest) {
    const res = await loginApi(loginData)
    token.value = res.data.token
    localStorage.setItem(TOKEN_KEY, res.data.token)
  }

  /**
   * 获取用户信息与菜单
   * @description 获取当前登录用户信息和菜单树，存入store
   */
  async function getInfo() {
    // TODO: 替换为实际的获取用户信息接口
    const mockUserInfo: UserInfo = {
      id: 1,
      username: 'admin',
      realName: '管理员',
      avatar: '',
      roles: ['admin'],
      permissions: ['*']
    }
    userInfo.value = mockUserInfo

    const menuRes = await menuTree()
    menuList.value = menuRes.data || []
  }

  /**
   * 用户登出
   * @description 调用登出接口，清除本地状态，跳转登录页
   */
  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      userInfo.value = null
      menuList.value = []
      localStorage.removeItem(TOKEN_KEY)
      router.push('/login')
    }
  }

  return {
    token,
    userInfo,
    menuList,
    login,
    getInfo,
    logout
  }
})
