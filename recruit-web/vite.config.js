import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 在线招聘平台前端：开发端口 5180，/api 代理到网关 18090
export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5180,
    proxy: {
      '/api/': {
        target: 'http://localhost:18090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
