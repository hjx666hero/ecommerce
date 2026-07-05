<template>
  <div class="sku-selector">
    <div v-for="group in skuGroups" :key="group.name" class="sku-group">
      <div class="sku-group-label">{{ group.name }}：</div>
      <div class="sku-group-options">
        <span
          v-for="option in group.options"
          :key="option.value"
          :class="['sku-option', {
            'is-selected': isSelected(group.name, option.value),
            'is-disabled': isDisabled(group.name, option.value)
          }]"
          @click="handleSelect(group.name, option.value)"
        >
          {{ option.label }}
        </span>
      </div>
    </div>
    <div v-if="selectedSku" class="sku-info">
      <span class="sku-price">¥{{ formatPrice(selectedSku.price) }}</span>
      <span class="sku-stock">库存：{{ selectedSku.stock }}件</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { formatPrice } from '@/utils'

const props = defineProps({
  skuGroups: {
    type: Array,
    default: () => []
  },
  skuList: {
    type: Array,
    default: () => []
  },
  selected: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:selected', 'change'])

const localSelected = ref({ ...props.selected })

function isSelected(groupName, optionValue) {
  return localSelected.value[groupName] === optionValue
}

function isDisabled(groupName, optionValue) {
  // 简单实现：如果没有选中任何规格，全部可选
  if (Object.keys(localSelected.value).length === 0) return false
  // 检查该选项是否与已选规格匹配
  const testSelected = { ...localSelected.value, [groupName]: optionValue }
  const specValues = Object.values(testSelected).filter(v => v)
  if (specValues.length === 0) return false
  return !props.skuList.some(sku => {
    if (!sku.specs) return false
    return specValues.every(v => sku.specs.includes(v))
  })
}

function handleSelect(groupName, optionValue) {
  if (isDisabled(groupName, optionValue)) return
  const newSelected = { ...localSelected.value }
  if (newSelected[groupName] === optionValue) {
    delete newSelected[groupName]
  } else {
    newSelected[groupName] = optionValue
  }
  localSelected.value = newSelected
  emit('update:selected', newSelected)
  emit('change', newSelected)
}

const selectedSku = computed(() => {
  const specValues = Object.values(localSelected.value).filter(v => v)
  if (specValues.length === 0) return null
  return props.skuList.find(sku => {
    if (!sku.specs) return false
    return specValues.every(v => sku.specs.includes(v))
  }) || null
})
</script>

<style scoped>
.sku-selector {
  padding: 0;
}

.sku-group {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.sku-group-label {
  font-size: 13px;
  color: #999;
  line-height: 30px;
  min-width: 50px;
  flex-shrink: 0;
}

.sku-group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sku-option {
  display: inline-block;
  padding: 4px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
}

.sku-option:hover:not(.is-disabled) {
  border-color: #409EFF;
  color: #409EFF;
}

.sku-option.is-selected {
  border-color: #409EFF;
  background: #409EFF;
  color: #fff;
}

.sku-option.is-disabled {
  border-color: #eee;
  color: #ccc;
  cursor: not-allowed;
  background: #fafafa;
}

.sku-info {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #eee;
}

.sku-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
  margin-right: 16px;
}

.sku-stock {
  font-size: 13px;
  color: #999;
}
</style>