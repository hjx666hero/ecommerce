<template>
  <div class="product-detail-page" v-loading="loading">
    <template v-if="product">
      <!-- 返回按钮 -->
      <div class="back-nav">
        <el-button :icon="ArrowLeft" @click="$router.back()" text>返回</el-button>
      </div>
      <!-- Hero 横幅 -->
      <div class="product-hero">
        <div class="hero-inner">
          <div class="hero-breadcrumb">
            <span class="hero-category">商品详情</span>
          </div>
          <h1 class="hero-title">{{ product.name }}</h1>
          <p class="hero-desc" v-if="product.description">{{ product.description }}</p>
        </div>
      </div>

      <div class="product-top">
        <!-- 商品图片 -->
        <div class="product-images">
          <div class="image-gallery">
            <el-image
              :src="product.mainImage || 'https://via.placeholder.com/450x450?text=No+Image'"
              fit="cover"
              :preview-src-list="productImages"
              class="main-image"
            />
          </div>
          <div class="thumb-list" v-if="product.images && product.images.length > 1">
            <img
              v-for="(img, idx) in product.images"
              :key="idx"
              :src="img"
              :class="['thumb', { active: currentImage === img }]"
              @click="currentImage = img"
            />
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-desc" v-if="product.description">{{ product.description }}</p>

          <!-- 闪购价格卡片 -->
          <div class="price-card">
            <div class="price-card-tag">
              <span class="flash-icon">⚡</span> 闪购价
            </div>
            <div class="price-card-main">
              <span class="current-price">
                <span class="price-symbol">¥</span>{{ formatPrice(product.minPrice || product.price) }}
              </span>
              <span v-if="product.maxPrice && product.maxPrice > product.minPrice" class="price-range">
                - ¥{{ formatPrice(product.maxPrice) }}
              </span>
            </div>
            <div class="price-card-extra">
              <span v-if="product.originalPrice" class="original-price">
                原价 ¥{{ formatPrice(product.originalPrice) }}
              </span>
            </div>
          </div>

          <div class="sales-info">
            <span class="sales-item">销量 <strong>{{ product.sales || 0 }}</strong></span>
            <span class="sales-divider">|</span>
            <span class="sales-item">库存 <strong>{{ product.stock || 0 }}</strong></span>
          </div>

          <!-- SKU选择器 -->
          <div v-if="skuGroups.length > 0" class="sku-section">
            <div class="sku-section-title">选择规格</div>
            <div class="sku-selector-wrapper">
              <SkuSelector
                :sku-groups="skuGroups"
                :sku-list="skuList"
                :selected="selectedSku"
                @update:selected="onSkuChange"
                @change="onSkuChange"
              />
            </div>
          </div>

          <!-- 数量选择 -->
          <div class="quantity-section">
            <span class="qty-label">数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="selectedSkuObj?.stock || product.stock || 99"
              size="large"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button class="btn-add-cart" size="large" @click="handleAddToCart" :loading="cartLoading">
              <span class="btn-icon">🛒</span> 加入购物车
            </el-button>
            <el-button class="btn-buy-now" size="large" @click="handleBuyNow" :loading="buyLoading">
              <span class="btn-icon">⚡</span> 立即购买
            </el-button>
            <el-button
              class="btn-favorite"
              :class="{ 'is-favorited': isFavorited }"
              size="large"
              :icon="Star"
              @click="handleToggleFavorite"
              :loading="favLoading"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情 & 评价 -->
      <div class="product-bottom">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content" v-html="product.detail || '暂无详情'"></div>
          </el-tab-pane>
          <el-tab-pane label="商品评价" name="reviews">
            <div class="review-section">
              <div v-if="reviews.length > 0">
                <div v-for="review in reviews" :key="review.id" class="review-card">
                  <div class="review-header">
                    <el-avatar :size="40" :src="review.avatar" class="review-avatar" />
                    <div class="review-user-info">
                      <span class="review-user">{{ review.nickname || '匿名用户' }}</span>
                      <el-rate :model-value="review.rating" disabled size="small" />
                    </div>
                    <span class="review-time">{{ formatDate(review.createTime) }}</span>
                  </div>
                  <p class="review-content">{{ review.content }}</p>
                  <div v-if="review.images && review.images.length > 0" class="review-images">
                    <el-image
                      v-for="(img, idx) in review.images"
                      :key="idx"
                      :src="img"
                      fit="cover"
                      style="width: 80px; height: 80px; border-radius: 4px; margin-right: 8px;"
                      :preview-src-list="review.images"
                    />
                  </div>
                  <div v-if="review.reply" class="review-reply">
                    <span class="reply-label">商家回复</span>
                    <span class="reply-text">{{ review.reply }}</span>
                  </div>
                </div>
                <el-pagination
                  v-if="reviewTotal > 10"
                  v-model:current-page="reviewPage"
                  :page-size="10"
                  :total="reviewTotal"
                  layout="prev, pager, next"
                  small
                  @current-change="fetchReviews"
                  style="margin-top:16px;justify-content:center"
                />
              </div>
              <el-empty v-else description="暂无评价，快来第一个评价吧" />

              <!-- 写评价 -->
              <div v-if="userStore.isLoggedIn" class="write-review-card">
                <div class="write-review-title">写评价</div>
                <div class="review-form">
                  <div class="rating-row">
                    <span class="rating-label">评分</span>
                    <el-rate v-model="reviewForm.rating" />
                  </div>
                  <el-input
                    v-model="reviewForm.content"
                    type="textarea"
                    :rows="3"
                    placeholder="分享你的使用体验..."
                    maxlength="500"
                    show-word-limit
                  />
                  <el-button
                    type="primary"
                    :loading="reviewSubmitting"
                    @click="submitReview"
                    class="btn-submit-review"
                  >
                    提交评价
                  </el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
    <el-empty v-else description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, getProductReviews, addProductReview, addFavorite, removeFavorite, checkFavorite } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { formatPrice, formatDate } from '@/utils'
