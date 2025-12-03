<template>
  <el-dialog :title="mode==='edit' ? '编辑读者' : '新增读者'" :visible.sync="visible">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="证件类型" prop="idType">
        <el-input v-model="form.idType" />
      </el-form-item>
      <el-form-item label="证件号" prop="idNumber">
        <el-input v-model="form.idNumber" />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="可借书数" prop="borrowLimit">
        <el-input-number v-model="form.borrowLimit" :min="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
const props = defineProps({ visible: Boolean, reader: Object })
const emit = defineEmits(['save','cancel'])
const formRef = ref(null)
const mode = props.reader ? 'edit' : 'create'
const form = reactive({ id:null, name:'', idType:'身份证', idNumber:'', phone:'', borrowLimit:5 })

const idRegex = /^(?:\d{15}|\d{17}[\dXx])$/
const phoneRegex = /^1[3-9]\d{9}$/
const rules = { name:[{ required:true, message:'请输入姓名', trigger:'blur' }], idNumber:[{ required:true, message:'请输入证件号', trigger:'blur' }, { pattern:idRegex, message:'证件号格式错误', trigger:'blur' }], phone:[{ required:true, message:'请输入电话', trigger:'blur' }, { pattern:phoneRegex, message:'手机号码格式错误', trigger:'blur' }] }

watch(()=>props.reader, (r)=>{ if (r) Object.assign(form,r); else Object.assign(form,{ id:null, name:'', idType:'身份证', idNumber:'', phone:'', borrowLimit:5 }) })

function submit(){ formRef.value.validate((valid)=>{ if (!valid) return; emit('save',{ ...form }) }) }
</script>
