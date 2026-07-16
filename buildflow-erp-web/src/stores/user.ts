import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { menuTree } from '@/api/system'
import type { LoginRequest, UserInfo, MenuItem } from '@/types'
import router from '@/router'

/**
 * 将后端菜单数据（menuName字段）映射为前端 MenuItem 格式（name字段）
 * icon 为空时使用默认图标 "Menu"，避免 component :is 渲染异常
 * @param menus 后端返回的菜单列表
 * @returns 映射后的前端菜单列表
 */
function mapMenus(menus: any[]): MenuItem[] {
  return menus.map(menu => ({
    id: menu.id,
    name: menu.menuName || menu.name,
    path: menu.path,
    component: menu.component || '',
    icon: menu.icon || 'Menu',
    sort: menu.sort || 0,
    children: menu.children?.length ? mapMenus(menu.children) : undefined
  }))
}

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

    // 登录接口已返回用户信息和菜单，直接保存
    if (res.data) {
      userInfo.value = {
        id: res.data.userId,
        username: res.data.username || '',
        realName: res.data.realName || '',
        avatar: '',
        roles: res.data.roleName ? [res.data.roleName] : [],
        permissions: ['*']
      }
      if (res.data.menus?.length) {
        menuList.value = mapMenus(res.data.menus)
      }
    }
  }

  /**
   * 获取用户信息与菜单
   * @description 页面刷新后store丢失时，通过此方法重新获取菜单树
   */
  async function getInfo() {
    // 使用登录时保存的信息或默认管理员信息
    if (!userInfo.value) {
      userInfo.value = {
        id: 0,
        username: '',
        realName: '用户',
        avatar: '',
        roles: [],
        permissions: ['*']
      }
    }

    // 获取菜单树
    const menuRes = await menuTree()
    menuList.value = mapMenus(menuRes.data || [])
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
