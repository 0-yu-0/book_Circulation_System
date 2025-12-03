<template>
  <el-container class="layout-container">
    <el-aside 
      :width="collapsed ? '64px' : '200px'" 
      class="sidebar"
      :class="{ 'sidebar-collapsed': collapsed, 'sidebar-expanded': !collapsed }"
    >
      <div class="logo-container">
        <div class="logo-icon">
          <el-icon size="24"><Collection /></el-icon>
        </div>
        <div class="logo-text" v-show="!collapsed">
          <div class="logo-title">图书借还</div>
          <div class="logo-subtitle">管理系统</div>
        </div>
      </div>
      <el-menu 
        :default-active="$route.path" 
        router
        :collapse="collapsed"
        class="menu"
        :unique-opened="true"
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span v-show="!collapsed">概览</span>
        </el-menu-item>
        <el-menu-item index="/books">
          <el-icon><Collection /></el-icon>
          <span v-show="!collapsed">图书管理</span>
        </el-menu-item>
        <el-menu-item index="/readers">
          <el-icon><User /></el-icon>
          <span v-show="!collapsed">读者管理</span>
        </el-menu-item>
        <el-menu-item index="/borrow">
          <el-icon><Reading /></el-icon>
          <span v-show="!collapsed">借书</span>
        </el-menu-item>
        <el-menu-item index="/return">
          <el-icon><DocumentChecked /></el-icon>
          <span v-show="!collapsed">还书</span>
        </el-menu-item>
        <el-menu-item index="/borrow-history">
          <el-icon><Tickets /></el-icon>
          <span v-show="!collapsed">借阅历史</span>
        </el-menu-item>
        <el-menu-item index="/overdue-records">
          <el-icon><Warning /></el-icon>
          <span v-show="!collapsed">逾期记录</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span v-show="!collapsed">统计</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span v-show="!collapsed">设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button 
            class="toggle-button" 
            @click="toggleSidebar" 
            circle 
          >
            <el-icon v-if="collapsed"><ArrowRight /></el-icon>
            <el-icon v-else><ArrowLeft /></el-icon>
          </el-button>
          <!-- 添加面包屑导航 -->
          <el-breadcrumb v-show="!collapsed" separator="/" class="breadcrumb">
<!--            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>-->
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 用户头像替换文本退出按钮 -->
          <el-dropdown @command="handleCommand">
            <span class="user-avatar">
              <el-avatar :icon="UserFilled" size="small" />
              <span v-show="!collapsed" class="user-name">管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useUIStore } from '../stores/ui'
import { useAuthStore } from '../stores/auth'
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  House,
  Collection,
  User,
  Reading,
  DocumentChecked,
  DataAnalysis,
  UserFilled,
  Setting,
  Tickets,
  Warning,
  SwitchButton,
  ArrowLeft,
  ArrowRight
} from '@element-plus/icons-vue'

const ui = useUIStore()
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const collapsed = ref(ui.sidebarCollapsed)

// 计算当前页面标题
const currentPageTitle = computed(() => {
  const routeMap = {
    '/': '概览',
    '/books': '图书管理',
    '/readers': '读者管理',
    '/borrow': '借书',
    '/return': '还书',
    '/borrow-history': '借阅历史',
    '/overdue-records': '逾期记录',
    '/statistics': '统计',
    '/settings': '设置'
  }
  return routeMap[route.path] || '未知页面'
})

function toggleSidebar() {
  collapsed.value = !collapsed.value
  ui.setSidebarCollapsed(collapsed.value)
}

function handleCommand(command) {
  if (command === 'logout') {
    auth.logout().then(() => {
      router.push('/login')
    })
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

// Handle window resize for responsive sidebar
onMounted(() => {
  const handleResize = () => {
    if (window.innerWidth < 768) {
      collapsed.value = true
      ui.setSidebarCollapsed(true)
    } else {
      collapsed.value = ui.sidebarCollapsed
    }
  }

  handleResize()
  window.addEventListener('resize', handleResize)
  
  // Store cleanup function
  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
  })
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  background-color: #ffffff;
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-expanded {
  width: 200px;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e6e6e6;
  background: linear-gradient(90deg, #f0f8ff 0%, #e6f7ff 100%);
  transition: all 0.3s ease;
}

.logo-container:hover {
  background: linear-gradient(90deg, #e6f7ff 0%, #f0f8ff 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #409EFF 0%, #1890FF 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.logo-text {
  white-space: nowrap;
  transition: all 0.3s ease;
}

.logo-title {
  font-weight: bold;
  font-size: 16px;
  color: #1890FF;
  line-height: 1.2;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.logo-subtitle {
  font-size: 12px;
  color: #666;
  line-height: 1.2;
}

.menu {
  border-right: none !important;
  height: calc(100% - 77px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #e6e6e6;
  background-color: #ffffff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.breadcrumb {
  margin-left: 8px;
}

.page-title {
  margin: 0;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.user-name {
  font-size: 14px;
}

.main-content {
  padding: 16px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

/* Responsive styles */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }
  
  .page-title {
    font-size: 16px;
  }
  
  .header {
    padding: 0 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 8px;
  }
  
  .toggle-button {
    padding: 8px;
  }
}
</style>