<template>
  <div class="register-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <div class="brand-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="8" y="14" width="32" height="28" rx="3" stroke="currentColor" stroke-width="2.5" fill="none"/>
            <path d="M16 14V10C16 6.68629 18.6863 4 22 4H26C29.3137 4 32 6.68629 32 10V14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            <path d="M14 24L21 31L34 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="brand-title">云创优品</h1>
        <p class="brand-subtitle">创建新账号，开启购物之旅</p>
      </div>

      <!-- 注册表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="form.code" placeholder="请输入验证码" prefix-icon="Message">
            <template #append>
              <el-button
                :disabled="smsCountdown > 0"
                @click="sendSmsCode"
                style="width: 120px;"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s后重发` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部链接 -->
      <div class="register-link">
        <span>已有账号？</span>
        <router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendSms } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const smsCountdown = ref(0)

const form = reactive({
  mobile: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const formRef = ref(null)

function sendSmsCode() {
  if (!form.mobile || !/^1[3-9]\d{9}$/.test(form.mobile)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  sendSms({ mobile: form.mobile }).then(() => {
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  }).catch(() => {})
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register({
      mobile: form.mobile,
      code: form.code,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* CSS 变量 */
.register-page {
  --primary: #6366f1;
  --primary-light: #818cf8;
  --primary-dark: #4f46e5;
  --accent: #a78bfa;
  --text-secondary: #9ca3af;
  --text-placeholder: #c0c4cc;
  --bg-white: #ffffff;
  --shadow-lg: 0 20px 60px rgba(99, 102, 241, 0.15);
  --radius-md: 12px;
  --radius-lg: 24px;
  --transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 页面容器 - 全屏渐变背景 */
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: #fff;
}

.bg-shape-1 {
  width: 500px;
  height: 500px;
  top: -200px;
  right: -150px;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  left: -80px;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  top: 40%;
  left: 10%;
}

/* 注册卡片 */
.register-card {
  width: 440px;
  max-width: 100%;
  background: var(--bg-white);
  border-radius: 20px;
  padding: 48px 44px 40px;
  box-shadow: var(--shadow-lg), 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 1;
  animation: cardFadeIn 0.6s ease-out;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 品牌区域 */
.brand-area {
  text-align: center;
  margin-bottom: 28px;
}

.brand-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(167, 139, 250, 0.1) 100%);
  border-radius: 16px;
  padding: 12px;
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 6px;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 输入框 - 药丸风格 */
.register-page :deep(.el-input__wrapper) {
  border-radius: var(--radius-lg);
  box-shadow: 0 0 0 1px #e5e7eb inset;
  padding: 2px 16px;
  transition: box-shadow var(--transition), border-color var(--transition);
}

.register-page :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light) inset;
}

.register-page :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2) inset;
}

.register-page :deep(.el-input__inner) {
  font-size: 15px;
  height: 44px;
  line-height: 44px;
}

.register-page :deep(.el-input__prefix) {
  color: var(--text-secondary);
  font-size: 18px;
  transition: color var(--transition);
}

.register-page :deep(.el-input.is-focus .el-input__prefix) {
  color: var(--primary);
}

.register-page :deep(.el-input-group__append) {
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

.register-page :deep(.el-input-group__append .el-button) {
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  height: 100%;
  border: none;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.06), rgba(167, 139, 250, 0.06));
  color: var(--primary);
  font-weight: 500;
  font-size: 13px;
}

.register-page :deep(.el-input-group__append .el-button.is-disabled) {
  background: #f9fafb;
  color: var(--text-secondary);
}

.register-page :deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 4px;
  border-radius: var(--radius-lg);
  border: none;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  transition: opacity var(--transition), transform var(--transition);
  margin-top: 4px;
}

.register-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.3);
}

.register-btn:active {
  transform: translateY(0);
}

/* 底部链接 */
.register-link {
  text-align: center;
  margin-top: 28px;
  font-size: 13px;
  color: var(--text-secondary);
}

.register-link a {
  color: var(--primary);
  margin-left: 4px;
  transition: color var(--transition);
  text-decoration: none;
}

.register-link a:hover {
  color: var(--accent);
}
</style>
