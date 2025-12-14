<template>
  <layout>
    <template #default>
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="读者">
          <el-input v-model="card" placeholder="读者姓名或证件号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="status" placeholder="状态" :clearable="false" style="width:140px">
<!--            <el-option label="全部" value="all" />-->
            <el-option label="在借" value="borrowed" />
            <el-option label="逾期" value="overdue" />
          </el-select>
        </el-form-item>
        <el-form-item label="借出日期">
          <el-date-picker v-model="borrowDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilters" :loading="loading">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <data-table
        :data="records"
        :loading="loading"
        :total="total"
        :page="page"
        :page-size="pageSize"
        :show-selection="false"
        @page-change="onPageChange"
        @size-change="onSizeChange"
      >
        <template #columns>
          <el-table-column prop="borrowId" label="借阅编号" />
          <el-table-column prop="bookTitle" label="书名" />
          <el-table-column prop="readerName" label="读者姓名" />
          <el-table-column prop="borrowDate" label="借出日期" />
          <el-table-column prop="dueDate" label="应还日期" />
          <el-table-column prop="statusText" label="状态" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button @click="returnOne(row)" :loading="row.returning" :disabled="row.borrowStates !== 0 && row.borrowStates !== 2">归还</el-button>
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
import { ref, onMounted } from 'vue'
import * as borrowApi from '../api/borrow'
import * as readerApi from '../api/reader'
import * as statisticsApi from '../api/statistics'
import { ElMessage, ElMessageBox } from 'element-plus'

// filters & pagination state
const card = ref('')
// Default to show only borrowed records (not returned)
const status = ref('borrowed')
const borrowDateRange = ref([]) // [start, end]
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const records = ref([])
const loading = ref(false)

onMounted(() => {
  fetchRecords()
})

async function applyFilters(){
  page.value = 1
  await fetchRecords()
}

function resetFilters(){
  card.value = ''
  status.value = 'borrowed'
  borrowDateRange.value = []
  page.value = 1
  pageSize.value = 10
  fetchRecords()
}

async function onPageChange(p){
  page.value = p
  fetchRecords()
}

async function onSizeChange(size){
  pageSize.value = size
  page.value = 1
  fetchRecords()
}

