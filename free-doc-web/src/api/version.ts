import { get, post, del } from '@/utils/request'
import type { DocVersion } from '@/types'

export function getDocVersions(docId: string): Promise<DocVersion[]> {
  return get(`/version/doc/${docId}`)
}

export function getVersion(versionId: string): Promise<DocVersion> {
  return get(`/version/${versionId}`)
}

export function rollbackToVersion(docId: string, versionId: string): Promise<void> {
  return post(`/version/rollback/${docId}/${versionId}`)
}

export function deleteVersionsAfter(docId: string, versionNum: number): Promise<void> {
  return del(`/version/after/${docId}/${versionNum}`)
}
