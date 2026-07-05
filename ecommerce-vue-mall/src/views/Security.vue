<template>
  <div class="security-page">
    <div class="security-hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
        </svg>
      </div>
      <div class="hero-text">
        <h2 class="hero-title">账户安全</h2>
        <p class="hero-desc">定期修改密码有助于保护您的账户安全</p>
      </div>
    </div>

    <div class="security-status">
      <span class="status-badge status-safe">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
        安全等级：高
      </span>
    </div>

    <div class="security-card">
      <div class="card-header">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
        <span>修改密码</span>
      </div>
      <div class="card-body">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="security-form">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="form.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit" size="large" class="submit-btn">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功')
    Object.assign(form, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
.security-page {
  --primary: #409eff;
  --primary-light: #ecf5ff;
  --success: #67c23a;
  --success-light: #f0f9eb;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-placeholder: #909399;
  --bg-white: #fff;
  --bg-card: #fff;
  --border-color: #e4e7ed;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --radius-md: 10px;
  --radius-lg: 12px;
  --transition: all 0.3s ease;

  max-width: 560px;
}

.security-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #f0f7ff, #e8f4fd);
  border-radius: var(--radius-lg);
}

.hero-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hero-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.hero-desc {
  font-size: 13px;
  color: var(--text-placeholder);
  margin: 0;
}

.security-status {
  margin-bottom: 20px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 14px;
  border-radius: 20px;
}

.status-safe {
  background: var(--success-light);
  color: var(--success);
}

.status-safe svg {
  color: var(--success);
}

.security-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
  background: #fafbfc;
}

.card-header svg {
  color: var(--primary);
}

.card-body {
  padding: 28px 24px 12px;
}

.security-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

.security-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  transition: var(--transition);
}

.security-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.security-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary) inset;
}

.submit-btn {
  width: 100%;
  border-radius: 8px;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
}
</style>