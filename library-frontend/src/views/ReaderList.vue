<template>
  <layout>
    <template #default>
      <div class="toolbar">
        <search-form @search="onSearch" @reset="resetFilters">
          <template #fields>
            <el-form-item label="搜索">
              <el-input v-model="filters.search" placeholder="姓名/证件号" />
            </el-form-item>
          </template>
        </search-form>
        <div class="actions">
          <el-upload :show-file-list="false" :before-upload="beforeUpload">
            <el-button>导入 CSV</el-button>
          </el-upload>
          <el-button @click="exportData">导出 CSV</el-button>
          <el-button type="danger" :disabled="selected.length === 0" @click="batchDelete">批量删除</el-button>
          <el-button type="primary" @click="openCreate">新增读者</el-button>
        </div>
      </div>

      <data-table 
        :data="readers" 
        :total="total" 
        :page="page"
        :page-size="pageSize"
        :show-selection="false"
        @page-change="onPageChange" 
        @size-change="onSizeChange"
        :loading="loading" 
        @selection-change="onSelectionChange"
      >
        <template #columns>
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="id" label="读者ID" />
          <el-table-column prop="idNumber" label="证件号" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="registerDate" label="注册日期" />
          <el-table-column prop="statusText" label="状态" />
          <el-table-column prop="currentBorrowCount" label="当前借阅数" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="text" @click="edit(row)">编辑</el-button>
              <el-button type="text" @click="del(row)">删除</el-button>
              <el-button type="text" @click="viewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </template>
      </data-table>

      <el-dialog title="确认删除" v-model="showConfirm" width="30%">
        <span>确定要删除这个读者吗？</span>
        <template #footer>
          <el-button @click="onCancelDelete">取 消</el-button>
          <el-button type="primary" @click="doDelete">确 定</el-button>
        </template>
      </el-dialog>
      
      <el-dialog title="批量删除确认" v-model="showBatchConfirm">
        <span>确定要删除选中的 {{ selected.length }} 个读者吗？</span>
        <template #footer>
          <el-button @click="showBatchConfirm = false">取 消</el-button>
          <el-button type="primary" @click="doBatchDelete">确 定</el-button>
        </template>
      </el-dialog>

      <reader-form 
        v-model="showForm" 
        :reader="editingReader" 
        @save="onSave" 
        @cancel="onCancel" 
      />
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import SearchForm from '../components/SearchForm.vue'
import ReaderForm from '../components/ReaderForm.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as api from '../api/reader'
import { exportToCsv, parseCsvFile } from '../utils/csv'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const filters = ref({ search: '' })
const readers = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const showConfirm = ref(false)
const showBatchConfirm = ref(false)
const toDelete = ref(null)
const showForm = ref(false)
const editingReader = ref(null)
const loading = ref(false)
const selected = ref([])

