import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id){
          if (id.includes('node_modules')){
            if (id.includes('element-plus')) return 'vendor_element'
            if (id.includes('vue')) return 'vendor_vue'
            return 'vendor_misc'
          }
        }
      }
    }
  }
})
