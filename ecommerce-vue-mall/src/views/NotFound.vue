<template>
  <div class="not-found-page">
    <div class="not-found-card">
      <div class="error-code">404</div>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-desc">您访问的页面不存在或已被移除，请检查链接是否正确</p>
      <el-button type="primary" size="large" class="home-btn" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { WarningFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.not-found-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 600px;
  padding: 60px 0;
}

.not-found-card {
  text-align: center;
  animation: fadeInUp 0.5s ease-out;
}

.error-code {
  font-size: 140px;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -4px;
  margin-bottom: 8px;
}

.error-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.error-desc {
  font-size: 15px;
  color: var(--text-placeholder);
  margin-bottom: 32px;
  max-width: 380px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.home-btn {
  border-radius: var(--radius-sm);
  font-weight: 600;
  letter-spacing: 1px;
  padding: 14px 40px;
  font-size: 15px;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>