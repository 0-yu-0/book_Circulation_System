import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/books', name: 'BookList', component: () => import('../views/BookList.vue'), meta: { requiresAuth: true } },
  { path: '/books/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue'), meta: { requiresAuth: true } },
  { path: '/readers', name: 'ReaderList', component: () => import('../views/ReaderList.vue'), meta: { requiresAuth: true } },
  { path: '/readers/:id', name: 'ReaderDetail', component: () => import('../views/ReaderDetail.vue'), meta: { requiresAuth: true } },
  { path: '/borrow', name: 'Borrow', component: () => import('../views/BorrowPage.vue'), meta: { requiresAuth: true } },
  { path: '/return', name: 'Return', component: () => import('../views/ReturnPage.vue'), meta: { requiresAuth: true } },
  { path: '/statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { requiresAuth: true } },
  { path: '/borrow-history', name: 'BorrowHistory', component: () => import('../views/BorrowHistory.vue'), meta: { requiresAuth: true } },
  { path: '/overdue-records', name: 'OverdueRecords', component: () => import('../views/OverdueRecords.vue'), meta: { requiresAuth: true } },
  { path: '/settings', name: 'Settings', component: () => import('../views/Settings.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// import { useAuthStore } from '../stores/auth'
// router.beforeEach((to, from, next) => {
//   const auth = useAuthStore()
//   if (to.meta.requiresAuth && !auth.isLoggedIn) {
//     next({ path: '/login' })
//   } else next()
// })

export default router