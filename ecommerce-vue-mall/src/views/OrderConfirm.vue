<template>
  <div class="order-confirm-page" v-loading="loading">
    <!-- Hero Banner -->
    <div class="confirm-hero">
      <div class="hero-content">
        <h2 class="hero-title">确认订单</h2>
        <div class="hero-steps">
          <span class="step active">1. 确认订单</span>
          <span class="step-arrow">→</span>
          <span class="step">2. 支付</span>
          <span class="step-arrow">→</span>
          <span class="step">3. 完成</span>
        </div>
      </div>
    </div>

    <!-- 收货地址 -->
    <div class="section address-section">
      <div class="section-header">
        <h3><span class="section-icon">📍</span> 收货地址</h3>
        <el-button type="primary" size="small" round @click="showAddressForm = true">+ 新增地址</el-button>
      </div>
      <div class="address-list" v-if="addresses.length > 0">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          :class="['address-card', { active: selectedAddress?.id === addr.id }]"
          @click="selectedAddress = addr"
        >
          <div class="address-card-inner">
            <div class="address-radio">
              <span class="radio-dot" v-if="selectedAddress?.id === addr.id"></span>
              <span class="radio-circle" v-else></span>
            </div>
            <div class="address-info">
              <p class="address-contact">
                <span class="name">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <span v-if="addr.isDefault" class="default-badge">默认</span>
              </p>
              <p class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
            </div>
            <el-icon v-if="selectedAddress?.id === addr.id" class="check-icon" color="#409EFF" :size="22"><CircleCheckFilled /></el-icon>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无收货地址，请添加">
        <el-button type="primary" @click="showAddressForm = true">新增地址</el-button>
      </el-empty>
    </div>

    <!-- 商品列表 -->
    <div class="section products-section">
      <h3><span class="section-icon">📦</span> 商品信息</h3>
      <div class="order-products">
        <div v-for="item in orderItems" :key="item.id" class="order-product-item">
          <div class="product-img-wrap">
            <el-image
              :src="item.spuImage || 'https://via.placeholder.com/80x80?text=No+Image'"
              fit="cover"
              class="product-img"
            />
          </div>
          <div class="product-detail">
            <p class="product-name">{{ item.spuName }}</p>
            <p class="product-spec" v-if="item.skuSpec">
              <span class="spec-badge">{{ item.skuSpec }}</span>
            </p>
          </div>
          <span class="product-price">¥{{ formatPrice(item.price) }}</span>
          <span class="product-quantity">x{{ item.quantity }}</span>
          <span class="product-subtotal">¥{{ formatPrice(item.price * item.quantity) }}</span>
        </div>
      </div>
    </div>

    <!-- 优惠券 & 备注 -->
    <div class="section coupon-section">
      <div class="coupon-row">
        <h3><span class="section-icon">🎫</span> 优惠券</h3>
        <el-select v-model="selectedCouponId" placeholder="选择优惠券" clearable class="coupon-select" popper-class="coupon-popper">
          <el-option
            v-for="coupon in availableCoupons"
            :key="coupon.id"
            :label="`${coupon.name} - ¥${formatPrice(coupon.discountValue)}`"
            :value="coupon.id"
          >
            <span>{{ coupon.name }}</span>
            <span class="coupon-discount-badge">-¥{{ formatPrice(coupon.discountValue) }}</span>
          </el-option>
        </el-select>
      </div>
    </div>

    <div class="section remark-section">
      <h3><span class="section-icon">📝</span> 订单备注</h3>
      <el-input
        v-model="remark"
        type="textarea"
        :rows="2"
        placeholder="选填：请填写备注信息"
        maxlength="200"
        show-word-limit
        class="remark-input"
      />
    </div>

    <!-- 金额汇总 & 提交 -->
    <div class="section summary-section">
      <h3><span class="section-icon">💰</span> 金额汇总</h3>
      <div class="amount-summary">
        <div class="amount-row">
          <span>商品总额</span>
          <span class="amount-val">¥{{ formatPrice(totalAmount) }}</span>
        </div>
        <div class="amount-row">
          <span>运费</span>
          <span class="amount-val">¥{{ formatPrice(freight) }}</span>
        </div>
        <div class="amount-row" v-if="couponDiscount > 0">
          <span>优惠券抵扣</span>
          <span class="discount">-¥{{ formatPrice(couponDiscount) }}</span>
        </div>
        <div class="amount-row total-row">
          <span>应付金额</span>
          <span class="final-price">¥{{ formatPrice(payAmount) }}</span>
        </div>
      </div>
      <div class="submit-area">
        <el-button
          type="danger"
          size="large"
          :loading="submitting"
          @click="handleSubmitOrder"
          class="submit-btn"
        >
          提交订单 ¥{{ formatPrice(payAmount) }}
        </el-button>
      </div>
    </div>

    <!-- 新增地址弹窗 -->
    <el-dialog v-model="showAddressForm" title="新增地址" width="520px" :close-on-click-modal="false">
      <el-form :model="addressForm" :rules="addressRules" ref="addrFormRef" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionData"
            placeholder="请选择省/市"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="街道、门牌号等（无需重复填写省市区）" maxlength="100" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressForm = false">取消</el-button>
        <el-button type="primary" :loading="addrSaving" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAddressList, addAddress } from '@/api/user'
