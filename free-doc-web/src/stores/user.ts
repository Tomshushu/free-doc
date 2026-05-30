import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import { getToken, setToken, removeToken, getStoredUser, setStoredUser, clearAuth } from '@/utils/auth'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<User | null>(getStoredUser())
  const isLoggedIn = ref(!!token.value)

  async function login(account: string, password: string) {
    const res = await loginApi({ account, password })
    token.value = res.token
    user.value = {
      userId: res.userId,
      userName: res.userName,
      userIcon: res.userIcon,
      account,
      createTime: ''
    }
    setToken(res.token)
    isLoggedIn.value = true
    setStoredUser(user.value)

    // 登录后立即拉取完整用户信息，补齐 account/createTime 等字段
    try {
      const fullUser = await getCurrentUser()
      user.value = fullUser
      setStoredUser(user.value)
    } catch {
      // 兜底保留已登录状态与基础用户信息
    }
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      user.value = await getCurrentUser()
      setStoredUser(user.value)
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    isLoggedIn.value = false
    clearAuth()
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    fetchUser,
    logout
  }
})
