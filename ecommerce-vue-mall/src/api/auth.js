import request from './request'

// 密码登录
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data: { account: data.mobile, password: data.password } })
}

// 验证码登录
export function loginBySms(data) {
  return request({ url: '/auth/login/sms', method: 'post', data })
}

// 发送验证码
export function sendSms(data) {
  return request({ url: '/auth/send-code', method: 'post', data: { phone: data.mobile } })
}

// 注册
export function register(data) {
  return request({ url: '/auth/register', method: 'post', data: { phone: data.mobile, password: data.password, verifyCode: data.code } })
}

// 忘记密码
export function forgetPassword(data) {
  return request({ url: '/auth/forget-password', method: 'post', data })
}

// 重置密码
export function resetPassword(data) {
  return request({ url: '/auth/reset-password', method: 'post', data })
}

// 退出登录
export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}