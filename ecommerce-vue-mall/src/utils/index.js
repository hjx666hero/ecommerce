/**
 * 格式化价格（分转元）
 */
export function formatPrice(price, decimals = 2) {
  if (price == null) return '0.00'
  const num = Number(price)
  // 如果价格已经是元为单位（小于10000），直接显示
  if (num < 10000) return num.toFixed(decimals)
  return (num / 100).toFixed(decimals)
}

/**
 * 格式化日期
 */
export function formatDate(dateStr, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化倒计时
 */
export function formatCountdown(seconds) {
  if (seconds <= 0) return { hours: '00', minutes: '00', seconds: '00' }
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return {
    hours: String(h).padStart(2, '0'),
    minutes: String(m).padStart(2, '0'),
    seconds: String(s).padStart(2, '0')
  }
}

/**
 * 手机号脱敏
 */
export function maskMobile(mobile) {
  if (!mobile) return ''
  return mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 订单状态映射
 */
export const orderStatusMap = {
  0: { label: '待付款', type: 'warning' },
  1: { label: '已支付', type: 'primary' },
  2: { label: '已发货', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' },
  5: { label: '已退款', type: 'info' }
}

/**
 * 获取订单状态信息
 */
export function getOrderStatus(status) {
  return orderStatusMap[status] || { label: '未知', type: 'info' }
}

/**
 * 优惠券状态映射
 */
export const couponStatusMap = {
  'UNUSED': { label: '未使用', type: 'primary' },
  'USED': { label: '已使用', type: 'info' },
  'EXPIRED': { label: '已过期', type: 'info' }
}

/**
 * 获取优惠券状态信息
 */
export function getCouponStatus(status) {
  return couponStatusMap[status] || { label: '未知', type: 'info' }
}

/**
 * 防抖
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 节流
 */
export function throttle(fn, delay = 300) {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= delay) {
      last = now
      fn.apply(this, args)
    }
  }
}