import request from './request'

// 获取首页数据（轮播图、分类、推荐商品等）
export function getHomeData() {
  return request({ url: '/home', method: 'get' })
}

// 获取轮播图
export function getBanners() {
  return request({ url: '/home', method: 'get' })
}

// 获取推荐商品
export function getRecommendProducts() {
  return request({ url: '/product/list', method: 'get', params: { sort: 'recommend', page: 1, size: 8 } })
}

// 获取热门商品
export function getHotProducts() {
  return request({ url: '/product/list', method: 'get', params: { sort: 'sales', page: 1, size: 8 } })
}