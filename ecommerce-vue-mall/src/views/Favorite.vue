<template>
  <div class="favorite-page">
    <div class="page-hero">
      <div class="hero-content">
        <div class="hero-icon">
          <span class="heart-emoji">❤️</span>
        </div>
        <div class="hero-text">
          <h2 class="hero-title">我的收藏</h2>
          <p class="hero-subtitle">共 <span class="count-num">{{ total }}</span> 件宝贝</p>
        </div>
      </div>
    </div>

    <div v-loading="loading">
      <template v-if="favoriteList.length > 0">
        <div class="product-grid">
          <div v-for="product in favoriteList" :key="product.id" class="product-card" @click="$router.push(`/product/${product.id}`)">
            <div class="product-image-wrapper">
              <el-image
                :src="product.mainImage || 'https://via.placeholder.com/200x200?text=No+Image'"
                fit="cover"
                class="product-image"
              />
              <div class="hover-overlay">
                <span class="overlay-text">查看详情</span>
              </div>
            </div>
            <div class="product-info">
              <p class="product-name">{{ product.name }}</p>
              <div class="product-bottom">
                <span class="product-price">¥{{ formatPrice(product.minPrice || product.price) }}</span>
                <button class="remove-btn" @click.stop="handleRemoveFavorite(product.id)" title="取消收藏">
                  <el-icon :size="18"><StarFilled /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchFavorites"
          />
        </div>
      </template>
      <div v-else class="empty-state">
        <div class="empty-icon">
          <el-icon :size="80" color="#dcdfe6"><StarFilled /></el-icon>
        </div>
        <p class="empty-text">暂无收藏</p>
        <p class="empty-hint">快去发现喜欢的宝贝吧</p>
        <el-button type="primary" size="large" round @click="$router.push('/')">去逛逛</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavoriteList, removeFavorite } from '@/api/product'
import { formatPrice } from '@/utils'
import { StarFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const favoriteList = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getFavoriteList({ page: page.value, size: pageSize.value })
    favoriteList.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally {
    loading.value = false
  }
}

async function handleRemoveFavorite(spuId) {
  try {
    await removeFavorite(spuId)
    ElMessage.success('已取消收藏')
    fetchFavorites()
  } catch {}
}

onMounted(() => {
  fetchFavorites()
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

.favorite-page {
  min-height: 400px;
}

.page-hero {
  background: linear-gradient(135deg, #fef0f0 0%, #fce4e4 50%, #fff5f5 100%);
  border-radius: var(--radius-lg);
  padding: 32px 36px;
  margin-bottom: 28px;
  box-shadow: var(--shadow-sm);
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.hero-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b6b, #f56c6c);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.3);
}

.heart-emoji {
  font-size: 30px;
  color: #fff;
}

.hero-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.hero-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
}

.count-num {
  color: var(--accent);
  font-weight: 700;
  font-size: 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
}

.product-image-wrapper {
  position: relative;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 220px;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.hover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: var(--transition);
}

.product-card:hover .hover-overlay {
  opacity: 1;
}

.overlay-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  padding: 8px 24px;
  border: 2px solid #fff;
  border-radius: 24px;
}

.product-info {
  padding: 14px 16px;
}

.product-name {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 10px;
  min-height: 38px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
}

.remove-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #fef0f0;
  color: #f56c6c;
  cursor: pointer;
  transition: var(--transition);
}

.remove-btn:hover {
  background: #f56c6c;
  color: #fff;
  transform: scale(1.1);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.empty-icon {
  margin-bottom: 20px;
  opacity: 0.5;
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
  margin-bottom: 24px;
}
</style>