<template>
  <layout>
    <template #default>
      <el-tabs v-model="tab">
        <el-tab-pane label="热门图书" name="popular">
          <div style="margin-top:12px">
            <el-card>
              <div ref="barChartContainer" class="chart-container"></div>
            </el-card>
            <data-table :data="popularList" :loading="loading">
              <template #columns>
                <el-table-column prop="title" label="书名" />
                <el-table-column prop="borrowCount" label="借阅次数" />
              </template>
            </data-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="逾期未还" name="overdue">
          <el-card style="margin-bottom: 16px;">
            <div ref="pieChartContainer" class="chart-container"></div>
          </el-card>
          <data-table :data="overdue" :loading="loading">
            <template #columns>
              <el-table-column prop="borrowId" label="借阅编号" />
              <el-table-column prop="bookTitle" label="书名" />
              <el-table-column prop="readerName" label="读者姓名" />
              <el-table-column prop="dueDate" label="应还日期" />
              <el-table-column prop="overdueDays" label="逾期天数" />
              <el-table-column prop="category" label="分类" />
            </template>
          </data-table>
        </el-tab-pane>
      </el-tabs>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import DataTable from '../components/DataTable.vue'
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as api from '../api/statistics'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const tab = ref('popular')
const popularList = ref([])
const overdue = ref([])
const loading = ref(false)

// Chart containers
const barChartContainer = ref(null)
const pieChartContainer = ref(null)

// Chart instances
let barChart = null
let pieChart = null

onMounted(async () => {
  loadData()
  
  // Handle window resize
  const handleResize = () => {
    if (barChart) barChart.resize()
    if (pieChart) pieChart.resize()
  }
  
  window.addEventListener('resize', handleResize)
  
  // Store cleanup function
  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    if (barChart) barChart.dispose()
    if (pieChart) pieChart.dispose()
  })
})

onBeforeUnmount(() => {
  if (barChart) barChart.dispose()
  if (pieChart) pieChart.dispose()
})

async function loadData() {
  loading.value = true
  try {
    const p = await api.getPopularBooks(10)
    if (p && p.code === 0) {
      popularList.value = p.data
      // Render bar chart after data loaded
      await nextTick()
      renderBarChart(popularList.value)
    } else {
      ElMessage.error(p?.message || '获取热门图书失败')
    }

    // 修复：移除不必要的参数
    const o = await api.getOverdueBooks()
    if (o && o.code === 0) {
      // 修复：确保传递给DataTable的是数组而不是对象
      overdue.value = Array.isArray(o.data) ? o.data : (o.data.items || [])
      // Render pie chart after data loaded
      await nextTick()
      renderPieChart(overdue.value)
    } else {
      ElMessage.error(o?.message || '获取逾期记录失败')
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

function renderBarChart(data) {
  // Destroy existing chart if it exists
  if (barChart) barChart.dispose()

  // Bar chart for popular books
  if (barChartContainer.value) {
    // 确保容器元素已正确渲染且有尺寸
    setTimeout(() => {
      if (barChartContainer.value && barChartContainer.value.clientWidth > 0) {
        barChart = echarts.init(barChartContainer.value)
        const option = {
          title: {
            text: '热门图书借阅排行',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: [
            {
              type: 'category',
              data: data.map(item => item.title || item.bookTitle || 'Unknown'),
              axisTick: {
                alignWithLabel: true
              }
            }
          ],
          yAxis: [
            {
              type: 'value'
            }
          ],
          series: [
            {
              name: '借阅次数',
              type: 'bar',
              barWidth: '60%',
              data: data.map(item => Math.max(0, item.borrowCount || 0)),
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
            }
          ]
        }
        barChart.setOption(option)
      } else {
        console.warn('Bar chart container not ready')
      }
    }, 150) // 增加延迟确保DOM渲染完成
  }
}

function renderPieChart(data) {
  // Destroy existing chart if it exists
  if (pieChart) pieChart.dispose()

  // Check if we have data to display
  if (!data || data.length === 0) {
    console.warn('No data for pie chart')
    return
  }

  // Prepare data for pie chart
  const categories = {}
  data.forEach(item => {
    const category = item.category || '未知'
    categories[category] = (categories[category] || 0) + 1
  })

  // Pie chart for overdue books by category
  if (pieChartContainer.value) {
    // Ensure container is rendered and has dimensions
    setTimeout(() => {
      if (pieChartContainer.value && pieChartContainer.value.clientWidth > 0) {
        pieChart = echarts.init(pieChartContainer.value)
        const option = {
          title: {
            text: '逾期图书分类统计',
            left: 'center'
          },
          tooltip: {
            trigger: 'item'
          },
          legend: {
            orient: 'horizontal',
            bottom: 'bottom'
          },
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
              labelLine: {
                show: false
              },
              data: Object.keys(categories).map(key => ({
                value: categories[key],
                name: key
              }))
            }
          ]
        }
        pieChart.setOption(option)
      } else {
        console.warn('Pie chart container not ready')
      }
    }, 100) // Increase delay to ensure DOM rendering
  }
}
</script>
<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
}
</style>

