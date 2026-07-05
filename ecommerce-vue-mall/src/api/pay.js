import request from './request'

// 发起支付
export function pay(data) {
  return request({ url: '/pay/pay', method: 'post', data })
}

// 查询支付结果
export function queryPayResult(orderNo) {
  return request({ url: `/pay/query/${orderNo}`, method: 'get' })
}

// 退款
export function refund(data) {
  return request({ url: '/pay/refund', method: 'post', data })
}