<template>
  <div class="forgot-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <!-- 忘记密码卡片 -->
    <div class="forgot-card">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <div class="brand-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="8" y="14" width="32" height="28" rx="3" stroke="currentColor" stroke-width="2.5" fill="none"/>
            <path d="M16 14V10C16 6.68629 18.6863 4 22 4H26C29.3137 4 32 6.68629 32 10V14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            <path d="M14 24L21 31L34 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="brand-title">拼夕夕</h1>
        <p class="brand-subtitle">忘记密码</p>
      </div>

      <!-- 步骤条 -->
      <el-steps :active="step" align-center class="steps">
        <el-step title="验证手机号" />
        <el-step title="设置新密码" />
        <el-step title="完成" />
      </el-steps>

      <!-- Step 1: 验证码 -->
      <el-form v-if="step === 0" ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="80px" class="form">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入注册手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码" />
            <el-button :disabled="codeCountdown > 0" @click="sendCode" style="margin-left:12px;white-space:nowrap">
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="verifying" @click="verifyCode" style="width:100%">下一步</el-button>
        </el-form-item>
      </el-form>

      <!-- Step 2: 新密码 -->
      <el-form v-if="step === 1" ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="form">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" placeholder="请输入新密码（6-20位）" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="resetting" @click="resetPassword" style="width:100%">重置密码</el-button>
        </el-form-item>
      </el-form>

      <!-- Step 3: 完成 -->
      <div v-if="step === 2" class="success">
        <div class="success-icon">
          <el-icon :size="48" color="#fff"><CircleCheckFilled /></el-icon>
        </div>
        <p class="success-title">密码重置成功</p>
        <p class="success-desc">您现在可以使用新密码登录了</p>
        <el-button type="primary" size="large" @click="$router.push('/login')" style="margin-top:20px">去登录</el-button>
      </div>

      <!-- 返回链接 -->
      <div class="back-link">
        <router-link to="/login">← 返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import request from '@/api/request'

const step = ref(0)
const verifying = ref(false)
const resetting = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const phoneForm = reactive({ phone: '', code: '' })
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const phoneFormRef = ref(null)

const pwdForm = reactive({ password: '', confirmPassword: '' })
const pwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const pwdFormRef = ref(null)

async function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    await request({ url: '/auth/send-code', method: 'post', data: { phone: phoneForm.phone } })
    ElMessage.success('验证码已发送')
    codeCountdown.value = 60
    countdownTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch {}
}

async function verifyCode() {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  verifying.value = true
  try {
    // 验证码不在此处校验，直接进入下一步，重置密码时后端会校验
    step.value = 1
  } catch {} finally {
    verifying.value = false
  }
}

async function resetPassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try {
    await request({
      url: '/auth/reset-password',
      method: 'post',
      data: {
        phone: phoneForm.phone,
        verifyCode: phoneForm.code,
        newPassword: pwdForm.password
      }
    })
    ElMessage.success('密码重置成功')
    step.value = 2
  } catch {} finally {
    resetting.value = false
  }
}
</script>

<style scoped>
/* CSS 变量 */
.forgot-page {
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
.forgot-page {
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

/* 卡片 */
.forgot-card {
  width: 500px;
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
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(167, 139, 250, 0.1) 100%);
  border-radius: 14px;
  padding: 10px;
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 22px;
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

/* 步骤条 */
.steps {
  margin-bottom: 32px;
  padding: 0 8px;
}

.steps :deep(.el-step__title) {
  font-size: 13px;
  font-weight: 500;
}

.steps :deep(.el-step__head.is-finish .el-step__icon) {
  background: var(--primary);
  border-color: var(--primary);
}

.steps :deep(.el-step__head.is-process .el-step__icon) {
  background: var(--primary);
  border-color: var(--primary);
}

.steps :deep(.el-step__title.is-process) {
  color: var(--primary);
  font-weight: 600;
}

.steps :deep(.el-step__title.is-finish) {
  color: var(--primary);
}

/* 表单 */
.form {
  margin-top: 4px;
}

/* 输入框 - 药丸风格 */
.forgot-page :deep(.el-input__wrapper) {
  border-radius: var(--radius-lg);
  box-shadow: 0 0 0 1px #e5e7eb inset;
  padding: 2px 16px;
  transition: box-shadow var(--transition), border-color var(--transition);
}

.forgot-page :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light) inset;
}

.forgot-page :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2) inset;
}

.forgot-page :deep(.el-input__inner) {
  font-size: 15px;
  height: 44px;
  line-height: 44px;
}

.forgot-page :deep(.el-form-item) {
  margin-bottom: 20px;
}

.forgot-page :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

/* 表单内按钮 */
.forgot-page :deep(.el-button--primary) {
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  border-radius: var(--radius-lg);
  border: none;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  transition: opacity var(--transition), transform var(--transition);
}

.forgot-page :deep(.el-button--primary):hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.3);
}

.forgot-page :deep(.el-button--primary):active {
  transform: translateY(0);
}

/* 验证码行 */
.code-row {
  display: flex;
  width: 100%;
}

.code-row .el-button {
  height: 44px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.06), rgba(167, 139, 250, 0.06));
  color: var(--primary);
  font-weight: 500;
  font-size: 13px;
  border: none;
}

.code-row .el-button.is-disabled {
  background: #f9fafb;
  color: var(--text-secondary);
}

/* 成功步骤 */
.success {
  text-align: center;
  padding: 40px 0 32px;
  animation: successFadeIn 0.5s ease-out;
}

@keyframes successFadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981, #34d399);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: successPulse 0.6s ease-out;
  box-shadow: 0 8px 30px rgba(16, 185, 129, 0.3);
}

@keyframes successPulse {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px;
}

.success-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 返回链接 */
.back-link {
  text-align: center;
  margin-top: 24px;
}

.back-link a {
  color: var(--primary);
  font-size: 14px;
  transition: color var(--transition);
  text-decoration: none;
}

.back-link a:hover {
  color: var(--accent);
}
</style>
