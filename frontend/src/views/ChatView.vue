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
          @dblclick="startRename(conv)"
          title="双击重命名"
        >
          <div class="conversation-content">
            <span class="conversation-title">{{ conv.title }}</span>
            <span class="conversation-date">{{ formatDate(conv.updated_at) }}</span>
          </div>
          <button class="delete-btn" @click.stop="deleteConversation(conv.id)">×</button>
        </div>
        <div v-if="chatStore.conversations.length === 0" class="empty-history">
          <span>暂无历史记录</span>
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

    <!-- 重命名对话框 -->
    <el-dialog v-model="showRenameDialog" title="重命名对话" width="400px">
      <el-input
        v-model="renameTitle"
        placeholder="请输入对话标题"
        @keyup.enter="confirmRename"
      />
      <template #footer>
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
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
const showRenameDialog = ref(false)
const renameTitle = ref('')
const renamingConversationId = ref<string | null>(null)

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

const startRename = (conv: any) => {
  renamingConversationId.value = conv.id
  renameTitle.value = conv.title
  showRenameDialog.value = true
}

const confirmRename = async () => {
  if (renamingConversationId.value && renameTitle.value.trim()) {
    await chatStore.renameConversation(renamingConversationId.value, renameTitle.value.trim())
    showRenameDialog.value = false
    renamingConversationId.value = null
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
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

.empty-history {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
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

.conversation-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conversation-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.conversation-date {
  font-size: 11px;
  color: #999;
}

.delete-btn {
  opacity: 0;
  padding: 4px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #999;
  flex-shrink: 0;
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
