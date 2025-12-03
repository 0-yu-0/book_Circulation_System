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
          />
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
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { User, Lock } from '@element-plus/icons-vue'

const formRef = ref(null)
const form = ref({ username:'', password:'' })
const rules = { username:[{ required:true, message:'请输入用户名', trigger:'blur' }], password:[{ required:true, message:'请输入密码', trigger:'blur' }] }
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()

async function onSubmit(){
  loading.value = true
  try{
    const res = await auth.login({ username: form.value.username, password: form.value.password })
    if (res && res.code===0) router.push('/')
  }finally{ loading.value = false }
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