<template>
  <layout>
    <template #default>
      <el-skeleton :loading="loading" animated :rows="10">
        <template #default>
          <el-page-header @back="() => router.go(-1)">
            <template #content>
              <span>图书详情</span>
            </template>
          </el-page-header>
          
          <el-card style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>{{ book.title }}</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="24" :md="16">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="书名">{{ book.title }}</el-descriptions-item>
                  <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
                  <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
                  <el-descriptions-item label="出版社">{{ book.publisher }}</el-descriptions-item>
                  <el-descriptions-item label="出版日期">{{ book.publishDate }}</el-descriptions-item>
                  <el-descriptions-item label="分类">{{ book.category }}</el-descriptions-item>
                  <el-descriptions-item label="位置">{{ book.location }}</el-descriptions-item>
                  <el-descriptions-item label="总册数">{{ book.totalCopies }}</el-descriptions-item>
                  <el-descriptions-item label="在馆数">{{ book.availableCopies }}</el-descriptions-item>
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
            
            <div style="margin-top: 20px; text-align: right;">
              <el-button @click="router.push(`/books/${book.id}/edit`)">编辑</el-button>
            </div>
          </el-card>
          
          <el-card style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>借阅记录</span>
              </div>
            </template>
            
            <data-table :data="borrowRecords" :loading="borrowLoading">
              <template #columns>
                <el-table-column prop="borrowId" label="借阅编号" />
                <el-table-column prop="readerName" label="借阅人" />
                <el-table-column prop="borrowDate" label="借出日期" />
                <el-table-column prop="dueDate" label="应还日期" />
                <el-table-column prop="returnDate" label="归还日期" />
                <el-table-column prop="status" label="状态" />
              </template>
            </data-table>
          </el-card>
        </template>
      </el-skeleton>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as bookApi from '../api/book'
import * as borrowApi from '../api/borrow'
import { ElMessage } from 'element-plus'
import { Picture as IconPicture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const book = ref({})
const loading = ref(false)

const borrowRecords = ref([])
const borrowLoading = ref(false)

onMounted(async () => {
  await loadBook()
  await loadBorrowRecords()
})

async function loadBook() {
  loading.value = true
  try {
    const res = await bookApi.getBook(id)
    if (res && res.code === 0) {
      book.value = res.data
    } else {
      ElMessage.error(res?.message || '获取图书详情失败')
    }
  } catch (error) {
    ElMessage.error('获取图书详情失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

async function loadBorrowRecords() {
  borrowLoading.value = true
  try {
    const res = await borrowApi.fetchBorrowRecords({ bookId: id })
    if (res && res.code === 0) {
      borrowRecords.value = res.data.items || res.data
    } else {
      ElMessage.error(res?.message || '获取借阅记录失败')
    }
  } catch (error) {
    ElMessage.error('获取借阅记录失败: ' + (error.message || error))
  } finally {
    borrowLoading.value = false
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