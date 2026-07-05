<template>
  <div class="pay-page" v-loading="loading">
    <template v-if="orderInfo">
      <div class="pay-card">
        <!-- Gradient Top Border -->
        <div class="pay-card-border"></div>

        <div class="pay-header">
          <div class="pay-status-badge">
            <el-icon :size="20"><Clock /></el-icon>
            <span>待支付</span>
          </div>
        </div>

        <div class="order-info">
          <div class="info-row">
            <span class="label">订单编号</span>
            <span class="value">{{ orderInfo.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">应付金额</span>
            <span class="value amount">¥{{ formatPrice(orderInfo.payAmount) }}</span>
          </div>
        </div>

        <div class="pay-methods">
          <h3>选择支付方式</h3>
          <div class="methods">
            <div
              :class="['method-item', 'method-alipay', { active: payMethod === 'alipay' }]"
              @click="payMethod = 'alipay'"
            >
              <div class="method-icon alipay-icon">
                <svg viewBox="0 0 48 48" width="36" height="36"><rect width="48" height="48" rx="8" fill="#1677FF"/><text x="24" y="30" text-anchor="middle" fill="#fff" font-size="18" font-weight="700">支</text></svg>
              </div>
              <span>支付宝</span>
              <span v-if="payMethod === 'alipay'" class="method-check">✓</span>
            </div>
            <div
              :class="['method-item', 'method-wechat', { active: payMethod === 'wechat' }]"
              @click="payMethod = 'wechat'"
            >
              <div class="method-icon wechat-icon">
                <svg viewBox="0 0 48 48" width="36" height="36"><rect width="48" height="48" rx="8" fill="#07C160"/><text x="24" y="30" text-anchor="middle" fill="#fff" font-size="18" font-weight="700">微</text></svg>
              </div>
              <span>微信支付</span>
              <span v-if="payMethod === 'wechat'" class="method-check">✓</span>
            </div>
          </div>
        </div>

        <div class="pay-action">
          <el-button
            type="danger"
            size="large"
            :loading="paying"
            @click="handlePay"
            class="pay-btn"
          >
            立即支付 ¥{{ formatPrice(orderInfo.payAmount) }}
          </el-button>
        </div>
      </div>
    </template>

    <!-- 支付结果弹窗 -->
    <el-dialog v-model="showResult" :title="payResult.success ? '支付成功' : '支付失败'" width="420px" :close-on-click-modal="false" center>
      <div class="pay-result">
        <div v-if="payResult.success" class="result-icon success-icon">
          <el-icon :size="72" color="#67c23a"><CircleCheckFilled /></el-icon>
        </div>
        <div v-else class="result-icon fail-icon">
          <el-icon :size="72" color="#f56c6c"><CircleCloseFilled /></el-icon>
        </div>
        <p class="result-text">{{ payResult.message }}</p>
      </div>
      <template #footer>
        <el-button @click="$router.push('/orders')" class="result-btn-outline">查看订单</el-button>
        <el-button type="primary" @click="$router.push('/')" class="result-btn-primary">返回首页</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetailByOrderNo } from '@/api/order'
import { pay } from '@/api/pay'
import { Clock, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { formatPrice } from '@/utils'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paying = ref(false)
const orderInfo = ref(null)
const payMethod = ref('alipay')
const showResult = ref(false)
const payResult = reactive({ success: false, message: '' })

async function fetchOrderInfo() {
  loading.value = true
  try {
    const res = await getOrderDetailByOrderNo(route.params.orderNo)
    orderInfo.value = res.data
  } catch {} finally {
    loading.value = false
  }
}

async function handlePay() {
  paying.value = true
  try {
    await pay({
      orderNo: orderInfo.value.orderNo,
      payType: payMethod.value === 'alipay' ? 1 : 2
    })
    payResult.success = true
    payResult.message = '支付成功！感谢您的购买。'
    showResult.value = true
  } catch (err) {
    payResult.success = false
    payResult.message = err.message || '支付失败，请重试'
    showResult.value = true
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  fetchOrderInfo()
})
</script>

<style scoped>
/* ===== CSS Variables ===== */
.pay-page {
  --primary: #409EFF;
  --primary-light: #ecf5ff;
  --primary-dark: #337ecc;
  --accent: #f56c6c;
  --accent-light: #fef0f0;
  --text-primary: #303133;
  --text-secondary: #909399;
  --bg-white: #ffffff;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);
  --radius-md: 8px;
  --radius-lg: 12px;
  --transition: 0.3s ease;
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

/* ===== Main Card ===== */
.pay-card {
  width: 560px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
}
.pay-card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67c23a, #f56c6c);
}

/* ===== Header Badge ===== */
.pay-header {
  padding: 32px 32px 0;
  display: flex;
  justify-content: center;
}
.pay-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--primary-light);
  color: var(--primary);
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
}
.pay-status-badge .el-icon {
  animation: clock-tick 1.5s ease-in-out infinite;
}
@keyframes clock-tick {
  0%, 100% { transform: rotate(0deg); }
  20% { transform: rotate(10deg); }
  40% { transform: rotate(-5deg); }
  60% { transform: rotate(5deg); }
  80% { transform: rotate(-2deg); }
}

