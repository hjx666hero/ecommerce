import request from './request'

/**
 * 发送客服消息
 * @param {string} message 用户消息
 * @param {string} sessionId 会话ID（可选，不传则后端自动生成）
 */
export function sendMessage(message, sessionId) {
  return request({
    url: '/v1/customer-service/chat',
    method: 'post',
    data: { message },
    headers: sessionId ? { 'X-Session-Id': sessionId } : {}
  })
}
