// src/api/chat.ts
import request from './request'
import type { ChatRequest, ChatResponse, Conversation } from '@/types/chat'

export interface StreamChatResult {
  content: string
  conversationId?: string
}

export const chatApi = {
  // 流式聊天
  async *streamChat(requestData: ChatRequest): AsyncGenerator<StreamChatResult> {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || ''}`,
      },
      body: JSON.stringify(requestData),
    })

    if (!response.ok) {
      throw new Error('Request failed')
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    let conversationId: string | undefined

    while (true) {
      const { done, value } = await reader!.read()
      if (done) {
        // 返回 conversationId
        if (conversationId) {
          yield { content: '', conversationId }
        }
        break
      }

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data.trim()) {
            try {
              const parsed = JSON.parse(data)
              if (parsed.content === '[DONE]') {
                // 保存 conversationId 供最后返回
                conversationId = parsed.conversation_id
              } else if (parsed.content) {
                yield { content: parsed.content, conversationId: undefined }
              }
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
      }
    }
  },

  // 获取对话列表
  async getConversations(): Promise<Conversation[]> {
    return request.get('/chat/conversations')
  },

  // 创建对话
  async createConversation(title?: string): Promise<Conversation> {
    return request.post('/chat/conversations', { title })
  },

  // 获取对话详情
  async getConversation(id: string): Promise<Conversation> {
    return request.get(`/chat/conversations/${id}`)
  },

  // 删除对话
  async deleteConversation(id: string): Promise<void> {
    return request.delete(`/chat/conversations/${id}`)
  },

  // 重命名对话
  async renameConversation(id: string, title: string): Promise<Conversation> {
    return request.put(`/chat/conversations/${id}`, { title })
  },
}
