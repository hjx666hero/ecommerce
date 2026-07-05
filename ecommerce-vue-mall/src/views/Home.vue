<template>
  <div class="home-page">
    <!-- Hero 横幅区 -->
    <section class="page-hero home-hero fade-in-up">
      <div class="hero-content">
        <h2>✨ 拼夕夕</h2>
        <p>精选好物 · 品质生活 · 尽在拼夕夕</p>
        <div class="hero-tags">
          <span>全场包邮</span>
          <span>七天退换</span>
          <span>正品保证</span>
          <span>极速物流</span>
        </div>
      </div>
    </section>

    <!-- 轮播图 -->
    <section class="home-section fade-in-up">
      <el-skeleton :loading="loading" animated>
        <template #template>
          <div class="banner-skeleton" />
        </template>
        <template #default>
          <el-carousel :interval="4000" type="card" height="400px" v-if="banners.length > 0">
            <el-carousel-item v-for="item in banners" :key="item.id">
              <img :src="item.image" :alt="item.title" class="banner-img" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="banner-placeholder">
            <div class="placeholder-icon-wrapper">
              <el-icon :size="60"><PictureFilled /></el-icon>
            </div>
            <p>暂无轮播图</p>
          </div>
        </template>
      </el-skeleton>
    </section>

    <!-- 分类入口 -->
    <section class="home-section fade-in-up">
      <div class="section-header">
        <h2 class="section-title">商品分类</h2>
        <router-link to="/categories" class="more-link">查看全部分类 →</router-link>
      </div>
      <el-skeleton :loading="loading" animated :count="8">
        <template #template>
          <div class="category-grid">
            <div v-for="i in 8" :key="i" class="category-item-skeleton">
              <el-skeleton-item variant="circle" style="width: 72px; height: 72px;" />
              <el-skeleton-item variant="text" style="width: 60px; margin-top: 10px;" />
            </div>
          </div>
        </template>
        <template #default>
          <div class="category-grid-modern" v-if="categories.length > 0">
            <div
              v-for="(item, index) in categories"
              :key="item.id"
              :class="['category-card', `cat-color-${index % 6}`]"
              @click="$router.push(`/category/${item.id}`)"
            >
              <div class="cat-card-icon">
                <span class="cat-emoji">{{ categoryIcons[index] || '🛍️' }}</span>
              </div>
              <div class="cat-card-info">
                <span class="cat-name">{{ item.name }}</span>
                <span class="cat-count">{{ item.productCount || 20 }}+ 商品</span>
              </div>
              <el-icon class="cat-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
          <el-empty v-else description="暂无分类" />
        </template>
      </el-skeleton>
    </section>

    <!-- 推荐商品 -->
    <section class="home-section fade-in-up">
      <div class="section-header">
        <h2 class="section-title">🔥 为你推荐</h2>
        <router-link to="/products?sort=recommend" class="more-link">查看更多 →</router-link>
      </div>
      <el-skeleton :loading="loading" animated :count="4">
        <template #template>
          <div class="product-grid-modern">
            <div v-for="i in 4" :key="i" class="product-skeleton-card">
              <el-skeleton-item variant="image" style="width: 100%; height: 220px;" />
              <el-skeleton-item variant="text" style="width: 80%; margin-top: 12px;" />
              <el-skeleton-item variant="text" style="width: 50%; margin-top: 8px;" />
            </div>
          </div>
        </template>
        <template #default>
          <div class="product-grid-modern" v-if="recommendProducts.length > 0">
            <ProductCard v-for="item in recommendProducts" :key="item.id" :product="item" />
          </div>
          <el-empty v-else description="暂无推荐商品">
            <template #image>
              <div class="empty-placeholder-shape" />
            </template>
          </el-empty>
        </template>
      </el-skeleton>
    </section>

    <!-- 热门商品 -->
    <section class="home-section fade-in-up">
      <div class="section-header">
        <h2 class="section-title">🏆 热门商品</h2>
        <router-link to="/products?sort=sales" class="more-link">查看更多 →</router-link>
      </div>
      <el-skeleton :loading="loading" animated :count="4">
        <template #template>
          <div class="product-grid-modern">
            <div v-for="i in 4" :key="i" class="product-skeleton-card">
              <el-skeleton-item variant="image" style="width: 100%; height: 220px;" />
              <el-skeleton-item variant="text" style="width: 80%; margin-top: 12px;" />
              <el-skeleton-item variant="text" style="width: 50%; margin-top: 8px;" />
            </div>
          </div>
        </template>
        <template #default>
          <div class="product-grid-modern" v-if="hotProducts.length > 0">
            <ProductCard v-for="item in hotProducts" :key="item.id" :product="item" />
          </div>
          <el-empty v-else description="暂无热门商品">
            <template #image>
              <div class="empty-placeholder-shape" />
            </template>
          </el-empty>
        </template>
      </el-skeleton>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeData } from '@/api/home'
