<template>
  <el-table :data="data" style="width:100%" @selection-change="$emit('selection-change',$event)" v-loading="loading">
    <el-table-column type="selection" width="55" />
    <slot name="columns" />
  </el-table>
  <div style="margin-top:12px;display:flex;justify-content:flex-end">
    <el-pagination :total="total" :page-size="pageSize" :current-page="page" @current-change="$emit('page-change',$event)" />
  </div>
</template>

<script setup>
import { toRefs } from 'vue'
const props = defineProps({ data: Array, total: Number, page: { type: Number, default: 1 }, pageSize: { type: Number, default: 20 }, loading: Boolean })
const { data, total, page, pageSize, loading } = toRefs(props)
defineEmits(['selection-change', 'page-change'])
</script>

