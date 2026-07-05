import request from './request'

// 获取我的优惠券列表
export function getMyCoupons(params) {
  return request({ url: '/coupon/my', method: 'get', params })
}

// 获取可领取优惠券列表
export function getAvailableCoupons() {
  return request({ url: '/coupon/available', method: 'get' })
}

// 领取优惠券
export function receiveCoupon(id) {
  return request({ url: `/coupon/receive/${id}`, method: 'post' })
}

// 获取订单可用优惠券
export function getUsableCoupons(orderAmount) {
  return request({ url: '/coupon/usable', method: 'get', params: { orderAmount } })
}