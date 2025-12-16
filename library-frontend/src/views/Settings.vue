<template>
  <layout>
    <template #default>
      <el-skeleton :loading="loading" animated :rows="8">
        <template #default>
<!--          <el-page-header>-->
<!--            <template #content>-->
<!--              <span>系统设置</span>-->
<!--            </template>-->
<!--          </el-page-header>-->
<!--          -->
          <el-tabs v-model="activeTab" style="margin-top: 20px;">
            <!-- 基本设置 Tab -->
            <el-tab-pane label="基本设置" name="basic">
              <el-card class="setting-card">
                <template #header>
                  <div class="card-header">
                    <el-icon><Setting /></el-icon>
                    图书馆规则设置
                  </div>
                </template>
                
                <div class="rules-container">
                  <el-row :gutter="20">
                    <el-col :span="24" :md="8">
                      <div class="rule-item">
                        <div class="rule-icon">
                          <el-icon class="rule-icon-inner"><Calendar /></el-icon>
                        </div>
                        <div class="rule-content">
                          <div class="rule-label">默认借阅期限</div>
                          <div class="rule-value">{{ settings.defaultBorrowDays }} <span class="rule-unit">天</span></div>
                        </div>
                      </div>
                    </el-col>
                    
                    <el-col :span="24" :md="8">
                      <div class="rule-item">
                        <div class="rule-icon">
                          <el-icon class="rule-icon-inner"><PriceTag /></el-icon>
                        </div>
                        <div class="rule-content">
                          <div class="rule-label">每日罚款金额</div>
                          <div class="rule-value">{{ settings.dailyFine }} <span class="rule-unit">元/天</span></div>
                        </div>
                      </div>
                    </el-col>
                    
                    <el-col :span="24" :md="8">
                      <div class="rule-item">
                        <div class="rule-icon">
                          <el-icon class="rule-icon-inner"><Collection /></el-icon>
                        </div>
                        <div class="rule-content">
                          <div class="rule-label">默认最大借书数</div>
                          <div class="rule-value">{{ settings.defaultMaxBorrow }} <span class="rule-unit">本</span></div>
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                  
                  <div class="rules-notice">
                    <el-alert
                      title="注意：以上规则为系统默认设置，如需修改请联系系统管理员"
                      type="info"
                      show-icon
                      :closable="false"
                    />
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            

            
            <!-- 系统信息 Tab -->
            <el-tab-pane label="系统信息" name="info">
              <el-card class="setting-card">
                <template #header>
                  <div class="card-header">
                    <el-icon><InfoFilled /></el-icon>
                    系统基本信息
                  </div>
                </template>
                
                <el-descriptions :column="1" class="info-table">
                  <el-descriptions-item label="系统名称">
                    <strong>图书借还管理系统</strong>
                  </el-descriptions-item>
                  <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
                  <el-descriptions-item label="Vue版本">3.x</el-descriptions-item>
                  <el-descriptions-item label="Element Plus版本">2.x</el-descriptions-item>
                  <el-descriptions-item label="开发者">图书馆技术部</el-descriptions-item>
                  <el-descriptions-item label="最后更新">2025-12-16</el-descriptions-item>
                </el-descriptions>
                
                <div style="margin-top: 20px;">
                  <p><strong>系统描述:</strong></p>
                  <p class="system-description">
                    本系统用于管理图书馆书籍借阅、归还及相关统计工作，提供便捷高效的图书管理服务。
                  </p>
                </div>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </template>
      </el-skeleton>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { Setting, InfoFilled, Calendar, PriceTag, Collection } from '@element-plus/icons-vue'

const activeTab = ref('basic')

const settings = ref({
  defaultBorrowDays: 14,  // 与后端一致：默认借阅14天
  dailyFine: 1.0,         // 与后端一致：每天罚金1元
  defaultMaxBorrow: 5     // 默认最大借阅量5本
})

const loading = ref(true)

onMounted(() => {
  setTimeout(() => {
    loadSettings()
    loading.value = false
  }, 300)
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
</script>

<style scoped>
.card-header {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.setting-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: none;
}

.rules-container {
  padding: 20px 0;
}

.rule-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.rule-item:hover {
  background-color: #eef1f6;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.rule-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.rule-icon-inner {
  font-size: 24px;
  color: #409eff;
}

.rule-content {
  flex: 1;
}

.rule-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.rule-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.rule-unit {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}

.rules-notice {
  margin-top: 20px;
}

.settings-form {
  max-width: 600px;
}

.settings-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.setting-input {
  max-width: 200px;
}

.info-table :deep(.el-descriptions__label) {
  font-weight: 500;
  width: 100px;
}

.info-table :deep(.el-descriptions__content) {
  color: #606266;
}

.system-description {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
  text-align: justify;
}

@media (max-width: 768px) {
  .rule-item {
    padding: 15px;
  }
  
  .rule-icon {
    width: 40px;
    height: 40px;
  }
  
  .rule-icon-inner {
    font-size: 20px;
  }
  
  .rule-value {
    font-size: 18px;
  }
}
</style>