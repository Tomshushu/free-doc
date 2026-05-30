import { get, post, del } from '@/utils/request'
import { shareGet, sharePost } from '@/utils/shareRequest'
import type { ShareCreateRequest, ShareVO, ShareProjectContent, ShareDocItem } from '@/types'

// ===== 需要JWT认证的接口 =====

export function createShare(data: ShareCreateRequest): Promise<ShareVO> {
  return post('/share', data)
}

export function deleteShare(shareId: string): Promise<void> {
  return del(`/share/${shareId}`)
}

export function getShareList(targetType?: string, targetId?: string): Promise<ShareVO[]> {
  const params: Record<string, string> = {}
  if (targetType) params.targetType = targetType
  if (targetId) params.targetId = targetId
  return get('/share/list', { params })
}

// ===== 无需JWT认证的公开接口 =====

export function getSharePublicInfo(shareCode: string): Promise<ShareVO> {
  return shareGet(`/share/no-auth/${shareCode}`)
}

export function verifyShare(shareCode: string, password: string): Promise<ShareVO> {
  return sharePost(`/share/no-auth/${shareCode}/verify`, { password })
}

export function getShareProjectContent(shareCode: string): Promise<ShareProjectContent> {
  return shareGet(`/share/no-auth/${shareCode}/project`)
}

export function getShareDocContent(shareCode: string, docId: string): Promise<ShareDocItem> {
  return shareGet(`/share/no-auth/${shareCode}/doc/${docId}`)
}
