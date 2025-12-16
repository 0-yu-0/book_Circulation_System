import request from './request'

export async function getOverview(){
  const res = await request.get('/statistics/overview')
  if (!res) return res
  if (res.code === 0) {
    const d = res.data || {}
    // normalize field names (backend may return borrowedNow/overdue)
    const normalized = {
      totalBooks: d.totalBooks ?? d.total_books ?? 0,
      totalReaders: d.totalReaders ?? d.total_readers ?? 0,
      borrowedCount: d.borrowedCount ?? d.borrowedNow ?? d.borrowed_count ?? 0,
      overdueCount: d.overdueCount ?? d.overdue ?? 0,
      todayBorrows: d.todayBorrows ?? d.todayBorrows ?? 0,
      todayReturns: d.todayReturns ?? d.todayReturns ?? 0
    }
    return { code: 0, data: normalized }
  }
  // fallback: server returned raw object
  return { code: 0, data: res }
}

export async function getPopularBooks(top=10){
  const res = await request.get('/statistics/popular-books', { params: { top } })
  if (!res) return res
  if (res.code === 0) {
    // Backend may return { code:0, data: { items: [...] } } or { code:0, data: [...] }
    const items = Array.isArray(res.data) ? res.data : (res.data && res.data.items) ? res.data.items : []
    return { code: 0, data: items }
  }
  return { code: res?.code ?? 1, message: res?.message || '请求失败' }
}

export async function getOverdueBooks(params){
  // Convert page/size to offset/limit for backend compatibility
  const backendParams = { ...params }
  if (params.page && params.size) {
    backendParams.offset = (params.page - 1) * params.size
    backendParams.limit = params.size
    // Remove page/size to avoid confusion
    delete backendParams.page
    delete backendParams.size
  }
  
  const res = await request.get('/statistics/overdue-books', { params: backendParams })
  if (!res) return res
  if (res.code === 0) {
    // Backend returns { code:0, data: { items: [], total: number } }
    const data = res.data || {}
    const items = Array.isArray(data) ? data : (data.items || [])
    const total = data.total ?? items.length
    return { code: 0, data: { items, total } }
  }
  return { code: res?.code ?? 1, message: res?.message || '请求失败' }
}

export async function getVacantBooks(params){
  const res = await request.get('/statistics/vacant-books', { params })
  if (!res) return res
  if (res.code === 0) {
    // Backend returns { code:0, data: { items: [], total: number } }
    const data = res.data || {}
    const items = Array.isArray(data) ? data : (data.items || [])
    const total = data.total ?? items.length
    return { code: 0, data: { items, total } }
  }
  return { code: res?.code ?? 1, message: res?.message || '请求失败' }
}

export function getBorrowDetails(readerId){
  return request.get(`/statistics/borrow-details/${readerId}`)
}