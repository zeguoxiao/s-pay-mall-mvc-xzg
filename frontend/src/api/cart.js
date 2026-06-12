import request from '../utils/request'

// 购物车列表
export function getCartList() {
  return request.get('/api/v1/cart/list')
}

// 加购物车
export function addCart(productId, quantity = 1) {
  return request.post(`/api/v1/cart/add?productId=${productId}&quantity=${quantity}`)
}

// 改数量
export function updateCart(productId, quantity) {
  return request.post(`/api/v1/cart/update?productId=${productId}&quantity=${quantity}`)
}

// 删商品
export function removeCart(productId) {
  return request.post(`/api/v1/cart/remove?productId=${productId}`)
}