// Centralized fetch that supports pagination and filters
async function fetchRecords(){
  loading.value = true
  try {
    // If status is 'borrowed' -> query borrow API with status=borrowed
    // If status is 'overdue' -> if reader specified, fetch borrowed by reader and filter overdue client-side; otherwise call statistics overdue endpoint

    if (status.value === 'borrowed' || status.value === 'all') {
      const params = {
        page: page.value,
        size: pageSize.value
      }
      
      if (status.value === 'borrowed') {
        // For borrowed status, query both borrowed (0) and overdue (2) books
        // We'll fetch all records and filter client-side
        params.status = 'all' // Fetch all records
      } else if (status.value !== 'all') {
        params.status = status.value
      }

      // borrow date range
      if (borrowDateRange.value && borrowDateRange.value.length === 2) {
        params.borrowDateFrom = borrowDateRange.value[0]
        params.borrowDateTo = borrowDateRange.value[1]
      }

      // reader card or id number -> resolve to readerId if provided
      if (card.value) {
        try {
          // Use fetchReaders directly for better search support (name, card number, etc.)
          const searchRes = await readerApi.fetchReaders({ search: card.value, size: 5 })
          if (searchRes && searchRes.code === 0 && searchRes.data && searchRes.data.items && searchRes.data.items.length > 0) {
            // Use the first match
            params.readerId = searchRes.data.items[0].id
          } else {
            records.value = []
            total.value = 0
            ElMessage.warning('未找到匹配的读者，已清空结果')
            loading.value = false
            return
          }
        } catch (err) {
          ElMessage.error('查找读者失败: ' + (err.message || err))
          loading.value = false
          return
        }
      }

      const res = await borrowApi.fetchBorrowRecords(params)
      if (res && res.code === 0) {
        const data = res.data || {}
        const items = data.items || data || []
        
        // Filter out returned books when status is 'borrowed'
        let filteredItems = items
        if (status.value === 'borrowed') {
          filteredItems = items.filter(item => {
            const statusValue = item.borrowStates ?? item.borrowStatus ?? item.status
            return statusValue === 0 || statusValue === 2 // Only show borrowed (0) and overdue (2) books
          })
        }
        
        records.value = normalizeRecords(filteredItems)
        total.value = filteredItems.length
      } else {
        ElMessage.error(res?.message || '加载借阅记录失败')
        records.value = []
        total.value = 0
      }

    } else if (status.value === 'overdue') {
      // overdue branch
      if (card.value) {
        // get borrowed records for reader and filter overdue client-side
        try {
          let readerId = null
          // Use fetchReaders directly for better search support (name, card number, etc.)
          const searchRes = await readerApi.fetchReaders({ search: card.value, size: 5 })
          if (searchRes && searchRes.code === 0 && searchRes.data && searchRes.data.items && searchRes.data.items.length > 0) {
            // Use the first match
            readerId = searchRes.data.items[0].id
          } else {
            records.value = []
            total.value = 0
            ElMessage.warning('未找到匹配的读者，已清空结果')
            loading.value = false
            return
          }
          const res = await borrowApi.fetchBorrowRecords({ readerId, status: 'borrowed', page: page.value, size: pageSize.value })
          if (res && res.code === 0) {
            const items = res.data.items || res.data || []
            // filter overdue by dueDate < today
            const overdueItems = (items || []).filter(it => {
              const due = it.dueDate || it.due_date
              if (!due) return false
              const dueDate = new Date(due)
              const today = new Date()
              return dueDate < new Date(today.toDateString()) // compare date-only
            })
            records.value = normalizeRecords(overdueItems)
            total.value = overdueItems.length
          } else {
            ElMessage.error(res?.message || '加载借阅记录失败')
            records.value = []
            total.value = 0
          }
        } catch (err) {
          ElMessage.error('加载逾期记录失败: ' + (err.message || err))
          records.value = []
          total.value = 0
        }
      } else {
        // global overdue list via statistics API
        try {
          const offset = (page.value - 1) * pageSize.value
          const lim = pageSize.value
          const params = { offset, limit: lim }
          
          // 添加读者姓名筛选参数（使用card搜索框的内容）
          if (card.value) {
            params.readerName = card.value
          }
          
          const res = await statisticsApi.getOverdueBooks(params)
          if (res && res.code === 0) {
            const items = Array.isArray(res.data) ? res.data : (res.data && res.data.items) ? res.data.items : []
            records.value = normalizeRecords(items)
            total.value = items.length
          } else {
            ElMessage.error(res?.message || '加载逾期记录失败')
            records.value = []
            total.value = 0
          }
        } catch (err) {
          ElMessage.error('加载逾期记录失败: ' + (err.message || err))
          records.value = []
          total.value = 0
        }
      }
    } else {
      // default safeguard - treat as borrowed
      status.value = 'all'
      await fetchRecords()
    }
  } catch (error) {
    ElMessage.error('加载借阅记录失败: ' + (error.message || error))
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function normalizeRecords(list){
  return (list || []).map(record => {
    let statusText = '未知'
    let overdueDays = 0

    // Calculate overdue days for all records that have a due date
    const due = record.dueDate || record.due_date
    if (due) {
      const dueDate = new Date(due)
      const today = new Date()
      const diffTime = today - dueDate
      overdueDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      if (overdueDays < 0) overdueDays = 0
    }

    // Set status text based on borrow status
    if (record.borrowStates === 0 || record.borrowStatus === 0 || record.status === 0) {
      statusText = '在借'
    } else if (record.borrowStates === 1 || record.borrowStatus === 1) {
      statusText = '已还'
    } else if (record.borrowStates === 2 || record.borrowStatus === 2) {
      statusText = '逾期'
    }

    return {
      ...record,
      statusText,
      overdueDays,
      borrowId: record.borrowId ?? record.id ?? record.borrow_id,
      bookTitle: record.bookTitle ?? record.title ?? record.bookName ?? record.book_name,
      readerName: record.readerName ?? record.reader_name ?? (record.reader && (record.reader.name || record.reader.readerName))
    }
  })
}

async function returnOne(row){
  // Check if the book is overdue
  if (row.borrowStates === 2 || row.overdueDays > 0) {
    // Show fine payment dialog for overdue books
    showFinePaymentDialog(row)
  } else {
    // Normal return for non-overdue books
    await performReturn(row)
  }
}

// Show fine payment dialog
function showFinePaymentDialog(row) {
  const overdueDays = row.overdueDays || 0
  const fineAmount = overdueDays * 1.0 // 每天罚金1元，与后端一致
  
  ElMessageBox.confirm(
    `该书已逾期 ${overdueDays} 天，需缴纳罚金 ${fineAmount.toFixed(2)} 元（每天1元）。确认缴纳罚金并归还吗？`,
    '逾期归还',
    {
      confirmButtonText: '确认缴纳',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await performReturn(row)
  }).catch(() => {
    // User cancelled
    ElMessage.info('已取消归还')
  })
}

// Perform the actual return operation
async function performReturn(row) {
  row.returning = true
  try {
    const returnData = {
      borrowIds: [row.borrowId], 
      returnDate: new Date().toISOString().slice(0,10)
    }
    
    const res = await borrowApi.returnBooks(returnData)
    if (res && res.code===0) {
      // 后端会自动计算并保存罚金到归还记录表
      const message = row.overdueDays > 0 ? `归还成功，已缴纳罚金 ${(row.overdueDays * 1.0).toFixed(2)} 元` : '归还成功'
      ElMessage.success(message)
      // refresh current page
      fetchRecords()
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

<style scoped>
.filter-form {
  margin-bottom: 16px;
}
</style>