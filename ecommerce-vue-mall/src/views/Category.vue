<template>
  <div class="category-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <el-button :icon="ArrowLeft" @click="$router.back()" text>返回</el-button>
    </div>

    <!-- 页面顶部 Hero -->
    <div class="page-hero category-hero">
      <h2>商品分类</h2>
      <p>探索您感兴趣的商品品类</p>
    </div>

    <div class="category-body">
      <!-- 左侧分类树 -->
      <div class="category-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-icon">☰</span>
          <span>全部分类</span>
          <span class="sidebar-count">{{ categories.length }} 个分类</span>
        </div>
        <el-menu
          :default-active="String(currentCategoryId)"
          @select="handleSelect"
        >
          <el-menu-item
            v-for="cat in categories"
            :key="cat.id"
            :index="String(cat.id)"
          >
            <span class="menu-label">{{ cat.name }}</span>
            <el-icon v-if="cat.children && cat.children.length" class="menu-arrow"><ArrowRight /></el-icon>
          </el-menu-item>
        </el-menu>
        <el-empty v-if="!loading && categories.length === 0" description="暂无分类" :image-size="60" />
      </div>

      <!-- 右侧内容区 -->
      <div class="category-main">
        <!-- 当前分类标题 -->
        <div class="category-header" v-if="currentCategory">
          <h3>{{ currentCategory.name }}</h3>
          <span class="product-count">共 {{ total }} 件商品</span>
        </div>

        <!-- 子分类筛选 -->
        <div v-if="subCategories.length > 0" class="sub-categories">
          <span class="sub-label">子分类：</span>
          <span
            v-for="sub in subCategories"
            :key="sub.id"
            :class="['sub-cat-item', { active: subCategoryId === sub.id }]"
            @click="subCategoryId = sub.id"
          >
            {{ sub.name }}
          </span>
        </div>

        <!-- 分类横幅 -->
        <div class="category-banner">
          <img
            :src="currentCategory?.banner || 'https://via.placeholder.com/880x200?text=Category+Banner'"
            alt="分类横幅"
          />
        </div>

        <!-- 商品网格 -->
        <div class="product-grid" v-if="products.length > 0">
          <ProductCard v-for="item in products" :key="item.id" :product="item" />
        </div>
        <el-empty v-else-if="!loading" description="该分类下暂无商品" :image-size="80" />

        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchProducts"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategoryTree, getProductList } from '@/api/product'
import { ArrowRight, ArrowLeft } from '@element-plus/icons-vue'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const loading = ref(false)
const categories = ref([])
const currentCategoryId = ref(Number(route.params.id) || 0)
const subCategoryId = ref(0)
const products = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const currentCategory = computed(() => {
  return categories.value.find(c => c.id === currentCategoryId.value) || null
})

const subCategories = computed(() => {
  return currentCategory.value?.children || []
})

watch(() => route.params.id, (val) => {
  currentCategoryId.value = Number(val) || 0
  subCategoryId.value = 0
  page.value = 1
  fetchProducts()
})

function handleSelect(index) {
  currentCategoryId.value = Number(index)
  subCategoryId.value = 0
  page.value = 1
  fetchProducts()
}

async function fetchCategories() {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
    if (categories.value.length > 0 && !currentCategoryId.value) {
      currentCategoryId.value = categories.value[0].id
    }
  } catch {}
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      categoryId: currentCategoryId.value || undefined
    }
    if (subCategoryId.value) {
      params.categoryId = subCategoryId.value
    }
    const res = await getProductList(params)
    products.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.category-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== 返回按钮 ===== */
.back-nav {
  margin-bottom: 8px;
}

.back-nav :deep(.el-button) {
  font-size: 15px;
  color: #606266;
  padding: 0;
  height: auto;
}

.back-nav :deep(.el-button:hover) {
  color: var(--primary);
}

/* ===== Hero 区域 ===== */
.category-hero {
  margin-bottom: 24px;
}

/* ===== 主体两栏布局 ===== */
.category-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ===== 左侧分类侧栏 ===== */
.category-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  position: sticky;
  top: 80px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--border-light);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.sidebar-icon {
  font-size: 16px;
  color: var(--primary);
}

.sidebar-count {
  margin-left: auto;
  font-size: 12px;
  font-weight: 400;
  color: var(--text-placeholder);
  background: var(--bg-page);
  padding: 2px 8px;
  border-radius: 10px;
}

.category-sidebar :deep(.el-menu) {
  border-right: none !important;
  padding: 4px 0;
}

.category-sidebar :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  padding: 0 16px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  transition: all var(--transition);
}

.category-sidebar :deep(.el-menu-item:hover) {
  color: var(--primary);
  background: var(--primary-bg);
}

.category-sidebar :deep(.el-menu-item.is-active) {
  color: var(--primary) !important;
  background: linear-gradient(90deg, var(--primary-bg), transparent);
  font-weight: 600;
  border-left: 3px solid var(--primary);
  padding-left: 13px !important;
}

.menu-label {
  flex: 1;
}

.menu-arrow {
  font-size: 12px;
  color: var(--text-placeholder);
  transition: transform var(--transition);
}

.category-sidebar :deep(.el-menu-item:hover) .menu-arrow {
  color: var(--primary);
  transform: translateX(3px);
}

/* ===== 右侧主区域 ===== */
.category-main {
  flex: 1;
  min-width: 0;
}

/* ===== 分类标题栏 ===== */
.category-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
  padding: 0 4px;
}

.category-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.product-count {
  font-size: 13px;
  color: var(--text-placeholder);
}

/* ===== 子分类筛选 ===== */
.sub-categories {
  background: var(--bg-white);
  border-radius: var(--radius-sm);
  padding: 10px 16px;
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  box-shadow: var(--shadow-sm);
}

.sub-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  margin-right: 4px;
}

.sub-cat-item {
  padding: 5px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  background: var(--bg-page);
  transition: all var(--transition);
  border: 1px solid transparent;
}

.sub-cat-item:hover {
  color: var(--primary);
  background: var(--primary-bg);
  border-color: var(--primary-light);
}

.sub-cat-item.active {
  color: #fff;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-color: transparent;
  font-weight: 500;
}

/* ===== 分类横幅 ===== */
.category-banner {
  margin-bottom: 20px;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.category-banner img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

/* ===== 商品网格 ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  animation: fadeIn 0.4s ease-out;
}

/* ===== 分页 ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px 0;
}

/* Empty 定制 */
.category-main :deep(.el-empty) {
  padding: 60px 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>