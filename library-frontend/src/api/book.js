import request from './request'

function normalizeBook(b){
  if (!b) return null
  return {
    id: b.bookId ?? b.id,
    title: b.bookName ?? b.title,
    author: b.bookAuthor ?? b.author,
    publisher: b.bookPublisher ?? b.publisher,
    publishDate: b.bookPubDate ?? b.publishDate,
    category: b.bookCategory ?? b.category,
    availableCopies: b.bookAvailableCopies ?? b.availableCopies ?? 0,
    totalCopies: b.bookTotalCopies ?? b.totalCopies ?? 0,
    isbn: b.isbn ?? b.ISBN,
    location: b.bookLocation ?? b.location,
    borrowCount: b.borrowCount ?? 0
  }
}

export async function fetchBooks(params){
  const res = await request.get('/books', { params })
  if (res && res.code === 0) {
    const data = res.data || {}
    const items = (data.items || []).map(normalizeBook)
    return { code: 0, data: { items, total: data.total ?? items.length } }
  }
  return { code:0, data: { items: [], total: 0 } }
}

export async function getBook(id){
  const res = await request.get(`/books/${id}`)
  console.log('[getBook] Raw response:', res); // Debugging log

  if (res && res.code===0) return { code:0, data: normalizeBook(res.data) }
  // backend might return raw object
  return { code:0, data: normalizeBook(res) }
}

export function createBook(data){
  return request.post('/books', data)
}

export function updateBook(id, data){
  return request.put(`/books/${id}`, data)
}

export function deleteBook(id){
  return request.delete(`/books/${id}`)
}

export function patchStock(id, data){
  return request.patch(`/books/${id}/stock`, data)
}

export function fetchCategories(){
  return request.get('/books/categories')
}