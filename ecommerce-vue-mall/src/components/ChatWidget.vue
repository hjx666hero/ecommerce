<template>
  <div class="chat-widget-container">
    <!-- 悬浮按钮 -->
    <transition name="btn-fade">
      <button v-if="!visible" class="chat-fab" @click="openChat">
        <el-icon :size="26"><ChatDotRound /></el-icon>
      </button>
    </transition>

    <!-- 聊天窗口 -->
    <transition name="panel-slide">
      <div v-if="visible" class="chat-panel">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="chat-header-left">
            <span class="chat-header-avatar">🤖</span>
            <span class="chat-header-title">智能客服</span>
            <span class="chat-status">在线</span>
          </div>
          <button class="chat-close-btn" @click="closeChat">
            <el-icon :size="18"><Close /></el-icon>
          </button>
        </div>

        <!-- 消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <!-- 空状态欢迎语 -->
          <div v-if="messages.length === 0" class="chat-empty">
            <div class="empty-avatar">🤖</div>
            <div class="empty-text">您好！我是拼夕夕智能客服助手，请问有什么可以帮您？</div>
            <div class="empty-suggestions">
              <span
                v-for="item in defaultSuggestions"
                :key="item"
                class="suggestion-tag"
                @click="handleSuggestionClick(item)"
              >{{ item }}</span>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.role === 'user' ? 'message-right' : 'message-left'"
          >
            <div class="message-avatar" v-if="msg.role === 'assistant'">🤖</div>
            <div class="message-bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-assistant'">
              {{ msg.content }}
            </div>
            <div class="message-avatar" v-if="msg.role === 'user'">😊</div>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="message-item message-left">
            <div class="message-avatar">🤖</div>
            <div class="message-bubble bubble-assistant bubble-loading">
              <span class="loading-dot"></span>
              <span class="loading-dot"></span>
              <span class="loading-dot"></span>
            </div>
          </div>
        </div>

        <!-- 快捷建议 -->
        <div v-if="currentSuggestions.length > 0 && messages.length > 0" class="chat-suggestions">
          <span
            v-for="item in currentSuggestions"
            :key="item"
            class="suggestion-tag"
            @click="handleSuggestionClick(item)"
          >{{ item }}</span>
        </div>

        <!-- 底部输入区 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            placeholder="请输入您的问题..."
            class="chat-input"
            @keyup.enter="handleSend"
            :disabled="loading"
          >
            <template #suffix>
              <el-button
                class="send-btn"
                :disabled="!inputText.trim() || loading"
                @click="handleSend"
                :loading="loading"
              >
                <span v-if="!loading">发送</span>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Close } from '@element-plus/icons-vue'
import { sendMessage } from '@/api/customerService'

const visible = ref(false)
const loading = ref(false)
const inputText = ref('')
const sessionId = ref(null)
const messages = ref([])
const currentSuggestions = ref([])
const messagesContainer = ref(null)

const defaultSuggestions = ['查物流', '退换货', '订单咨询']

function openChat() {
  visible.value = true
  nextTick(() => {
    scrollToBottom()
  })
}

function closeChat() {
  visible.value = false
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function handleSuggestionClick(text) {
  inputText.value = text
  handleSend()
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  currentSuggestions.value = []
  scrollToBottom()

  loading.value = true

  try {
    const res = await sendMessage(text, sessionId.value)
    messages.value.push({ role: 'assistant', content: res.data.reply })
    if (res.data.sessionId) {
      sessionId.value = res.data.sessionId
    }
    if (res.data.suggestions && res.data.suggestions.length > 0) {
      currentSuggestions.value = res.data.suggestions
    }
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，我暂时无法回复您的问题，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

watch(visible, (val) => {
  if (val) {
    scrollToBottom()
  }
})
</script>

<style scoped>
.chat-widget-container {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
}

/* ===== 悬浮按钮 ===== */
.chat-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #e1251b;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 16px rgba(225, 37, 27, 0.45);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.chat-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(225, 37, 27, 0.55);
}

.chat-fab:active {
  transform: scale(0.96);
}

/* 按钮淡入淡出 */
.btn-fade-enter-active,
.btn-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.btn-fade-enter-from,
.btn-fade-leave-to {
  opacity: 0;
  transform: scale(0.6);
}

/* ===== 聊天窗口 ===== */
.chat-panel {
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 面板滑入动画 */
.panel-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.panel-slide-leave-active {
  transition: all 0.2s ease-in;
}

.panel-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.95);
}

/* ===== 头部 ===== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #e1251b, #ff4d4f);
  color: #fff;
  flex-shrink: 0;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-header-avatar {
  font-size: 22px;
}

.chat-header-title {
  font-size: 16px;
  font-weight: 600;
}

.chat-status {
  font-size: 11px;
  background: rgba(255, 255, 255, 0.25);
  padding: 2px 8px;
  border-radius: 10px;
}

.chat-close-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.chat-close-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* ===== 消息区域 ===== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-messages::-webkit-scrollbar {
  width: 5px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #dcdcdc;
  border-radius: 10px;
}

/* ===== 空状态 ===== */
.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
  text-align: center;
}

.empty-avatar {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 20px;
  max-width: 280px;
}

.empty-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

/* ===== 消息项 ===== */
.message-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 85%;
}

.message-right {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-left {
  align-self: flex-start;
}

.message-avatar {
  font-size: 20px;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
}

/* ===== 气泡 ===== */
.message-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.bubble-user {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bubble-assistant {
  background: #fff;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* 加载动画气泡 */
.bubble-loading {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 18px;
}

.loading-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #909399;
  animation: dot-bounce 1.4s ease-in-out infinite both;
}

.loading-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dot:nth-child(2) {
  animation-delay: -0.16s;
}

.loading-dot:nth-child(3) {
  animation-delay: 0s;
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ===== 快捷建议 ===== */
.chat-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 16px;
  background: #f5f6fa;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.suggestion-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  color: #e1251b;
  background: rgba(225, 37, 27, 0.08);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  user-select: none;
}

.suggestion-tag:hover {
  background: #e1251b;
  color: #fff;
}

/* ===== 输入区域 ===== */
.chat-input-area {
  padding: 10px 14px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-right: 0;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.chat-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.chat-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #e1251b inset;
}

.chat-input :deep(.el-input__inner) {
  font-size: 14px;
}

.chat-input :deep(.el-input__suffix) {
  display: flex;
  align-items: center;
}

.send-btn {
  height: 32px;
  border-radius: 16px;
  background: #e1251b;
  border-color: #e1251b;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  padding: 0 18px;
  margin-right: 2px;
  transition: opacity 0.2s;
}

.send-btn:hover:not(.is-disabled) {
  background: #c11c14;
  border-color: #c11c14;
  color: #fff;
}

.send-btn.is-disabled,
.send-btn.is-disabled:hover {
  background: #e1251b;
  border-color: #e1251b;
  color: #fff;
  opacity: 0.5;
}
</style>
