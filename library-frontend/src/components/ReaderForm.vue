<template>
  <el-dialog 
    :title="mode === 'edit' ? '编辑读者' : '新增读者'" 
    :model-value="visibleComputed"
    @close="handleClose"
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
      <el-button @click="cancel" class="form-button">取消</el-button>
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
import { reactive, ref, watch, computed } from 'vue'
// accept both modelValue and visible for compatibility; accept reader or data as prop name
const props = defineProps({ modelValue: Boolean, visible: Boolean, reader: Object, data: Object })
const emit = defineEmits(['save','cancel','update:modelValue'])
const formRef = ref(null)
const submitting = ref(false)

// computed visible to support v-model and legacy `visible` prop
const visibleComputed = computed({
  get() {
    // prefer modelValue, fallback to visible
    return typeof props.modelValue === 'boolean' ? props.modelValue : !!props.visible
  },
  set(v){
    emit('update:modelValue', v)
  }
})

// mode is dynamic based on incoming reader/data
const mode = computed(() => {
  const r = props.reader || props.data
  return r && (r.id || r.id === 0) ? 'edit' : 'create'
})

const form = reactive({ id:null, name:'', idType:'身份证', idNumber:'', phone:'', borrowLimit:5 })

const idRegex = /^(?:\d{15}|\d{17}[\dXx])$/
const phoneRegex = /^1[3-9]\d{9}$/
const rules = { 
  name:[{ required:true, message:'请输入姓名', trigger:'blur' }], 
  idNumber:[
    { required:true, message:'请输入证件号', trigger:'blur' }, 
    { 
      validator: (rule, value, callback) => {
        if (form.idType === '身份证' && value && !idRegex.test(value)) {
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

// keep form in sync when props.reader or props.data change
watch(()=>props.reader || props.data, (r)=>{
  if (r && Object.keys(r).length) { Object.assign(form, r) }
  else { Object.assign(form,{ id:null, name:'', idType:'身份证', idNumber:'', phone:'', borrowLimit:5 }) }
})

function handleClose(){
  // emit update for v-model and emit cancel
  emit('update:modelValue', false)
  emit('cancel')
}

function cancel(){
  emit('update:modelValue', false)
  emit('cancel')
}

function submit(){ 
  formRef.value.validate((valid)=>{ 
    if (!valid) return
    
    submitting.value = true
    
    // Map frontend field names to backend API field names
    const backendForm = {
      readerName: form.name,
      readerCardType: form.idType,
      readerCardNumber: form.idNumber,
      readerPhoneNumber: form.phone,
      // Set default values for required fields
      readerStatus: 0, // 0表示正常状态
      totalBorrowNumber: 0, // 初始总借书数为0
      nowBorrowNumber: 0, // 初始当前借书数为0
      registerDate: new Date().toISOString().split('T')[0] // 当前日期
    }
    
    // 模拟提交延迟，提供更好的用户体验
    setTimeout(() => {
      emit('save', backendForm)
      submitting.value = false
      // close dialog
      emit('update:modelValue', false)
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