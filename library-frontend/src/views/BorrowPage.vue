<template>
  <layout>
    <template #default>
      <el-steps :active="step">
        <el-step title="选择读者" />
        <el-step title="选择图书" />
        <el-step title="确认借阅" />
      </el-steps>

      <div style="margin-top:16px">
        <div v-if="step===1">
          <el-row :gutter="20">
            <el-col :span="24">
              <search-form @search="onSearchReader" @reset="resetReaderSearch">
                <template #fields>
                  <el-form-item>
                    <el-input v-model="readerSearch" placeholder="读者姓名/证件号" />
                  </el-form-item>
                </template>
              </search-form>
              
              <el-row :gutter="20">
                <el-col :span="16">
                  <data-table 
                    :data="readers" 
                    :loading="loading" 
                    :show-selection="false"
                    :total="readerTotal"
                    :page="readerPage"
                    :page-size="readerPageSize"
                    @page-change="handleReaderPageChange"
                    @size-change="handleReaderSizeChange"
                  >
                    <template #columns>
                      <el-table-column prop="name" label="姓名" />
                      <el-table-column prop="id" label="卡号" />
                      <el-table-column label="操作">
                        <template #default="{ row }">
                          <el-button @click="selectReader(row)">选择</el-button>
                        </template>
                      </el-table-column>
                    </template>
                  </data-table>
                </el-col>
                
                <el-col :span="8">
                  <el-card v-if="reader" class="selected-reader">
                    <template #header>
                      <div class="clearfix">
                        <span>已选读者</span>
                      </div>
                    </template>
                    <div class="reader-info">
                      <el-descriptions :column="1" size="small" border>
                        <el-descriptions-item label="姓名">{{ reader.name }}</el-descriptions-item>
                        <el-descriptions-item label="卡号">{{ reader.id }}</el-descriptions-item>
                        <el-descriptions-item label="状态">{{ readerStatusMap[reader.status] || '正常' }}</el-descriptions-item>
                      </el-descriptions>
                    </div>
                    <el-button type="primary" @click="nextStep" :disabled="!reader" style="margin-top: 15px; width: 100%;">下一步</el-button>
                  </el-card>
                  
                  <el-card v-else class="no-selected-reader">
                    <template #header>
                      <div class="clearfix">
                        <span>已选读者</span>
                      </div>
                    </template>
                    <div class="no-reader-placeholder">
                      <el-empty description="暂未选择读者" :image-size="80" />
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </el-col>
          </el-row>
        </div>
        <div v-else-if="step===2">
          <el-row :gutter="20">
            <el-col :span="isOpenBasket ? 16 : 22">
              <search-form @search="onSearch" @reset="resetBookSearch">
                <template #fields>
                  <el-form-item label="书名">
                    <el-input v-model="q" placeholder="请输入书名" />
                  </el-form-item>
                  <el-form-item label="作者">
                    <el-input v-model="author" placeholder="请输入作者" />
                  </el-form-item>
                </template>
              </search-form>
              <data-table 
                :data="books" 
                :loading="loading" 
                :show-selection="false"
                :total="bookTotal"
                :page="bookPage"
                :page-size="bookPageSize"
                @page-change="handleBookPageChange"
                @size-change="handleBookSizeChange"
                :fixed-height="true"
              >
                <template #columns>
                  <el-table-column prop="title" label="书名" />
                  <el-table-column prop="author" label="作者" />
                  <el-table-column prop="availableCopies" label="在馆数" />
                  <el-table-column label="操作">
                    <template #default="{ row }">
                      <el-button @click="add(row)" :disabled="row.availableCopies<=0">加入借书篮</el-button>
                    </template>
                  </el-table-column>
                </template>
              </data-table>
              <div class="step-buttons">
                <el-button @click="prevStep">上一步</el-button>
                <el-button type="primary" @click="nextStep" :disabled="selectedCount===0">下一步</el-button>
              </div>
            </el-col>
            
            <el-col :span="isOpenBasket ? 8 : 2">
              <div v-if="isOpenBasket" class="basket-container">
                <div class="basket-header" @click="toggleBasket">
                  <span>借书篮 ({{ selectedCount }})</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
                <div class="basket-content">
                  <div class="basket-items">
                    <div v-if="selectedBooks.length === 0" class="empty-basket">
                      借书篮为空
                    </div>
                    <div v-else class="basket-items-list">
                      <div v-for="book in selectedBooks" :key="book.id" class="basket-item">
                        <div class="book-info">
                          <div class="book-title">{{ book.title }}</div>
                          <div class="book-author">{{ book.author }}</div>
                        </div>
                        <div class="book-actions">
                          <el-input-number 
                            v-model="book.count" 
                            :min="0" 
                            :max="book.availableCopies"
                            size="small"
                            @change="(val) => updateBookCount(book.id, val)"
                          />
                          <el-button type="danger" icon="Delete" size="small" @click="remove(book.id)"></el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="basket-footer">
                    <el-button type="danger" @click="clearBasket" :disabled="selectedBooks.length === 0">清空借书篮</el-button>
                  </div>
                </div>
              </div>
              
              <div v-else class="basket-toggle" @click="toggleBasket">
                <el-popover
                  placement="left"
                  trigger="hover"
                  :width="200"
                  popper-class="basket-popover"
                >
                  <template #reference>
                    <el-badge :value="selectedCount" :max="99" :hidden="selectedCount === 0" class="basket-badge">
                      <el-button type="primary" :icon="Reading" circle size="large" />
                    </el-badge>
                  </template>
                  <div class="basket-preview">
                    <div class="basket-preview-header">
                      <strong>借书篮预览</strong>
                      <span>({{ selectedCount }})</span>
                    </div>
                    <div class="basket-preview-content">
                      <div v-if="selectedBooks.length === 0" class="preview-empty">
                        借书篮为空
                      </div>
                      <div v-else>
                        <div v-for="book in selectedBooks.slice(0, 3)" :key="book.id" class="preview-item">
                          <div class="preview-item-title">{{ book.title }}</div>
                          <div class="preview-item-count">数量: {{ book.count }}</div>
                        </div>
                        <div v-if="selectedBooks.length > 3" class="preview-more">
                          还有 {{ selectedBooks.length - 3 }} 本书...
                        </div>
                      </div>
                    </div>
                    <div class="basket-preview-footer">
                      <el-button type="primary" size="small" @click="toggleBasket">查看借书篮</el-button>
                    </div>
                  </div>
                </el-popover>
              </div>
            </el-col>
          </el-row>
        </div>
        <div v-else>
          <div>确认借阅</div>
          <div>读者：{{reader?.name}}</div>
          <el-table :data="selectedBooks" style="width: 100%">
            <el-table-column prop="title" label="书名" />
            <el-table-column prop="author" label="作者" />
            <el-table-column prop="count" label="数量" />
          </el-table>
          <div class="step-buttons">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" :loading="submitting" @click="submit">提交借阅</el-button>
          </div>
        </div>
      </div>

      <receipt-dialog :visible.sync="showReceipt" :receipt="receipt" @close="closeReceipt" />
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import ReceiptDialog from '../components/ReceiptDialog.vue'
import { ref, computed, onMounted, watch } from 'vue'
import * as readerApi from '../api/reader'
import * as bookApi from '../api/book'
import * as borrowApi from '../api/borrow'
import { useBorrowStore } from '../stores/borrow'
import { ElMessage, ElBadge, ElPopover } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowRight, Reading } from '@element-plus/icons-vue'

