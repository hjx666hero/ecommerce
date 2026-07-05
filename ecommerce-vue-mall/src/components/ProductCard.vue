<template>
  <div class="product-card" @click="goDetail">
    <div class="product-image">
      <div class="image-inner">
        <el-image :src="product.mainImage || product.image || 'https://via.placeholder.com/220x220?text=No+Image'" fit="cover" lazy>
          <template #error>
            <div class="image-error">
              <el-icon :size="40"><PictureFilled /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
      <div v-if="product.seckillPrice" class="tag-seckill">秒杀</div>
      <div class="image-overlay">
        <span class="overlay-btn">查看详情</span>
      </div>
    </div>
    <div class="product-info">
      <h4 class="product-name">{{ product.name }}</h4>
      <div class="product-tags">
        <span class="tag-item tag-sales">已售{{ product.sales || 0 }}</span>
        <span v-if="product.storeName" class="tag-item tag-store">{{ product.storeName }}</span>
      </div>
      <div class="product-price">
        <span class="price-symbol">¥</span>
        <span class="current-price">{{ formatPrice(product.minPrice || product.price || product.seckillPrice) }}</span>
        <span v-if="product.maxPrice && product.maxPrice > product.minPrice" class="price-range">起</span>
        <span v-if="product.originalPrice && product.originalPrice > (product.minPrice || product.price)" class="original-price">¥{{ formatPrice(product.originalPrice) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { PictureFilled } from '@element-plus/icons-vue'
import { formatPrice } from '@/utils'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

function goDetail() {
  if (props.product.id) {
    router.push(`/product/${props.product.id}`)
  }
}
</script>

<style scoped>
:root {
  --primary: #e1251b;
  --primary-light: #fff1f0;
  --text-primary: #222;
  --text-secondary: #666;
  --text-placeholder: #999;
  --bg-white: #fff;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.15);
  --radius-md: 10px;
  --transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.product-card {
  --primary: #e1251b;
  --primary-light: #fff1f0;
  --text-primary: #222;
  --text-secondary: #666;
  --text-placeholder: #999;
  --bg-white: #fff;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.15);
  --radius-md: 10px;
  --transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);

  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
}

.product-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f7f7f7;
}

.image-inner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.image-inner .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.45s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.product-card:hover .image-inner .el-image {
  transform: scale(1.05);
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #d0d0d0;
  background: #f7f7f7;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.45));
  padding: 24px 12px 10px;
  display: flex;
  justify-content: center;
  opacity: 0;
  transform: translateY(8px);
  transition: var(--transition);
}

.product-card:hover .image-overlay {
  opacity: 1;
  transform: translateY(0);
}

.overlay-btn {
  color: #fff;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 20px;
  padding: 4px 18px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.tag-seckill {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 4px;
  z-index: 2;
  letter-spacing: 1px;
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.35);
}

.product-info {
  padding: 12px 14px 14px;
}

.product-name {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.45;
  height: 38px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
  font-weight: 400;
}

.product-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.tag-item {
  font-size: 11px;
  color: var(--text-placeholder);
  display: flex;
  align-items: center;
  gap: 3px;
}

.tag-sales {
  color: #ff6b35;
  background: #fff7f0;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 500;
}

.tag-store {
  padding: 2px 6px;
  background: #f5f5f5;
  border-radius: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80px;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-symbol {
  font-size: 13px;
  font-weight: 700;
  color: #e1251b;
  margin-right: 1px;
}

.current-price {
  font-size: 22px;
  font-weight: 800;
  color: #e1251b;
  line-height: 1;
  letter-spacing: -0.5px;
}

.price-range {
  font-size: 11px;
  color: #e1251b;
  margin-left: 2px;
}

.original-price {
  font-size: 12px;
  color: var(--text-placeholder);
  text-decoration: line-through;
  margin-left: 8px;
}
</style>