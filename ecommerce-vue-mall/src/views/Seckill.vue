<template>
  <div class="seckill-page">
    <div class="page-hero">
      <div class="hero-content">
        <div class="hero-left">
          <span class="hero-emoji">⚡</span>
          <h2 class="hero-title">限时秒杀</h2>
          <span class="hero-badge">HOT</span>
        </div>
        <div class="hero-timer" v-if="currentActivity">
          <span class="timer-label">距离结束</span>
          <div class="timer-digits">
            <span class="timer-block">{{ countdown.hours }}</span>
            <span class="timer-sep">:</span>
            <span class="timer-block">{{ countdown.minutes }}</span>
            <span class="timer-sep">:</span>
            <span class="timer-block">{{ countdown.seconds }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-loading="loading">
      <div v-if="activities.length > 0" class="activity-products">
        <div
          v-for="product in activities"
          :key="product.id"
          class="activity-product"
          @click="$router.push(`/seckill/${product.id}`)"
        >
          <div class="product-image">
            <el-image
              :src="product.spuImage || 'https://via.placeholder.com/180x180?text=Seckill'"
              fit="cover"
              style="width: 100%; height: 200px;"
            />
            <div class="seckill-tag">秒杀</div>
          </div>
          <div class="product-info">
            <p class="product-name">{{ product.spuName }}</p>
            <div class="product-price">
              <span class="seckill-price">¥{{ formatPrice(product.seckillPrice) }}</span>
              <span class="original-price">¥{{ formatPrice(product.originalPrice) }}</span>
            </div>
            <div class="progress-wrapper">
              <div class="progress-bar">
                <div class="progress-inner" :style="{ width: Math.min(((product.stock - (product.soldCount || 0)) / product.stock) * 100, 100) + '%' }" />
              </div>
              <span class="progress-text">剩余 {{ product.stock }} 件</span>
            </div>
            <button class="rush-btn" @click.stop="$router.push(`/seckill/${product.id}`)">立即抢购</button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">⚡</div>
        <p class="empty-text">暂无秒杀活动</p>
        <p class="empty-hint">敬请期待下一场秒杀</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { getSeckillList } from '@/api/seckill'
import { formatPrice, formatCountdown } from '@/utils'

const loading = ref(false)
const activities = ref([])
const currentActivity = ref(null)
const remainingSeconds = ref(0)
let timer = null

const countdown = computed(() => formatCountdown(remainingSeconds.value))

function startCountdown() {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    }
  }, 1000)
}

async function fetchActivities() {
  loading.value = true
  try {
    const res = await getSeckillList()
    const data = res.data?.records || res.data || []
    activities.value = data
    if (activities.value.length > 0) {
      currentActivity.value = activities.value[0]
      remainingSeconds.value = currentActivity.value.remainSeconds || 3600
      startCountdown()
    }
  } catch {} finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchActivities()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
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

.seckill-page {
  min-height: 400px;
}

.page-hero {
  background: linear-gradient(135deg, #ffe0e0 0%, #ffcccc 30%, #fff0f0 100%);
  border-radius: var(--radius-lg);
  padding: 32px 36px;
  margin-bottom: 28px;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(245, 108, 108, 0.12);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-emoji {
  font-size: 36px;
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  color: #e04040;
  letter-spacing: 1px;
}

.hero-badge {
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 10px;
  letter-spacing: 1px;
  animation: badgePulse 2s infinite;
}

@keyframes badgePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.hero-timer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.timer-label {
  font-size: 15px;
  font-weight: 600;
  color: #666;
}

.timer-digits {
  display: flex;
  align-items: center;
  gap: 5px;
}

.timer-block {
  background: #2d2d2d;
  color: #fff;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 26px;
  font-weight: 800;
  min-width: 44px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.timer-sep {
  font-size: 26px;
  font-weight: 800;
  color: #e04040;
}

.activity-products {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.activity-product {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}

.activity-product:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
}

.product-image {
  position: relative;
  overflow: hidden;
}

.seckill-tag {
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(135deg, #ff4444, #f56c6c);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 5px 12px;
  border-radius: 0 0 10px 0;
  letter-spacing: 1px;
}

.product-info {
  padding: 14px 16px 18px;
}

.product-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.seckill-price {
  font-size: 22px;
  font-weight: 800;
  color: #f56c6c;
}

.original-price {
  font-size: 13px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #fde2e2;
  border-radius: 10px;
  overflow: hidden;
}

.progress-inner {
  height: 100%;
  background: linear-gradient(90deg, #ff4444, #ff6b6b, #e6a23c);
  border-radius: 10px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 12px;
  color: #c0c4cc;
  white-space: nowrap;
}

.rush-btn {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ff4444, #f56c6c);
  cursor: pointer;
  transition: var(--transition);
  letter-spacing: 1px;
}

.rush-btn:hover {
  background: linear-gradient(135deg, #e03030, #e05050);
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.4);
  transform: scale(1.03);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.4;
}

.empty-text {
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: var(--text-secondary);
}
</style>