import { getOrderConfirm, createOrder } from '@/api/order'
import { getAvailableCoupons } from '@/api/coupon'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { regionData } from '@/utils/region'
import { formatPrice } from '@/utils'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const addrSaving = ref(false)
const addrFormRef = ref(null)

const addresses = ref([])
const selectedAddress = ref(null)
const orderItems = ref([])
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const remark = ref('')
const freight = ref(0)
const couponDiscount = ref(0)
const showAddressForm = ref(false)

const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detail: '',
  isDefault: false
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const payAmount = computed(() => {
  return totalAmount.value + freight.value - couponDiscount.value
})

async function fetchData() {
  loading.value = true
  try {
    const [addrRes, confirmRes, couponRes] = await Promise.allSettled([
      getAddressList().catch(() => ({ data: [] })),
      getOrderConfirm().catch(() => ({ data: [] })),
      getAvailableCoupons().catch(() => ({ data: [] }))
    ])

    if (addrRes.status === 'fulfilled') {
      addresses.value = addrRes.value?.data || []
      // 优先选择默认地址
      if (addresses.value.length > 0) {
        const defaultAddr = addresses.value.find(a => a.isDefault === 1)
        selectedAddress.value = defaultAddr || addresses.value[0]
      }
    }

    if (confirmRes.status === 'fulfilled') {
      const confirmData = confirmRes.value?.data
      if (confirmData) {
        orderItems.value = Array.isArray(confirmData) ? confirmData : (confirmData.items || [])
      }
    }

    if (couponRes.status === 'fulfilled') {
      availableCoupons.value = couponRes.value?.data || []
    }
  } catch {} finally {
    loading.value = false
  }
}

async function saveAddress() {
  const valid = await addrFormRef.value?.validate().catch(() => false)
  if (!valid) return

  addrSaving.value = true
  try {
    const [province, city] = addressForm.region || ['', '']
    await addAddress({
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province,
      city,
      district: city,
      detail: addressForm.detail,
      isDefault: addressForm.isDefault ? 1 : 0
    })
    ElMessage.success('地址添加成功')
    showAddressForm.value = false

    // 重置表单
    addressForm.receiverName = ''
    addressForm.receiverPhone = ''
    addressForm.region = []
    addressForm.detail = ''
    addressForm.isDefault = false

    // 重新加载地址列表
    fetchData()
  } catch {} finally {
    addrSaving.value = false
  }
}

async function handleSubmitOrder() {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const res = await createOrder({
      addressId: selectedAddress.value.id,
      couponId: selectedCouponId.value,
      remark: remark.value
    })
    ElMessage.success('订单创建成功')
    router.push(`/pay/${res.data.orderNo}`)
  } catch {} finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* ===== CSS Variables ===== */
