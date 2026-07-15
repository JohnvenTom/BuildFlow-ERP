import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

/**
 * Vite 构建配置
 * @description 根据当前 mode 加载对应的 .env 文件，配置开发服务器代理和路径别名
 * @param env.mode 当前运行模式（development/production）
 * @returns Vite 配置对象
 */
export default defineConfig(({ mode }) => {
  // 加载对应模式的 .env 文件（.env / .env.[mode] / .env.local）
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      // 端口从环境变量读取，默认 3000
      port: Number(env.VITE_APP_PORT) || 3000,
      open: true,
      proxy: {
        [env.VITE_API_BASE_URL || '/api']: {
          // 代理目标地址从环境变量读取
          target: env.VITE_API_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        }
      }
    }
  }
})
