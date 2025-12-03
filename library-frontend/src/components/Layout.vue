<template>
  <el-container class="layout-container">
    <el-aside 
      :width="collapsed ? '64px' : '200px'" 
      class="sidebar"
      :class="{ 'sidebar-collapsed': collapsed, 'sidebar-expanded': !collapsed }"
    >
      <div class="logo">Library</div>
      <el-menu 
        :default-active="$route.path" 
        router
        :collapse="collapsed"
        class="menu"
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
            :icon="collapsed ? 'Expand' : 'Fold'"
          />
          <h2 class="page-title" v-show="!collapsed">Library Circulation System</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <el-button type="text">
              <el-icon><UserFilled /></el-icon>
              <span v-show="!collapsed">管理员</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item command="logout">退出</el-dropdown-item>
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
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
  Warning
} from '@element-plus/icons-vue'

const ui = useUIStore()
const auth = useAuthStore()
const router = useRouter()
const collapsed = ref(ui.sidebarCollapsed)

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
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-expanded {
  width: 200px;
}

.logo {
  padding: 16px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  border-bottom: 1px solid #e6e6e6;
}

.menu {
  border-right: none !important;
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

.page-title {
  margin: 0;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
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