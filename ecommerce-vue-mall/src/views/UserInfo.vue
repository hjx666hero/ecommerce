<template>
  <div class="user-info-page">
    <div class="page-header-bar">
      <h3 class="page-title">个人信息</h3>
      <p class="page-subtitle">完善您的个人信息，获得更好的体验</p>
    </div>
    <div class="info-card">
      <el-form :model="form" label-width="80px" class="info-form">
        <el-form-item label="头像" class="avatar-form-item">
          <el-upload
            class="avatar-uploader"
            action="/api/user/avatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <div class="avatar-upload-wrapper">
              <el-avatar :size="96" :src="form.avatar" class="user-avatar" />
              <div class="avatar-overlay">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
                <span>更换头像</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" size="large" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender" class="gender-group">
            <el-radio :value="1" class="gender-radio">男</el-radio>
            <el-radio :value="2" class="gender-radio">女</el-radio>
            <el-radio :value="0" class="gender-radio">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            size="large"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input :model-value="userStore.phone" disabled size="large" class="phone-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave" size="large" class="save-btn" round>保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'

const userStore = useUserStore()
const saving = ref(false)

const form = reactive({
  avatar: '',
  nickname: '',
  gender: 0,
  birthday: ''
})

onMounted(() => {
  if (userStore.userInfo) {
    form.avatar = userStore.userInfo.avatar || ''
    form.nickname = userStore.userInfo.nickname || ''
    form.gender = userStore.userInfo.gender || 0
    form.birthday = userStore.userInfo.birthday || ''
  }
})

function handleAvatarSuccess(response) {
  form.avatar = response.data?.url || ''
  ElMessage.success('头像上传成功')
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB')
    return false
  }
  return true
}

async function handleSave() {
  saving.value = true
  try {
    await updateUserInfo({
      avatar: form.avatar,
      nickname: form.nickname,
      gender: form.gender,
      birthday: form.birthday
    })
    userStore.setUserInfo({ ...userStore.userInfo, ...form })
    ElMessage.success('保存成功')
  } catch {} finally {
    saving.value = false
  }
}
</script>

<style scoped>
:root {
  --primary: #409EFF;
  --primary-light: #ecf5ff;
  --accent: #f56c6c;
  --text-primary: #303133;
  --text-secondary: #909399;
  --bg-white: #ffffff;
  --shadow-sm: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 20px rgba(0, 0, 0, 0.1);
  --radius-md: 12px;
  --radius-lg: 16px;
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-info-page {
  max-width: 640px;
}

.page-header-bar {
  margin-bottom: 28px;
}

.page-title {
  font-size: 22px;
  color: var(--text-primary);
  font-weight: 700;
  margin-bottom: 6px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.info-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 40px;
}

.avatar-form-item {
  margin-bottom: 32px;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar-upload-wrapper {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar {
  border: 3px solid #f0f2f5;
  transition: var(--transition);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: var(--transition);
  border-radius: 50%;
}

.avatar-upload-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-upload-wrapper:hover .user-avatar {
  border-color: var(--primary);
}

.gender-group {
  display: flex;
  gap: 24px;
}

.gender-radio {
  margin-right: 0 !important;
  height: 40px;
  line-height: 40px;
}

.phone-input {
  cursor: not-allowed;
}

.save-btn {
  min-width: 160px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 8px;
}
</style>