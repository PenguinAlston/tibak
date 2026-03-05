// src/api/auth.ts
import request from './request'
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '@/types/chat'

export const authApi = {
  // 登录
  async login(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/auth/login', data)
  },

  // 注册
  async register(data: RegisterRequest): Promise<User> {
    return request.post('/auth/register', data)
  },

  // 获取当前用户信息
  async getCurrentUser(): Promise<User> {
    return request.get('/auth/me')
  },

  // 登出
  logout(): void {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  },
}
