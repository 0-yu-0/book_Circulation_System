<template>
  <layout>
    <template #default>
      <el-skeleton :loading="loading" animated :rows="10">
        <template #default>
          <el-page-header @back="() => router.go(-1)">
          </el-page-header>

          <el-card style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>{{ reader.name }}</span>
              </div>
            </template>
            
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ reader.name }}</el-descriptions-item>
              <el-descriptions-item label="读者ID">{{ reader.id }}</el-descriptions-item>
              <el-descriptions-item label="证件类型">{{ reader.idType }}</el-descriptions-item>
              <el-descriptions-item label="证件号">{{ reader.idNumber }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ reader.phone }}</el-descriptions-item>
              <el-descriptions-item label="注册日期">{{ reader.registerDate }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ reader.status }}</el-descriptions-item>
              <el-descriptions-item label="最大借书数">{{ reader.borrowLimit }}</el-descriptions-item>
              <el-descriptions-item label="当前借书数">{{ reader.currentBorrowCount }}</el-descriptions-item>
            </el-descriptions>
            
<!--            <div style="margin-top: 20px; text-align: right;">-->
<!--              <el-button @click="router.push({ path: '/readers', query: { editId: reader.id } })">编辑</el-button>-->
<!--            </div>-->
          </el-card>
        </template>
      </el-skeleton>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as readerApi from '../api/reader'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const reader = ref({})
const loading = ref(false)

onMounted(async () => {
  await loadReader()
})

async function loadReader() {
  loading.value = true
  try {
    const res = await readerApi.getReader(id)
    if (res && res.code === 0) {
      reader.value = res.data
    } else {
      ElMessage.error(res?.message || '获取读者详情失败')
    }
  } catch (error) {
    ElMessage.error('获取读者详情失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>