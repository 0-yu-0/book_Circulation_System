<template>
  <layout>
    <template #default>
      <el-skeleton :loading="loading" animated :rows="4">
        <template #default>
          <el-row :gutter="16" justify="space-between">
            <el-col :xs="24" :sm="12" :md="12" :lg="6" v-for="(k,i) in cards" :key="i">
              <el-card class="kpi-card" shadow="hover">
                <div class="kpi-icon" :class="k.iconClass">
                  <el-icon size="24"><component :is="k.icon" /></el-icon>
                </div>
                <div class="kpi-content">
                  <div class="kpi-title">{{k.title}}</div>
                  <div class="kpi-value">{{k.value}}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px;">
            <el-col :xs="24" :sm="24" :md="16">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><TrendCharts /></el-icon>
                    热门图书
                  </div>
                </template>
                <div ref="chartContainer" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="24" :md="8">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><PieChart /></el-icon>
                    借阅统计
                  </div>
                </template>
                <div ref="pieChartContainer" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>
    </template>
  </layout>
</template>

<script setup>
import Layout from '../components/Layout.vue'
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as api from '../api/statistics'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Collection,
  User,
  Reading,
  Document,
  TrendCharts,
  PieChart,
  CollectionTag,
  UserFilled
} from '@element-plus/icons-vue'

const cards = ref([
  { title:'总图书', value:0, icon: Collection, iconClass: 'icon-blue' },
  { title:'总读者', value:0, icon: User, iconClass: 'icon-green' },
  { title:'在借', value:0, icon: Reading, iconClass: 'icon-orange' },
  { title:'今日借书', value:0, icon: Document, iconClass: 'icon-red' }
])
const popular = ref([])
const loading = ref(false)
const chartContainer = ref(null)
const pieChartContainer = ref(null)
let barChart = null
let pieChart = null

async function load(){
  loading.value = true
  try{
    const res = await api.getOverview()
    if (res && res.code===0){
      const d = res.data
      cards.value[0].value = d.totalBooks
      cards.value[1].value = d.totalReaders
      cards.value[2].value = d.borrowedCount || d.totalBorrows || 0
      cards.value[3].value = d.todayBorrows || 0
    } else {
      ElMessage.error(res?.message || '获取统计数据失败')
    }
    
    const p = await api.getPopularBooks(8)
    if (p && p.code===0) {
      popular.value = p.data
      // Render charts after both data loaded
      await nextTick()
      renderCharts(popular.value)
    } else {
      ElMessage.error(p?.message || '获取热门图书失败')
    }
  }catch(e){ 
    console.error(e)
    ElMessage.error('加载数据失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function renderCharts(data) {
  // Destroy existing charts if they exist
  if (barChart) barChart.dispose()
  if (pieChart) pieChart.dispose()
  
  // Bar chart for popular books
  if (chartContainer.value) {
    // 确保容器已正确渲染
    setTimeout(() => {
      if (chartContainer.value && chartContainer.value.clientWidth > 0) {
        barChart = echarts.init(chartContainer.value)
        const option = {
          title: {
            text: '热门图书借阅排行',
            left: 'center'
          },
          tooltip: {},
          xAxis: {
            type: 'category',
            data: data.map(item => item.title || item.bookTitle || 'Unknown')
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            type: 'bar',
            data: data.map(item => item.borrowCount || 0),
            itemStyle: {
              color: '#409EFF'
            }
          }]
        }
        barChart.setOption(option)
      } else {
        console.warn('Chart container not ready')
      }
    }, 150) // 增加延时确保DOM渲染完成
  }
  
  // Pie chart for borrowing statistics
  if (pieChartContainer.value) {
    // 确保容器已正确渲染
    setTimeout(() => {
      if (pieChartContainer.value && pieChartContainer.value.clientWidth > 0) {
        pieChart = echarts.init(pieChartContainer.value)
        const inLibraryCount = cards.value[0].value - cards.value[2].value;
        const option = {
          title: {
            text: '借阅状态分布',
            left: 'center'
          },
          tooltip: {
            trigger: 'item'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [
            {
              type: 'pie',
              radius: '50%',
              data: [
                { value: Math.max(0, cards.value[2].value), name: '在借图书' },
                { value: Math.max(0, inLibraryCount), name: '在馆图书' }
              ],
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        }
        pieChart.setOption(option)
      } else {
        console.warn('Pie chart container not ready')
      }
    }, 150) // 增加延时确保DOM渲染完成
  }
}

// 添加窗口大小变化处理
onMounted(()=>{
  load()
  window.addEventListener('borrow-success', load)
  
  // Handle window resize
  const handleResize = () => {
    if (barChart) barChart.resize()
    if (pieChart) pieChart.resize()
  }
  
  window.addEventListener('resize', handleResize)
  
  // 清理函数
  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
  })
})

onBeforeUnmount(()=>{
  window.removeEventListener('borrow-success', load)
  if (barChart) barChart.dispose()
  if (pieChart) pieChart.dispose()
})
</script>
<style scoped>
.kpi-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: none;
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  float: left;
  margin-right: 16px;
}

.icon-blue {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.icon-green {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.icon-orange {
  background-color: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.icon-red {
  background-color: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

.kpi-content {
  overflow: hidden;
}

.kpi-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 250px;
  }
  
  .kpi-value {
    font-size: 20px;
  }
}
</style>