import SkuSelector from '@/components/SkuSelector.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const product = ref(null)
const currentImage = ref('')
const quantity = ref(1)
const activeTab = ref('detail')
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewSubmitting = ref(false)
const reviewForm = reactive({ rating: 5, content: '' })
const cartLoading = ref(false)
const buyLoading = ref(false)
const favLoading = ref(false)
const isFavorited = ref(false)

const skuGroups = ref([])
const skuList = ref([])
const selectedSku = ref({})
const selectedSkuObj = ref(null)

const productImages = computed(() => {
  if (!product.value) return []
  return [product.value.mainImage, ...(product.value.images || [])].filter(Boolean)
})

function onSkuChange(selected) {
  selectedSku.value = selected
  const specs = Object.values(selected).filter(v => v)
  if (specs.length > 0) {
    selectedSkuObj.value = skuList.value.find(sku => {
      if (!sku.specs) return false
      return specs.every(v => sku.specs.includes(v))
    }) || null
  } else {
    selectedSkuObj.value = null
  }
}

async function fetchProduct() {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
    if (product.value) {
      currentImage.value = product.value.mainImage || ''
      if (product.value.skuGroups) skuGroups.value = product.value.skuGroups
      if (product.value.skuList) skuList.value = product.value.skuList
    }
    fetchReviews()
    fetchFavoriteStatus()
  } catch {} finally {
    loading.value = false
  }
}

async function fetchFavoriteStatus() {
  try {
    const res = await checkFavorite(route.params.id)
    isFavorited.value = res.data === true
  } catch {}
}

async function fetchReviews() {
  try {
    const res = await getProductReviews(route.params.id, { page: reviewPage.value, size: 10 })
    reviews.value = res.data?.records || res.data?.list || []
    reviewTotal.value = res.data?.total || 0
  } catch {}
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await addProductReview({
      spuId: Number(route.params.id),
      rating: reviewForm.rating,
      content: reviewForm.content
    })
    ElMessage.success('评价提交成功')
    reviewForm.content = ''
    reviewForm.rating = 5
    reviewPage.value = 1
    fetchReviews()
  } catch {} finally {
    reviewSubmitting.value = false
  }
}

async function handleAddToCart() {
  if (!product.value) return
  cartLoading.value = true
  try {
    await addToCart({
      productId: product.value.id,
      skuId: selectedSkuObj.value?.id,
      quantity: quantity.value
    })
    ElMessage.success('已加入购物车')
    cartStore.increment()
  } catch {} finally {
    cartLoading.value = false
  }
}

function handleBuyNow() {
  if (!product.value) return
  buyLoading.value = true
  addToCart({
    productId: product.value.id,
    skuId: selectedSkuObj.value?.id,
    quantity: quantity.value
  }).then(() => {
    router.push('/cart')
  }).catch(() => {}).finally(() => {
    buyLoading.value = false
  })
}

