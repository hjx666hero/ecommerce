import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '云创优品' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forget-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { title: '忘记密码' }
  },
  {
    path: '/category/:id?',
    name: 'Category',
    component: () => import('@/views/Category.vue'),
    meta: { title: '商品分类' }
  },
  {
    path: '/categories',
    redirect: '/category'
  },
  {
    path: '/products',
    name: 'ProductList',
    component: () => import('@/views/ProductList.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/OrderConfirm.vue'),
    meta: { title: '确认订单', requiresAuth: true }
  },
  {
    path: '/pay/:orderNo',
    name: 'Pay',
    component: () => import('@/views/Pay.vue'),
    meta: { title: '支付', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/OrderList.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/:orderNo',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/UserCenter.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/user/info',
    name: 'UserInfo',
    component: () => import('@/views/UserInfo.vue'),
    meta: { title: '个人信息', requiresAuth: true }
  },
  {
    path: '/user/address',
    name: 'AddressList',
    component: () => import('@/views/AddressList.vue'),
    meta: { title: '地址管理', requiresAuth: true }
  },
  {
    path: '/user/security',
    name: 'Security',
    component: () => import('@/views/Security.vue'),
    meta: { title: '账号安全', requiresAuth: true }
  },
  {
    path: '/user/coupons',
    name: 'CouponList',
    component: () => import('@/views/CouponList.vue'),
    meta: { title: '我的优惠券', requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'FavoriteList',
    component: () => import('@/views/Favorite.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/coupons',
    name: 'CouponCenter',
    component: () => import('@/views/CouponCenter.vue'),
    meta: { title: '领券中心' }
  },
  {
    path: '/seckill',
    name: 'Seckill',
    component: () => import('@/views/Seckill.vue'),
    meta: { title: '秒杀' }
  },
  {
    path: '/seckill/:id',
    name: 'SeckillDetail',
    component: () => import('@/views/SeckillDetail.vue'),
    meta: { title: '秒杀详情', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'MessageList',
    component: () => import('@/views/MessageList.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { title: '搜索' }
  },
  // ========== 管理员后台 ==========
  {
    path: '/admin/coupons',
    name: 'AdminCoupon',
    component: () => import('@/views/admin/AdminCoupon.vue'),
    meta: { title: '优惠券管理 - 管理后台' }
  },
  {
    path: '/admin/seckill',
    name: 'AdminSeckill',
    component: () => import('@/views/admin/AdminSeckill.vue'),
    meta: { title: '秒杀管理 - 管理后台' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '云创优品'
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router