import { getCategoryList } from '@/api/product'
import { PictureFilled, ArrowRight } from '@element-plus/icons-vue'
import ProductCard from '@/components/ProductCard.vue'

const categoryIcons = ['📱', '💻', '🏠', '👗', '🍜', '💄', '📚', '⚽']

const loading = ref(true)
const banners = ref([])
const categories = ref([])
const recommendProducts = ref([])
const hotProducts = ref([])

onMounted(async () => {
  try {
    const [homeRes, catRes] = await Promise.allSettled([
      getHomeData().catch(() => ({ data: null })),
      getCategoryList().catch(() => ({ data: [] }))
    ])

    // 轮播图和推荐/热卖从首页接口获取
    if (homeRes.status === 'fulfilled' && homeRes.value?.data) {
      const d = homeRes.value.data
      banners.value = d.carousels || d.banners || []
      recommendProducts.value = d.newProducts || d.recommendProducts || []
      hotProducts.value = d.hotProducts || []
    }

    // 分类始终从 category/tree 接口获取
    if (catRes.status === 'fulfilled') {
      categories.value = catRes.value?.data || []
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-page {
  padding: 0;
}

/* ---- Hero 区定制 ---- */
.home-hero {
  margin-bottom: 24px;
}

.home-hero h2 {
  font-size: 32px !important;
  margin-bottom: 8px !important;
}

.home-hero p {
  font-size: 16px !important;
  margin-bottom: 16px !important;
}

.hero-tags {
  display: flex;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.hero-tags span {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

/* ---- 区块通用 ---- */
.home-section {
  margin-bottom: 32px;
}

/* ---- 轮播图 ---- */
.banner-skeleton {
  height: 400px;
  background: var(--border-light);
  border-radius: var(--radius-lg);
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.banner-placeholder {
  height: 400px;
  background: linear-gradient(135deg, #fafafa 0%, #f0f0f0 100%);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.banner-placeholder::before {
  content: '';
  position: absolute;
  right: -40px;
  bottom: -40px;
  width: 160px;
  height: 160px;
  background: var(--border-light);
  border-radius: 50%;
}

.placeholder-icon-wrapper {
  color: var(--text-placeholder);
  position: relative;
  z-index: 1;
}

.banner-placeholder p {
  margin-top: 14px;
  font-size: 14px;
  color: var(--text-placeholder);
  position: relative;
  z-index: 1;
}

/* ---- 区块标题 ---- */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.more-link {
  font-size: 14px;
  color: var(--text-placeholder);
  text-decoration: none;
  transition: color var(--transition);
}

.more-link:hover {
  color: var(--primary);
}

/* ---- 分类网格 ---- */
/* ===== 分类卡片 (新版) ===== */
.category-grid-modern {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.category-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 18px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--shadow-sm);
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
}

.category-card::after {
  content: '';
  position: absolute;
  top: -30px;
  right: -30px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  opacity: 0.06;
  transition: all var(--transition);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-base);
}

.category-card:hover::after {
  transform: scale(1.5);
}

.cat-card-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 26px;
  transition: transform var(--transition);
}

.category-card:hover .cat-card-icon {
  transform: scale(1.1);
}

.cat-card-info {
  flex: 1;
  min-width: 0;
}

.cat-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.3;
}

.cat-count {
  font-size: 12px;
  color: var(--text-placeholder);
}

.cat-arrow {
  color: #ccc;
  font-size: 14px;
  transition: all var(--transition);
  flex-shrink: 0;
}

.category-card:hover .cat-arrow {
  color: var(--primary);
  transform: translateX(3px);
}

/* 各分类卡片颜色 */
.cat-color-0 .cat-card-icon { background: #FFF0F2; }
.cat-color-1 .cat-card-icon { background: #EDF4FF; }
.cat-color-2 .cat-card-icon { background: #FFF8E8; }
.cat-color-3 .cat-card-icon { background: #E8F8E8; }
.cat-color-4 .cat-card-icon { background: #FFF0E8; }
.cat-color-5 .cat-card-icon { background: #F3E8FF; }

.cat-color-0::after { background: #FF4757; }
.cat-color-1::after { background: #3742FA; }
.cat-color-2::after { background: #FFA502; }
.cat-color-3::after { background: #2ED573; }
.cat-color-4::after { background: #FC6621; }
.cat-color-5::after { background: #9B59B6; }

/* ---- 商品骨架屏卡片 ---- */
.product-skeleton-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 12px;
  box-shadow: var(--shadow-sm);
}

/* ---- 空状态装饰形状 ---- */
.empty-placeholder-shape {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--border-light), var(--bg-page));
  position: relative;
}

.empty-placeholder-shape::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--border-base);
}
</style>