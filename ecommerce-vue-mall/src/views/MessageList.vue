<template>
  <div class="message-list-page">
    <div class="page-hero">
      <div class="hero-content">
        <div class="hero-icon">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
        </div>
        <div class="hero-info">
          <h1 class="hero-title">消息中心</h1>
          <p class="hero-desc">查看系统通知与消息提醒</p>
        </div>
      </div>
      <el-button class="mark-all-btn" type="primary" link @click="handleMarkAllRead">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
        全部标记已读
      </el-button>
    </div>

    <div v-loading="loading" class="message-content">
      <div v-if="messages.length > 0" class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message-card', { unread: !msg.isRead, read: msg.isRead }]"
          @click="handleRead(msg)"
        >
          <div class="msg-indicator">
            <span v-if="!msg.isRead" class="unread-dot"></span>
            <span v-else class="read-dot"></span>
          </div>
          <div class="msg-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
          </div>
          <div class="msg-body">
            <div class="msg-header">
              <h4 class="msg-title">{{ msg.title }}</h4>
              <span class="msg-time">{{ formatDate(msg.createTime) }}</span>
            </div>
            <p class="msg-preview">{{ msg.content }}</p>
          </div>
          <el-button
            class="msg-delete"
            type="danger"
            link
            size="small"
            @click.stop="handleDelete(msg)"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </el-button>
        </div>
      </div>
      <div v-else class="empty-state">
        <svg viewBox="0 0 120 120" width="90" height="90" fill="none">
          <rect x="18" y="22" width="84" height="68" rx="8" stroke="#d0d0d0" stroke-width="2.5"/>
          <line x1="30" y1="42" x2="90" y2="42" stroke="#e0e0e0" stroke-width="2"/>
          <line x1="30" y1="54" x2="76" y2="54" stroke="#e0e0e0" stroke-width="2"/>
          <line x1="30" y1="66" x2="66" y2="66" stroke="#e0e0e0" stroke-width="2"/>
        </svg>
        <p class="empty-text">暂无消息</p>
      </div>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchMessages"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageList, markAsRead, markAllAsRead, deleteMessage } from '@/api/message'
import { formatDate } from '@/utils'

const loading = ref(false)
const messages = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessageList({ page: page.value, pageSize: pageSize.value })
    messages.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {} finally {
    loading.value = false
  }
}

async function handleRead(msg) {
  if (msg.isRead) return
  try {
    await markAsRead(msg.id)
    msg.isRead = true
  } catch {}
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    messages.value.forEach(m => m.isRead = true)
    ElMessage.success('已全部标记为已读')
  } catch {}
}

async function handleDelete(msg) {
  try {
    await ElMessageBox.confirm('确定要删除该消息吗？', '提示', { type: 'warning' })
    await deleteMessage(msg.id)
    ElMessage.success('已删除')
    fetchMessages()
  } catch {}
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.message-list-page {
  --primary: #409eff;
  --primary-light: #ecf5ff;
  --danger: #f56c6c;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-placeholder: #909399;
  --bg-white: #fff;
  --bg-unread: #f5f9ff;
  --border-color: #ebeef5;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06);
  --radius-md: 10px;
  --radius-lg: 12px;
  --transition: all 0.25s ease;

  min-height: 400px;
}

/* Hero */
.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f0f7ff, #e8f4fd);
  border-radius: var(--radius-lg);
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px;
}

.hero-desc {
  font-size: 13px;
  color: var(--text-placeholder);
  margin: 0;
}

.mark-all-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

/* Content */
.message-content {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* Message Card */
.message-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition);
  position: relative;
}

.message-card:last-child {
  border-bottom: none;
}

.message-card:hover {
  background: #fafbfc;
}

.message-card.unread {
  background: var(--bg-unread);
}

.message-card.unread:hover {
  background: #edf4ff;
}

.msg-indicator {
  padding-top: 6px;
  flex-shrink: 0;
  width: 10px;
  display: flex;
  justify-content: center;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  display: block;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.15);
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.15); }
  50% { box-shadow: 0 0 0 6px rgba(255, 77, 79, 0.05); }
}

.read-dot {
  width: 8px;
  height: 8px;
  display: block;
  border-radius: 50%;
  background: transparent;
}

.msg-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f0f5ff;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.message-card.unread .msg-icon {
  background: var(--primary-light);
  color: var(--primary);
}

.message-card.read .msg-icon {
  background: #f5f5f5;
  color: #c0c4cc;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.msg-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.message-card.read .msg-title {
  font-weight: 400;
  color: var(--text-secondary);
}

.msg-time {
  font-size: 12px;
  color: var(--text-placeholder);
  white-space: nowrap;
  flex-shrink: 0;
}

.msg-preview {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-card.read .msg-preview {
  color: var(--text-placeholder);
}

.msg-delete {
  flex-shrink: 0;
  opacity: 0;
  transition: var(--transition);
  align-self: center;
}

.message-card:hover .msg-delete {
  opacity: 1;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 20px;
}

.empty-text {
  font-size: 15px;
  color: var(--text-placeholder);
  margin: 16px 0 0;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>