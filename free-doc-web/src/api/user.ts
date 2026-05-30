import { get, post, put } from '@/utils/request'
import type { User } from '@/types'

/** 搜索用户（用于邀请成员场景） */
export function searchUsers(keyword: string): Promise<User[]> {
  return get('/user/search', { params: { keyword } })
}

/** 根据 ID 获取用户信息 */
export function getUserById(userId: string): Promise<User> {
  return get(`/user/${userId}`)
}

/** 更新当前用户信息 */
export function updateUserInfo(data: { userName?: string; userIcon?: string }): Promise<User> {
  return put('/user/info', data)
}

/** 修改密码（后端 POST 接口） */
export function changePassword(oldPassword: string, newPassword: string): Promise<void> {
  return post('/user/password', { oldPassword, newPassword })
}
