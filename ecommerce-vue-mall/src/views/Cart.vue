<template>
  <div class="cart-page">
    <!-- Hero Banner -->
    <div class="cart-hero">
      <div class="hero-content">
        <h2 class="hero-title">🛒 我的购物车</h2>
        <p class="hero-subtitle">选中心仪好物，一键结算</p>
      </div>
      <div class="hero-decor"></div>
    </div>

    <template v-if="cartList.length > 0">
      <div class="cart-table-wrapper">
        <el-table
          :data="cartList"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          ref="tableRef"
          row-key="id"
          class="cart-table"
          :header-cell-style="{ background: '#fafafa', color: 'var(--text-primary)', fontWeight: '600', fontSize: '14px', borderBottom: '2px solid var(--border-light)' }"
          :cell-style="{ borderBottom: '1px solid var(--border-light)' }"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column label="商品信息" min-width="400">
            <template #default="{ row }">
              <div class="cart-product">
                <div class="cart-product-img-wrap">
                  <el-image
                    :src="row.spuImage || 'https://via.placeholder.com/80x80?text=No+Image'"
                    fit="cover"
                    class="cart-product-img"
                  />
                </div>
                <div class="cart-product-info">
                  <p class="cart-product-name">{{ row.spuName }}</p>
                  <p class="cart-product-spec" v-if="row.skuSpec">
                    <span class="spec-tag">{{ row.skuSpec }}</span>
                  </p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              <span class="price">¥{{ formatPrice(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.stock || 99"
                size="small"
                @change="handleQuantityChange(row)"
                class="qty-input"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="price subtotal">¥{{ formatPrice(row.price * row.quantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="danger" size="small" link @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Sticky Footer -->
      <div class="cart-footer">
        <div class="cart-footer-inner">
          <div class="cart-footer-left">
            <el-checkbox v-model="isAllSelected" @change="handleSelectAll" class="select-all-check">全选</el-checkbox>
            <el-button type="danger" link @click="handleClear" class="clear-btn">清空购物车</el-button>
          </div>
          <div class="cart-footer-right">
            <span class="selected-count">已选 <em>{{ selectedItems.length }}</em> 件</span>
            <span class="total-price">
              合计：<em>¥{{ formatPrice(totalPrice) }}</em>
            </span>
            <el-button
              type="danger"
              size="large"
              :disabled="selectedItems.length === 0"
              @click="handleCheckout"
              class="checkout-btn"
            >
              去结算 ¥{{ formatPrice(totalPrice) }}
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty State -->
    <div v-else class="cart-empty">
      <div class="empty-icon">
        <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="60" cy="60" r="56" stroke="#e0e0e0" stroke-width="2" stroke-dasharray="6 4"/>
          <path d="M35 45h50l-5 35H40l-5-35z" stroke="#ccc" stroke-width="2" fill="none"/>
          <path d="M42 50V38a18 18 0 0136 0v12" stroke="#ccc" stroke-width="2" fill="none" stroke-linecap="round"/>
          <circle cx="55" cy="70" r="3" fill="#ddd"/>
          <circle cx="65" cy="70" r="3" fill="#ddd"/>
        </svg>
      </div>
      <p class="empty-title">购物车是空的</p>
      <p class="empty-desc">快去挑选心仪的商品吧~</p>
      <el-button type="primary" size="large" round class="go-shopping-btn" @click="$router.push('/')">
        去逛逛
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartList, updateCartItem, deleteCartItem, selectAllCart, clearCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { formatPrice } from '@/utils'

const router = useRouter()
const cartStore = useCartStore()

const cartList = ref([])
const selectedItems = ref([])
const tableRef = ref(null)
const isAllSelected = ref(false)

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + item.price * item.quantity
  }, 0)
})

function handleSelectionChange(val) {
  selectedItems.value = val
  isAllSelected.value = val.length === cartList.value.length && cartList.value.length > 0
}

function handleSelectAll(val) {
  if (val) {
    cartList.value.forEach(row => {
      tableRef.value?.toggleRowSelection(row, true)
    })
  } else {
    tableRef.value?.clearSelection()
  }
}

async function fetchCartList() {
  try {
    const res = await getCartList()
    cartList.value = res.data || []
    cartStore.setCount(cartList.value.length)
    // 默认全选
    setTimeout(() => {
      cartList.value.forEach(row => {
        tableRef.value?.toggleRowSelection(row, true)
      })
    }, 100)
  } catch {}
}

async function handleQuantityChange(row) {
  try {
    await updateCartItem(row.id, { quantity: row.quantity })
  } catch {
    fetchCartList()
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    await deleteCartItem(row.id)
    ElMessage.success('已删除')
    fetchCartList()
  } catch {}
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', { type: 'warning' })
    await clearCart()
    ElMessage.success('购物车已清空')
    cartStore.clear()
    cartList.value = []
  } catch {}
}

function handleCheckout() {
  router.push('/order/confirm')
}

onMounted(() => {
  fetchCartList()
})
</script>

<style scoped>
/* ===== CSS Variables ===== */
.cart-page {
  --primary: #409EFF;
  --primary-light: #ecf5ff;
  --primary-dark: #337ecc;
  --accent: #f56c6c;
  --accent-light: #fef0f0;
  --text-primary: #303133;
  --text-secondary: #909399;
  --bg-white: #ffffff;
  --bg-light: #fafafa;
  --border-light: #f0f0f0;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.1);
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --transition: 0.3s ease;
  min-height: 400px;
}

/* ===== Hero Banner ===== */
.cart-hero {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: var(--radius-lg);
  padding: 36px 40px;
  margin-bottom: 24px;
  overflow: hidden;
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px 0;
}
.hero-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}
.hero-decor {
  position: absolute;
  top: -30px;
  right: -20px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

/* ===== Table Wrapper ===== */
.cart-table-wrapper {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.cart-table {
  --el-table-border-color: transparent;
}

/* ===== Product Column ===== */
.cart-product {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 4px 0;
}
.cart-product-img-wrap {
  flex-shrink: 0;
}
.cart-product-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}
.cart-product-info {
  min-width: 0;
}
.cart-product-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin: 0;
}
.spec-tag {
  display: inline-block;
  font-size: 11px;
  color: var(--text-secondary);
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 3px;
  margin-top: 4px;
  border: 1px solid var(--border-light);
}

