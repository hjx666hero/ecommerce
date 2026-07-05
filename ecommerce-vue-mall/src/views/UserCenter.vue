<template>
  <div class="user-center-page">
    <div class="user-sidebar">
      <div class="user-profile">
        <div class="avatar-wrapper">
          <el-avatar :size="80" :src="userStore.avatar" class="profile-avatar" />
        </div>
        <div class="profile-info">
          <p class="nickname">{{ userStore.nickname || '未设置昵称' }}</p>
          <p class="phone">{{ userStore.phone }}</p>
        </div>
      </div>
      <div class="menu-section">
        <div class="menu-item" :class="{ active: activeMenu === '/user/info' }" @click="$router.push('/user/info')">
          <span class="menu-icon"><el-icon :size="18"><User /></el-icon></span>
          <span class="menu-label">个人信息</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/address' }" @click="$router.push('/user/address')">
          <span class="menu-icon"><el-icon :size="18"><Location /></el-icon></span>
          <span class="menu-label">地址管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/security' }" @click="$router.push('/user/security')">
          <span class="menu-icon"><el-icon :size="18"><Lock /></el-icon></span>
          <span class="menu-label">账号安全</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/coupons' }" @click="$router.push('/user/coupons')">
          <span class="menu-icon"><el-icon :size="18"><Ticket /></el-icon></span>
          <span class="menu-label">我的优惠券</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/favorites' }" @click="$router.push('/favorites')">
          <span class="menu-icon"><el-icon :size="18"><StarFilled /></el-icon></span>
          <span class="menu-label">我的收藏</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/messages' }" @click="$router.push('/messages')">
          <span class="menu-icon"><el-icon :size="18"><Bell /></el-icon></span>
          <span class="menu-label">我的消息</span>
        </div>
      </div>
    </div>
    <div class="user-main">
      <div class="welcome-header">
        <h3 class="welcome-title">欢迎回来，{{ userStore.nickname || '用户' }}</h3>
        <p class="welcome-subtitle">在这里可以管理您的个人信息、收货地址、优惠券和订单</p>
      </div>
      <div class="quick-links">
        <div class="quick-link-card" @click="$router.push('/orders')">
          <div class="quick-link-icon" style="background: var(--primary-light);">
            <el-icon :size="26" color="#409EFF"><Document /></el-icon>
          </div>
          <div class="quick-link-text">
            <span class="quick-link-title">我的订单</span>
            <span class="quick-link-sub">查看全部订单</span>
          </div>
        </div>
        <div class="quick-link-card" @click="$router.push('/user/address')">
          <div class="quick-link-icon" style="background: #e1f3d8;">
            <el-icon :size="26" color="#67c23a"><Location /></el-icon>
          </div>
          <div class="quick-link-text">
            <span class="quick-link-title">收货地址</span>
            <span class="quick-link-sub">管理收货地址</span>
          </div>
        </div>
        <div class="quick-link-card" @click="$router.push('/user/coupons')">
          <div class="quick-link-icon" style="background: #fdf6ec;">
            <el-icon :size="26" color="#e6a23c"><Ticket /></el-icon>
          </div>
          <div class="quick-link-text">
            <span class="quick-link-title">我的优惠券</span>
            <span class="quick-link-sub">查看可用优惠券</span>
          </div>
        </div>
        <div class="quick-link-card" @click="$router.push('/favorites')">
          <div class="quick-link-icon" style="background: #fef0f0;">
            <el-icon :size="26" color="#f56c6c"><StarFilled /></el-icon>
          </div>
          <div class="quick-link-text">
            <span class="quick-link-title">我的收藏</span>
            <span class="quick-link-sub">收藏的商品</span>
          </div>
        </div>
        <div class="quick-link-card" @click="$router.push('/messages')">
          <div class="quick-link-icon" style="background: #fef0f0;">
            <el-icon :size="26" color="#f56c6c"><Bell /></el-icon>
          </div>
          <div class="quick-link-text">
            <span class="quick-link-title">消息中心</span>
            <span class="quick-link-sub">查看系统消息</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Location, Lock, Ticket, StarFilled, Bell, Document } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleMenuSelect(index) {
  router.push(index)
}
</script>

<style scoped>
:root {
  --primary: #409EFF;
  --primary-light: #ecf5ff;
  --accent: #f56c6c;
  --text-primary: #303133;
  --text-secondary: #909399;
  --bg-white: #ffffff;
  --shadow-sm: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 20px rgba(0, 0, 0, 0.1);
  --radius-md: 12px;
  --radius-lg: 16px;
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-center-page {
  display: flex;
  gap: 24px;
  min-height: 500px;
}

.user-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 20px 24px;
  background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 50%, #74b9ff 100%);
}

.avatar-wrapper {
  margin-bottom: 14px;
}

.profile-avatar {
  border: 3px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.profile-info {
  text-align: center;
  color: #fff;
}

.nickname {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 6px;
  letter-spacing: 0.5px;
}

.phone {
  font-size: 14px;
  opacity: 0.85;
}

.menu-section {
  padding: 12px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  cursor: pointer;
  position: relative;
  transition: var(--transition);
  color: var(--text-primary);
  font-size: 15px;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background: var(--primary-light);
  border-radius: 0 8px 8px 0;
}

.menu-item.active {
  color: var(--primary);
  font-weight: 600;
  background: var(--primary-light);
  border-left-color: var(--primary);
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  transition: var(--transition);
}

.menu-item:hover .menu-icon {
  background: rgba(64, 158, 255, 0.12);
}

.menu-item.active .menu-icon {
  background: rgba(64, 158, 255, 0.15);
  color: var(--primary);
}

.menu-label {
  flex: 1;
}

.user-main {
  flex: 1;
  min-width: 0;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 36px 40px;
}

.welcome-header {
  margin-bottom: 40px;
}

.welcome-title {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 10px;
  font-weight: 700;
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-link-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--radius-md);
  background: #fafbfc;
  cursor: pointer;
  transition: var(--transition);
  border: 1px solid transparent;
}

.quick-link-card:hover {
  background: var(--bg-white);
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.quick-link-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  flex-shrink: 0;
  transition: var(--transition);
}

.quick-link-card:hover .quick-link-icon {
  transform: scale(1.08);
}

.quick-link-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quick-link-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.quick-link-sub {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>