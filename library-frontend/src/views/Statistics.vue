<template>
  <layout>
    <template #default>
      <el-tabs v-model="tab">
        <el-tab-pane label="热门图书" name="popular">
          <div style="margin-top:12px">
            <el-card>
              <div ref="barChartContainer" class="chart-container"></div>
            </el-card>
            <data-table 
              :data="popularList" 
              :loading="loading" 
              :showSelection="false"
              :page="page"
              :page-size="pageSize"
              :total="popularTotal"
              @page-change="handlePopularPageChange"
              @size-change="handlePopularSizeChange"
            >
              <template #columns>
                <el-table-column prop="title" label="书名" />
                <el-table-column prop="borrowCount" label="借阅次数" />
              </template>
            </data-table>
          </div>
        </el-tab-pane>

      </el-tabs>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as api from '../api/statistics'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { initECharts } from '../utils/echartsHelper'

// Helper: normalize title extraction
function getBookTitle(item) {
  if (!item) return 'Unknown'
  return (
    item.title ||
    item.bookTitle ||
    item.book_title ||
    item.bookName ||
    item.name ||
    (item.book && (item.book.title || item.book.name)) ||
    'Unknown'
  )
}

const tab = ref('popular')
const popularList = ref([])
const overdue = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const popularTotal = ref(0)
const overdueTotal = ref(0)

// Chart containers
const barChartContainer = ref(null)
const pieChartContainer = ref(null)

// Chart instances
let barChart = null
let pieChart = null
let barChartDispose = null
let pieChartDispose = null
let pieChartInitialized = false

// Load data
async function loadData() {
  try {
    loading.value = true
    
    // 修复：热门图书不需要分页，所以保持原有逻辑
    const p = await api.getPopularBooks()
    if (p && p.code === 0) {
      popularList.value = Array.isArray(p.data) ? p.data : (p.data.items || [])
      popularTotal.value = popularList.value.length
      console.debug('[Statistics] popularList loaded, count=', popularList.value.length)
      
      // Render bar chart after data loaded
      renderBarChart(popularList.value)
    } else {
      ElMessage.error(p?.message || '获取热门图书失败')
    }

    // 修复：传递正确的分页参数给getOverdueBooks
    const o = await api.getOverdueBooks({ page: page.value, size: pageSize.value })
    if (o && o.code === 0) {
      // 修复：正确处理API响应数据结构
      const data = o.data || {}
      // normalize: if backend returns {items, total}
      if (Array.isArray(o.data)) {
        overdue.value = o.data
        overdueTotal.value = o.data.length
      } else if (o.data && Array.isArray(o.data.items)) {
        overdue.value = o.data.items
        overdueTotal.value = o.data.total || o.data.items.length
      } else {
        overdue.value = Array.isArray(o.data) ? o.data : (o.data.items || [])
        overdueTotal.value = overdue.value.length
      }

      console.debug('[Statistics] overdue loaded, count=', overdue.value.length, 'total=', overdueTotal.value)
      // Only render pie chart if the overdue tab is currently active; otherwise defer to tab switch

      if (tab.value === 'overdue') {
        renderPieChart(overdue.value)
      }
    }
  } catch (err) {
    console.error('[Statistics] loadData failed:', err)
    ElMessage.error('加载统计数据失败: ' + (err.message || err))
  } finally {
    loading.value = false
  }
}

function handlePopularPageChange(p) {
  page.value = p
  loadData()
}

function handlePopularSizeChange(size) {
  pageSize.value = size
  page.value = 1
  loadData()
}

function handleOverduePageChange(newPage) {
  page.value = newPage
  loadData()
}

function handleOverdueSizeChange(size) {
  pageSize.value = size
  page.value = 1
  loadData()
}