const store = useBorrowStore()
const router = useRouter()
const step = ref(1)
const card = ref('')
const reader = ref(null)
const q = ref('')
const books = ref([])
const loading = ref(false)
const isOpenBasket = ref(false) // 控制借书篮是否展开

// 读者列表相关状态
const readers = ref([])
const readerSearch = ref('')
const readerPage = ref(1)
const readerPageSize = ref(10)
const readerTotal = ref(0)

// 图书列表相关状态
const bookPage = ref(1)
const bookPageSize = ref(10)
const bookTotal = ref(0)

// 读者状态映射
const readerStatusMap = {
  0: '正常',
  1: '挂失',
  2: '注销'
}

const selectedBooks = computed(() => store.state.selectedBooks)
const selectedCount = computed(() => selectedBooks.value.length)

const submitting = ref(false)
const showReceipt = ref(false)
const receipt = ref(null)

// 页面加载时获取读者列表
onMounted(() => {
  loadReaders()
  
  // 监听借书成功事件，重新加载图书列表
  const handleBorrowSuccess = () => {
    if (step.value === 2) {
      loadBooks()
    }
  }
  
  window.addEventListener('borrow-success', handleBorrowSuccess)
  
  // 组件卸载时移除事件监听器
  return () => {
    window.removeEventListener('borrow-success', handleBorrowSuccess)
  }
})

