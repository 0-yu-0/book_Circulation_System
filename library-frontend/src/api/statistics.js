import request from './request'

export function getOverview(){
  return request.get('/statistics/overview')
}

export function getPopularBooks(top=10){
  return request.get('/statistics/popular-books', { params: { top } })
}

export function getOverdueBooks(params){
  return request.get('/statistics/overdue-books', { params })
}

export function getVacantBooks(params){
  return request.get('/statistics/vacant-books', { params })
}

export function getBorrowDetails(readerId){
  return request.get(`/statistics/borrow-details/${readerId}`)
}