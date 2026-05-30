import { get, post, put, del } from '@/utils/request'
import type { Doc, DocDetail, DocCreateRequest, DocUpdateRequest, PageResult } from '@/types'

export function createDoc(data: DocCreateRequest): Promise<Doc> {
  return post('/doc', data)
}

export function updateDoc(data: DocUpdateRequest): Promise<Doc> {
  return put('/doc', data)
}

export function deleteDoc(docId: string): Promise<void> {
  return del(`/doc/${docId}`)
}

export function getDocById(docId: string): Promise<Doc> {
  return get(`/doc/${docId}`)
}

export function getDocByIdWithUser(docId: string): Promise<DocDetail> {
  return get(`/doc/detail/${docId}`)
}

export function getDirectoryDocs(directoryId: string): Promise<Doc[]> {
  return get(`/doc/directory/${directoryId}`)
}

export function searchDocs(keyword: string, page = 1, size = 10, projectId?: string): Promise<PageResult<Doc>> {
  const params: Record<string, any> = { keyword, page, size }
  if (projectId) params.projectId = projectId
  return get(`/doc/search`, { params })
}

export function getRecentDocs(limit = 10): Promise<Doc[]> {
  return get(`/doc/recent`, { params: { limit } })
}

/** 获取项目下所有文档 */
export function getProjectDocs(projectId: string): Promise<Doc[]> {
  return get(`/doc/project/${projectId}`)
}

/** 获取文档内容（用于版本回滚后的刷新） */
export function getDocContent(docId: string): Promise<string> {
  return get(`/doc/${docId}/content`)
}

// ========== 版本历史 ==========
export interface DocVersion {
  versionId: string
  docId: string
  versionNum: number
  docContent: string
  isCurrent: boolean
  contentHash: string
  diffContent?: string
  createTime: string
  createUser: string
}

export function getDocVersions(docId: string): Promise<DocVersion[]> {
  return get(`/version/doc/${docId}`)
}

export function getVersion(versionId: string): Promise<DocVersion> {
  return get(`/version/${versionId}`)
}

export function rollbackToVersion(docId: string, versionId: string): Promise<void> {
  return post(`/version/rollback/${docId}/${versionId}`, null)
}
