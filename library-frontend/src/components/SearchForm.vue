<template>
  <el-form :inline="true" @submit.prevent="handleSubmit">
    <slot name="fields" />
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
const emit = defineEmits(['search', 'reset'])

const handleSubmit = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}
</script>