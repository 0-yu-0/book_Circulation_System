import { defineStore } from 'pinia'
import { reactive, computed } from 'vue'

export const useBorrowStore = defineStore('borrow', () => {
  // selectedBooks: array of { id, title, count, ...bookProps }
  const state = reactive({ selectedReader: null, selectedBooks: [], step: 1 })

  function addBook(book, qty = 1){
    const existing = state.selectedBooks.find(b => b.id === (book.id ?? book.bookId))
    if (existing) {
      existing.count = Math.max(0, (existing.count || 0) + qty)
    } else {
      state.selectedBooks.push(Object.assign({}, { id: book.id ?? book.bookId, title: book.title ?? book.bookName, count: qty }, book))
    }
  }

  function removeBook(bookId){
    state.selectedBooks = state.selectedBooks.filter(b => b.id !== bookId)
  }

  function setCount(bookId, count){
    const it = state.selectedBooks.find(b => b.id === bookId)
    if (!it) return
    it.count = Math.max(0, Number(count) || 0)
    if (it.count === 0) removeBook(bookId)
  }

  function clearSelection(){
    state.selectedBooks = []
    state.selectedReader = null
    state.step = 1
  }

  function setReader(reader){ state.selectedReader = reader }

  const selectedCount = computed(() => state.selectedBooks.reduce((s, b) => s + (b.count || 0), 0))

  return { state, addBook, removeBook, setCount, clearSelection, setReader, selectedCount }
})
