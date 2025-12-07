<template>
  <el-dialog 
    :title="mode === 'edit' ? '编辑图书' : '新增图书'" 
    :model-value="visible" 
    width="600px"
    @close="$emit('cancel')"
  >
    <el-form 
      :model="form" 
      :rules="rules" 
      ref="formRef" 
      label-width="100px"
      class="book-form"
    >
      <el-form-item label="书名" prop="title">
        <el-input v-model="form.title" placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="form.author" placeholder="请输入作者" />
      </el-form-item>
      <el-form-item label="ISBN" prop="isbn">
        <el-input v-model="form.isbn" placeholder="请输入ISBN" />
      </el-form-item>
      <el-form-item label="出版社" prop="publisher">
        <el-input v-model="form.publisher" placeholder="请输入出版社" />
      </el-form-item>
      <el-form-item label="出版日期" prop="publishDate">
        <el-date-picker 
          v-model="form.publishDate" 
          type="date" 
          placeholder="选择日期" 
          style="width:100%" 
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-input v-model="form.category" placeholder="请输入分类" />
      </el-form-item>
      <el-form-item label="位置" prop="location">
        <el-input v-model="form.location" placeholder="请输入位置" />
      </el-form-item>
      <el-form-item label="总册数" prop="totalCopies">
        <el-input-number 
          v-model="form.totalCopies" 
          :min="0" 
          controls-position="right"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('cancel')" class="form-button">取消</el-button>
      <el-button 
        type="primary" 
        @click="submit" 
        class="form-button"
        :loading="submitting"
      >
        {{ submitting ? '保存中...' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, watch, ref, computed } from 'vue'
const props = defineProps({ visible: Boolean, book: Object })
const emit = defineEmits(['save','cancel'])
const formRef = ref(null)
const submitting = ref(false)
const mode = computed(() => props.book ? 'edit' : 'create')

const form = reactive({ id: null, title:'', author:'', isbn:'', publisher:'', publishDate:'', category:'', location:'', totalCopies:1 })

const isbnRegex = /^(?:\d{9}[\dXx]|\d{13})$/
const rules = {
  title:[{ required:true, message:'请输入书名', trigger:'blur' }],
  author:[{ required:true, message:'请输入作者', trigger:'blur' }],
  isbn:[{ required:true, message:'请输入ISBN', trigger:'blur' }, { pattern:isbnRegex, message:'ISBN 格式错误（支持 ISBN-10/13）', trigger:'blur' }],
  totalCopies:[{ type:'number', min:0, message:'数量不能为负' }]
}

watch(()=>props.book, (b)=>{
  console.log('BookForm: received book object:', b);
  if (b){ 
    // Map book object fields to form fields, handling both id and bookId
    const mappedForm = {
      id: b.id || b.bookId, // Handle both id and bookId fields
      title: b.title || b.bookName,
      author: b.author || b.bookAuthor,
      isbn: b.isbn,
      publisher: b.publisher || b.bookPublisher,
      publishDate: b.publishDate || b.bookPubDate,
      category: b.category || b.bookCategory,
      location: b.location || b.bookLocation,
      totalCopies: b.totalCopies || b.bookTotalCopies || 1
    };
    console.log('BookForm: mapped form data:', mappedForm);
    Object.assign(form, mappedForm);
  } else { 
    console.log('BookForm: clearing form for create mode');
    Object.assign(form, { id:null, title:'', author:'', isbn:'', publisher:'', publishDate:'', category:'', location:'', totalCopies:1 }) 
  }
  console.log('BookForm: current form.id:', form.id);
  console.log('BookForm: current mode:', mode);
})

function submit(){
  formRef.value.validate((valid)=>{
    if (!valid) return
    
    submitting.value = true
    
    // normalize publishDate to YYYY-MM-DD if Date object
    if (form.publishDate && form.publishDate instanceof Date) {
      form.publishDate = form.publishDate.toISOString().slice(0,10)
    }
    
    // Map frontend field names to backend API field names
    const backendForm = {
      bookName: form.title,
      bookAuthor: form.author,
      isbn: form.isbn,
      bookPublisher: form.publisher,
      bookPubDate: form.publishDate,
      bookCategory: form.category,
      bookLocation: form.location,
      bookTotalCopies: form.totalCopies
    }
    
    if (mode.value === 'edit') {
      // Edit mode: use existing bookId and preserve existing values
      if (form.id) {
        backendForm.bookId = form.id;
        // Also set the id field for BookList.vue to recognize edit mode
        backendForm.id = form.id;
      }
      // For edit mode, use existing values from the book object
      // Don't set default values as they will overwrite existing data
    } else {
      // Create mode: set default values for new books
      backendForm.bookAvailableCopies = form.totalCopies || 1; // 默认可用册数等于总册数
      backendForm.bookPrice = 0; // 默认价格
      backendForm.borrowCount = 0; // 初始借阅次数
    }

    console.log('BookForm: submitting backendForm:', backendForm);
    console.log('BookForm: backendForm.id:', backendForm.id);
    console.log('BookForm: backendForm.bookId:', backendForm.bookId);

    // 模拟提交延迟，提供更好的用户体验
    setTimeout(() => {
      emit('save', backendForm)
      submitting.value = false
    }, 500)
  })
}
</script>

<style scoped>
.book-form {
  padding-right: 10px;
}

.form-button {
  border-radius: 6px;
  padding: 10px 20px;
}
</style>