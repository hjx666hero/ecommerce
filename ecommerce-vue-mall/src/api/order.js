import request from './request'

// 创建订单
export function createOrder(data) {
  return request({ url: '/order/create', method: 'post', data })
}

// 获取订单列表
export function getOrderList(params) {
  return request({ url: '/order/list', method: 'get', params })
}

// 获取订单详情
export function getOrderDetail(orderId) {
  return request({ url: `/order/detail/${orderId}`, method: 'get' })
}

// 获取订单详情（通过订单号）
export function getOrderDetailByOrderNo(orderNo) {
  return request({ url: `/order/detail/no/${orderNo}`, method: 'get' })
}

// 取消订单（通过订单号）
export function cancelOrderByOrderNo(orderNo) {
  return request({ url: `/order/cancel/${orderNo}`, method: 'put' })
}

// 确认收货（通过订单号）
export function confirmReceiveByOrderNo(orderNo) {
  return request({ url: `/order/confirm/${orderNo}`, method: 'put' })
}

// 删除订单
export function deleteOrder(orderId) {
  return request({ url: `/order/detail/${orderId}`, method: 'delete' })
}

// 获取订单确认页数据（购物车选中商品）
export function getOrderConfirm() {
  return request({ url: '/cart/selected', method: 'get' })
}