// Render bar chart after data loaded only if overdue tab is active
function renderBarChart(data, attempts = 0) {
  // dispose previous
  barChartDispose && barChartDispose()
  barChart = null
  barChartDispose = null

  if (!data || data.length === 0) {
    // initialize an empty chart with a 'no data' placeholder so container is visible
    initECharts(barChartContainer.value, ({ chart }) => {
      const option = {
        title: { text: '热门图书借阅排行', left: 'center' },
        graphic: [{
          type: 'text',
          left: 'center',
          top: 'middle',
          style: { text: '暂无数据', fontSize: 14, fill: '#999' }
        }],
        xAxis: { show: false },
        yAxis: { show: false },
        series: []
      }
      return option
    }, { waitOptions: { interval: 100, timeout: 2000 } })
      .then(({ chart, dispose }) => { barChart = chart; barChartDispose = dispose })
      .catch(err => console.warn('Failed to init empty bar chart:', err.message))
    return
  }

  const charLimit = 10

  initECharts(barChartContainer.value, ({ chart, charLimit: passedCharLimit }) => {
    const titles = data.map(item => getBookTitle(item))
    const useLimit = passedCharLimit || charLimit
    const option = {
      title: { text: '热门图书借阅排行', left: 'center' },
      tooltip: { 
        trigger: 'axis', 
        axisPointer: { type: 'shadow' }, 
        formatter: function(params) { 
          if (Array.isArray(params) && params[0]) { 
            const p = params[0]
            return `${p.name}<br/>${p.seriesName}: ${p.value}`
          } 
          return params.name 
        } 
      },
      grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
      xAxis: [{
        type: 'category', 
        data: titles, 
        axisTick: { alignWithLabel: true }, 
        axisLabel: { 
          interval: 0, 
          rotate: 30, 
          formatter: v => v && v.length > useLimit ? v.slice(0,useLimit) + '...' : v
        }
      }],
      yAxis: [{ type: 'value' }],
      series: [{
        name: '借阅次数',
        type: 'bar',
        barWidth: '60%',
        data: data.map(i => i.borrowCount || i.count || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }]
    }
    return option
  }, { waitOptions: { interval: 100, timeout: 3000 } })
    .then(({ chart, dispose }) => { barChart = chart; barChartDispose = dispose })
    .catch(err => console.warn('Failed to init bar chart:', err.message))

  // Update popularList for table display
  popularList.value = data.map(item => ({
    title: getBookTitle(item),
    borrowCount: item.borrowCount || item.count || 0
  }))
}

// Render pie chart for overdue books by category
function renderPieChart(data) {
  // dispose previous
  pieChartDispose && pieChartDispose()
  pieChart = null
  pieChartDispose = null

  if (!data || data.length === 0) {
    // initialize an empty chart with a 'no data' placeholder so container is visible
    initECharts(pieChartContainer.value, ({ chart }) => {
      const option = {
        title: { text: '逾期图书分类统计', left: 'center' },
        graphic: [{
          type: 'text',
          left: 'center',
          top: 'middle',
          style: { text: '暂无数据', fontSize: 14, fill: '#999' }
        }],
        series: []
      }
      return option
    }, { waitOptions: { interval: 100, timeout: 2000 } })
      .then(({ chart, dispose }) => { pieChart = chart; pieChartDispose = dispose; pieChartInitialized = true })
      .catch(err => console.warn('Failed to init empty pie chart:', err.message))
    return
  }

  initECharts(pieChartContainer.value, ({ chart }) => {
    const categoryKeys = [...new Set(data.map(r => r.category).filter(Boolean))]
    const categories = {}
    categoryKeys.forEach(k => categories[k] = 0)
    data.forEach(r => {
      if (r.category) {
        categories[r.category] = (categories[r.category] || 0) + 1
      }
    })

    const option = {
      title: { text: '逾期图书分类统计', left: 'center' },
      tooltip: { trigger: 'item' },
      legend: { orient: 'horizontal', bottom: 'bottom' },
      series: [
        {
          name: '逾期数量',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { 
            borderRadius: 10, 
            borderColor: '#fff', 
            borderWidth: 2 
          },
          label: { 
            show: false, 
            position: 'center' 
          },
          emphasis: { 
            label: { 
              show: true, 
              fontSize: '16', 
              fontWeight: 'bold' 
            } 
          },
          labelLine: { show: false },
          data: categoryKeys.map(key => ({ 
            value: Math.max(0, categories[key]), // 确保值不为负数
            name: key 
          }))
        }
      ]
    }
    return option
  }, { waitOptions: { interval: 100, timeout: 3000 } })
    .then(({ chart, dispose }) => { 
      pieChart = chart
      pieChartDispose = dispose
      pieChartInitialized = true
    })
    .catch(err => console.warn('Failed to init pie chart:', err.message))
}

// Handle tab switching - render charts lazily
function handleTabSwitch(name) {
  nextTick(() => {
    if (name === 'popular' && barChartContainer.value) {
      renderBarChart(popularList.value)
    } else if (name === 'overdue' && pieChartContainer.value && !pieChartInitialized) {
      renderPieChart(overdue.value)
    }
  })
}

// Watch tab changes
watch(tab, handleTabSwitch)

onMounted(() => {
  loadData()
})

onBeforeUnmount(() => {
  // Clean up chart instances
  barChartDispose && barChartDispose()
  pieChartDispose && pieChartDispose()
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px;
}
</style>