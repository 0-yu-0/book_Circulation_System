import request from './request'

function normalizeReader(r){
  if (!r) return null
  return {
    id: r.readerId ?? r.id,
    name: r.readerName ?? r.name,
    idType: r.readerCardType ?? r.idType,
    idNumber: r.readerCardNumber ?? r.idNumber,
    phone: r.readerPhoneNumber ?? r.phone,
    registerDate: r.registerDate ?? r.registerDate,
    status: r.readerStatus ?? r.status ?? 0,
    borrowLimit: r.totalBorrowNumber ?? r.borrowLimit ?? 5, // 优先使用totalBorrowNumber字段
    currentBorrowCount: r.currentBorrowCount ?? r.nowBorrowNumber ?? 0
  }
}

export async function fetchReaders(params){
  const res = await request.get('/readers', { params })
  if (res && res.code === 0) {
    const data = res.data || {}
    const items = (data.items || []).map(normalizeReader)
    return { code:0, data: { items, total: data.total ?? items.length } }
  }
  if (Array.isArray(res)) return { code:0, data: { items: res.map(normalizeReader), total: res.length } }
  return { code:0, data: { items: [], total: 0 } }
}

export async function getReader(id){
  const res = await request.get(`/readers/${id}`)
  if (res && res.code===0) return { code:0, data: normalizeReader(res.data) }
  return { code:0, data: normalizeReader(res) }
}

export function createReader(data){
  return request.post('/readers', data)
}

export function updateReader(id, data){
  return request.put(`/readers/${id}`, data)
}

export function deleteReader(id){
  return request.delete(`/readers/${id}`)
}

export async function getReaderByCard(cardNumber){
  const res = await request.get(`/readers/byCard/${cardNumber}`)
  if (res && res.code===0) return { code:0, data: normalizeReader(res.data) }
  return { code:0, data: normalizeReader(res) }
}