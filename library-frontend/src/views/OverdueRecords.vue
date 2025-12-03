<template>
  <layout>
    <template #default>
      <el-page-header>
        <template #content>
          <span>逾期记录</span>
        </template>
      </el-page-header>
      
      <el-card style="margin-top: 20px;">
        <el-form :inline="true" @submit.prevent="search">
          <el-form-item label="读者姓名">
            <el-input v-model="searchForm.readerName" placeholder="请输入读者姓名" />
          </el-form-item>
          <el-form-item label="图书名称">
            <el-input v-model="searchForm.bookTitle" placeholder="请输入图书名称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <el-card style="margin-top: 20px;">
        <data-table 
          :data="overdueRecords" 
          :loading="loading" 
          :total="total"
          :page="page"
          @page-change="handlePageChange"
        >
          <template #columns>
            <el-table-column prop="borrowId" label="借阅编号" />
            <el-table-column prop="readerName" label="读者姓名" />
            <el-table-column prop="bookTitle" label="书名" />
            <el-table-column prop="borrowDate" label="借出日期" />
            <el-table-column prop="dueDate" label="应还日期" />
            <el-table-column prop="overdueDays" label="逾期天数" />
            <el-table-column prop="fine" label="罚款金额" />
          </template>
        </data-table>
      </el-card>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import { ref, onMounted } from 'vue'
import * as statisticsApi from '../api/statistics'
import { ElMessage } from 'element-plus'

const searchForm = ref({
  readerName: '',
  bookTitle: ''
})

const overdueRecords = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = ref(10)

onMounted(() => {
  loadOverdueRecords()
})

async function loadOverdueRecords() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    
    const res = await statisticsApi.getOverdueBooks(params)
    if (res && res.code === 0) {
      overdueRecords.value = res.data.items || res.data
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res?.message || '获取逾期记录失败')
    }
  } catch (error) {
    ElMessage.error('获取逾期记录失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  loadOverdueRecords()
}

function handlePageChange(newPage) {
  page.value = newPage
  loadOverdueRecords()
}
</script>