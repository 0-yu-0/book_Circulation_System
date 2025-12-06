<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo" @click="toggleCollapse">
        <el-icon v-if="isCollapse" size="24" class="logo-icon"><Collection /></el-icon>
        <div v-else class="logo-text-wrapper">
          <div class="logo-main">图书借还</div>
          <div class="logo-sub">管理系统</div>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#f5f5f5"
        text-color="#333333"
        active-text-color="#409eff"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-sub-menu index="/books">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>图书管理</span>
          </template>
          <el-menu-item index="/books">
            <el-icon><CollectionTag /></el-icon>
            <template #title>图书列表</template>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>统计</template>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/readers">
          <template #title>
            <el-icon><User /></el-icon>
            <span>读者管理</span>
          </template>
          <el-menu-item index="/readers">
            <el-icon><User /></el-icon>
            <template #title>读者列表</template>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/transactions">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>借还管理</span>
          </template>
          <el-menu-item index="/borrow">
            <el-icon><CreditCard /></el-icon>
            <template #title>借书</template>
          </el-menu-item>
          <el-menu-item index="/return">
            <el-icon><SoldOut /></el-icon>
            <template #title>还书</template>
          </el-menu-item>
          <el-menu-item index="/borrow-history">
            <el-icon><Tickets /></el-icon>
            <template #title>借阅历史</template>
          </el-menu-item>
          <el-menu-item index="/overdue-records">
            <el-icon><Warning /></el-icon>
            <template #title>逾期记录</template>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon @click="toggleCollapse" class="menu-toggle"><Expand /></el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :icon="UserFilled" size="small" />
              <span class="username">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  House,
  Collection,
  User,
  Document,
  DataAnalysis,
  Setting,
  Expand,
  ArrowDown,
  UserFilled,
  CollectionTag,
  CreditCard,
  SoldOut,
  Tickets,
  Warning
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isCollapse = ref(false)

const activeMenu = computed(() => {
  const { meta, path } = route
  // if set path, the sidebar will highlight the path you set
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleUserCommand(command) {
  if (command === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #f5f5f5;
  transition: width 0.3s ease;
  overflow-x: hidden;
  border-right: 1px solid #e4e7ed;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  color: #409eff;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  border-bottom: 1px solid #e4e7ed;
}

.logo-icon {
  color: #409eff;
}

.logo-text-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.logo-main {
  font-size: 18px;
  font-weight: bold;
  line-height: 1.2;
}

.logo-sub {
  font-size: 12px;
  font-weight: normal;
  line-height: 1.2;
  opacity: 0.8;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.header {
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.menu-toggle {
  cursor: pointer;
  font-size: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.username {
  font-size: 14px;
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}
</style>