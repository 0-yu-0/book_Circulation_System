<template>
  <div class="login-page">
    <el-card class="login-card">
      <h3>登录</h3>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

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
.login-page{display:flex;align-items:center;justify-content:center;height:100vh}
.login-card{width:380px;padding:24px}
</style>

