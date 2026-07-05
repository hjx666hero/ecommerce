<template>
  <div class="seckill-detail-page" v-loading="loading">
    <template v-if="detail">
      <div class="seckill-hero">
        <div class="hero-left">
          <span class="hero-emoji">⚡</span>
          <h2 class="hero-title">限时秒杀</h2>
        </div>
        <div class="hero-timer">
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
      <div class="product-detail">
        <div class="product-image-section">
          <div class="product-image">
            <el-image
              :src="detail.spuImage || 'https://via.placeholder.com/400x400?text=Seckill'"
              fit="cover"
              style="width: 420px; height: 420px; border-radius: 12px;"
            />
            <div class="seckill-corner-tag">
              <span>秒杀</span>
            </div>
          </div>
        </div>
        <div class="product-info">
          <h1 class="product-name">{{ detail.spuName }}</h1>
          <div class="price-section">
            <div class="price-top">
              <div class="seckill-price-row">
                <span class="price-label">秒杀价</span>
                <span class="price-symbol">¥</span>
                <span class="seckill-price">{{ formatPrice(detail.seckillPrice) }}</span>
              </div>
              <div class="original-price-row">
                <span class="price-label">原价</span>
                <span class="original-price">¥{{ formatPrice(detail.originalPrice) }}</span>
              </div>
            </div>
            <div class="price-save">
              省 ¥{{ (detail.originalPrice - detail.seckillPrice).toFixed(2) }}
            </div>
          </div>
          <div class="stock-progress">
            <div class="stock-header">
              <span class="stock-label">库存进度</span>
              <span class="stock-percent">已售 {{ progressPercent }}%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-inner" :style="{ width: progressPercent + '%' }">
                <div class="progress-shine" />
              </div>
            </div>
            <div class="stock-info">
              <span>库存 {{ detail.stock }} 件</span>
              <span class="sold-count">已售 {{ detail.soldCount || 0 }} 件</span>
            </div>
          </div>
          <div class="action-section">
            <div v-if="detail.stock > 0">
              <div class="address-select" style="margin-bottom: 16px;">
                <div class="address-select-label">收货地址</div>
                <el-select v-model="selectedAddressId" placeholder="请选择收货地址" style="width: 100%;" v-if="addresses.length > 0" size="large">
                  <el-option
                    v-for="addr in addresses"
                    :key="addr.id"
                    :label="`${addr.receiverName} ${addr.receiverPhone} ${addr.province}${addr.city}${addr.district}${addr.detail}`"
                    :value="addr.id"
                  />
                </el-select>
                <el-button v-else type="primary" plain size="large" class="add-address-btn" @click="$router.push('/user/address')">请先添加收货地址</el-button>
              </div>
              <el-button
                type="danger"
                size="large"
                :loading="buying"
                :disabled="!selectedAddressId"
                class="seckill-btn"
                @click="handleSeckill"
              >
                ⚡ 立即抢购
              </el-button>
            </div>
            <el-button
              v-else
              type="danger"
              size="large"
              disabled
              class="seckill-btn sold-out-btn"
            >
              已售罄
            </el-button>
          </div>
        </div>
      </div>
    </template>
    <div v-else class="empty-state">
      <div class="empty-icon">⚡</div>
      <p class="empty-text">活动不存在</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeckillDetail, seckillOrder } from '@/api/seckill'
import { getAddressList } from '@/api/user'
import { formatPrice, formatCountdown } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const buying = ref(false)
const detail = ref(null)
const addresses = ref([])
const selectedAddressId = ref(null)
const remainingSeconds = ref(0)
let timer = null

const countdown = computed(() => formatCountdown(remainingSeconds.value))
const progressPercent = computed(() => {
  if (!detail.value) return 0
  return Math.min(((detail.value.soldCount || 0) / detail.value.stock) * 100, 100)
})

function startCountdown() {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    }
  }, 1000)
}

async function fetchDetail() {
  loading.value = true
  try {
    const [detailRes, addrRes] = await Promise.allSettled([
      getSeckillDetail(route.params.id),
      getAddressList().catch(() => ({ data: [] }))
    ])
    if (detailRes.status === 'fulfilled') {
      detail.value = detailRes.value.data
      remainingSeconds.value = detail.value?.remainSeconds || 3600
      startCountdown()
    }
    if (addrRes.status === 'fulfilled') {
      addresses.value = addrRes.value?.data || []
      if (addresses.value.length > 0) {
        selectedAddressId.value = addresses.value.find(a => a.isDefault === 1)?.id || addresses.value[0].id
      }
    }
  } catch {} finally {
    loading.value = false
  }
}

