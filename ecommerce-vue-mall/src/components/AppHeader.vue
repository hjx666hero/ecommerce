<template>
  <header class="app-header">
    <!-- 顶部操作栏 -->
    <div class="header-top-bar">
      <div class="header-top-inner">
        <span class="top-bar-welcome">
          <svg class="welcome-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="5" fill="currentColor"/>
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          欢迎来到拼夕夕
        </span>
        <div class="top-bar-links">
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/favorites">我的收藏</router-link>
          <router-link to="/help">帮助中心</router-link>
          <router-link to="/about">关于我们</router-link>
        </div>
      </div>
    </div>

    <!-- 主头部 -->
    <div class="header-main">
      <div class="header-inner">
        <div class="header-left">
          <router-link to="/" class="logo-link">
            <span class="logo-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <defs>
                  <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:var(--primary)" />
                    <stop offset="100%" style="stop-color:var(--accent)" />
                  </linearGradient>
                </defs>
                <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4H6z" fill="url(#logoGrad)" />
                <path d="M3 6h18M16 10a4 4 0 01-8 0" stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none" />
              </svg>
            </span>
            <span class="logo-text">拼夕夕</span>
          </router-link>
        </div>
        <div class="header-center">
          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索想要的商品"
              size="large"
              @keyup.enter="handleSearch"
              clearable
              class="search-input-round"
            >
              <template #prepend>
                <el-icon :size="18"><Search /></el-icon>
              </template>
              <template #append>
                <el-button class="search-btn-gradient" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/cart" class="header-icon cart-icon-circle">
              <el-badge :value="cartStore.count" :hidden="cartStore.count === 0" :max="99">
                <el-icon :size="22"><ShoppingCart /></el-icon>
              </el-badge>
            </router-link>
            <router-link to="/messages" class="header-icon">
              <el-icon :size="22"><Bell /></el-icon>
            </router-link>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="34" :src="userStore.avatar" />
                <span class="user-name">{{ userStore.nickname || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="center">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="coupons">我的优惠券</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="header-link">登录</router-link>
            <router-link to="/register" class="header-link btn-primary-outline">注册</router-link>
          </template>
        </div>
      </div>
    </div>

    <!-- 底部渐变装饰线 -->
    <div class="header-bottom-line" />
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { Search, ShoppingCart, Bell, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const keyword = ref('')

function handleSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
  }
}

function handleCommand(command) {
  switch (command) {
    case 'center':
      router.push('/user')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'coupons':
      router.push('/user/coupons')
      break
    case 'logout':
      userStore.logout()
      cartStore.clear()
      router.push('/')
      break
  }
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
}

/* ---- 顶部操作栏 ---- */
.header-top-bar {
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
}

.header-top-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 36px;
  padding: 0 20px;
}

.top-bar-welcome {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.85);
}

.welcome-icon {
  color: var(--accent);
}

.top-bar-links {
  display: flex;
  align-items: center;
  gap: 0;
}

.top-bar-links a {
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  padding: 0 12px;
  transition: color var(--transition);
  font-size: 12px;
}

.top-bar-links a:hover {
  color: #fff;
}

.top-bar-links a + a {
  border-left: 1px solid rgba(255, 255, 255, 0.15);
}

/* ---- 主头部 ---- */
.header-main {
  background: var(--bg-white);
  box-shadow: var(--shadow-sm);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 20px;
}

.header-left {
  flex-shrink: 0;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 40px;
}

.search-box {
  width: 100%;
  max-width: 560px;
}

/* 搜索框圆角药丸样式 */
.search-input-round :deep(.el-input__wrapper) {
  border-radius: 50px !important;
  box-shadow: 0 0 0 1px var(--border-base) inset !important;
  padding-left: 8px;
  transition: box-shadow var(--transition);
}

.search-input-round :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light) inset !important;
}

.search-input-round :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary) inset !important;
}

.search-input-round :deep(.el-input-group__prepend) {
  border-radius: 50px 0 0 50px !important;
  background: transparent;
  padding: 0 0 0 16px;
  border: none;
  box-shadow: none;
  color: var(--text-placeholder);
}

.search-input-round :deep(.el-input-group__append) {
  border-radius: 0 50px 50px 0 !important;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 3px;
}

.search-btn-gradient {
  background: linear-gradient(135deg, var(--primary), var(--accent)) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600 !important;
  letter-spacing: 1px !important;
  border-radius: 50px !important;
  padding: 10px 24px !important;
  transition: all var(--transition) !important;
}

.search-btn-gradient:hover {
  background: linear-gradient(135deg, var(--primary-dark), var(--primary)) !important;
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4) !important;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition);
  display: flex;
  align-items: center;
}

.header-icon:hover {
  color: var(--primary);
}

.cart-icon-circle {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--primary-bg);
  transition: background var(--transition), color var(--transition);
  margin-right: 2px;
}

.cart-icon-circle:hover {
  background: var(--primary);
  color: #fff !important;
}

.header-link {
  color: var(--text-secondary);
  font-size: 14px;
  padding: 4px 12px;
  transition: color var(--transition);
  text-decoration: none;
}

.header-link:hover {
  color: var(--primary);
}

.header-link + .header-link {
  border-left: 1px solid var(--border-base);
  padding-left: 16px;
}

.btn-primary-outline {
  color: var(--primary) !important;
  border: 1px solid var(--primary);
  border-radius: var(--radius-sm);
  padding: 5px 16px !important;
  margin-left: 4px;
  transition: all var(--transition) !important;
}

.btn-primary-outline:hover {
  background: var(--primary) !important;
  color: #fff !important;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition);
}

.user-dropdown:hover {
  background: var(--primary-bg);
}

.user-name {
  font-size: 14px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---- 底部渐变装饰线 ---- */
.header-bottom-line {
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--accent), var(--primary-light));
}
</style>