import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' }, // 根路径重定向到登录页
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/dashboard', name: 'Overview', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/books', name: 'BookList', component: () => import('../views/BookList.vue'), meta: { requiresAuth: true } },
  { path: '/books/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue'), meta: { requiresAuth: true } },
  { path: '/readers', name: 'ReaderList', component: () => import('../views/ReaderList.vue'), meta: { requiresAuth: true } },
  { path: '/readers/:id', name: 'ReaderDetail', component: () => import('../views/ReaderDetail.vue'), meta: { requiresAuth: true } },
  { path: '/borrow', name: 'Borrow', component: () => import('../views/BorrowPage.vue'), meta: { requiresAuth: true } },
  { path: '/return', name: 'Return', component: () => import('../views/ReturnPage.vue'), meta: { requiresAuth: true } },
  { path: '/borrow-history', name: 'BorrowHistory', component: () => import('../views/BorrowHistory.vue'), meta: { requiresAuth: true } },
  { path: '/overdue-records', name: 'OverdueRecords', component: () => import('../views/OverdueRecords.vue'), meta: { requiresAuth: true } },
  { path: '/statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { requiresAuth: true } },
  { path: '/settings', name: 'Settings', component: () => import('../views/Settings.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

import { useAuthStore } from '../stores/auth'
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  
  // 登录页面逻辑：总是允许访问登录页，不自动重定向到仪表板
  if (to.path === '/login') {
    next()
    return
  }
  
  // 其他需要认证的页面逻辑
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})

export default router