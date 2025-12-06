import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  // 默认不自动登录，始终初始化为false
  const isLoggedIn = ref(false)

  async function login(payload){
    const res = await api.login(payload)
    if (res && res.code === 0){
      token.value = res.data.token
      user.value = res.data.user || { username: payload.username } // 确保用户对象存在
      isLoggedIn.value = true
      localStorage.setItem('token', token.value)
    }
    return res
  }

  function logout(){
    token.value = ''
    user.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    return api.logout()
  }

  function loadFromStorage(){
    const t = localStorage.getItem('token')
    if (t) {
      token.value = t
      // 即使有token也默认不自动登录，需要用户手动登录
      // 但在有token的情况下，认为用户已登录以允许访问受保护的资源
      isLoggedIn.value = true;
      user.value = { username: 'local_user' }; // 设置一个默认用户
    }
  }

  return { user, token, isLoggedIn, login, logout, loadFromStorage }
})