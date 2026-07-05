import request from './request'

// 获取商品列表
export function getProductList(params) {
  return request({ url: '/product/list', method: 'get', params })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({ url: `/product/detail/${id}`, method: 'get' })
}

// 获取商品SKU信息
export function getProductSku(id) {
  return request({ url: `/product/detail/${id}`, method: 'get' })
}

// 搜索商品
export function searchProducts(params) {
  return request({ url: '/product/search', method: 'get', params })
}

// 获取分类列表
export function getCategoryList() {
  return request({ url: '/product/category/tree', method: 'get' })
}

// 获取分类树
export function getCategoryTree() {
  return request({ url: '/product/category/tree', method: 'get' })
}

// 获取商品评价
export function getProductReviews(productId, params) {
  return request({ url: `/review/list/${productId}`, method: 'get', params })
}

// 添加商品评价
export function addProductReview(data) {
  return request({ url: '/review/add', method: 'post', data })
}

// 获取收藏列表
export function getFavoriteList(params) {
  return request({ url: '/product/favorite/list', method: 'get', params })
}

// 添加收藏
export function addFavorite(spuId) {
  return request({ url: `/product/favorite/${spuId}`, method: 'post' })
}

// 取消收藏
export function removeFavorite(spuId) {
  return request({ url: `/product/favorite/${spuId}`, method: 'delete' })
}

// 检查是否已收藏
export function checkFavorite(spuId) {
  return request({ url: `/product/favorite/check/${spuId}`, method: 'get' })
}