/* ===== Price / Quantity ===== */
.price {
  color: var(--accent);
  font-weight: 600;
  font-size: 14px;
}
.subtotal {
  font-size: 16px;
  font-weight: 700;
}
.qty-input {
  width: 120px;
}

/* ===== Zebra Striping ===== */
:deep(.cart-table .el-table__row--striped) {
  background: #fafafa;
}

/* ===== Sticky Footer ===== */
.cart-footer {
  position: sticky;
  bottom: 0;
  z-index: 10;
  margin-top: 16px;
}
.cart-footer-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 16px 24px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}
.cart-footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.select-all-check {
  font-size: 14px;
  color: var(--text-primary);
}
.clear-btn {
  font-size: 13px;
}
.cart-footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.selected-count {
  font-size: 14px;
  color: var(--text-secondary);
}
.selected-count em {
  font-style: normal;
  color: var(--accent);
  font-weight: 600;
}
.total-price {
  font-size: 14px;
  color: var(--text-secondary);
}
.total-price em {
  font-style: normal;
  font-size: 22px;
  color: var(--accent);
  font-weight: 700;
}
.checkout-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #e04545 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  padding: 14px 36px;
  border-radius: var(--radius-md);
  letter-spacing: 1px;
  transition: all var(--transition);
  box-shadow: 0 4px 14px rgba(245, 108, 108, 0.35);
}
.checkout-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.45);
  background: linear-gradient(135deg, #f78989 0%, #e85555 100%);
}
.checkout-btn:disabled {
  background: #c0c4cc;
  box-shadow: none;
}

/* ===== Empty State ===== */
.cart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.empty-icon svg {
  width: 120px;
  height: 120px;
  opacity: 0.7;
}
.empty-title {
  font-size: 18px;
  color: var(--text-primary);
  margin: 20px 0 8px 0;
  font-weight: 500;
}
.empty-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px 0;
}
.go-shopping-btn {
  padding: 12px 40px;
  font-size: 15px;
}
</style>
