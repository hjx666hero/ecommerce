<template>
  <div class="product-list-page">
    <!-- 页面 Hero -->
    <div class="page-hero product-hero">
      <h2>{{ route.query.keyword ? '搜索：' + route.query.keyword : '全部商品' }}</h2>
      <p>共 <strong>{{ total }}</strong> 件商品</p>
    </div>

    <div class="product-body">
      <!-- 筛选侧栏 -->
      <div class="filter-sidebar">
        <div class="filter-card">
          <div class="filter-section-title">
            <span class="filter-icon">📂</span>
            <span>商品分类</span>
          </div>
          <el-tree
            :data="categoryTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            :default-expanded-keys="expandedKeys"
            highlight-current
            @node-click="handleCategoryClick"
          />
        </div>
        <div class="filter-card">
          <div class="filter-section-title">
            <span class="filter-icon">💰</span>
            <span>价格区间</span>
          </div>
          <div class="price-range">
            <el-input v-model="minPrice" placeholder="最低价" size="small" class="price-input" />
            <span class="separator">—</span>
            <el-input v-model="maxPrice" placeholder="最高价" size="small" class="price-input" />
            <el-button type="primary" size="small" @click="applyPriceFilter" class="price-btn">确定</el-button>
          </div>
        </div>
      </div>

      <!-- 商品主区域 -->
      <div class="product-main">
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
            <el-icon v-if="item.value === 'price_asc'" class="sort-icon"><SortUp /></el-icon>
            <el-icon v-else-if="item.value === 'price_desc'" class="sort-icon"><SortDown /></el-icon>
          </span>
        </div>

        <!-- 商品网格 -->
        <div class="product-grid" v-loading="loading">
          <ProductCard v-for="item in products" :key="item.id" :product="item" />
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && products.length === 0" class="empty-state">
          <div class="empty-icon">🔍</div>
          <p class="empty-title">暂无商品</p>
          <p class="empty-desc">换个关键词或筛选条件试试吧</p>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="fetchProducts"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryTree, getProductList } from '@/api/product'
import { SortUp, SortDown } from '@element-plus/icons-vue'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const categoryTree = ref([])
const expandedKeys = ref([])
const products = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const minPrice = ref('')
const maxPrice = ref('')
const currentSort = ref('')
const currentCategoryId = ref(null)

const sortOptions = [
  { label: '综合', value: '' },
  { label: '价格升序', value: 'price_asc' },
  { label: '价格降序', value: 'price_desc' },
  { label: '销量', value: 'sales' },
  { label: '最新', value: 'newest' }
]

function handleCategoryClick(data) {
  currentCategoryId.value = data.id
  page.value = 1
  fetchProducts()
}

function handleSort(sort) {
  currentSort.value = sort
  page.value = 1
  fetchProducts()
}

function applyPriceFilter() {
  page.value = 1
  fetchProducts()
}

async function fetchCategories() {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
    if (categoryTree.value.length > 0) {
      expandedKeys.value = [categoryTree.value[0].id]
    }
  } catch {}
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    const queryCategoryId = route.query.categoryId
    if (currentCategoryId.value) {
      params.categoryId = currentCategoryId.value
    } else if (queryCategoryId) {
      params.categoryId = queryCategoryId
    }
    if (route.query.keyword) {
      params.keyword = route.query.keyword
    }
    if (minPrice.value) params.minPrice = minPrice.value
    if (maxPrice.value) params.maxPrice = maxPrice.value
    if (currentSort.value) params.sort = currentSort.value

    const res = await getProductList(params)
    products.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally {
    loading.value = false
  }
}

watch(() => route.query, () => {
  page.value = 1
  fetchProducts()
}, { deep: true })

onMounted(async () => {
  await fetchCategories()
  if (route.query.categoryId) {
    currentCategoryId.value = Number(route.query.categoryId)
  }
  fetchProducts()
})
</script>

<style scoped>
.product-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== Hero ===== */
.product-hero {
  margin-bottom: 24px;
}

.product-hero h2 strong,
.product-hero p strong {
  font-weight: 700;
}

/* ===== 主体布局 ===== */
.product-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ===== 筛选侧栏 ===== */
.filter-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 80px;
}

.filter-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.filter-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--border-light);
}

.filter-icon {
  font-size: 16px;
}

.filter-sidebar :deep(.el-tree) {
  background: transparent;
  font-size: 13px;
}

.filter-sidebar :deep(.el-tree-node__content) {
  height: 36px;
  border-radius: var(--radius-sm);
  padding: 0 8px;
  transition: all var(--transition);
}

.filter-sidebar :deep(.el-tree-node__content:hover) {
  background: var(--primary-bg);
}

.filter-sidebar :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: linear-gradient(90deg, var(--primary-bg), transparent);
  color: var(--primary);
  font-weight: 600;
}

/* ===== 价格区间 ===== */
.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.price-input {
  width: 75px;
  flex-shrink: 0;
}

.separator {
  color: var(--text-placeholder);
  font-size: 14px;
}

.price-btn {
  width: 100%;
  margin-top: 8px;
  border-radius: var(--radius-sm);
  font-weight: 500;
  letter-spacing: 1px;
}

/* ===== 商品主区域 ===== */
.product-main {
  flex: 1;
  min-width: 0;
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
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  white-space: nowrap;
  position: relative;
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

.sort-icon {
  font-size: 12px;
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
  margin-bottom: 16px;
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