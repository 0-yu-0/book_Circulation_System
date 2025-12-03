<template>
  <layout>
    <template #default>
      <div class="toolbar">
        <search-form @search="onSearch">
          <template #fields>
            <el-form-item>
              <el-input v-model="filters.search" placeholder="姓名/证件号" />
            </el-form-item>
          </template>
        </search-form>
        <div class="actions">
          <el-upload :show-file-list="false" :before-upload="beforeUpload">
            <el-button>导入 CSV</el-button>
          </el-upload>
          <el-button @click="exportCsv">导出 CSV</el-button>
          <el-button type="primary" @click="openCreate">新增读者</el-button>
        </div>
      </div>
      <data-table :data="readers" :total="total" @page-change="onPageChange" :loading="loading">
        <template #columns>
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="idNumber" label="证件号" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="registerDate" label="注册日期" />
          <el-table-column prop="status" label="状态" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="text" @click="edit(row)">编辑</el-button>
              <el-button type="text" @click="confirmDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </template>
      </data-table>

      <reader-form :visible.sync="showForm" :reader="editingReader" @save="onSave" @cancel="onCancel" />
      <confirm-dialog :visible.sync="showConfirm" message="确认删除该读者吗？" @confirm="doDelete" @cancel="()=>{showConfirm=false}" />
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import SearchForm from '../components/SearchForm.vue'
import ReaderForm from '../components/ReaderForm.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { ref, onMounted } from 'vue'
import * as api from '../api/reader'
import { exportToCsv, parseCsvFile } from '../utils/csv'
import { ElMessage } from 'element-plus'

const filters = ref({ search: '' })
const readers = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const showForm = ref(false)
const editingReader = ref(null)
const showConfirm = ref(false)
const toDelete = ref(null)
const loading = ref(false)

async function fetch(){
  loading.value = true
  try {
    const res = await api.fetchReaders({ page: page.value, size: pageSize.value, search: filters.value.search })
    if (res && res.code===0){ readers.value = res.data.items; total.value = res.data.total }
  } catch (error) {
    ElMessage.error('获取读者列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

onMounted(fetch)
function onSearch(){ page.value = 1; fetch() }
function onPageChange(p){ page.value = p; fetch() }
function openCreate(){ editingReader.value = null; showForm.value = true }
function edit(row){ editingReader.value = { ...row }; showForm.value = true }
function onCancel(){ showForm.value = false }

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

function confirmDelete(row){ toDelete.value = row; showConfirm.value = true }
async function doDelete(){ 
  if (!toDelete.value) return
  try {
    const res = await api.deleteReader(toDelete.value.id)
    if (res && res.code===0){ 
      showConfirm.value=false
      ElMessage.success('删除成功')
      fetch() 
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败: ' + (error.message || error))
  }
}

async function exportCsv(){
  const headers = ['ID','姓名','证件类型','证件号','电话','注册日期','状态','可借书数']
  const data = readers.value.map(r => [r.id,r.name,r.idType,r.idNumber,r.phone,r.registerDate,r.status,r.borrowLimit])
  exportToCsv(`readers_${Date.now()}.csv`, headers, data)
}

async function beforeUpload(file){
  try{
    const { headers, rows } = await parseCsvFile(file)
    const mapIndex = (name) => headers.findIndex(h => h.toLowerCase().includes(name))
    const get = (row, name) => row[mapIndex(name)] ?? ''
    const payload = rows.map(r => ({
      name: get(r,'姓名') || get(r,'name'),
      idType: get(r,'证件类型') || '身份证',
      idNumber: get(r,'证件号') || get(r,'idnumber'),
      phone: get(r,'电话') || get(r,'phone'),
      registerDate: get(r,'注册') || get(r,'registerDate'),
      borrowLimit: Number(get(r,'可借书数') || get(r,'borrowLimit') || 5)
    }))
    for (const p of payload){ await api.createReader(p) }
    ElMessage.success('导入成功')
    fetch()
  }catch(e){ ElMessage.error('导入失败: ' + (e.message || e)) }
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
