import { post, get } from '@/utils/request'

export type ExportFormat = 'md' | 'html' | 'pdf' | 'docx'

export interface ExportTaskStatus {
  taskId: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  progress?: number
  message?: string
  errorMessage?: string
}

export interface ExportProgress {
  taskId: string
  totalFiles: number
  processedFiles: number
  currentFile?: string
  progress: number
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  errorMessage?: string
}

/**
 * 导出单个文档
 */
export function exportDocument(docId: string, format: ExportFormat): Promise<Blob> {
  return post(`export/document/${docId}`, null, {
    params: { format },
    responseType: 'blob'
  })
}

/**
 * 导出目录
 */
export function exportDirectory(
  directoryId: string, 
  format: ExportFormat, 
  recursive: boolean = false
): Promise<Blob> {
  return post(`export/directory/${directoryId}`, null, {
    params: { format, recursive },
    responseType: 'blob'
  })
}

/**
 * 导出项目
 */
export function exportProject(
  projectId: string, 
  format: ExportFormat
): Promise<Blob> {
  return post(`export/project/${projectId}`, null, {
    params: { format },
    responseType: 'blob'
  })
}

/**
 * 异步导出单个文档
 */
export function exportDocumentAsync(docId: string, format: ExportFormat): Promise<string> {
  return post(`export/document/${docId}/async`, null, {
    params: { format }
  })
}

/**
 * 异步导出目录
 */
export function exportDirectoryAsync(
  directoryId: string, 
  format: ExportFormat, 
  recursive: boolean = false,
  mergeDocuments: boolean = false
): Promise<string> {
  return post(`export/directory/${directoryId}/async`, null, {
    params: { format, recursive, mergeDocuments }
  })
}

/**
 * 异步导出项目
 */
export function exportProjectAsync(
  projectId: string, 
  format: ExportFormat,
  mergeDocuments: boolean = false
): Promise<string> {
  return post(`export/project/${projectId}/async`, null, {
    params: { format, mergeDocuments }
  })
}

/**
 * 获取导出任务状态
 * 注意：后端返回的是字符串状态，不是对象
 * 返回值：'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'NOT_FOUND'
 */
export function getExportTaskStatus(taskId: string): Promise<string> {
  return get(`export/task/${taskId}/status`)
}

/**
 * 获取导出任务结果
 */
export function getExportTaskResult(taskId: string): Promise<Blob> {
  return get(`export/task/${taskId}/result`, {
    responseType: 'blob'
  })
}

/**
 * 下载文件
 */
export function downloadFile(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 从响应头中提取文件名
 */
export function extractFilename(response: any): string {
  const contentDisposition = response.headers['content-disposition']
  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename\*?=([^;]+)/)
    if (filenameMatch) {
      let filename = filenameMatch[1].trim()
      // 处理 UTF-8 编码的文件名
      if (filename.startsWith("UTF-8''")) {
        filename = decodeURIComponent(filename.substring(7))
      } else {
        filename = filename.replace(/['"]/g, '')
      }
      return filename
    }
  }
  return 'download'
}