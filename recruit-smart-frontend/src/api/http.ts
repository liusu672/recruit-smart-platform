import axios, { AxiosError, type AxiosInstance } from 'axios'

import { useSessionStore } from '@/stores/session'
import { ApiError, type Result } from '@/types/api'

// 所有 REST 请求统一经过网关；业务模块不得直接访问后端服务。
export const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15_000,
})

http.interceptors.request.use((config) => {
  const session = useSessionStore()

  if (session.token) {
    config.headers.Authorization = `${session.tokenType} ${session.token}`
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<Result<unknown>>) => {
    if (error.response?.status === 401) {
      const session = useSessionStore()
      session.clearSession()
    }

    const result = error.response?.data
    if (result?.message) {
      return Promise.reject(
        new ApiError(result.code || error.response?.status || 500, result.message),
      )
    }

    return Promise.reject(error)
  },
)

// 页面只消费业务数据，不直接处理 Axios 响应壳或后端 Result<T> 包装。
export async function unwrapResult<T>(request: Promise<{ data: Result<T> }>): Promise<T> {
  const response = await request
  const result = response.data

  if (result.code !== 0 && result.code !== 200) {
    throw new ApiError(result.code, result.message || '请求处理失败')
  }

  if (result.data === null) {
    throw new ApiError(result.code, '接口未返回有效数据')
  }

  return result.data
}

// 后端的更新、发布、关闭接口以 data: null 表示成功，不能复用要求非空数据的解包逻辑。
export async function unwrapVoidResult(request: Promise<{ data: Result<null> }>): Promise<void> {
  const response = await request
  const result = response.data

  if (result.code !== 0 && result.code !== 200) {
    throw new ApiError(result.code, result.message || '请求处理失败')
  }
}
