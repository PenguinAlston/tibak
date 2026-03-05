// src/stores/chat.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatApi } from '@/api/chat'
import type { Message, Conversation } from '@/types/chat'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<Conversation[]>([])
  const currentConversationId = ref<string | null>(null)
  const messages = ref<Message[]>([])
  const isLoading = ref(false)
  const isStreaming = ref(false)

  const currentConversation = computed(() =>
    conversations.value.find(c => c.id === currentConversationId.value)
  )

  // 创建新对话
  async function createConversation(title = '新对话') {
    const conversation = await chatApi.createConversation(title)
    conversations.value.unshift(conversation)
    currentConversationId.value = conversation.id
    messages.value = conversation.messages || []
    return conversation
  }

  // 发送消息并接收流式响应
  async function sendMessage(content: string) {
    const userMessage: Message = {
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
    }
    messages.value.push(userMessage)
    isLoading.value = true
    isStreaming.value = true

    const assistantMessage: Message = {
      role: 'assistant',
      content: '',
      createdAt: new Date().toISOString(),
    }
    messages.value.push(assistantMessage)

    try {
      for await (const chunk of chatApi.streamChat({
        user_message: content,
        conversation_id: currentConversationId.value || undefined,
      })) {
        assistantMessage.content += chunk
        // 触发重新渲染
        messages.value = [...messages.value]
      }

      // 更新当前对话的消息列表
      if (currentConversationId.value) {
        const conv = conversations.value.find(c => c.id === currentConversationId.value)
        if (conv) {
          conv.messages = conv.messages || []
          conv.messages.push(userMessage, assistantMessage)
        }
      }
    } catch (e) {
      console.error('Send message error:', e)
      // 移除失败的消息
      messages.value.pop()
      messages.value.pop()
    } finally {
      isLoading.value = false
      isStreaming.value = false
    }

    return assistantMessage
  }

  // 加载对话列表
  async function loadConversations() {
    conversations.value = await chatApi.getConversations()
  }

  // 删除对话
  async function deleteConversation(id: string) {
    await chatApi.deleteConversation(id)
    conversations.value = conversations.value.filter(c => c.id !== id)
    if (currentConversationId.value === id) {
      currentConversationId.value = null
      messages.value = []
    }
  }

  // 选择对话
  async function selectConversation(id: string) {
    currentConversationId.value = id
    const conversation = await chatApi.getConversation(id)
    messages.value = conversation.messages || []
  }

  // 重命名对话
  async function renameConversation(id: string, title: string) {
    const conversation = await chatApi.renameConversation(id, title)
    const index = conversations.value.findIndex(c => c.id === id)
    if (index !== -1) {
      conversations.value[index] = conversation
    }
  }

  return {
    conversations,
    currentConversationId,
    messages,
    isLoading,
    isStreaming,
    currentConversation,
    createConversation,
    sendMessage,
    loadConversations,
    deleteConversation,
    selectConversation,
    renameConversation,
  }
})
