import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:19001',
        changeOrigin: true,
      },
      '/collab': {
        target: 'ws://localhost:1234',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/collab/, ''),
      },
    },
  },
})