// 监听路由变化，如果离开借书页面则清空借书篮
watch(() => router.currentRoute.value, (to, from) => {
  // 如果从借书页面跳转到其他页面，则清空借书篮
  if (from.path === '/borrow' && to.path !== '/borrow') {
    step.value = 1
    clearBasket()
  }
}, { immediate: false })

// 切换借书篮显示状态
function toggleBasket() {
  isOpenBasket.value = !isOpenBasket.value
}

// 加载读者列表
async function loadReaders() {
  loading.value = true
  try {
    const params = {
      page: readerPage.value,
      size: readerPageSize.value,
      search: readerSearch.value
    }
    
    const res = await readerApi.fetchReaders(params)
    if (res && res.code === 0) {
      readers.value = res.data.items || []
      readerTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res?.message || '获取读者列表失败')
    }
  } catch (error) {
    ElMessage.error('获取读者列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

// 处理读者分页变化
function handleReaderPageChange(page) {
  readerPage.value = page
  loadReaders()
}

// 处理读者分页大小变化
function handleReaderSizeChange(size) {
  readerPageSize.value = size
  readerPage.value = 1
  loadReaders()
}

// 搜索读者
async function onSearchReader() {
  readerPage.value = 1
  await loadReaders()
}

// 重置读者搜索
function resetReaderSearch() {
  readerSearch.value = ''
  readerPage.value = 1
  loadReaders()
}

// 选择读者
function selectReader(selectedReader) {
  reader.value = selectedReader
  store.setReader(selectedReader)
}

// 加载图书列表
async function loadBooks() {
  loading.value = true
  try {
    const params = {
      page: bookPage.value,
      size: bookPageSize.value,
      search: q.value,
      availableOnly: 'true'  // 只获取可用图书
    }
    
    const res = await bookApi.fetchBooks(params)
    if (res && res.code === 0) {
      const data = res.data || {}
      const items = data.items || []
      
      // 后端已经过滤了可用图书，直接使用返回的数据
      books.value = items
      bookTotal.value = data.total || 0
    } else {
      ElMessage.error(res?.message || '获取图书列表失败')
    }
  } catch (error) {
    ElMessage.error('获取图书列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

// 处理图书分页变化
function handleBookPageChange(page) {
  bookPage.value = page
  loadBooks()
}

// 处理图书分页大小变化
function handleBookSizeChange(size) {
  bookPageSize.value = size
  bookPage.value = 1
  loadBooks()
}

// 搜索图书
async function onSearch(){
  bookPage.value = 1
  await loadBooks()
}

// 重置图书搜索
function resetBookSearch() {
  q.value = ''
  bookPage.value = 1
  bookPageSize.value = 10
  loadBooks()
}

// 页面切换到第二步时加载图书
watch(step, (newStep) => {
  if (newStep === 2) {
    loadBooks()
  }
})

function add(book){ 
  store.addBook(book) 
  // 点击加入借书篮按钮时自动展开借书篮
  isOpenBasket.value = true
}

function remove(bookId) {
  store.removeBook(bookId)
}

function clearBasket() {
  store.clearSelection()
}

function updateBookCount(bookId, count) {
  if (count === 0) {
    remove(bookId)
  } else {
    store.setCount(bookId, count)
  }
}

function prevStep(){ 
  step.value = Math.max(1, step.value-1) 
  // 返回上一步时清空借书篮
  clearBasket()
}

function nextStep(){ 
  step.value = Math.min(3, step.value+1) 
}

async function submit(){
  // re-check stock for each selected book
  submitting.value = true
  try{
    for (const b of selectedBooks.value){
      const bk = await bookApi.getBook(b.id)
      if (!(bk && bk.code===0)) throw new Error('无法验证图书库存')
      if (bk.data.availableCopies <= 0) throw new Error(`《${bk.data.title}》库存不足`)
    }

    // Check borrow limit before submitting
    const readerDetail = await readerApi.getReader(reader.value.id ?? reader.value.readerId)
    if (readerDetail && readerDetail.code === 0) {
      const readerInfo = readerDetail.data
      const currentBorrows = readerInfo.currentBorrowCount || 0
      const maxBorrows = readerInfo.borrowLimit || 5  // Default to 5 if not set
      const newBorrowCount = selectedBooks.value.reduce((sum, book) => sum + (book.count || 1), 0)
      
      if (currentBorrows + newBorrowCount > maxBorrows) {
        throw new Error(`借阅数量超过限制。当前已借: ${currentBorrows}本，最大可借: ${maxBorrows}本，本次尝试借阅: ${newBorrowCount}本`)
      }
    } else {
      throw new Error('无法获取读者信息')
    }

    const payload = { readerId: reader.value.id ?? reader.value.readerId, books: selectedBooks.value.map(b=>({ bookId: b.id, count: b.count })), borrowDate: new Date().toISOString().slice(0,10), dueDate: '' }
    const res = await borrowApi.createBorrow(payload)
    if (res && res.code===0){
      // backend may return { borrowIds: [...] } or { receipt: {...} }
      receipt.value = res.data.receipt || res.data || { borrowIds: res.data.borrowIds }
      showReceipt.value = true
      // clear selection
      store.clearSelection()
      // notify others to refresh
      window.dispatchEvent(new CustomEvent('borrow-success'))
      ElMessage.success('借阅成功')
    } else {
      ElMessage.error(res?.message || '借阅失败')
    }
  }catch(e){
    // show error
    console.error(e)
    ElMessage.error(e.message || '借阅失败')
  }finally{ submitting.value = false }
}

function closeReceipt(){ showReceipt.value = false }
</script>

<style scoped>
.step-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
}

.basket-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.basket-header {
  padding: 12px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.basket-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.basket-items {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.basket-items-list {
  max-height: calc(100vh - 250px);
  overflow-y: auto;
}

.empty-basket {
  text-align: center;
  color: #999;
  padding: 40px 20px;
}

.basket-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.book-info {
  flex: 1;
}

.book-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.book-author {
  font-size: 12px;
  color: #666;
}

.book-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.basket-footer {
  padding: 15px;
  border-top: 1px solid #ebeef5;
  background-color: #f5f7fa;
  text-align: center;
}

.basket-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  margin-top: 10px;
}

.basket-badge :deep(.el-badge__content) {
  transform: translateY(-50%) translateX(100%) scale(0.8) !important;
  top: 0 !important;
  right: 10px !important;
}

.selected-reader,
.no-selected-reader {
  height: 100%;
}

.reader-info {
  margin-bottom: 15px;
}

.no-reader-placeholder {
  text-align: center;
  padding: 20px 0;
}

/* 借书篮预览样式 */
.basket-preview {
  padding: 10px;
}

.basket-preview-header {
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
  padding-bottom: 5px;
}

.preview-item {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
}

.preview-item-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-item-count {
  font-size: 12px;
  color: #666;
}

.preview-more {
  font-size: 12px;
  color: #999;
  text-align: center;
  padding: 5px 0;
}

.basket-preview-footer {
  margin-top: 10px;
  text-align: center;
}

:deep(.basket-popover) {
  padding: 0 !important;
}
</style>