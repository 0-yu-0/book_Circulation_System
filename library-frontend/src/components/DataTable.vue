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
    <el-table-column v-if="showSelection" type="selection" width="55" />
    <slot name="columns" />
  </el-table>
  <div v-if="showPagination" class="pagination-container">
    <el-pagination 
      :total="total" 
      :page-size="pageSize" 
      :current-page="page" 
      @current-change="$emit('page-change',$event)"
      @size-change="$emit('size-change',$event)"
      layout="total, sizes, prev, pager, next, jumper"
      background
      :page-sizes="[5, 10, 20, 50, 100]"
    />
  </div>
</template>

<script setup>
import { toRefs, ref, onMounted, onBeforeUnmount } from 'vue'
const props = defineProps({ 
  data: Array, 
  total: Number, 
  page: { type: Number, default: 1 }, 
  pageSize: { type: Number, default: 10 }, 
  loading: Boolean,
  fixedHeight: { type: Boolean, default: false },
  showSelection: { type: Boolean, default: true },
  showPagination: { type: Boolean, default: true }
})

const { data, total, page, pageSize, loading, showSelection, showPagination } = toRefs(props)

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

defineEmits(['selection-change', 'page-change', 'size-change'])

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
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
  background-color: #fff;
}
</style>