async function fetch(){
  loading.value = true
  try {
    const res = await api.fetchReaders({ page: page.value, size: pageSize.value, search: filters.value.search })
    if (res && res.code===0){ 
      readers.value = res.data.items.map(r => ({ 
        ...r, 
        statusText: r.status === 0 ? '正常' : r.status === 1 ? '挂失' : '注销' 
      }))
      total.value = res.data.total 
    }
  } catch (error) {
    ElMessage.error('获取读者列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

onMounted(async ()=>{
  await fetch()
  // If navigated here with ?editId=xxx then open edit modal
  const editId = route.query.editId || route.query.editId === 0 ? String(route.query.editId) : null
  if (editId) {
    try {
      const r = await api.getReader(editId)
      if (r && r.code === 0) {
        editingReader.value = r.data
        showForm.value = true
        // remove query param from history
        router.replace({ path: '/readers', query: {} })
      }
    } catch (e) {
      console.error('打开编辑失败', e)
    }
  }
})
function onSearch(){ page.value = 1; fetch() }
function onPageChange(p){ page.value = p; fetch() }
function onSizeChange(size){ pageSize.value = size; page.value = 1; fetch() }
function onSelectionChange(list){ selected.value = list }
function openCreate(){ editingReader.value = null; showForm.value = true }
function edit(row){ editingReader.value = { ...row }; showForm.value = true }
function onCancel(){ showForm.value = false }

function viewDetail(row){ router.push(`/readers/${row.id}`) }

function resetFilters() {
  filters.value.search = ''
  page.value = 1
  pageSize.value = 10
  fetch()
}

async function onSave(payload){
  try{
    let res
    if (payload.id){
      res = await api.updateReader(payload.id, payload)
      if (res && res.code===0){ 
        showForm.value=false
        fetch()
        ElMessage.success('保存成功') 
      }
    } else {
      res = await api.createReader(payload)
      if (res && res.code===0){ 
        showForm.value=false
        fetch()
        ElMessage.success('创建成功') 
      }
    }
    
    if (res && res.code !== 0) {
      ElMessage.error(res.message || '操作失败')
    }
 }catch(e){ 
    console.error('保存读者失败:', e)
    ElMessage.error('保存失败: ' + (e.message || '未知错误')) 
  }
}

function del(row){ 
  if (row && row.id) {
    toDelete.value = row; 
    showConfirm.value = true
  } else {
    ElMessage.error('无效的读者信息')
  }
}
function onCancelDelete(){ toDelete.value = null; showConfirm.value = false }

async function doDelete(){
  if (!toDelete.value || !toDelete.value.id) {
    ElMessage.error('无效的读者信息')
    return
  }
  
  try {
    const res = await api.deleteReader(toDelete.value.id)
    if (res && res.code === 0) {
      ElMessage.success('删除成功')
      showConfirm.value = false
      toDelete.value = null
      fetch()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    console.error('删除读者失败:', error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error('删除失败: ' + error.response.data.message)
    } else {
      ElMessage.error('删除失败: ' + (error.message || '网络错误'))
    }
  }
}

async function batchDelete() {
  if (selected.value.length === 0) return
  showBatchConfirm.value = true
}

async function doBatchDelete() {
  if (!selected.value || selected.value.length === 0) {
    ElMessage.error('未选择任何读者')
    return
  }
  
  try {
    const deletePromises = selected.value.map(reader => {
      if (reader && reader.id) {
        return api.deleteReader(reader.id)
      } else {
        // 返回一个被拒绝的Promise，以便在结果中处理错误
        return Promise.reject(new Error('无效的读者信息'))
      }
    })
    
    const results = await Promise.allSettled(deletePromises)
    
    let successCount = 0
    let failCount = 0
    
    results.forEach(result => {
      if (result.status === 'fulfilled') {
        if (result.value && result.value.code === 0) {
          successCount++
        } else {
          failCount++
        }
      } else {
        failCount++
      }
    })
    
    if (failCount > 0) {
      ElMessage.warning(`成功删除 ${successCount} 个读者，${failCount} 个删除失败`)
    } else {
      ElMessage.success(`成功删除 ${successCount} 个读者`)
    }
    
    showBatchConfirm.value = false
    selected.value = []
    fetch()
  } catch (error) {
    console.error('批量删除失败:', error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error('批量删除失败: ' + error.response.data.message)
    } else {
      ElMessage.error('批量删除失败: ' + (error.message || '网络错误'))
    }
  }
}

function exportData(){
  const headers = ['姓名', '读者卡号', '证件号', '电话', '注册日期', '状态', '可借书数']
  const rows = readers.value.map(r => [r.name, r.id, r.idNumber, r.phone, r.registerDate, r.status, r.borrowLimit])
  exportToCsv(`readers_${Date.now()}.csv`, headers, rows)
}

function beforeUpload(file){
  parseCsvFile(file, async (data) => {
    try {
      // 简单处理CSV导入，逐条创建读者
      for (const item of data) {
        await api.createReader(item)
      }
      ElMessage.success('导入成功')
      fetch()
    } catch (error) {
      ElMessage.error('导入失败: ' + (error.message || error))
    }
  })
  return false
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .actions {
    justify-content: center;
  }
}
</style>