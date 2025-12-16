<template>
  <layout>
    <template #default>
      <div class="toolbar">
        <search-form @search="onSearch" @reset="resetFilters">
          <template #fields>
            <el-form-item>
              <el-input v-model="filters.search" placeholder="书名/作者/ISBN" />
            </el-form-item>
            <el-form-item>
              <el-select v-model="filters.category" placeholder="选择类别" clearable style="width: 150px;">
                <el-option
                  v-for="category in categories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </template>
        </search-form>
        <div class="actions">
          <el-upload :show-file-list="false" :before-upload="beforeUpload">
            <el-button>导入数据</el-button>
          </el-upload>
          <el-button @click="exportCsv">导出数据</el-button>
          <el-button type="danger" :disabled="selected.length === 0" @click="batchDelete">批量删除</el-button>
          <el-button type="primary" @click="openCreate">新增图书</el-button>
        </div>
      </div>
      <data-table 
        :data="books" 
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
          <el-table-column prop="title" label="书名" />
          <el-table-column prop="id" label="图书ID" />
          <el-table-column prop="author" label="作者" />
          <el-table-column prop="publisher" label="出版社" />
          <el-table-column prop="publishDate" label="出版日期" />
          <el-table-column prop="category" label="分类" />
          <el-table-column prop="availableCopies" label="在馆数" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button type="text" @click="edit(row)">编辑</el-button>
              <el-button type="text" @click="confirmDelete(row)">删除</el-button>
              <el-button type="text" @click="viewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </template>
      </data-table>
      <el-dialog title="确认删除" v-model="showConfirm">
        <span>确定要删除这本书吗？</span>
        <template #footer>
          <el-button @click="showConfirm = false">取 消</el-button>
          <el-button type="primary" @click="doDelete">确 定</el-button>
        </template>
      </el-dialog>
      
      <el-dialog title="批量删除确认" v-model="showBatchConfirm">
        <span>确定要删除选中的 {{ selected.length }} 本书吗？</span>
        <template #footer>
          <el-button @click="showBatchConfirm = false">取 消</el-button>
          <el-button type="primary" @click="doBatchDelete">确 定</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const filters = ref({ search: '', category: '' })
const books = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const showConfirm = ref(false)
const showBatchConfirm = ref(false)
const toDelete = ref(null)
const showForm = ref(false)
const editingBook = ref(null)
const loading = ref(false)
const selected = ref([])
const categories = ref([])

async function fetch(){
  loading.value = true
  try {
    const res = await api.fetchBooks({ 
      page: page.value, 
      size: pageSize.value, 
      search: filters.value.search,
      category: filters.value.category
    })
    if (res && res.code===0){ 
      books.value = res.data.items
      total.value = res.data.total 
    } else {
      ElMessage.error(res?.message || '获取图书列表失败')
    }
  } catch (error) {
    ElMessage.error('获取图书列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

async function fetchCategories(){
  try {
    const res = await api.fetchCategories()
    if (res && res.code===0) {
      categories.value = res.data || []
    } else {
      ElMessage.error(res?.message || '获取图书类别失败')
    }
  } catch (error) {
    ElMessage.error('获取图书类别失败: ' + (error.message || error))
  }
}

onMounted(()=>{ 
  fetch(); 
  fetchCategories();
  window.addEventListener('borrow-success', fetch) 
})
function onSearch(){ page.value = 1; fetch() }
function onPageChange(p){ page.value = p; fetch() }
function onSizeChange(size){ pageSize.value = size; page.value = 1; fetch() }
function onSelectionChange(list){ selected.value = list }
function openCreate(){ editingBook.value = null; showForm.value = true }
function edit(row){ editingBook.value = { ...row }; showForm.value = true }
function viewDetail(row){ router.push(`/books/${row.id}`) }
function confirmDelete(row){ 
  if (row && row.id) {
    toDelete.value = row; 
    showConfirm.value = true
  } else {
    ElMessage.error('无效的图书信息')
  }
}

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
    console.error('删除图书失败:', error)
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
    ElMessage.error('未选择任何图书')
    return
  }
  
  try {
    const deletePromises = selected.value.map(book => {
      if (book && book.id) {
        return api.deleteBook(book.id)
      } else {
        // 返回一个被拒绝的Promise，以便在结果中处理错误
        return Promise.reject(new Error('无效的图书信息'))
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
      ElMessage.warning(`成功删除 ${successCount} 本书，${failCount} 本删除失败`)
    } else {
      ElMessage.success(`成功删除 ${successCount} 本书`)
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

async function onSave(payload){
  console.log('BookList: onSave called with payload:', payload);
  console.log('BookList: payload.id:', payload.id);
  try{
    let res
    if (payload.id){
      console.log('BookList: calling updateBook API');
      res = await api.updateBook(payload.id, payload)
      console.log('BookList: updateBook response:', res);
      if (res && res.code===0){ 
        showForm.value=false
        fetch()
        ElMessage.success('保存成功') 
      }
    } else {
      console.log('BookList: calling createBook API');
      res = await api.createBook(payload)
      console.log('BookList: createBook response:', res);
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

function resetFilters() {
  filters.value.search = ''
  filters.value.category = ''
  page.value = 1
  pageSize.value = 10
  fetch()
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