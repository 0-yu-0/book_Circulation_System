<template>
  <layout>
    <template #default>
      <el-page-header>
        <template #content>
          <span>系统设置</span>
        </template>
      </el-page-header>
      
      <el-card style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>基本设置</span>
          </div>
        </template>
        
        <el-form :model="settings" label-width="120px">
          <el-form-item label="默认借阅期限">
            <el-input-number v-model="settings.defaultBorrowDays" :min="1" :max="365" />
            <span style="margin-left: 10px;">天</span>
          </el-form-item>
          
          <el-form-item label="每日罚款金额">
            <el-input-number v-model="settings.dailyFine" :min="0" :step="0.1" />
            <span style="margin-left: 10px;">元/天</span>
          </el-form-item>
          
          <el-form-item label="默认最大借书数">
            <el-input-number v-model="settings.defaultMaxBorrow" :min="1" :max="100" />
            <span style="margin-left: 10px;">本</span>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <el-card style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>系统信息</span>
          </div>
        </template>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
          <el-descriptions-item label="Vue版本">3.x</el-descriptions-item>
          <el-descriptions-item label="Element Plus版本">2.x</el-descriptions-item>
          <el-descriptions-item label="最后更新">2023-01-01</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const settings = ref({
  defaultBorrowDays: 30,
  dailyFine: 0.5,
  defaultMaxBorrow: 5
})

onMounted(() => {
  loadSettings()
})

function loadSettings() {
  // 从localStorage加载设置
  const savedSettings = localStorage.getItem('librarySystemSettings')
  if (savedSettings) {
    try {
      settings.value = JSON.parse(savedSettings)
    } catch (e) {
      console.error('Failed to parse settings', e)
    }
  }
}

function saveSettings() {
  // 保存设置到localStorage
  localStorage.setItem('librarySystemSettings', JSON.stringify(settings.value))
  ElMessage.success('设置已保存')
}
</script>

<style scoped>
.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>