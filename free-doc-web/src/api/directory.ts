import { get, post, del, put } from '@/utils/request'
import type { Directory, DirectoryCreateRequest } from '@/types'

export function createDirectory(data: DirectoryCreateRequest): Promise<Directory> {
  return post('/directory', data)
}

export function getProjectDirectories(projectId: string): Promise<Directory[]> {
  return get(`/directory/project/${projectId}`)
}

export function deleteDirectory(directoryId: string): Promise<void> {
  return del(`/directory/${directoryId}`)
}

export function updateDirectoryName(directoryId: string, name: string): Promise<Directory> {
  return put(`/directory/${directoryId}/name?name=${encodeURIComponent(name)}`)
}
