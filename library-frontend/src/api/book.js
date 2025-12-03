import request from './request'

export function fetchBooks(params){
  return request.get('/books', { params })
}

export function getBook(id){
  return request.get(`/books/${id}`)
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

