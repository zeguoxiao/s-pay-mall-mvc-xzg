import request from '../utils/request'

// 购物车结算
export function cartCheckout(cartItems) {
  return request.post('/api/order/cart/checkout', cartItems)
}

// 查订单列表
export function getOrderList() {
  return request.get('/api/order/list')
}
