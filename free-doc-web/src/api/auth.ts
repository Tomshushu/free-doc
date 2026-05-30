import { post, get } from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '@/types'

export function login(data: LoginRequest): Promise<LoginResponse> {
  return post('/auth/login', data)
}

export function register(data: RegisterRequest): Promise<string> {
  return post('/auth/register', data)
}

export function getCurrentUser(): Promise<User> {
  return get('/user/info')
}

export function getUserById(userId: string): Promise<User> {
  return get(`/user/${userId}`)
}
