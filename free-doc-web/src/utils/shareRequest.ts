import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'

const shareInstance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：从 sessionStorage 读取 shareToken
shareInstance.interceptors.request.use(
  (config) => {
    const shareToken = sessionStorage.getItem('shareToken')
    if (shareToken) {
      config.headers['X-Share-Token'] = shareToken
    }
    const locale = localStorage.getItem('locale')
    if (locale) {
      config.headers['Accept-Language'] = locale
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：不跳转登录页，仅提示
shareInstance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      ElMessage.error('链接已失效，请重新验证')
      sessionStorage.removeItem('shareToken')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export async function shareGet<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await shareInstance.get<ApiResponse<T>>(url, config)
  return response.data.data
}

export async function sharePost<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const response = await shareInstance.post<ApiResponse<T>>(url, data, config)
  return response.data.data
}

export default shareInstance
