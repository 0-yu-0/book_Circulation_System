<template>
  <el-dialog :title="mode === 'edit' ? '编辑图书' : '新增图书'" :visible.sync="visible" width="600px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="书名" prop="title">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="form.author" />
      </el-form-item>
      <el-form-item label="ISBN" prop="isbn">
        <el-input v-model="form.isbn" />
      </el-form-item>
      <el-form-item label="出版社" prop="publisher">
        <el-input v-model="form.publisher" />
      </el-form-item>
      <el-form-item label="出版日期" prop="publishDate">
        <el-date-picker v-model="form.publishDate" type="date" placeholder="选择日期" style="width:100%" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-input v-model="form.category" />
      </el-form-item>
      <el-form-item label="位置" prop="location">
        <el-input v-model="form.location" />
      </el-form-item>
      <el-form-item label="总册数" prop="totalCopies">
        <el-input-number v-model="form.totalCopies" :min="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, watch, ref } from 'vue'
const props = defineProps({ visible: Boolean, book: Object })
const emit = defineEmits(['save','cancel'])
const formRef = ref(null)
const mode = props.book ? 'edit' : 'create'

const form = reactive({ id: null, title:'', author:'', isbn:'', publisher:'', publishDate:'', category:'', location:'', totalCopies:1 })

const isbnRegex = /^(?:\d{9}[\dXx]|\d{13})$/
const rules = {
  title:[{ required:true, message:'请输入书名', trigger:'blur' }],
  author:[{ required:true, message:'请输入作者', trigger:'blur' }],
  isbn:[{ required:true, message:'请输入ISBN', trigger:'blur' }, { pattern:isbnRegex, message:'ISBN 格式错误（支持 ISBN-10/13）', trigger:'blur' }],
  totalCopies:[{ type:'number', min:0, message:'数量不能为负' }]
}

watch(()=>props.book, (b)=>{
  if (b){ Object.assign(form, b) } else { Object.assign(form, { id:null, title:'', author:'', isbn:'', publisher:'', publishDate:'', category:'', location:'', totalCopies:1 }) }
})

function submit(){
  formRef.value.validate((valid)=>{
    if (!valid) return
    // normalize publishDate to YYYY-MM-DD if Date object
    if (form.publishDate && form.publishDate instanceof Date) form.publishDate = form.publishDate.toISOString().slice(0,10)
    emit('save', { ...form })
  })
}
</script>
