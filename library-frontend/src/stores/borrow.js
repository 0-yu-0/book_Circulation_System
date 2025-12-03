import { defineStore } from 'pinia'
import { reactive, computed } from 'vue'

export const useBorrowStore = defineStore('borrow', () => {
  const state = reactive({ selectedReader: null, selectedBooks: [], step: 1 })

  function addBook(book){
    if (!state.selectedBooks.find(b => b.id === book.id)) state.selectedBooks.push(book)
  }
  function removeBook(bookId){
    state.selectedBooks = state.selectedBooks.filter(b => b.id !== bookId)
  }
  function clearSelection(){
    state.selectedBooks = []
    state.selectedReader = null
    state.step = 1
  }
  function setReader(reader){ state.selectedReader = reader }

  const selectedCount = computed(() => state.selectedBooks.length)

  return { state, addBook, removeBook, clearSelection, setReader, selectedCount }
})