/* ===== Order Info ===== */
.order-info {
  background: linear-gradient(135deg, #f8f9ff, #f0f4ff);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  margin: 24px 32px;
  border: 1px solid #e8edf5;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}
.info-row .label {
  color: var(--text-secondary);
  font-size: 14px;
}
.info-row .value {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}
.info-row .amount {
  font-size: 26px;
  font-weight: 700;
  color: var(--accent);
}

/* ===== Payment Methods ===== */
.pay-methods {
  padding: 0 32px 0;
}
.pay-methods h3 {
  font-size: 15px;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}
.methods {
  display: flex;
  gap: 16px;
}
.method-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border: 2px solid #e8e8e8;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 14px;
  color: var(--text-secondary);
  position: relative;
}
.method-item:hover {
  transform: translateY(-2px);
}
.method-alipay:hover {
  border-color: #1677FF;
  background: #f0f7ff;
}
.method-alipay.active {
  border-color: #1677FF;
  background: #e6f4ff;
  color: #1677FF;
  box-shadow: 0 4px 14px rgba(22, 119, 255, 0.18);
}
.method-wechat:hover {
  border-color: #07C160;
  background: #f0faf4;
}
.method-wechat.active {
  border-color: #07C160;
  background: #e6f9ef;
  color: #07C160;
  box-shadow: 0 4px 14px rgba(7, 193, 96, 0.18);
}
.method-check {
  position: absolute;
  top: 8px;
  right: 10px;
  font-size: 12px;
  font-weight: 700;
}

/* ===== Pay Button ===== */
.pay-action {
  display: flex;
  justify-content: center;
  padding: 28px 32px 36px;
}
.pay-btn {
  width: 100%;
  background: linear-gradient(135deg, #f56c6c 0%, #e04545 100%);
  border: none;
  font-size: 18px;
  font-weight: 700;
  padding: 18px;
  border-radius: var(--radius-md);
  letter-spacing: 2px;
  transition: all var(--transition);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.4);
  animation: pay-glow 2s ease-in-out infinite;
}
.pay-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(245, 108, 108, 0.55);
  background: linear-gradient(135deg, #f78989 0%, #e85555 100%);
}
@keyframes pay-glow {
  0%, 100% { box-shadow: 0 6px 20px rgba(245, 108, 108, 0.4); }
  50% { box-shadow: 0 6px 32px rgba(245, 108, 108, 0.6); }
}

/* ===== Result Dialog ===== */
.pay-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
}
.result-icon {
  animation: result-bounce 0.6s ease;
}
@keyframes result-bounce {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); opacity: 1; }
}
.result-text {
  margin-top: 20px;
  font-size: 16px;
  color: var(--text-primary);
  text-align: center;
}
.result-btn-outline {
  border-color: #dcdfe6;
  color: var(--text-primary);
}
.result-btn-primary {
  background: linear-gradient(135deg, #409EFF, #337ecc);
  border: none;
}
</style>
