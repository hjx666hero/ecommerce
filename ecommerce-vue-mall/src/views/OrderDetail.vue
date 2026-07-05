<template>
  <div class="order-detail-page" v-loading="loading">
    <template v-if="order">
      <!-- Hero Banner -->
      <div class="detail-hero">
        <div class="hero-content">
          <h2 class="hero-title">📦 订单详情</h2>
          <span :class="['hero-status', 'hero-status-' + order.status]">
            {{ getOrderStatus(order.status).label }}
          </span>
        </div>
      </div>

      <!-- Status Progress -->
      <div class="section status-section">
        <el-steps :active="statusStep" align-center class="detail-steps">
          <el-step title="提交订单" :description="formatDate(order.createTime)" />
          <el-step title="付款" :description="order.payTime ? formatDate(order.payTime) : ''" />
          <el-step title="发货" :description="order.deliveryTime ? formatDate(order.deliveryTime) : ''" />
          <el-step title="收货" :description="order.receiveTime ? formatDate(order.receiveTime) : ''" />
          <el-step title="完成" />
        </el-steps>
      </div>

      <!-- Order Info & Address Grid -->
      <div class="info-grid-sections">
        <div class="section info-section">
          <h3><span class="section-icon">📋</span> 订单信息</h3>
          <div class="kv-grid">
            <div class="kv-row">
              <span class="kv-label">订单编号</span>
              <span class="kv-value mono">{{ order.orderNo }}</span>
            </div>
            <div class="kv-row">
              <span class="kv-label">订单状态</span>
              <span :class="['kv-status', 'kv-status-' + order.status]">
                {{ getOrderStatus(order.status).label }}
              </span>
            </div>
            <div class="kv-row">
              <span class="kv-label">创建时间</span>
              <span class="kv-value">{{ formatDate(order.createTime) }}</span>
            </div>
            <div class="kv-row" v-if="order.payTime">
              <span class="kv-label">付款时间</span>
              <span class="kv-value">{{ formatDate(order.payTime) }}</span>
            </div>
            <div class="kv-row" v-if="order.remark">
              <span class="kv-label">备注</span>
              <span class="kv-value remark-value">{{ order.remark }}</span>
            </div>
          </div>
        </div>

        <div class="section info-section">
          <h3><span class="section-icon">📍</span> 收货信息</h3>
          <div class="kv-grid">
            <div class="kv-row">
              <span class="kv-label">收货人</span>
              <span class="kv-value">{{ order.receiverName }}</span>
            </div>
            <div class="kv-row">
              <span class="kv-label">手机号</span>
              <span class="kv-value mono">{{ order.receiverPhone }}</span>
            </div>
            <div class="kv-row">
              <span class="kv-label">收货地址</span>
              <span class="kv-value">{{ order.receiverAddress }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Items -->
      <div class="section products-section">
        <h3><span class="section-icon">📦</span> 商品信息</h3>
        <div class="detail-items">
          <div v-for="item in order.items" :key="item.id" class="detail-item-card">
            <div class="item-img-wrap">
              <el-image
                :src="item.skuImage || 'https://via.placeholder.com/80x80?text=No+Image'"
                fit="cover"
                class="item-img"
              />
            </div>
            <div class="item-info">
              <p class="item-name">{{ item.spuName }}</p>
              <p class="item-spec" v-if="item.skuSpec">
                <span class="spec-chip">{{ item.skuSpec }}</span>
              </p>
            </div>
            <div class="item-price-col">
              <span class="item-price">¥{{ formatPrice(item.price) }}</span>
              <span class="item-quantity">x{{ item.quantity }}</span>
            </div>
            <span class="item-subtotal">¥{{ formatPrice(item.price * item.quantity) }}</span>
          </div>
        </div>
      </div>

      <!-- Amount Breakdown -->
      <div class="section amount-section">
        <h3><span class="section-icon">💰</span> 金额信息</h3>
        <div class="amount-breakdown">
          <div class="amount-row">
            <span>商品总额</span>
            <span class="amount-val">¥{{ formatPrice(order.totalAmount) }}</span>
          </div>
          <div class="amount-row">
            <span>运费</span>
            <span class="amount-val">¥{{ formatPrice(order.freight || 0) }}</span>
          </div>
          <div class="amount-row" v-if="order.discountAmount">
            <span>优惠金额</span>
            <span class="discount">-¥{{ formatPrice(order.discountAmount) }}</span>
          </div>
          <div class="amount-row total-row">
            <span>实付金额</span>
            <span class="final-price">¥{{ formatPrice(order.payAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="action-bar" v-if="order.status === 0 || order.status === 2">
        <div class="action-bar-inner">
          <el-button
            v-if="order.status === 0"
            type="danger"
            size="large"
            class="btn-pay"
            @click="$router.push(`/pay/${order.orderNo}`)"
          >
            去支付
          </el-button>
          <el-button
            v-if="order.status === 0"
            size="large"
            class="btn-cancel"
            @click="handleCancel"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 2"
            type="primary"
            size="large"
            class="btn-confirm"
            @click="handleConfirmReceive"
          >
            确认收货
          </el-button>
        </div>
      </div>
    </template>

    <div v-else class="detail-empty">
      <div class="empty-icon-wrap">
        <svg viewBox="0 0 100 100" width="100" height="100">
          <circle cx="50" cy="50" r="45" stroke="#ddd" stroke-width="1.5" fill="none" stroke-dasharray="5 4"/>
          <circle cx="50" cy="42" r="12" stroke="#ddd" stroke-width="1.5" fill="none"/>
          <line x1="50" y1="54" x2="50" y2="72" stroke="#ddd" stroke-width="2" stroke-linecap="round"/>
          <line x1="38" y1="62" x2="62" y2="62" stroke="#ddd" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-text">订单不存在</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetailByOrderNo, cancelOrderByOrderNo, confirmReceiveByOrderNo } from '@/api/order'
import { formatPrice, formatDate, getOrderStatus } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)

const statusStep = computed(() => {
  if (!order.value) return 0
  const status = order.value.status
  if (status === 0) return 0
  if (status === 1) return 1
  if (status === 2) return 2
  if (status === 3) return 4
  return 0
})

async function fetchOrderDetail() {
  loading.value = true
  try {
    const res = await getOrderDetailByOrderNo(route.params.orderNo)
    order.value = res.data
  } catch {} finally {
    loading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrderByOrderNo(order.value.orderNo)
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch {}
}

async function handleConfirmReceive() {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', { type: 'warning' })
    await confirmReceiveByOrderNo(order.value.orderNo)
    ElMessage.success('已确认收货')
    fetchOrderDetail()
  } catch {}
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
/* ===== CSS Variables ===== */
.order-detail-page {
  --primary: #409EFF;
  --primary-light: #ecf5ff;
  --primary-dark: #337ecc;
  --accent: #f56c6c;
  --accent-light: #fef0f0;
  --text-primary: #303133;
  --text-secondary: #909399;
  --bg-white: #ffffff;
  --border-light: #f0f0f0;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --transition: 0.3s ease;
  min-height: 400px;
}

/* ===== Hero Banner ===== */
.detail-hero {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  border-radius: var(--radius-lg);
  padding: 28px 36px;
  margin-bottom: 24px;
}
.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hero-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.hero-status {
  font-size: 14px;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 16px;
  color: #fff;
}
.hero-status-0 { background: rgba(255,255,255,0.3); }
.hero-status-1 { background: rgba(255,255,255,0.3); }
.hero-status-2 { background: rgba(255,255,255,0.3); }
.hero-status-3 { background: rgba(255,255,255,0.3); }

/* ===== Status Progress ===== */
.status-section {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 36px 40px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}
.detail-steps :deep(.el-step__title) {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.detail-steps :deep(.el-step__description) {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.detail-steps :deep(.el-step__head.is-success) {
  color: #67c23a;
  border-color: #67c23a;
}
.detail-steps :deep(.el-step__head.is-process) {
  color: var(--primary);
  border-color: var(--primary);
}
.detail-steps :deep(.is-process .el-step__icon) {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  border: none;
}
.detail-steps :deep(.is-success .el-step__icon) {
  background: linear-gradient(135deg, #67c23a, #5daf34);
  border: none;
}
.detail-steps :deep(.is-success .el-step__line) {
  background: linear-gradient(to right, #67c23a, var(--primary));
}
.detail-steps :deep(.el-step__line) {
  background-color: #e0e0e0;
}

/* ===== Section ===== */
.section {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 24px 28px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}
.section h3 {
  font-size: 16px;
  color: var(--text-primary);
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
}
.section-icon {
  font-size: 16px;
}

/* ===== Info Grid ===== */
.info-grid-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.info-section {
  margin-bottom: 0;
}
.kv-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.kv-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}
.kv-label {
  color: var(--text-secondary);
  width: 80px;
  flex-shrink: 0;
}
.kv-value {
  color: var(--text-primary);
  font-weight: 500;
}
.kv-value.mono {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 13px;
}
.remark-value {
  color: var(--text-secondary);
  font-style: italic;
}
.kv-status {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
}
.kv-status-0 {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
}
.kv-status-1 {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}
.kv-status-2 {
  background: linear-gradient(135deg, #f3e5f5, #e1bee7);
  color: #7b1fa2;
}
.kv-status-3 {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #2e7d32;
}

/* ===== Product Items ===== */
.detail-item-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  margin-bottom: 10px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  transition: box-shadow var(--transition);
}
.detail-item-card:hover {
  box-shadow: var(--shadow-sm);
}
.detail-item-card:last-child {
  margin-bottom: 0;
}
.item-img-wrap {
  flex-shrink: 0;
}
.item-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.spec-chip {
  display: inline-block;
  font-size: 11px;
  color: var(--text-secondary);
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 3px;
  margin-top: 4px;
  border: 1px solid var(--border-light);
}
.item-price-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  width: 80px;
}
.item-price {
  color: var(--accent);
  font-weight: 600;
  font-size: 14px;
}
.item-quantity {
  color: var(--text-secondary);
  font-size: 13px;
}
.item-subtotal {
  color: var(--accent);
  font-weight: 700;
  width: 90px;
  text-align: right;
  font-size: 16px;
}

/* ===== Amount Breakdown ===== */
.amount-breakdown {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 12px 0 0 0;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  width: 260px;
  padding: 7px 0;
  font-size: 14px;
  color: var(--text-secondary);
}
.amount-val {
  color: var(--text-primary);
  font-weight: 500;
}
.total-row {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
  margin-top: 6px;
}
.total-row span:first-child {
  font-weight: 600;
}
.discount {
  color: #67c23a;
  font-weight: 600;
}
.final-price {
  color: var(--accent);
  font-size: 24px;
  font-weight: 700;
}

/* ===== Action Bar ===== */
.action-bar {
  position: sticky;
  bottom: 0;
  z-index: 10;
  margin-top: 16px;
}
.action-bar-inner {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 20px 28px;
  box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.08);
}
.btn-pay {
  background: linear-gradient(135deg, #f56c6c, #e04545);
  border: none;
  font-weight: 600;
  padding: 14px 36px;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 14px rgba(245, 108, 108, 0.35);
  transition: all var(--transition);
}
.btn-pay:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.5);
  background: linear-gradient(135deg, #f78989, #e85555);
}
.btn-cancel {
  border-color: #dcdfe6;
  color: var(--text-secondary);
  padding: 14px 36px;
  border-radius: var(--radius-md);
  font-weight: 500;
}
.btn-confirm {
  background: linear-gradient(135deg, #409EFF, #337ecc);
  border: none;
  font-weight: 600;
  padding: 14px 36px;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.35);
  transition: all var(--transition);
}
.btn-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
  background: linear-gradient(135deg, #66b1ff, #409EFF);
}

/* ===== Empty State ===== */
.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.empty-icon-wrap {
  opacity: 0.5;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 16px;
  color: var(--text-primary);
  margin: 0;
}
</style>
