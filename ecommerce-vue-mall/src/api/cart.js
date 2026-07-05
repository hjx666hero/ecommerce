import request from './request'

// 获取购物车列表
export function getCartList() {
  return request({ url: '/cart/list', method: 'get' })
}

// 添加商品到购物车
export function addToCart(data) {
  return request({ url: '/cart/add', method: 'post', data })
}

// 更新购物车商品数量
export function updateCartItem(id, data) {
  return request({ url: `/cart/${id}/quantity`, method: 'put', data })
}

// 删除购物车商品
export function deleteCartItem(id) {
  return request({ url: `/cart/${id}`, method: 'delete' })
}

// 全选/取消全选
export function selectAllCart(data) {
  return request({ url: '/cart/select-all', method: 'put', data })
}

// 清空购物车
export function clearCart() {
  return request({ url: '/cart/clear', method: 'delete' })
}

// 获取购物车数量
export function getCartCount() {
  return request({ url: '/cart/list', method: 'get' })
}