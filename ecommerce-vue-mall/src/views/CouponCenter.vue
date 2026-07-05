<template>
  <div class="coupon-center-page">
    <div class="page-hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-icon">
          <svg viewBox="0 0 24 24" width="30" height="30" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 6v6l4 2"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1 class="hero-title">领券中心</h1>
          <p class="hero-desc">精选好券，先领再买更优惠</p>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="coupon-content">
      <div v-if="coupons.length > 0" class="coupon-grid">
        <div
          v-for="coupon in coupons"
          :key="coupon.id"
          :class="['coupon-card', { claimed: coupon.received }]"
        >
          <div class="coupon-body">
            <div class="coupon-left">
              <div class="coupon-amount">
                <span class="currency">¥</span>
                <span class="value">{{ formatPrice(coupon.discountValue) }}</span>
              </div>
              <span class="coupon-condition">{{ getConditionText(coupon) }}</span>
            </div>
            <div class="coupon-right">
              <h4 class="coupon-name">{{ coupon.name }}</h4>
              <p class="coupon-range">有效期：{{ formatDate(coupon.startTime, 'YYYY-MM-DD') }} - {{ formatDate(coupon.endTime, 'YYYY-MM-DD') }}</p>
              <p class="coupon-stock">剩余 {{ coupon.totalCount - coupon.receiveCount }} / {{ coupon.totalCount }} 张</p>
              <el-button
                :class="['claim-btn', { claimed: coupon.received }]"
                type="danger"
                size="small"
                :disabled="coupon.received"
                :loading="receivingId === coupon.id"
                @click="handleReceive(coupon)"
                round
              >
                <span v-if="!coupon.received" class="btn-text">立即领取</span>
                <span v-else class="btn-text">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                  已领取
                </span>
              </el-button>
            </div>
          </div>
          <div v-if="coupon.received" class="claimed-overlay"></div>
        </div>
      </div>
      <div v-else class="empty-state">
        <svg viewBox="0 0 120 120" width="100" height="100" fill="none">
          <circle cx="60" cy="55" r="30" stroke="#d0d0d0" stroke-width="2.5" stroke-dasharray="6 4"/>
          <line x1="40" y1="60" x2="80" y2="60" stroke="#d0d0d0" stroke-width="2"/>
          <line x1="40" y1="65" x2="80" y2="65" stroke="#d0d0d0" stroke-width="2"/>
        </svg>
        <p class="empty-text">暂无可用优惠券</p>
        <p class="empty-sub">敬请期待更多优惠</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCoupons, receiveCoupon } from '@/api/coupon'
import { formatPrice, formatDate } from '@/utils'

const loading = ref(false)
const coupons = ref([])
const receivingId = ref(null)

async function fetchCoupons() {
  loading.value = true
  try {
    const res = await getAvailableCoupons()
    const data = res.data?.records || res.data || []
    coupons.value = data
  } catch {} finally {
    loading.value = false
  }
}

function getConditionText(coupon) {
  if (coupon.type === 1) return `满 ¥${formatPrice(coupon.minAmount)} 可用`
  if (coupon.type === 2) return `${(coupon.discountValue * 10).toFixed(1)}折券`
  return '无门槛'
}

async function handleReceive(coupon) {
  receivingId.value = coupon.id
  try {
    await receiveCoupon(coupon.id)
    ElMessage.success('领取成功')
    coupon.received = true
  } catch {} finally {
    receivingId.value = null
  }
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.coupon-center-page {
  --primary: #e1251b;
  --primary-light: #fff1f0;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-placeholder: #909399;
  --bg-white: #fff;
  --border-color: #ebeef5;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 6px 20px rgba(0, 0, 0, 0.1);
  --radius-md: 10px;
  --radius-lg: 14px;
  --transition: all 0.3s ease;

  min-height: 400px;
}

/* Hero Banner */
.page-hero {
  position: relative;
  padding: 32px 36px;
  margin-bottom: 24px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #ff6b35, #e1251b, #d4145a);
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  z-index: 1;
}

.hero-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hero-title {
  font-size: 26px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 4px;
  letter-spacing: 1px;
}

.hero-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

/* Content */
.coupon-content {
  min-height: 200px;
}

/* Coupon Grid */
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

/* Coupon Card */
.coupon-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.coupon-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: #ffccc7;
}

.coupon-card.claimed {
  opacity: 0.75;
}

.coupon-card.claimed:hover {
  transform: none;
  box-shadow: var(--shadow-sm);
}

.coupon-body {
  display: flex;
}

.coupon-left {
  width: 120px;
  background: linear-gradient(160deg, #e1251b, #ff4d4f);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
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
  box-shadow: inset 0 2px 3px rgba(0,0,0,0.06);
}

.coupon-card.claimed .coupon-left {
  background: linear-gradient(160deg, #b0b0b0, #999);
}

.coupon-amount {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 16px;
  font-weight: 700;
}

.value {
  font-size: 38px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -1.5px;
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
  margin: 0 0 12px;
}

/* Claim Button */
.claim-btn {
  align-self: flex-start;
}

.claim-btn .btn-text {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.claim-btn.claimed {
  --el-button-bg-color: #f0f0f0;
  --el-button-border-color: #e0e0e0;
  --el-button-text-color: #999;
  --el-button-hover-bg-color: #f0f0f0;
  --el-button-hover-border-color: #e0e0e0;
  --el-button-hover-text-color: #999;
  cursor: default;
}

/* Claimed Overlay */
.claimed-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.3);
  pointer-events: none;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 72px 20px;
}

.empty-text {
  font-size: 16px;
  color: var(--text-placeholder);
  margin: 16px 0 6px;
  font-weight: 500;
}

.empty-sub {
  font-size: 13px;
  color: #c0c4cc;
  margin: 0;
}
</style>