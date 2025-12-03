<template>
  <layout>
    <template #default>
      <div class="toolbar">
        <search-form @search="onSearch">
          <template #fields>
            <el-form-item>
              <el-input v-model="filters.search" placeholder="书名/作者/ISBN" />
            </el-form-item>
          </template>
        </search-form>
        <div class="actions">
          <el-upload :show-file-list="false" :before-upload="beforeUpload">
            <el-button>导入 CSV</el-button>
          </el-upload>
          <el-button @click="exportCsv">导出 CSV</el-button>
          <el-button type="primary" @click="openCreate">新增图书</el-button>
        </div>
      </div>
      <data-table :data="books" :total="total" @page-change="onPageChange" :loading="loading">
        <template #columns>
          <el-table-column prop="title" label="书名" />
          <el-table-column prop="author" label="作者" />
          <el-table-column prop="publisher" label="出版社" />
          <el-table-column prop="publishDate" label="出版日期" />
          <el-table-column prop="category" label="分类" />
          <el-table-column prop="availableCopies" label="在馆数" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="text" @click="edit(row)">编辑</el-button>
              <el-button type="text" @click="confirmDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </template>
      </data-table>
      <el-dialog title="确认删除" :visible.sync="showConfirm">
        <span>确定要删除这本书吗？</span>
        <template #footer>
          <el-button @click="showConfirm = false">取 消</el-button>
          <el-button type="primary" @click="doDelete">确 定</el-button>
        </template>
      </el-dialog>

      <book-form :visible.sync="showForm" :book="editingBook" @save="onSave" @cancel="onCancel" />
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import SearchForm from '../components/SearchForm.vue'
import BookForm from '../components/BookForm.vue'
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as api from '../api/book'
import { exportToCsv, parseCsvFile } from '../utils/csv'
import { ElMessage } from 'element-plus'

const filters = ref({ search: '' })
const books = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const showConfirm = ref(false)
const toDelete = ref(null)
const showForm = ref(false)
const editingBook = ref(null)
const loading = ref(false)

async function fetch(){
  loading.value = true
  try {
    const res = await api.fetchBooks({ page: page.value, size: pageSize.value, search: filters.value.search })
    if (res && res.code===0){ books.value = res.data.items; total.value = res.data.total }
  } catch (error) {
    ElMessage.error('获取图书列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

onMounted(()=>{ fetch(); window.addEventListener('borrow-success', fetch) })
function onSearch(){ page.value = 1; fetch() }
function onPageChange(p){ page.value = p; fetch() }
function openCreate(){ editingBook.value = null; showForm.value = true }
function edit(row){ editingBook.value = { ...row }; showForm.value = true }
function confirmDelete(row){ toDelete.value = row; showConfirm.value = true }
async function doDelete(){ 
  if (!toDelete.value) return
  try {
    const res = await api.deleteBook(toDelete.value.id)
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

async function onSave(payload){
  try{
    let res
    if (payload.id){
      res = await api.updateBook(payload.id, payload)
      if (res && res.code===0){ 
        showForm.value=false
        fetch()
        ElMessage.success('保存成功') 
      }
    } else {
      res = await api.createBook(payload)
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
    console.error('保存图书失败:', e)
    ElMessage.error('保存失败: ' + (e.message || '未知错误')) 
  }
}
function onCancel(){ showForm.value = false }

async function exportCsv(){
  const headers = ['ID','书名','作者','出版社','ISBN','出版日期','分类','位置','总册数','在馆数']
  const data = books.value.map(r => [r.id,r.title,r.author,r.publisher,r.isbn,r.publishDate,r.category,r.location,r.totalCopies,r.availableCopies])
  exportToCsv(`books_${Date.now()}.csv`, headers, data)
}

async function beforeUpload(file){
  try{
    const { headers, rows } = await parseCsvFile(file)
    const mapIndex = (name) => headers.findIndex(h => h.toLowerCase().includes(name))
    const get = (row, name) => row[mapIndex(name)] ?? ''
    const payload = rows.map(r => ({
      title: get(r,'书名') || get(r,'title'),
      author: get(r,'作者') || get(r,'author'),
      publisher: get(r,'出版社') || get(r,'publisher'),
      isbn: get(r,'isbn') || get(r,'ISBN'),
      publishDate: get(r,'出版') || get(r,'publishDate'),
      category: get(r,'分类') || get(r,'category'),
      location: get(r,'位置') || get(r,'location'),
      totalCopies: Number(get(r,'总册数') || get(r,'totalCopies') || 1)
    }))
    // create sequentially to avoid hammering server
    for (const p of payload){ await api.createBook(p) }
    ElMessage.success('导入成功')
    fetch()
  }catch(e){ ElMessage.error('导入失败: ' + (e.message || e)) }
  return false
}

onBeforeUnmount(()=>{ window.removeEventListener('borrow-success', fetch) })
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
