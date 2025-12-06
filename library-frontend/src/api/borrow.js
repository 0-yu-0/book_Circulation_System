import request from './request'

export function createBorrow(payload){
  // payload can be { readerId, bookId } or { readerId, books: [{bookId,count}] }
  return request.post('/borrow', payload)
}

export function returnBooks(payload){
  // payload should be { borrowIds: [...], returnDate }
  return request.post('/return', payload)
}

export async function fetchBorrowRecords(params){
  const res = await request.get('/borrow', { params })
  // normalize: backend may return {code:0, data:{items,total}} or directly {items,total}
  if (res && res.code === 0) return res
  return { code: 0, data: res }
}

// Modified: accept optional pagination options: { page, size }
export async function fetchBorrowedByReader(readerId, options = {}){
  const params = { readerId, status: 'borrowed' }
  if (options.page) params.page = options.page
  if (options.size) params.size = options.size
  const res = await request.get('/borrow', { params })
  if (res && res.code === 0) return res
  return { code: 0, data: res }
}