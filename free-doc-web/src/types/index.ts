export interface User {
  userId: string
  userName: string
  userIcon: string
  account: string
  createTime: string
}

export interface Team {
  teamId: string
  teamName: string
  teamIcon: string
  teamDesc: string
  createUser: string
  createTime: string
}

export interface TeamUser {
  id: string
  userId: string
  teamId: string
  type: 'OWNER' | 'PARTICIPANT'
  isDefault: boolean
}

export interface Project {
  projectId: string
  projectName: string
  projectIcon: string
  teamId: string
  projectDesc: string
  createUser: string
  createTime: string
}

export interface ProjectUser {
  id: string
  projectId: string
  userId: string
  type: 'OWNER' | 'PARTICIPANT'
  permission: 'r' | 'rw'
}

export interface Directory {
  id: string
  name: string
  pid: string
  pids: string
  projectId: string
  teamId: string
  createUser: string
  createTime: string
  updateTime: string
  updateUser: string
}

export interface Doc {
  docId: string
  directoryId: string
  docIcon: string
  docTitle: string
  docContent: string
  docSummary?: string
  teamId: string
  projectId: string
  createUser: string
  createUserName?: string
  createUserAccount?: string
  createTime: string
  updateUser: string
  updateUserName?: string
  updateUserAccount?: string
  updateTime: string
  directoryName?: string
}

export interface DocDetail extends Doc {
  viewCount?: number
  versionCount?: number
}

export interface DocVersion {
  versionId: string
  versionNum: number
  docId: string
  docContent: string
  diffContent: string
  isCurrent: boolean
  contentHash: string
  createTime: string
  createUser: string
}

export interface Comment {
  commentId: string
  docId: string
  parentCommentId: string
  content: string
  createBy: string
  createTime: string
  updateTime: string
}

export interface LoginRequest {
  account: string
  password: string
}

export interface RegisterRequest {
  account: string
  password: string
  userName?: string
  userIcon?: string
}

export interface LoginResponse {
  token: string
  userId: string
  userName: string
  userIcon: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// ====== 请求类型 ======

export interface TeamCreateRequest {
  teamName: string
  teamIcon?: string
  teamDesc?: string
}

export interface TeamMemberRequest {
  userId: string
  type: 'OWNER' | 'PARTICIPANT'
}

export interface ProjectCreateRequest {
  projectName: string
  projectIcon?: string
  teamId: string
  projectDesc?: string
}

export interface ProjectMemberRequest {
  userId: string
  type: 'OWNER' | 'PARTICIPANT'
  permission: 'r' | 'rw'
}

export interface DirectoryCreateRequest {
  name: string
  pid: string
  projectId: string
}

export interface DocCreateRequest {
  docTitle: string
  directoryId?: string
  docIcon?: string
  docContent?: string
  teamId?: string
  projectId?: string
}

export interface DocUpdateRequest {
  docId: string
  docTitle?: string
  docContent?: string
  docSummary?: string
  directoryId?: string
  docIcon?: string
  createVersion?: boolean
  updateCurrentVersion?: boolean
}

export interface UpdateUserRequest {
  userName?: string
  userIcon?: string
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface CommentCreateRequest {
  docId: string
  content: string
  parentCommentId?: string
}

// ===== 分享相关类型 =====

export interface Share {
  shareId: string
  shareCode: string
  targetType: 'PROJECT' | 'DOC'
  targetId: string
  password?: string
  expireTime?: string
  createUser: string
  createTime: string
}

export interface ShareCreateRequest {
  targetType: 'PROJECT' | 'DOC'
  targetId: string
  password?: string
  expireHours?: number
}

export interface ShareVO {
  shareId: string
  shareCode: string
  targetType: 'PROJECT' | 'DOC'
  targetId: string
  needPassword: boolean
  isExpired: boolean
  expireTime?: string
  createUser: string
  createTime: string
  projectName?: string
  projectDesc?: string
  projectIcon?: string
  docTitle?: string
  docIcon?: string
  shareUserName?: string
}

export interface ShareProjectContent {
  project: Project
  directories: Directory[]
  docs: ShareDocItem[]
}

export interface ShareDocItem {
  docId: string
  docTitle: string
  docIcon: string
  docContent: string
  directoryId: string
  projectId: string
  createUser: string
  createUserName?: string
  createTime: string
  updateUser: string
  updateUserName?: string
  updateTime: string
}

// ===== 导出相关类型 =====

export type ExportFormat = 'md' | 'html' | 'pdf' | 'docx'

export type ExportType = 'document' | 'directory' | 'project'

export interface ExportOptions {
  targetId: string
  targetType: ExportType
  format: ExportFormat
  recursive?: boolean
  async?: boolean
}

export interface ExportProgress {
  taskId: string
  totalFiles: number
  processedFiles: number
  currentFile?: string
  progress: number
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  errorMessage?: string
  startTime?: string
  endTime?: string
}

export interface BatchExportInfo {
  isDirectory: boolean
  directoryName?: string
  docTitle?: string
  recursive?: boolean
  estimatedFileCount?: number
}
