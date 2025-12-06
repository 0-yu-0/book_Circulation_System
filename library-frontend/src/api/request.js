import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  // Normalize pagination: support page/size in frontend -> convert to offset/limit for backend
  if (config.params) {
    const p = config.params
    if (p.page !== undefined && p.size !== undefined) {
      const page = Number(p.page) || 1
      const size = Number(p.size) || 20
      p.offset = Math.max(0, (page - 1)) * size
      p.limit = size
      delete p.page
      delete p.size
    }
  }

  return config
})

instance.interceptors.response.use(
  res => {
    // Return the entire response data, not just res.data
    return res.data
  },
  err => {
    // Handle 401 Unauthorized - token expired or invalid
    if (err.response && err.response.status === 401) {
      // Clear token and redirect to login
      localStorage.removeItem('token')
      // Only redirect if not already on login page
      if (router.currentRoute.value.path !== '/login') {
        router.push({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
      }
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(err)
    }
    
    // Handle other errors
    const errorMessage = err.response?.data?.message || err.message || '网络错误'
    ElMessage.error(errorMessage)
    return Promise.reject(err)
  }
)

export default instance