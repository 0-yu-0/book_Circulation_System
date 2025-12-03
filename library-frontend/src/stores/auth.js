import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const isLoggedIn = ref(!!token.value)

  async function login(payload){
    const res = await api.login(payload)
    if (res && res.code === 0){
      token.value = res.data.token
      user.value = res.data.user
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
    token.value = t || ''
    isLoggedIn.value = !!t
  }

  return { user, token, isLoggedIn, login, logout, loadFromStorage }
})