async function handleToggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  favLoading.value = true
  try {
    if (isFavorited.value) {
      await removeFavorite(product.value.id)
      ElMessage.success('已取消收藏')
      isFavorited.value = false
    } else {
      await addFavorite(product.value.id)
      ElMessage.success('已收藏')
      isFavorited.value = true
    }
  } catch {} finally {
    favLoading.value = false
  }
}

onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
/* =========================== 页面容器 =========================== */
.product-detail-page {
  min-height: 500px;
  padding-bottom: 40px;
}

/* =========================== Hero 横幅 =========================== */
.product-hero {
  background: linear-gradient(135deg, #FF4757 0%, #FC6621 100%);
  border-radius: var(--radius-lg);
  padding: 32px 40px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  color: #fff;
}

.product-hero::before {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 180px;
  height: 180px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.product-hero::after {
  content: '';
  position: absolute;
  right: 100px;
  bottom: -30px;
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 50%;
}

.hero-inner {
  position: relative;
  z-index: 1;
}

.hero-breadcrumb {
  font-size: 13px;
  opacity: 0.8;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.hero-category {
  letter-spacing: 1px;
}

.hero-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 6px;
  line-height: 1.35;
}

.hero-desc {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
  line-height: 1.5;
}

/* =========================== 商品主区域 =========================== */
.product-top {
  display: flex;
  gap: 30px;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

/* =========================== 图片区域 =========================== */
.product-images {
  width: 450px;
  flex-shrink: 0;
}

.image-gallery {
  width: 450px;
  height: 450px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-base);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition);
  background: #fafafa;
}

.image-gallery:hover {
  box-shadow: var(--shadow-md);
}

.main-image {
  width: 100%;
  height: 100%;
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-gallery:hover .main-image :deep(img) {
  transform: scale(1.06);
}

.thumb-list {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.thumb {
  width: 64px;
  height: 64px;
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  object-fit: cover;
  transition: border-color var(--transition), transform var(--transition);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.thumb:hover {
  border-color: var(--primary-light);
  transform: translateY(-2px);
}

.thumb.active {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(255, 71, 87, 0.2);
}

/* =========================== 商品信息 =========================== */
.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin: 0 0 6px;
}

.product-desc {
  font-size: 13px;
  color: var(--text-placeholder);
  margin: 0 0 16px;
  line-height: 1.5;
}

/* =========================== 价格卡片 =========================== */
.price-card {
  background: linear-gradient(135deg, #FF4757 0%, #FF6B81 100%);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}

.price-card::after {
  content: '';
  position: absolute;
  right: -20px;
  top: -20px;
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.price-card-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 10px;
  border-radius: 20px;
  margin-bottom: 10px;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

.flash-icon {
  font-size: 13px;
}

.price-card-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.current-price {
  font-size: 34px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: -1px;
}

.price-symbol {
  font-size: 20px;
  font-weight: 600;
  margin-right: 2px;
}

.price-range {
  font-size: 18px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
}

.price-card-extra {
  position: relative;
  z-index: 1;
  margin-top: 6px;
}

.original-price {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: line-through;
}

/* =========================== 销量库存 =========================== */
.sales-info {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  padding: 10px 14px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.sales-item strong {
  color: var(--text-primary);
  font-weight: 600;
  margin-left: 4px;
}

.sales-divider {
  color: var(--border-base);
}

/* =========================== SKU 选择器 =========================== */
.sku-section {
  margin-bottom: 20px;
  padding: 14px 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
}

.sku-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.sku-selector-wrapper {
  /* 通过 :deep 穿透 SkuSelector 内部样式 */
}

.sku-selector-wrapper :deep(.sku-group-label) {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.sku-selector-wrapper :deep(.sku-option) {
  padding: 6px 18px;
  border: 1.5px solid var(--border-base);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition);
  user-select: none;
  background: var(--bg-white);
}

.sku-selector-wrapper :deep(.sku-option:hover:not(.is-disabled)) {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-bg, #FFF0F2);
}

.sku-selector-wrapper :deep(.sku-option.is-selected) {
  border-color: var(--primary);
  background: var(--primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.sku-selector-wrapper :deep(.sku-option.is-disabled) {
  border-color: var(--border-light);
  color: var(--text-disabled, #c0c4cc);
  cursor: not-allowed;
  background: #fafafa;
}

.sku-selector-wrapper :deep(.sku-info) {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed var(--border-base);
}

.sku-selector-wrapper :deep(.sku-price) {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
  margin-right: 16px;
}

.sku-selector-wrapper :deep(.sku-stock) {
  font-size: 13px;
  color: var(--text-placeholder);
}

/* =========================== 数量选择 =========================== */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.qty-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* =========================== 操作按钮 =========================== */
.action-buttons {
  display: flex;
  gap: 14px;
  align-items: center;
}

.btn-add-cart {
  flex: 0 0 auto;
  height: 48px !important;
  padding: 0 28px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-sm) !important;
  background: linear-gradient(135deg, #FC6621, #FF8C5A) !important;
  border: none !important;
  color: #fff !important;
  letter-spacing: 1px !important;
  transition: all var(--transition) !important;
  box-shadow: 0 4px 14px rgba(252, 102, 33, 0.35) !important;
}

.btn-add-cart:hover {
  background: linear-gradient(135deg, #E55A18, #FC6621) !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(252, 102, 33, 0.45) !important;
}

.btn-add-cart:active {
  transform: translateY(0);
}

.btn-buy-now {
  flex: 0 0 auto;
  height: 48px !important;
  padding: 0 28px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-sm) !important;
  background: linear-gradient(135deg, #FF4757, #FF6B81) !important;
  border: none !important;
  color: #fff !important;
  letter-spacing: 1px !important;
  transition: all var(--transition) !important;
  box-shadow: 0 4px 14px rgba(255, 71, 87, 0.35) !important;
}

.btn-buy-now:hover {
  background: linear-gradient(135deg, #E0333F, #FF4757) !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(255, 71, 87, 0.45) !important;
}

.btn-buy-now:active {
  transform: translateY(0);
}

.btn-icon {
  margin-right: 4px;
  font-size: 16px;
}

.btn-favorite {
  flex: 0 0 auto;
  height: 48px !important;
  padding: 0 24px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  border-radius: var(--radius-sm) !important;
  background: var(--bg-white) !important;
  border: 1.5px solid var(--border-base) !important;
  color: var(--text-secondary) !important;
  transition: all var(--transition) !important;
}

.btn-favorite:hover {
  border-color: var(--primary-light) !important;
  color: var(--primary) !important;
  background: var(--primary-bg, #FFF0F2) !important;
}

.btn-favorite.is-favorited {
  border-color: var(--warning, #FFA502) !important;
  color: var(--warning, #FFA502) !important;
  background: #FFF9F0 !important;
}

/* =========================== 底部详情/评价 =========================== */
.product-bottom {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 8px 24px 24px;
  box-shadow: var(--shadow-sm);
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 4px;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--border-light);
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
  color: var(--text-secondary);
  transition: color var(--transition);
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 600;
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, var(--primary), var(--accent));
  height: 3px;
  border-radius: 2px;
}

.detail-content {
  padding: 24px 0;
  line-height: 1.9;
  color: var(--text-secondary);
  font-size: 14px;
}

/* =========================== 评价区域 =========================== */
.review-section {
  padding-top: 8px;
}

.review-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  margin-bottom: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: box-shadow var(--transition);
}

.review-card:hover {
  box-shadow: var(--shadow-md);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.review-avatar {
  flex-shrink: 0;
  border: 2px solid var(--border-light);
}

.review-user-info {
  flex: 1;
  min-width: 0;
}

.review-user {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  display: block;
  margin-bottom: 2px;
}

.review-time {
  font-size: 12px;
  color: var(--text-placeholder);
  flex-shrink: 0;
}

.review-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 10px;
  padding-left: 2px;
}

.review-images {
  display: flex;
  margin-bottom: 4px;
}

.review-reply {
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  margin-top: 10px;
  border-left: 3px solid var(--primary-light);
  display: flex;
  gap: 8px;
}

.reply-label {
  color: var(--primary);
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
}

.reply-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* =========================== 写评价 =========================== */
.write-review-card {
  margin-top: 24px;
  background: var(--bg-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
}

.write-review-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.btn-submit-review {
  align-self: flex-start;
  padding: 0 28px !important;
  height: 40px !important;
  font-weight: 500 !important;
  letter-spacing: 0.5px !important;
}

.back-nav {
  margin-bottom: 12px;
}

.back-nav .el-button {
  font-size: 15px;
  color: #606266;
  padding: 0;
  height: auto;
}

.back-nav .el-button:hover {
  color: var(--primary);
}
</style>