.order-confirm-page {
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
  --radius-md: 8px;
  --radius-lg: 12px;
  --transition: 0.3s ease;
  min-height: 400px;
}

/* ===== Hero Banner ===== */
.confirm-hero {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  border-radius: var(--radius-lg);
  padding: 32px 40px;
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
.hero-steps {
  display: flex;
  align-items: center;
  gap: 10px;
}
.hero-steps .step {
  font-size: 14px;
  color: rgba(255,255,255,0.7);
  font-weight: 500;
}
.hero-steps .step.active {
  color: #fff;
  font-weight: 700;
}
.hero-steps .step-arrow {
  color: rgba(255,255,255,0.5);
}

/* ===== Section ===== */
.section {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 24px 28px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.section h3 {
  font-size: 16px;
  color: var(--text-primary);
  margin: 0 0 14px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}
.section-icon {
  font-size: 16px;
}

/* ===== Address Cards ===== */
.address-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.address-card {
  border: 2px solid #e8e8e8;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
  min-width: 280px;
  flex: 1;
  max-width: 360px;
}
.address-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}
.address-card.active {
  border-color: var(--primary);
  background: var(--primary-light);
}
.address-card-inner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
}
.address-radio {
  padding-top: 2px;
  flex-shrink: 0;
}
.radio-dot {
  display: block;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--primary);
  position: relative;
}
.radio-dot::after {
  content: '';
  position: absolute;
  top: 4px;
  left: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
}
.radio-circle {
  display: block;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #d0d0d0;
}
.address-info {
  flex: 1;
  min-width: 0;
}
.address-contact {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px 0;
}
.address-contact .name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.address-contact .phone {
  font-size: 13px;
  color: var(--text-secondary);
}
.default-badge {
  font-size: 10px;
  color: #fff;
  background: var(--accent);
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 500;
}
.address-detail {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}
.check-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

/* ===== Products ===== */
.order-product-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-light);
}
.order-product-item:last-child {
  border-bottom: none;
}
.product-img-wrap {
  flex-shrink: 0;
}
.product-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}
.product-detail {
  flex: 1;
  min-width: 0;
}
.product-name {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
}
.spec-badge {
  display: inline-block;
  font-size: 11px;
  color: var(--text-secondary);
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 3px;
  margin-top: 4px;
  border: 1px solid var(--border-light);
}
.product-price {
  color: var(--accent);
  font-weight: 600;
  width: 80px;
  text-align: center;
  font-size: 14px;
}
.product-quantity {
  color: var(--text-secondary);
  width: 50px;
  text-align: center;
  font-size: 14px;
}
.product-subtotal {
  color: var(--accent);
  font-weight: 700;
  width: 80px;
  text-align: right;
  font-size: 15px;
}

/* ===== Coupon ===== */
.coupon-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.coupon-row h3 {
  margin: 0;
}
.coupon-select {
  width: 300px;
}
.coupon-discount-badge {
  float: right;
  color: var(--accent);
  font-weight: 600;
  font-size: 13px;
}
.remark-input {
  max-width: 600px;
}

/* ===== Amount Summary ===== */
.amount-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 16px 0 0 0;
  border-top: 1px dashed var(--border-light);
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

/* ===== Submit Area ===== */
.submit-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}
.submit-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #e04545 100%);
  border: none;
  font-size: 17px;
  font-weight: 600;
  padding: 16px 48px;
  border-radius: var(--radius-md);
  letter-spacing: 1px;
  transition: all var(--transition);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.35);
  animation: submit-pulse 2s ease-in-out infinite;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(245, 108, 108, 0.5);
  background: linear-gradient(135deg, #f78989 0%, #e85555 100%);
}
@keyframes submit-pulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(245, 108, 108, 0.35); }
  50% { box-shadow: 0 4px 28px rgba(245, 108, 108, 0.55); }
}
</style>
