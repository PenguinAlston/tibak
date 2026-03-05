// src/api/request.ts
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          alert('无权访问')
          break
        case 404:
          alert('请求的资源不存在')
          break
        case 500:
          alert('服务器错误')
          break
        default:
          alert(error.response.data?.message || '请求失败')
      }
    }
    return Promise.reject(error)
  }
)

export default request
