// src/types/chat.ts

export interface Message {
  id?: string
  role: 'user' | 'assistant'
  content: string
  createdAt?: string
}

export interface ChatRequest {
  system_message?: string
  user_message: string
  model?: string
  conversation_id?: string
}

export interface ChatResponse {
  content: string
  conversation_id?: string
}

export interface Conversation {
  id: string
  user_id: string
  title: string
  model: string
  created_at: string
  updated_at: string
  messages: Message[]
}

export interface User {
  id: string
  username: string
  email: string
  avatar: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  refresh_token: string
  user: User
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}