async function handleSeckill() {
  if (!detail.value || detail.value.stock <= 0) return
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  buying.value = true
  try {
    const res = await seckillOrder({ activityId: detail.value.id, addressId: selectedAddressId.value })
    ElMessage.success('抢购成功，请尽快支付')
    router.push(`/pay/${res.data}`)
  } catch {} finally {
    buying.value = false
  }
}

onMounted(() => {
  fetchDetail()
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

.seckill-detail-page {
  min-height: 400px;
}

.seckill-hero {
  background: linear-gradient(135deg, #ffe0e0 0%, #ffcccc 30%, #fff0f0 100%);
  border-radius: var(--radius-lg);
  padding: 28px 36px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(245, 108, 108, 0.12);
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-emoji {
  font-size: 34px;
}

.hero-title {
  font-size: 26px;
  font-weight: 800;
  color: #e04040;
  letter-spacing: 1px;
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
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 28px;
  font-weight: 800;
  min-width: 50px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.25);
  letter-spacing: 2px;
}

.timer-sep {
  font-size: 28px;
  font-weight: 800;
  color: #e04040;
}

.product-detail {
  display: flex;
  gap: 36px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 36px;
  box-shadow: var(--shadow-sm);
}

.product-image-section {
  flex-shrink: 0;
}

.product-image {
  position: relative;
}

.seckill-corner-tag {
  position: absolute;
  top: 16px;
  left: -4px;
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  padding: 6px 18px;
  border-radius: 0 6px 6px 0;
  box-shadow: 2px 2px 8px rgba(245, 108, 108, 0.4);
  letter-spacing: 2px;
}

.seckill-corner-tag::before {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  border: 4px solid #c0392b;
  border-left-color: transparent;
  border-bottom-color: transparent;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
  line-height: 1.4;
}

.price-section {
  background: linear-gradient(135deg, #fff5f5, #fef0f0);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  margin-bottom: 20px;
  border: 1px solid #fde2e2;
}

.price-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 12px;
}

.seckill-price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-label {
  font-size: 13px;
  color: #999;
  margin-right: 8px;
}

.price-symbol {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.seckill-price {
  font-size: 42px;
  font-weight: 800;
  color: #f56c6c;
  line-height: 1;
}

.original-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.original-price {
  font-size: 18px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.price-save {
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 20px;
  display: inline-block;
}

.stock-progress {
  margin-bottom: 28px;
}

.stock-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.stock-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.stock-percent {
  font-size: 13px;
  color: #f56c6c;
  font-weight: 700;
}

.progress-bar {
  height: 12px;
  background: #fde2e2;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-inner {
  height: 100%;
  background: linear-gradient(90deg, #ff4444, #ff6b6b, #e6a23c);
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.progress-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shine 2s infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 200%; }
}

.stock-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #999;
}

.sold-count {
  color: #f56c6c;
  font-weight: 600;
}

.action-section {
  margin-top: 4px;
}

.address-select-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.add-address-btn {
  width: 100%;
}

.seckill-btn {
  width: 100%;
  height: 56px;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 2px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #ff4444 0%, #ff6b6b 50%, #f56c6c 100%);
  box-shadow: 0 6px 24px rgba(245, 108, 108, 0.4);
  animation: glowPulse 2s infinite;
  transition: var(--transition);
}

.seckill-btn:hover {
  box-shadow: 0 8px 32px rgba(245, 108, 108, 0.55);
  transform: translateY(-2px);
}

.seckill-btn:active {
  transform: translateY(0);
}

@keyframes glowPulse {
  0%, 100% {
    box-shadow: 0 6px 24px rgba(245, 108, 108, 0.4);
  }
  50% {
    box-shadow: 0 6px 36px rgba(245, 108, 108, 0.65);
  }
}

.sold-out-btn {
  background: linear-gradient(135deg, #909399, #b0b3b8) !important;
  box-shadow: 0 4px 16px rgba(144, 147, 153, 0.3);
  animation: none;
  cursor: not-allowed;
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
}
</style>