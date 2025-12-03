<template>
  <el-card shadow="hover">
    <div class="cover" v-if="book.cover"><img :src="book.cover" alt="封面"/></div>
    <div class="info">
      <h4>{{book.title}}</h4>
      <p>{{book.author}}</p>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({ book: Object })
</script>

<style scoped>
.cover img{width:80px;height:100px;object-fit:cover}
.info{padding-left:8px}
</style>

