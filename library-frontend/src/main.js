import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// Comment out mock service to use real backend data
// import './mock'
// Global print styles for receipts
import './styles/print-ticket.css'
// Global styles for visual upgrade
import './styles/global.scss'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 在应用启动时加载认证状态
import { useAuthStore } from './stores/auth'
router.isReady().then(() => {
  const authStore = useAuthStore()
  authStore.loadFromStorage()
  app.mount('#app')
})