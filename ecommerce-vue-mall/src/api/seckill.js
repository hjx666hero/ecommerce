import request from './request'

// 获取秒杀活动列表
export function getSeckillList(params) {
  return request({ url: '/seckill/list', method: 'get', params })
}

// 获取秒杀商品详情
export function getSeckillDetail(id) {
  return request({ url: `/seckill/detail/${id}`, method: 'get' })
}

// 秒杀下单
export function seckillOrder(data) {
  return request({ url: '/seckill/order', method: 'post', data })
}