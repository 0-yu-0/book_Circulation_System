import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const sidebarCollapsed = ref(false)
  const theme = ref('light')
  function toggleSidebar(){ sidebarCollapsed.value = !sidebarCollapsed.value }
  function setSidebarCollapsed(status){ sidebarCollapsed.value = status }
  return { sidebarCollapsed, theme, toggleSidebar, setSidebarCollapsed }
})

