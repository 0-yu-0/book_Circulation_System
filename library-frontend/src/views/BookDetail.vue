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
                <span>{{ book.title || '加载中...' }}</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="24" :md="16">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="书名">{{ book.title || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="作者">{{ book.author || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="ISBN">{{ book.isbn || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="出版社">{{ book.publisher || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="出版日期">{{ book.publishDate || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="分类">{{ book.category || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="位置">{{ book.location || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="总册数">{{ book.totalCopies || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="在馆数">{{ book.availableCopies || 0 }}</el-descriptions-item>
                </el-descriptions>
              </el-col>
              
              <el-col :span="24" :md="8" style="text-align: center;">
                <div class="book-cover-placeholder">
                  <el-image 
                    :src="book.cover || ''" 
                    fit="contain"
                    :preview-src-list="[book.cover || '']"
                    hide-on-click-modal
                  >
                    <template #error>
                      <div class="image-slot">
                        <el-icon><icon-picture /></el-icon>
                        <p>暂无封面</p>
                      </div>
                    </template>
                  </el-image>
                </div>
              </el-col>
            </el-row>
            
<!--            <div style="margin-top: 20px; text-align: right;">-->
<!--              <el-button @click="router.push(`/books/${book.id}/edit`)">编辑</el-button>-->
<!--            </div>-->
          </el-card>
        </template>
      </el-skeleton>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as bookApi from '../api/book'
import { ElMessage } from 'element-plus'
import { Picture as IconPicture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const book = ref({})
const loading = ref(false)

// 监听路由变化，确保在路由参数改变时重新加载数据
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId !== oldId) {
      loadBook(newId)
    }
  },
  { immediate: true }
)

onMounted(async () => {
  // 页面首次加载时也调用加载函数
  await loadBook(route.params.id)
})

async function loadBook(id) {
  if (!id) {
    ElMessage.error('无效的图书ID')
    return
  }
  
  loading.value = true
  try {
    const res = await bookApi.getBook(id)
    console.log('[BookDetail] getBook response:', res) // 添加日志以便调试
    
    if (res && res.code === 0) {
      book.value = res.data || {}
      console.log('[BookDetail] Book object after setting:', book.value) // 新增的调试日志
    } else {
      ElMessage.error(res?.message || '获取图书详情失败')
    }
  } catch (error) {
    console.error('获取图书详情失败:', error)
    ElMessage.error('获取图书详情失败: ' + (error.message || error))
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

.book-cover-placeholder {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  padding: 20px;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.image-slot .el-icon {
  font-size: 40px;
  color: #ccc;
}

.image-slot p {
  margin: 10px 0 0 0;
  color: #999;
}
</style>