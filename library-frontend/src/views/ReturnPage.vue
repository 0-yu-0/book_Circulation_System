<template>
  <layout>
    <template #default>
      <el-form>
        <el-form-item label="读者卡号">
          <el-input v-model="card" />
          <el-button @click="fetchBorrowed" :loading="loading">查询在借</el-button>
        </el-form-item>
      </el-form>
      <data-table :data="records" :loading="loading">
        <template #columns>
          <el-table-column prop="borrowId" label="借阅编号" />
          <el-table-column prop="title" label="书名" />
          <el-table-column prop="borrowDate" label="借出日期" />
          <el-table-column prop="dueDate" label="应还日期" />
          <el-table-column prop="overdueDays" label="逾期天数" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button @click="returnOne(row)" :loading="row.returning">归还</el-button>
            </template>
          </el-table-column>
        </template>
      </data-table>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import { ref, reactive } from 'vue'
import * as borrowApi from '../api/borrow'

const card = ref('')
const records = ref([])
const loading = ref(false)

async function fetchBorrowed(){
  loading.value = true
  try {
    const res = await borrowApi.fetchBorrowRecords({ card: card.value })
    if (res && res.code===0) records.value = res.data.items || res.data
    else ElMessage.error(res?.message || '查询在借记录失败')
  } catch (error) {
    ElMessage.error('查询在借记录失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

async function returnOne(row){
  // 添加归还状态标记
  row.returning = true
  try {
    const res = await borrowApi.returnBooks({ borrowIds: [row.borrowId], returnDate: new Date().toISOString().slice(0,10) })
    if (res && res.code===0) {
      ElMessage.success('归还成功')
      fetchBorrowed()
    } else {
      ElMessage.error(res?.message || '归还失败')
    }
  } catch (error) {
    ElMessage.error('归还失败: ' + (error.message || error))
  } finally {
    row.returning = false
  }
}
</script>

