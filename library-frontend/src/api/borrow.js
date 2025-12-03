import request from './request'

export function createBorrow(payload){
  return request.post('/borrow', payload)
}

export function returnBooks(payload){
  return request.post('/return', payload)
}

export function fetchBorrowRecords(params){
  return request.get('/borrow/record', { params })
}

export function fetchBorrowedByReader(readerId){
  return request.get('/borrow', { params: { readerId, status: 'borrowed' } })
}

