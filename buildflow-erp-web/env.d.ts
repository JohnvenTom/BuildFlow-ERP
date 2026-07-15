/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

/**
 * Vite 环境变量类型声明
 * @description 声明通过 .env 文件注入的环境变量类型，便于在代码中获得类型提示
 * @see https://vitejs.dev/guide/env-and-mode.html
 */
interface ImportMetaEnv {
  /** 应用标题 */
  readonly VITE_APP_TITLE: string
  /** API 基础路径 */
  readonly VITE_API_BASE_URL: string
  /** 后端服务地址（仅开发环境用于 vite proxy） */
  readonly VITE_API_TARGET?: string
  /** 应用端口 */
  readonly VITE_APP_PORT?: string
  /** 是否启用 Mock 数据 */
  readonly VITE_USE_MOCK?: string
  /** Token 在 localStorage 中的存储键名 */
  readonly VITE_TOKEN_KEY: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
