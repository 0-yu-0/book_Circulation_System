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
          <el-form>
            <el-form-item label="读者卡号">
              <el-input v-model="card" />
              <el-button @click="findReader" :loading="loading">查询</el-button>
            </el-form-item>
            <div v-if="reader">已选：{{reader.name}}</div>
            <el-button type="primary" @click="nextStep" :disabled="!reader">下一步</el-button>
          </el-form>
        </div>
        <div v-else-if="step===2">
          <search-form @search="onSearch">
            <template #fields>
              <el-form-item>
                <el-input v-model="q" placeholder="书名/作者" />
              </el-form-item>
            </template>
          </search-form>
          <data-table :data="books" :loading="loading">
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
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep" :disabled="selectedCount===0">下一步</el-button>
        </div>
        <div v-else>
          <div>确认借阅</div>
          <div>读者：{{reader?.name}}</div>
          <ul>
            <li v-for="b in selectedBooks" :key="b.id">{{b.title}}</li>
          </ul>
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">提交借阅</el-button>
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
import { ref, computed } from 'vue'
import * as readerApi from '../api/reader'
import * as bookApi from '../api/book'
import * as borrowApi from '../api/borrow'
import { useBorrowStore } from '../stores/borrow'

const store = useBorrowStore()
const step = ref(1)
const card = ref('')
const reader = ref(null)
const q = ref('')
const books = ref([])
const loading = ref(false)

const selectedBooks = computed(()=> store.state.selectedBooks)
const selectedCount = computed(()=> selectedBooks.value.length)

const submitting = ref(false)
const showReceipt = ref(false)
const receipt = ref(null)

async function findReader(){
  loading.value = true
  try {
    const res = await readerApi.getReaderByCard(card.value)
    if (res && res.code===0) reader.value = res.data
    else ElMessage.error(res?.message || '查询读者失败')
  } catch (error) {
    ElMessage.error('查询读者失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

async function onSearch(){
  loading.value = true
  try {
    const res = await bookApi.fetchBooks({ search: q.value })
    if (res && res.code===0) books.value = res.data.items
    else ElMessage.error(res?.message || '查询图书失败')
  } catch (error) {
    ElMessage.error('查询图书失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

function add(book){ store.addBook(book) }
function prevStep(){ step.value = Math.max(1, step.value-1) }
function nextStep(){ step.value = Math.min(3, step.value+1) }

async function submit(){
  // re-check stock for each selected book
  submitting.value = true
  try{
    for (const b of selectedBooks.value){
      const bk = await bookApi.getBook(b.id)
      if (!(bk && bk.code===0)) throw new Error('无法验证图书库存')
      if (bk.data.availableCopies <= 0) throw new Error(`《${bk.data.title}》库存不足`)
    }

    const payload = { readerId: reader.value.id, books: selectedBooks.value.map(b=>({ bookId: b.id, count:1 })), borrowDate: new Date().toISOString().slice(0,10), dueDate: '' }
    const res = await borrowApi.createBorrow(payload)
    if (res && res.code===0){
      receipt.value = res.data.receipt || res.data
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
