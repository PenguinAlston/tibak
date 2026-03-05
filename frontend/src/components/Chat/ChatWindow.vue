<!-- src/components/Chat/ChatWindow.vue -->
<template>
  <div class="chat-window">
    <!-- 消息列表 -->
    <div class="messages-container" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <h2>开始新的对话</h2>
        <p>输入消息开始与 AI 助手交流</p>
      </div>

      <div
        v-for="(message, index) in messages"
        :key="message.id || index"
        :class="['message', message.role]"
      >
        <div class="message-avatar">
          <span v-if="message.role === 'user'">👤</span>
          <span v-else>🤖</span>
        </div>
        <div class="message-content">
          <div v-html="renderMarkdown(message.content)" class="markdown-body"></div>
        </div>
      </div>

      <!-- 加载中状态 -->
      <div v-if="isStreaming" class="message assistant">
        <div class="message-avatar">🤖</div>
        <div class="message-content">
          <span class="typing-indicator">思考中...</span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-wrapper">
        <textarea
          v-model="inputMessage"
          :disabled="isLoading"
          placeholder="输入消息... (Shift+Enter 换行)"
          @keydown.enter.exact.prevent="sendMessage"
          rows="1"
          ref="textareaRef"
        />
        <button
          @click="sendMessage"
          :disabled="!inputMessage.trim() || isLoading"
          class="send-btn"
        >
          <span>发送</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const md = new MarkdownIt({
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(str, { language: lang }).value
    }
    return str
  },
})

const chatStore = useChatStore()
const messages = computed(() => chatStore.messages)
const isLoading = computed(() => chatStore.isLoading)
const isStreaming = computed(() => chatStore.isStreaming)

const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

const renderMarkdown = (content: string) => {
  return md.render(content)
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return

  inputMessage.value = ''
  await chatStore.sendMessage(content)

  // 滚动到底部
  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听消息变化自动滚动
watch(
  () => messages.value.length,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

// 监听当前对话变化
watch(
  () => chatStore.currentConversationId,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

// 自动调整 textarea 高度
const adjustTextareaHeight = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    const scrollHeight = textareaRef.value.scrollHeight
    textareaRef.value.style.height = Math.min(scrollHeight, 200) + 'px'
  }
}

watch(inputMessage, adjustTextareaHeight)

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h2 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #666;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px;
  border-radius: 8px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.message.user {
  background: #f5f5f5;
}

.message.assistant {
  background: transparent;
}

.message-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.typing-indicator {
  color: #999;
  font-style: italic;
}

.input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  background: #fff;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  max-width: 900px;
  margin: 0 auto;
}

.input-wrapper textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 12px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  min-height: 48px;
  max-height: 200px;
  transition: border-color 0.2s;
}

.input-wrapper textarea:focus {
  outline: none;
  border-color: #6366f1;
}

.send-btn {
  padding: 12px 24px;
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #5558e3;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
