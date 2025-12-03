<template>
  <el-table 
    :data="data" 
    style="width:100%" 
    @selection-change="$emit('selection-change',$event)" 
    v-loading="loading"
    stripe
    highlight-current-row
    :height="tableHeight"
  >
    <el-table-column type="selection" width="55" />
    <slot name="columns" />
  </el-table>
  <div class="pagination-container">
    <el-pagination 
      :total="total" 
      :page-size="pageSize" 
      :current-page="page" 
      @current-change="$emit('page-change',$event)"
      layout="total, prev, pager, next, jumper"
      background
    />
  </div>
</template>

<script setup>
import { toRefs, ref, onMounted, onBeforeUnmount } from 'vue'
const props = defineProps({ 
  data: Array, 
  total: Number, 
  page: { type: Number, default: 1 }, 
  pageSize: { type: Number, default: 20 }, 
  loading: Boolean,
  fixedHeight: { type: Boolean, default: false }
})

const { data, total, page, pageSize, loading } = toRefs(props)
defineEmits(['selection-change', 'page-change'])

// 表格高度计算
const tableHeight = ref(null)

// 处理窗口大小变化
const handleResize = () => {
  if (props.fixedHeight) {
    // 根据屏幕高度动态计算表格高度
    const screenHeight = window.innerHeight
    // 为分页器和其他元素预留空间
    tableHeight.value = screenHeight - 300
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 自定义分页样式 */
:deep(.el-pagination) {
  padding: 0;
}

:deep(.el-pagination .el-pager li.active) {
  background-color: #409EFF;
  color: white;
}

:deep(.el-pagination .btn-next),
:deep(.el-pagination .btn-prev) {
  border-radius: 4px;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 4px;
}
</style>