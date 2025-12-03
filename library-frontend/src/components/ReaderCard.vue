<template>
  <el-card>
    <div>
      <div><strong>{{reader.name}}</strong></div>
      <div>证件号：{{maskedId}}</div>
      <div>当前借书：{{reader.currentBorrowCount || 0}}</div>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({ reader: Object })
import { computed } from 'vue'
const maskedId = computed(()=>{
  const id = props.reader?.idNumber || ''
  if (id.length>6) return id.slice(0,2)+'****'+id.slice(-2)
  return id
})
</script>

