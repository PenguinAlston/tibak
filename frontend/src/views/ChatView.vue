<!-- src/views/ChatView.vue -->
<template>
  <div class="chat-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <button class="new-chat-btn" @click="createNewConversation">
          <span>+</span>
          <span v-if="!sidebarCollapsed">新建对话</span>
        </button>
        <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          {{ sidebarCollapsed ? '▶' : '◀' }}
        </button>
      </div>

      <div class="conversation-list">
        <div
          v-for="conv in chatStore.conversations"
          :key="conv.id"
          :class="['conversation-item', { active: conv.id === chatStore.currentConversationId }]"
          @click="selectConversation(conv.id)"
        >
          <span class="conversation-title">{{ conv.title }}</span>
          <button class="delete-btn" @click.stop="deleteConversation(conv.id)">×</button>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="user-info" @click="showSettings = true">
          <span class="avatar">{{ authStore.user?.username?.[0]?.toUpperCase() || 'U' }}</span>
          <span v-if="!sidebarCollapsed" class="username">{{ authStore.user?.username }}</span>
        </div>
      </div>
    </aside>

    <!-- 主聊天区域 -->
    <main class="chat-main">
      <ChatWindow />
    </main>

    <!-- 设置弹窗 -->
    <el-dialog v-model="showSettings" title="设置" width="400px">
      <div class="settings-content">
        <el-button type="danger" @click="handleLogout" style="width: 100%">
          退出登录
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import ChatWindow from '@/components/Chat/ChatWindow.vue'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()

const sidebarCollapsed = ref(false)
const showSettings = ref(false)

const createNewConversation = async () => {
  await chatStore.createConversation('新对话')
}

const selectConversation = async (id: string) => {
  await chatStore.selectConversation(id)
}

const deleteConversation = async (id: string) => {
  if (confirm('确定要删除这个对话吗？')) {
    await chatStore.deleteConversation(id)
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  chatStore.loadConversations()
})
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: #fff;
}

.sidebar {
  width: 260px;
  background: #f9f9f9;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.new-chat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.new-chat-btn:hover {
  background: #5558e3;
}

.collapse-btn {
  padding: 8px 12px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 12px;
  color: #666;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item:hover {
  background: #ececec;
}

.conversation-item.active {
  background: #e0e0ff;
}

.conversation-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.delete-btn {
  opacity: 0;
  padding: 4px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #999;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #f44336;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
}

.user-info:hover {
  background: #ececec;
}

.avatar {
  width: 32px;
  height: 32px;
  background: #6366f1;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.chat-main {
  flex: 1;
  overflow: hidden;
}

.settings-content {
  padding: 20px 0;
}
</style>
