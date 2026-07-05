import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartCount } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  const hasItems = computed(() => count.value > 0)

  async function fetchCartCount() {
    try {
      const res = await getCartCount()
      count.value = res.data || 0
    } catch {
      // ignore
    }
  }

  function setCount(val) {
    count.value = val
  }

  function increment() {
    count.value++
  }

  function decrement() {
    if (count.value > 0) count.value--
  }

  function clear() {
    count.value = 0
  }

  return {
    count,
    hasItems,
    fetchCartCount,
    setCount,
    increment,
    decrement,
    clear
  }
})