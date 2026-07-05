<template>
  <div class="order-list-page">
    <!-- Hero Banner -->
    <div class="order-list-hero">
      <div class="hero-content">
        <h2 class="hero-title">📋 我的订单</h2>
        <span class="hero-count" v-if="total > 0">共 {{ total }} 笔订单</span>
      </div>
    </div>

    <!-- Tabs -->
    <div class="order-tabs-wrapper">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="order-tabs">
        <el-tab-pane name="">
          <template #label>
            <span class="tab-label">全部</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="0">
          <template #label>
            <span class="tab-label">待付款</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="1">
          <template #label>
            <span class="tab-label">已支付</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="2">
          <template #label>
            <span class="tab-label">已发货</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="3">
          <template #label>
            <span class="tab-label">已完成</span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Order List -->
    <div v-loading="loading">
      <template v-if="orderList.length > 0">
        <div v-for="order in orderList" :key="order.id" class="order-card">
          <!-- Card Header -->
          <div class="order-card-header">
            <div class="header-left">
              <span class="order-no">订单编号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatDate(order.createTime) }}</span>
            </div>
            <span :class="['status-tag', 'status-' + order.status]">
              {{ getOrderStatus(order.status).label }}
            </span>
          </div>

          <!-- Card Items -->
          <div class="order-card-items">
            <div v-for="item in order.items" :key="item.id" class="order-item-row">
              <div class="item-img-wrap">
                <el-image
                  :src="item.skuImage || 'https://via.placeholder.com/60x60?text=No+Image'"
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
              <span class="item-price">¥{{ formatPrice(item.price) }}</span>
              <span class="item-quantity">x{{ item.quantity }}</span>
            </div>
          </div>

          <!-- Card Footer -->
          <div class="order-card-footer">
            <span class="order-total">
              共 {{ order.items?.length || 0 }} 件商品，合计：<em>¥{{ formatPrice(order.totalAmount) }}</em>
            </span>
            <div class="order-actions">
              <el-button size="small" class="btn-detail" @click="$router.push(`/order/${order.orderNo}`)">
                查看详情
              </el-button>
              <el-button
                v-if="order.status === 0"
                type="danger"
                size="small"
                class="btn-pay"
                @click="handlePay(order.orderNo)"
              >
                去支付
              </el-button>
              <el-button
                v-if="order.status === 0"
                size="small"
                class="btn-cancel"
                @click="handleCancel(order.orderNo)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.status === 2"
                type="primary"
                size="small"
                class="btn-confirm"
                @click="handleConfirmReceive(order.orderNo)"
              >
                确认收货
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- Empty State -->
      <div v-else class="order-empty">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 100 100" width="100" height="100">
            <rect x="20" y="35" width="60" height="45" rx="4" stroke="#ddd" stroke-width="1.5" fill="none"/>
            <line x1="20" y1="50" x2="80" y2="50" stroke="#eee" stroke-width="1"/>
            <line x1="28" y1="60" x2="55" y2="60" stroke="#eee" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="28" y1="67" x2="45" y2="67" stroke="#eee" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="50" cy="25" r="10" fill="none" stroke="#ddd" stroke-width="1.5"/>
          </svg>
        </div>
        <p class="empty-text">暂无订单</p>
        <p class="empty-sub">快去挑选心仪的商品吧</p>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrderByOrderNo, confirmReceiveByOrderNo } from '@/api/order'
import { formatPrice, formatDate, getOrderStatus } from '@/utils'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('')
const orderList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (activeTab.value) params.status = Number(activeTab.value)
    const res = await getOrderList(params)
    orderList.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchOrders()
}

function handlePay(orderNo) {
  router.push(`/pay/${orderNo}`)
}

async function handleCancel(orderNo) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrderByOrderNo(orderNo)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {}
}

async function handleConfirmReceive(orderNo) {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', { type: 'warning' })
    await confirmReceiveByOrderNo(orderNo)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch {}
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
/* ===== CSS Variables ===== */
.order-list-page {
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
.order-list-hero {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: var(--radius-lg);
  padding: 28px 36px;
  margin-bottom: 24px;
}
.hero-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.hero-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin: 0;
}
.hero-count {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 14px;
  border-radius: 12px;
  font-weight: 500;
}

/* ===== Tabs ===== */
.order-tabs-wrapper {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 4px 20px 0;
  box-shadow: var(--shadow-sm);
  margin-bottom: 16px;
}
.order-tabs :deep(.el-tabs__header) {
  margin: 0;
}
.order-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 2px;
  background: linear-gradient(90deg, var(--primary), var(--accent));
}
.order-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--primary), #f5576c);
}
.order-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
  color: var(--text-secondary);
}
.order-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
}

/* ===== Order Card ===== */
.order-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition);
}
.order-card:hover {
  box-shadow: var(--shadow-md);
}

/* Card Header */
.order-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, #fafafa, #f5f5f5);
  border-bottom: 1px solid var(--border-light);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.order-no {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: 'SF Mono', 'Consolas', monospace;
}
.order-time {
  font-size: 13px;
  color: var(--text-secondary);
}

/* Status Tags */
.status-tag {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 10px;
}
.status-0 {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
}
.status-1 {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}
.status-2 {
  background: linear-gradient(135deg, #f3e5f5, #e1bee7);
  color: #7b1fa2;
}
.status-3 {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #2e7d32;
}

/* Card Items */
.order-card-items {
  padding: 4px 20px;
}
.order-item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid #fafafa;
}
.order-item-row:last-child {
  border-bottom: none;
}
.item-img-wrap {
  flex-shrink: 0;
}
.item-img {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  object-fit: cover;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
  font-weight: 500;
}
.spec-chip {
  display: inline-block;
  font-size: 11px;
  color: var(--text-secondary);
  background: #f5f5f5;
  padding: 1px 8px;
  border-radius: 3px;
  margin-top: 4px;
  border: 1px solid var(--border-light);
}
.item-price {
  color: var(--accent);
  font-weight: 600;
  font-size: 14px;
  width: 80px;
  text-align: center;
}
.item-quantity {
  color: var(--text-secondary);
  font-size: 14px;
  width: 50px;
  text-align: center;
}

/* Card Footer */
.order-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-top: 1px solid var(--border-light);
  background: #fafafa;
}
.order-total {
  font-size: 14px;
  color: var(--text-secondary);
}
.order-total em {
  font-style: normal;
  font-size: 18px;
  color: var(--accent);
  font-weight: 700;
}
.order-actions {
  display: flex;
  gap: 10px;
}

/* Action Buttons */
.btn-detail {
  border-color: var(--primary);
  color: var(--primary);
  border-radius: var(--radius-sm);
}
.btn-detail:hover {
  background: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
}
.btn-pay {
  background: linear-gradient(135deg, #f56c6c, #e04545);
  border: none;
  border-radius: var(--radius-sm);
  color: #fff;
}
.btn-pay:hover {
  background: linear-gradient(135deg, #f78989, #e85555);
  color: #fff;
}
.btn-cancel {
  border-color: #dcdfe6;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
}
.btn-confirm {
  background: linear-gradient(135deg, #409EFF, #337ecc);
  border: none;
  border-radius: var(--radius-sm);
  color: #fff;
}
.btn-confirm:hover {
  background: linear-gradient(135deg, #66b1ff, #409EFF);
  color: #fff;
}

/* ===== Pagination ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 20px;
}

/* ===== Empty State ===== */
.order-empty {
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
  margin: 0 0 6px 0;
}
.empty-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}
</style>
