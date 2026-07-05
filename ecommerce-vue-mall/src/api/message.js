import request from './request'

// 获取消息列表
export function getMessageList(params) {
  return request({ url: '/messages', method: 'get', params })
}

// 获取未读消息数
export function getUnreadCount() {
  return request({ url: '/messages/unread', method: 'get' })
}

// 标记消息已读
export function markAsRead(id) {
  return request({ url: `/message/${id}/read`, method: 'put' })
}

// 标记全部已读
export function markAllAsRead() {
  return request({ url: '/messages/read-all', method: 'put' })
}

// 删除消息
export function deleteMessage(id) {
  return request({ url: `/message/${id}`, method: 'delete' })
}