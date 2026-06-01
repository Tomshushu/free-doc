import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { clearAuth, getToken } from './auth'
import router from '@/router'
import { useI18n } from 'vue-i18n'
import type { ApiResponse } from '@/types'

let isRedirectingToLogin = false

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const locale = localStorage.getItem('locale')
    if (locale) {
      config.headers['Accept-Language'] = locale
    }
    return config
  },
  (error) => Promise.reject(error)
)

function getI18nMessage(key: string, fallback: string): string {
  try {
    const locale = localStorage.getItem('locale') || 'zh-CN'
    const i18n = (window as any).__VUE_I18N_INSTANCE__
    if (i18n && i18n.global) {
      return i18n.global.t(key) || fallback
    }
    return fallback
  } catch {
    return fallback
  }
}

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data
    if (res.code !== 200) {
      if (res.code === 4011 || res.code === 4012) {
        handleAuthExpired(res.message, res.code === 4011)
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    const resData = error.response?.data

    if (status === 401 || status === 403) {
      const isExpired = resData?.code === 4011
      const message = resData?.message || (isExpired
        ? getI18nMessage('common.sessionExpired', '登录已过期，请重新登录')
        : getI18nMessage('common.tokenInvalid', '认证失败，请重新登录'))
      handleAuthExpired(message, isExpired)
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }

    return Promise.reject(error)
  }
)

function handleAuthExpired(message: string, isExpired: boolean) {
  if (isRedirectingToLogin) return
  isRedirectingToLogin = true

  clearAuth()

  const expiredMsg = message || getI18nMessage('common.sessionExpired', '登录已过期，请重新登录')

  ElMessageBox.alert(expiredMsg, getI18nMessage('common.sessionExpiredTitle', '提示'), {
    confirmButtonText: getI18nMessage('common.confirm', '确定'),
    type: 'warning',
    callback: () => {
      const currentFullPath = router.currentRoute.value.fullPath
      const redirect = currentFullPath && currentFullPath !== '/login' ? currentFullPath : undefined

      router.replace({
        path: '/login',
        query: redirect ? { redirect } : undefined
      }).finally(() => {
        isRedirectingToLogin = false
      })
    }
  })
}

export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.get<ApiResponse<T>>(url, config)
  // 对于 blob 类型的响应，直接返回 data
  if (config?.responseType === 'blob') {
    return response.data as T
  }
  return response.data.data
}

export async function post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.post<ApiResponse<T>>(url, data, config)
  // 对于 blob 类型的响应，直接返回 data
  if (config?.responseType === 'blob') {
    return response.data as T
  }
  return response.data.data
}

export async function put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.put<ApiResponse<T>>(url, data, config)
  return response.data.data
}

export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await instance.delete<ApiResponse<T>>(url, config)
  return response.data.data
}

export default instance
