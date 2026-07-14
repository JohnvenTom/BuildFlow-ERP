/** 登录请求参数 */
export interface LoginRequest {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
}

/** 登录响应数据 */
export interface LoginResponse {
  /** 访问令牌 */
  token: string
}

/** 用户信息 */
export interface UserInfo {
  /** 用户ID */
  id: number
  /** 用户名 */
  username: string
  /** 真实姓名 */
  realName: string
  /** 头像地址 */
  avatar: string
  /** 角色列表 */
  roles: string[]
  /** 权限标识列表 */
  permissions: string[]
}

/** 菜单项 */
export interface MenuItem {
  /** 菜单ID */
  id: number
  /** 菜单名称 */
  name: string
  /** 路由路径 */
  path: string
  /** 组件路径 */
  component: string
  /** 图标 */
  icon: string
  /** 排序号 */
  sort: number
  /** 子菜单 */
  children?: MenuItem[]
}

/** 分页查询参数 */
export interface PageParams {
  /** 当前页码，从1开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 查询关键词 */
  keyword?: string
}

/** 分页结果 */
export interface PageResult<T> {
  /** 数据列表 */
  list: T[]
  /** 总条数 */
  total: number
}

/** 统一响应结构 */
export interface R<T = any> {
  /** 状态码，200表示成功 */
  code: number
  /** 提示消息 */
  message: string
  /** 响应数据 */
  data: T
}

/** 用户表单数据 */
export interface UserForm {
  /** 用户ID，编辑时存在 */
  id?: number
  /** 用户名 */
  username: string
  /** 密码，新增时必填 */
  password?: string
  /** 真实姓名 */
  realName: string
  /** 手机号 */
  phone: string
  /** 邮箱 */
  email: string
  /** 角色ID列表 */
  roleIds: number[]
  /** 状态：0-禁用 1-启用 */
  status: number
}

/** 角色表单数据 */
export interface RoleForm {
  /** 角色ID，编辑时存在 */
  id?: number
  /** 角色名称 */
  name: string
  /** 角色编码 */
  code: string
  /** 角色描述 */
  description: string
  /** 状态：0-禁用 1-启用 */
  status: number
}

/** 操作日志 */
export interface LogItem {
  /** 日志ID */
  id: number
  /** 操作人 */
  operator: string
  /** 操作类型 */
  operation: string
  /** 请求方法 */
  method: string
  /** 请求参数 */
  params: string
  /** 执行时长(ms) */
  duration: number
  /** IP地址 */
  ip: string
  /** 创建时间 */
  createTime: string
}
