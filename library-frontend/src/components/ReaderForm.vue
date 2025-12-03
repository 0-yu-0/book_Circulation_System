<template>
  <el-dialog 
    :title="mode==='edit' ? '编辑读者' : '新增读者'" 
    :model-value="visible"
    @close="$emit('cancel')"
  >
    <el-form 
      :model="form" 
      :rules="rules" 
      ref="formRef" 
      label-width="110px"
      class="reader-form"
    >
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="证件类型" prop="idType">
        <el-select v-model="form.idType" placeholder="请选择证件类型" style="width: 100%">
          <el-option label="身份证" value="身份证" />
          <el-option label="护照" value="护照" />
          <el-option label="学生证" value="学生证" />
        </el-select>
      </el-form-item>
      <el-form-item label="证件号" prop="idNumber">
        <el-input v-model="form.idNumber" placeholder="请输入证件号" />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入电话" />
      </el-form-item>
      <el-form-item label="可借书数" prop="borrowLimit">
        <el-input-number 
          v-model="form.borrowLimit" 
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
import { reactive, ref, watch } from 'vue'
const props = defineProps({ visible: Boolean, reader: Object })
const emit = defineEmits(['save','cancel'])
const formRef = ref(null)
const submitting = ref(false)
const mode = props.reader ? 'edit' : 'create'
const form = reactive({ id:null, name:'', idType:'身份证', idNumber:'', phone:'', borrowLimit:5 })

const idRegex = /^(?:\d{15}|\d{17}[\dXx])$/
const phoneRegex = /^1[3-9]\d{9}$/
const rules = { 
  name:[{ required:true, message:'请输入姓名', trigger:'blur' }], 
  idNumber:[
    { required:true, message:'请输入证件号', trigger:'blur' }, 
    { 
      validator: (rule, value, callback) => {
        if (form.idType === '身份证' && !idRegex.test(value)) {
          callback(new Error('证件号格式错误'))
        } else {
          callback()
        }
      }, 
      trigger:'blur' 
    }
  ], 
  phone:[
    { required:true, message:'请输入电话', trigger:'blur' }, 
    { pattern:phoneRegex, message:'手机号码格式错误', trigger:'blur' }
  ] 
}

watch(()=>props.reader, (r)=>{ if (r) Object.assign(form,r); else Object.assign(form,{ id:null, name:'', idType:'身份证', idNumber:'', phone:'', borrowLimit:5 }) })

function submit(){ 
  formRef.value.validate((valid)=>{ 
    if (!valid) return
    
    submitting.value = true
    
    // 模拟提交延迟，提供更好的用户体验
    setTimeout(() => {
      emit('save',{ ...form })
      submitting.value = false
    }, 500)
  }) 
}
</script>

<style scoped>
.reader-form {
  padding-right: 10px;
}

.form-button {
  border-radius: 6px;
  padding: 10px 20px;
}
</style>