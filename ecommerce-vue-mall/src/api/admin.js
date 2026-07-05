import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 管理员接口专用 axios 实例，baseURL设置为空，直接用 /admin 路径
// 由 Vite 代理 /admin → http://localhost:8082
const adminRequest = axios.create({
  baseURL: '',
  timeout: 10000
})

// JWT拦截器（与request.js保持一致）
adminRequest.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
adminRequest.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    ElMessage.error(error.response?.data?.message || '服务器内部错误')
    return Promise.reject(error)
  }
)

// ==================== 优惠券管理 ====================

// 获取优惠券列表
export function getAdminCouponList(params) {
  return adminRequest({ url: '/admin/coupon/list', method: 'get', params })
}

// 新增/更新优惠券
export function saveAdminCoupon(data) {
  return adminRequest({ url: '/admin/coupon', method: 'post', data })
}

// 删除优惠券
export function deleteAdminCoupon(id) {
  return adminRequest({ url: `/admin/coupon/${id}`, method: 'delete' })
}

// 批量发放优惠券
export function batchDistributeCoupon(couponId, userIds) {
  return adminRequest({ url: `/admin/coupon/${couponId}/distribute`, method: 'post', data: { userIds } })
}

// ==================== 秒杀管理 ====================

// 获取秒杀活动列表
export function getAdminSeckillList(params) {
  return adminRequest({ url: '/admin/seckill/list', method: 'get', params })
}

// 新增/更新秒杀活动
export function saveAdminSeckill(data) {
  return adminRequest({ url: '/admin/seckill', method: 'post', data })
}

// 删除秒杀活动
export function deleteAdminSeckill(id) {
  return adminRequest({ url: `/admin/seckill/${id}`, method: 'delete' })
}
