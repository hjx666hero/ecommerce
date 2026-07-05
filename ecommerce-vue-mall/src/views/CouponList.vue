<template>
  <div class="coupon-list-page">
    <div class="page-hero">
      <div class="hero-content">
        <div class="hero-icon">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <line x1="2" y1="10" x2="22" y2="10"/>
          </svg>
        </div>
        <div class="hero-info">
          <h1 class="hero-title">我的优惠券</h1>
          <p class="hero-count">共 <span>{{ coupons.length }}</span> 张</p>
        </div>
      </div>
      <el-button class="hero-action" type="primary" round @click="$router.push('/coupons')">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        领券中心
      </el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="coupon-tabs">
      <el-tab-pane label="未使用" :name="0" />
      <el-tab-pane label="已使用" :name="1" />
      <el-tab-pane label="已过期" :name="2" />
    </el-tabs>

    <div v-loading="loading" class="coupon-content">
      <div v-if="coupons.length > 0" class="coupon-cards">
        <div
          v-for="coupon in coupons"
          :key="coupon.id"
          :class="['coupon-card', { 'coupon-expired': activeTab === 2, 'coupon-used': activeTab === 1 }]"
        >
          <div class="coupon-left">
            <div class="coupon-amount">
              <span class="currency">¥</span>
              <span class="value">{{ formatPrice(coupon.discountValue) }}</span>
            </div>
            <span class="coupon-condition">{{ coupon.typeText || '无门槛' }}</span>
          </div>
          <div class="coupon-right">
            <h4 class="coupon-name">{{ coupon.name }}</h4>
            <p class="coupon-range">有效期：{{ formatDate(coupon.startTime, 'YYYY-MM-DD') }} - {{ formatDate(coupon.endTime, 'YYYY-MM-DD') }}</p>
            <el-tag :type="getCouponStatusTag(activeTab)" size="small" effect="plain" round>
              {{ statusLabel(activeTab) }}
            </el-tag>
          </div>
          <div v-if="activeTab === 2" class="expired-overlay">
            <span>已过期</span>
          </div>
          <div v-if="activeTab === 1" class="used-overlay">
            <span>已使用</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <svg viewBox="0 0 120 120" width="100" height="100" fill="none">
          <rect x="15" y="25" width="90" height="70" rx="8" stroke="#d0d0d0" stroke-width="2.5" stroke-dasharray="6 4"/>
          <line x1="15" y1="48" x2="105" y2="48" stroke="#d0d0d0" stroke-width="2.5" stroke-dasharray="6 4"/>
          <circle cx="50" cy="65" r="12" stroke="#d0d0d0" stroke-width="2"/>
          <line x1="46" y1="65" x2="54" y2="65" stroke="#d0d0d0" stroke-width="2"/>
        </svg>
        <p class="empty-text">暂无优惠券</p>
        <el-button type="primary" round @click="$router.push('/coupons')">去领券</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyCoupons } from '@/api/coupon'
import { formatPrice, formatDate } from '@/utils'

const loading = ref(false)
const activeTab = ref(0)
const coupons = ref([])

function statusLabel(status) {
  const map = { 0: '未使用', 1: '已使用', 2: '已过期' }
  return map[status] || '未知'
}

function getCouponStatusTag(status) {
  const map = { 0: 'primary', 1: 'info', 2: 'info' }
  return map[status] || 'info'
}

async function fetchCoupons() {
  loading.value = true
  try {
    const res = await getMyCoupons({ status: activeTab.value })
    const data = res.data?.records || res.data || []
    coupons.value = data
  } catch {} finally {
    loading.value = false
  }
}

function handleTabChange() {
  fetchCoupons()
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.coupon-list-page {
  --primary: #e1251b;
  --primary-light: #fff1f0;
  --warning: #e6a23c;
  --success: #67c23a;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-placeholder: #909399;
  --bg-white: #fff;
  --border-color: #ebeef5;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.1);
  --radius-md: 10px;
  --radius-lg: 12px;
  --transition: all 0.3s ease;

  min-height: 400px;
}

/* Hero */
.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fff5f5, #fff0ed, #fff7f0);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(225, 37, 27, 0.08);
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary), #ff4d4f);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px;
}

.hero-count {
  font-size: 13px;
  color: var(--text-placeholder);
  margin: 0;
}

.hero-count span {
  color: var(--primary);
  font-weight: 700;
  font-size: 18px;
  margin: 0 2px;
}

.hero-action {
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 20px;
  padding: 10px 22px;
  font-weight: 500;
}

/* Tabs */
.coupon-tabs {
  margin-bottom: 8px;
}

.coupon-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.coupon-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.coupon-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
}

/* Content */
.coupon-content {
  padding-top: 16px;
}

/* Coupon Cards */
.coupon-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.coupon-card {
  display: flex;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  width: 360px;
  position: relative;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}

.coupon-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.coupon-card.coupon-expired,
.coupon-card.coupon-used {
  opacity: 0.65;
}

.coupon-card.coupon-expired .coupon-left {
  background: linear-gradient(135deg, #b0b0b0, #999);
}

.coupon-card.coupon-used .coupon-left {
  background: linear-gradient(135deg, #a0a0a0, #8c8c8c);
}

.coupon-left {
  width: 115px;
  background: linear-gradient(135deg, #e1251b, #ff4d4f);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 18px 12px;
  flex-shrink: 0;
  position: relative;
}

.coupon-left::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  background: var(--bg-white);
  border-radius: 50%;
  box-shadow: inset 0 2px 3px rgba(0,0,0,0.08);
}

.coupon-amount {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 15px;
  font-weight: 600;
}

.value {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -1px;
}

.coupon-condition {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.9;
  font-weight: 500;
}

.coupon-right {
  flex: 1;
  padding: 16px 20px;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-range {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0 0 4px;
}

.coupon-time {
  font-size: 11px;
  color: var(--text-placeholder);
  margin: 0 0 10px;
}

/* Overlays */
.expired-overlay,
.used-overlay {
  position: absolute;
  top: 12px;
  right: -28px;
  transform: rotate(45deg);
  width: 80px;
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 0;
  color: #fff;
  z-index: 1;
}

.expired-overlay {
  background: #c0c0c0;
}

.used-overlay {
  background: #a0a0a0;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 20px;
}

.empty-text {
  font-size: 15px;
  color: var(--text-placeholder);
  margin: 16px 0 20px;
}
</style>