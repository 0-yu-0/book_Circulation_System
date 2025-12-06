<template>
  <div class="login-page">
    <div class="login-background"></div>
    <el-card class="login-card">
      <div class="login-header">
        <el-avatar :icon="User" size="large" class="login-avatar" />
        <h3>图书借还管理系统</h3>
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
import { User, Lock } from '@element-plus/icons-vue'

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

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.8;
  z-index: 1;
}

.login-card {
  width: 380px;
  padding: 24px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 2;
  position: relative;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h3 {
  margin-top: 16px;
  color: #333;
  font-weight: 600;
}

.login-avatar {
  background-color: #409eff;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  border-radius: 8px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

@media (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 0 16px;
  }
}
</style>