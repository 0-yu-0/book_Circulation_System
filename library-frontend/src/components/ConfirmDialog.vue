<template>
  <el-dialog :visible.sync="visible" width="400px">
    <span>{{ message }}</span>
    <template #footer>
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="$emit('confirm')">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({ visible: Boolean, message: { type: String, default: '确认执行此操作？' } })
</script>

