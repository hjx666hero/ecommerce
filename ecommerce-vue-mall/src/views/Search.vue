<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <div class="search-hero">
      <div class="search-hero-content">
        <h2>
          搜索
          <span v-if="keyword" class="keyword-highlight">"{{ keyword }}"</span>
        </h2>
        <p v-if="keyword">共找到 <strong>{{ total }}</strong> 件相关商品</p>
      </div>
      <!-- 重新搜索栏 -->
      <form class="search-bar-large" @submit.prevent="(e) => { const v = e.target.querySelector('input').value; if(v) $router.push({ path: '/search', query: { keyword: v } }) }">
        <div class="search-input-wrap">
          <svg class="search-icon-svg" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            :value="keyword"
            placeholder="搜索您想要的商品..."
            class="search-input-native"
            autocomplete="off"
          />
        </div>
        <button type="submit" class="search-submit-btn">搜索</button>
      </form>
    </div>

    <!-- 排序栏 -->
    <div class="sort-bar">
      <span class="sort-label">排序：</span>
      <span
        v-for="item in sortOptions"
        :key="item.value"
        :class="['sort-pill', { active: currentSort === item.value }]"
        @click="handleSort(item.value)"
      >
        {{ item.label }}
      </span>
    </div>

    <!-- 商品网格 -->
    <div class="product-grid" v-loading="loading">
      <ProductCard v-for="item in products" :key="item.id" :product="item" />
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && products.length === 0" class="empty-state">
      <div class="empty-icon">🔍</div>
      <p class="empty-title">没有找到相关商品</p>
      <p class="empty-desc">试试其他关键词，或浏览商品分类发现更多好物</p>
      <el-button type="primary" class="empty-btn" @click="$router.push('/category')">浏览分类</el-button>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="doSearch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchProducts } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()

const loading = ref(false)
const keyword = ref('')
const products = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const currentSort = ref('')

const sortOptions = [
  { label: '综合', value: '' },
  { label: '价格升序', value: 'price_asc' },
  { label: '价格降序', value: 'price_desc' },
  { label: '销量', value: 'sales' },
  { label: '最新', value: 'newest' }
]

function handleSort(sort) {
  currentSort.value = sort
  page.value = 1
  doSearch()
}

async function doSearch() {
  keyword.value = route.query.keyword || ''
  if (!keyword.value) return
  loading.value = true
  try {
    const res = await searchProducts({
      keyword: keyword.value,
      page: page.value,
      pageSize: pageSize.value,
      sort: currentSort.value || undefined
    })
    products.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally {
    loading.value = false
  }
}

watch(() => route.query.keyword, () => {
  page.value = 1
  doSearch()
})

onMounted(() => {
  doSearch()
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 400px;
}

/* ===== 搜索头部 ===== */
.search-hero {
  background: linear-gradient(135deg, var(--primary), var(--accent));
  border-radius: var(--radius-lg);
  padding: 32px 40px;
  margin-bottom: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.search-hero::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 180px;
  height: 180px;
  background: rgba(255,255,255,0.08);
  border-radius: 50%;
}

.search-hero::before {
  content: '';
  position: absolute;
  right: 80px;
  bottom: -30px;
  width: 100px;
  height: 100px;
  background: rgba(255,255,255,0.05);
  border-radius: 50%;
}

.search-hero-content {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}

.search-hero-content h2 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 6px;
}

.keyword-highlight {
  color: #FFEAA7;
  font-weight: 700;
}

.search-hero-content p {
  font-size: 14px;
  opacity: 0.85;
}

.search-hero-content p strong {
  font-weight: 700;
}

/* ===== 大型搜索栏 ===== */
.search-bar-large {
  display: flex;
  position: relative;
  z-index: 1;
  max-width: 560px;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
  padding: 0 16px;
  transition: background var(--transition);
}

.search-input-wrap:focus-within {
  background: #fff;
}

.search-icon-svg {
  color: var(--text-placeholder);
  flex-shrink: 0;
}

.search-input-native {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: var(--text-primary);
  padding: 14px 12px;
  line-height: 1.4;
}

.search-input-native::placeholder {
  color: var(--text-placeholder);
}

.search-submit-btn {
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  transition: all var(--transition);
  white-space: nowrap;
  backdrop-filter: blur(4px);
}

.search-submit-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* ===== 排序栏 ===== */
.sort-bar {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 6px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: var(--shadow-sm);
}

.sort-label {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 0 12px;
  flex-shrink: 0;
}

.sort-pill {
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all var(--transition);
  font-weight: 500;
  white-space: nowrap;
}

.sort-pill:hover {
  color: var(--primary);
  background: var(--primary-bg);
}

.sort-pill.active {
  color: #fff;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

/* ===== 商品网格 ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  animation: fadeInUp 0.4s ease-out;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 0;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--text-placeholder);
  margin-bottom: 24px;
}

.empty-btn {
  border-radius: var(--radius-sm);
  font-weight: 500;
  letter-spacing: 1px;
}

/* ===== 分页 ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 24px 0;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>