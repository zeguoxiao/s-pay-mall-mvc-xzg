import request from '../utils/request'

// 商品列表
export function getProductList() {
  return request.get('/api/v1/product/list')
}

// 商品详情
export function getProduct(id) {
  return request.get(`/api/v1/product/detail/${id}`)
}
