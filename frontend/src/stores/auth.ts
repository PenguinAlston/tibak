// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User, LoginRequest, RegisterRequest } from '@/types/chat'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value && !!user.value)

  // 登录
  async function login(data: LoginRequest) {
    const response = await authApi.login(data)
    token.value = response.token
    user.value = response.user
    localStorage.setItem('token', response.token)
    localStorage.setItem('refreshToken', response.refresh_token)
    return response
  }

  // 注册
  async function register(data: RegisterRequest) {
    return await authApi.register(data)
  }

  // 登出
  function logout() {
    user.value = null
    token.value = ''
    authApi.logout()
  }

  // 获取当前用户
  async function getCurrentUser() {
    if (!token.value) return
    try {
      user.value = await authApi.getCurrentUser()
    } catch (e) {
      logout()
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    login,
    register,
    logout,
    getCurrentUser,
  }
})
