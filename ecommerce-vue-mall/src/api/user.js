import request from './request'

// 获取用户信息
export function getUserInfo() {
  return request({ url: '/user/profile', method: 'get' })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({ url: '/user/profile', method: 'put', data })
}

// 修改密码
export function changePassword(data) {
  return request({ url: '/user/change-password', method: 'put', data })
}

// 获取地址列表
export function getAddressList() {
  return request({ url: '/address/list', method: 'get' })
}

// 获取地址详情
export function getAddressDetail(id) {
  return request({ url: `/address/${id}`, method: 'get' })
}

// 新增地址
export function addAddress(data) {
  return request({ url: '/address', method: 'post', data })
}

// 更新地址
export function updateAddress(data) {
  return request({ url: '/address', method: 'put', data })
}

// 删除地址
export function deleteAddress(id) {
  return request({ url: `/address/${id}`, method: 'delete' })
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request({ url: `/address/${id}/default`, method: 'put' })
}