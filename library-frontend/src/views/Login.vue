<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-header">
        <el-avatar :icon="Collection" size="large" class="login-avatar" style="background-color: transparent; color: #333; width: 60px; height: 60px;" />
        <h2>图书借还管理系统</h2>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            size="large"
            prefix-icon="User"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住用户名</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="onSubmit" 
            size="large"
            class="login-button"
            style="width: 100%"
            round
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          show-icon
          closable
          @close="errorMessage = ''"
        />
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { User, Lock, Management, Collection } from '@element-plus/icons-vue'

const formRef = ref(null)
const form = ref({ username:'', password:'' })
const rules = { 
  username:[{ required:true, message:'请输入用户名', trigger:'blur' }], 
  password:[{ required:true, message:'请输入密码', trigger:'blur' }] 
}
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const rememberMe = ref(false)
const errorMessage = ref('')

// 页面加载时检查是否记住用户名
onMounted(() => {
  const savedUsername = localStorage.getItem('savedUsername')
  if (savedUsername) {
    form.value.username = savedUsername
    rememberMe.value = true
  }
})

async function onSubmit(){
  // 表单验证
  const isValid = await formRef.value.validate().catch(() => false)
  if (!isValid) return
  
  loading.value = true
  errorMessage.value = ''
  
  try{
    const res = await auth.login({ 
      username: form.value.username, 
      password: form.value.password 
    })
    
    if (res && res.code === 0) {
      // 如果选择了记住用户名，则保存用户名
      if (rememberMe.value) {
        localStorage.setItem('savedUsername', form.value.username)
      } else {
        localStorage.removeItem('savedUsername')
      }
      
      // 登录成功后重定向到仪表板页面
      const redirect = route.query.redirect || '/dashboard'
      router.push(redirect)
    } else {
      errorMessage.value = res?.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/login.scss';
</style>