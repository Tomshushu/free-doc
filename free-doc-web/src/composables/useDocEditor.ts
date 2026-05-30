import { ref, computed, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getDocById,
  updateDoc as updateDocApi,
  deleteDoc as deleteDocApi,
  getProjectDocs,
} from '@/api/doc'
import { getUserById } from '@/api/user'
import { getProjectById } from '@/api/project'
import { getTeamById } from '@/api/team'
import {
  getProjectDirectories,
  createDirectory as createDirectoryApi,
  deleteDirectory as deleteDirectoryApi,
} from '@/api/directory'
import { getDocVersions, rollbackToVersion } from '@/api/version'
import { getDocComments, createComment, deleteComment } from '@/api/comment'
import { useAppStore } from '@/stores/app'
import { useDirectoryStore } from '@/stores/directory'
import { useUserStore } from '@/stores/user'
import type { Doc, Directory, DocVersion, Comment as CommentType, User } from '@/types'

export function useDocEditor(options: {
  vditorSetValue: (val: string) => void
  vditorGetValue: () => string
  vditorDestroy: () => void
}) {
  const route = useRoute()
  const router = useRouter()
  const appStore = useAppStore()
  const directoryStore = useDirectoryStore()
  const userStore = useUserStore()

  const docId = route.params.docId as string

  // ===== 文档数据 =====
  const doc = ref<Doc | null>(null)
  const creator = ref<User | null>(null)
  const updater = ref<User | null>(null)
  const docTitle = ref('')
  const docContent = ref('')
  const originalContent = ref('')
  const directoryId = ref('')

  // ===== 编辑状态 =====
  const saving = ref(false)
  const saved = ref(false)
  const hasChanges = ref(false)
  const isTitleDirty = ref(false)
  const lastSavedTime = ref('-')

  // ===== 统计 =====
  const wordCount = ref(0)
  const lineCount = ref(0)
  const readingTime = computed(() => {
    const minutes = Math.max(1, Math.ceil(wordCount.value / 300))
    return minutes
  })

  // ===== 目录树 =====
  const directoryData = ref<any[]>([])
  const dirLoading = ref(false)
  const showCreateDir = ref(false)
  const newDirForm = ref({ name: '', pid: '0' })

  // ===== 版本 =====
  const versions = ref<DocVersion[]>([])
  const showVersionDrawer = ref(false)
  const currentVersionObj = computed(() => versions.value.find((v) => v.isCurrent))
  const recentVersions = computed(() => versions.value.slice(0, 5))

  // ===== 评论 =====
  const comments = ref<CommentType[]>([])

  // ===== 导出 =====
  const showExportDialog = ref(false)
  const exportOptions = ref<any>({
    targetId: '',
    targetType: 'document',
    format: 'md',
  })
  const exportDocInfo = ref<any>(null)
  const exportBatchInfo = ref<any>({
    isDirectory: false,
    estimatedFileCount: 0,
  })

  // ===== 用户信息 =====
  const currentUserId = computed(() => userStore.user?.userId)
  const currentUserName = computed(() => userStore.user?.userName)
  const projectName = computed(() => appStore.currentProjectName)
  const directoryName = ref('')

  // ===== 专注模式 =====
  const focusMode = ref(false)

  // ========== 数据加载 ==========
  async function loadDoc() {
    try {
      const data = await getDocById(docId)
      setDocData(data)
    } catch (e) {
      console.error(e)
    }
  }

  async function loadDocFor(id: string) {
    try {
      const data = await getDocById(id)
      setDocData(data)
      await Promise.all([loadDirectories(), loadVersions(), loadComments()])
    } catch (e) {
      console.error(e)
    }
  }

  function setDocData(data: Doc) {
    doc.value = data
    docTitle.value = data.docTitle
    docContent.value = data.docContent || ''
    originalContent.value = data.docContent || ''
    directoryId.value = data.directoryId
    hasChanges.value = false
    saved.value = false
    if (data.createUser) {
      getUserById(data.createUser).then((u) => (creator.value = u)).catch(() => (creator.value = null))
    }
    if (data.updateUser) {
      getUserById(data.updateUser).then((u) => (updater.value = u)).catch(() => (updater.value = null))
    } else {
      updater.value = null
    }
  }

  async function loadDirectories() {
    if (!appStore.currentProjectId) return
    dirLoading.value = true
    try {
      const dirs = await getProjectDirectories(appStore.currentProjectId)
      let allDocs: Doc[] = []
      try {
        allDocs = await getProjectDocs(appStore.currentProjectId)
      } catch {}
      directoryData.value = [
        ...dirs.map((d) => ({ ...d, isDoc: false })),
        ...allDocs
          .filter((d) => d.directoryId)
          .map((d) => ({
            id: d.docId,
            name: d.docTitle,
            pid: d.directoryId,
            isDoc: true,
            docIcon: d.docIcon,
          })),
      ]
      const curDir = dirs.find((d) => d.id === directoryId.value)
      directoryName.value = curDir?.name || ''
    } catch (e) {
      console.error(e)
    } finally {
      dirLoading.value = false
    }
  }

  async function loadVersions() {
    try {
      versions.value = await getDocVersions(docId)
    } catch (e) {
      console.error(e)
    }
  }

  async function loadComments() {
    try {
      comments.value = await getDocComments(docId)
    } catch (e) {
      console.error(e)
    }
  }

  // ========== 保存 ==========
  async function saveDoc() {
    if (!doc.value) return
    if (!hasChanges.value && !isTitleDirty.value) return
    saving.value = true
    saved.value = false
    try {
      await updateDocApi({
        docId: doc.value.docId,
        docTitle: docTitle.value,
        docContent: docContent.value,
      })
      originalContent.value = docContent.value
      hasChanges.value = false
      isTitleDirty.value = false
      saved.value = true
      lastSavedTime.value = new Date().toLocaleTimeString('zh-CN')
      setTimeout(() => {
        saved.value = false
      }, 2500)
      await loadVersions()
    } catch (e: any) {
      ElMessage.error(e.message || '保存失败')
    } finally {
      saving.value = false
    }
  }

  async function handleTitleBlur() {
    if (doc.value && docTitle.value !== doc.value.docTitle) {
      isTitleDirty.value = true
      await saveDoc()
    } else {
      isTitleDirty.value = false
    }
  }

  // ========== 统计 ==========
  function updateStats(content: string) {
    wordCount.value = content.replace(/\s/g, '').length
    lineCount.value = content.split('\n').length
  }

  // ========== 目录操作 ==========
  function handleCreateDir(parentPid?: string) {
    newDirForm.value = { name: '', pid: parentPid || '0' }
    showCreateDir.value = true
  }

  async function doCreateDirectory() {
    if (!newDirForm.value.name.trim()) return
    try {
      await createDirectoryApi({
        name: newDirForm.value.name.trim(),
        pid: newDirForm.value.pid,
        projectId: appStore.currentProjectId!,
      })
      ElMessage.success('目录已创建')
      showCreateDir.value = false
      await loadDirectories()
      await directoryStore.refresh()
    } catch (e: any) {
      ElMessage.error(e.message || '创建失败')
    }
  }

  async function handleDeleteDir(dirId: string) {
    try {
      await deleteDirectoryApi(dirId)
      ElMessage.success('删除成功')
      await loadDirectories()
      await directoryStore.refresh()
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
    }
  }

  function handleCreateDoc(dirId?: string) {
    router.push({ path: `/doc/new`, query: { dir: dirId } })
  }

  function handleCreateDocInDir(dirId: string) {
    router.push({ path: `/doc/new`, query: { dir: dirId } })
  }

  function handleTreeNodeClick(node: any) {
    if (node.isDoc && node.id !== docId) {
      navigateToDoc(node.id)
    }
  }

  function handleDocAction(doc: any) {
    if (doc._delete) {
      ElMessageBox.confirm('确定要删除该文档吗？此操作不可恢复！', '删除确认', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
      })
        .then(async () => {
          try {
            await deleteDocApi(doc.id)
            ElMessage.success('文档已删除')
            await loadDirectories()
            if (doc.id === docId) {
              router.push('/')
            }
          } catch (e: any) {
            ElMessage.error(e.message || '删除失败')
          }
        })
        .catch(() => {})
    } else {
      navigateToDoc(doc.id)
    }
  }

  function navigateToDoc(targetDocId: string) {
    router.push(`/doc/${targetDocId}`)
  }

  // ========== 版本操作 ==========
  function handleSelectVersion(ver: DocVersion) {
    if (ver.docContent) {
      options.vditorSetValue(ver.docContent)
      docContent.value = ver.docContent
      hasChanges.value = ver.docContent !== originalContent.value
    }
  }

  async function handleRollback(versionId: string) {
    const version = versions.value.find((v) => v.versionId === versionId)
    const versionNum = version ? version.versionNum : ''
    try {
      await ElMessageBox.confirm(
        `确定要恢复到版本 V${versionNum} 吗？注意：此操作将删除 V${versionNum} 之后的所有版本记录，且不可撤销！`,
        '恢复版本',
        { confirmButtonText: '确定恢复', cancelButtonText: '取消', type: 'warning' },
      )
      await rollbackToVersion(docId, versionId)
      ElMessage.success('回滚成功')
      showVersionDrawer.value = false
      await loadDoc()
      await loadVersions()
      options.vditorSetValue(docContent.value)
    } catch (e: any) {
      if (e !== 'cancel') ElMessage.error(e.message || '回滚失败')
    }
  }

  // ========== 评论操作 ==========
  async function handleAddComment(payload: { docId: string; content: string; parentCommentId?: string }) {
    try {
      await createComment(payload)
      ElMessage.success(payload.parentCommentId ? '回复成功' : '评论成功')
      await loadComments()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  }

  async function handleDeleteComment(commentId: string) {
    try {
      await deleteComment(commentId)
      ElMessage.success('删除成功')
      await loadComments()
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
    }
  }

  // ========== 导出 ==========
  async function handleExportDoc(docNode: any) {
    try {
      const docData = await getDocById(docNode.id)
      exportOptions.value = { targetId: docNode.id, targetType: 'document', format: 'md' }
      exportDocInfo.value = {
        docId: docData.docId,
        docTitle: docData.docTitle,
        docContent: docData.docContent,
        createTime: docData.createTime,
        updateTime: docData.updateTime,
      }
      exportBatchInfo.value = { isDirectory: false }
      showExportDialog.value = true
    } catch (error: any) {
      ElMessage.error('获取文档信息失败')
    }
  }

  async function handleExportCurrentDir(dir: any) {
    try {
      const estimatedCount = estimateDirectoryFileCount(dir.id, true)
      exportOptions.value = { targetId: dir.id, targetType: 'directory', format: 'md', recursive: true }
      exportDocInfo.value = null
      exportBatchInfo.value = { isDirectory: true, directoryName: dir.name, recursive: true, estimatedFileCount: estimatedCount }
      showExportDialog.value = true
    } catch (error: any) {
      ElMessage.error('获取目录信息失败')
    }
  }

  function estimateDirectoryFileCount(dirId: string, recursive: boolean): number {
    let count = 0
    const countInDirectory = (id: string, includeSubdirs: boolean) => {
      directoryData.value.forEach((item: any) => {
        if (item.pid === id) {
          if (item.isDoc) count++
          else if (includeSubdirs) countInDirectory(item.id, true)
        }
      })
    }
    countInDirectory(dirId, recursive)
    return count
  }

  function handleExportSuccess() {
    ElMessage.success('导出成功')
    showExportDialog.value = false
  }

  function handleExportError(error: string) {
    ElMessage.error(`导出失败: ${error}`)
  }

  function handleShareDoc(docNode: any) {
    navigateToDoc(docNode.id)
  }

  // ========== 导航 ==========
  function goBack() {
    if (window.history.length > 1) router.back()
    else router.push('/')
  }

  function consumeOpenVersionQuery() {
    if (route.query.openVersion !== '1') return
    showVersionDrawer.value = true
    const nextQuery = { ...route.query }
    delete nextQuery.openVersion
    router.replace({ query: nextQuery })
  }

  // ========== 格式化工具 ==========
  function formatDateTime(date: string | undefined): string {
    if (!date) return '-'
    return new Date(date).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  function formatRelativeTime(date: string): string {
    const diff = Date.now() - new Date(date).getTime()
    const min = Math.floor(diff / 60000)
    if (min < 1) return '刚刚'
    if (min < 60) return `${min}分钟前`
    const hour = Math.floor(min / 60)
    if (hour < 24) return `${hour}小时前`
    return new Date(date).toLocaleDateString('zh-CN')
  }

  return {
    docId,
    doc,
    creator,
    updater,
    docTitle,
    docContent,
    originalContent,
    directoryId,
    saving,
    saved,
    hasChanges,
    isTitleDirty,
    lastSavedTime,
    wordCount,
    lineCount,
    readingTime,
    directoryData,
    dirLoading,
    showCreateDir,
    newDirForm,
    versions,
    showVersionDrawer,
    currentVersionObj,
    recentVersions,
    comments,
    showExportDialog,
    exportOptions,
    exportDocInfo,
    exportBatchInfo,
    currentUserId,
    currentUserName,
    projectName,
    directoryName,
    focusMode,
    loadDoc,
    loadDocFor,
    loadDirectories,
    loadVersions,
    loadComments,
    saveDoc,
    handleTitleBlur,
    updateStats,
    handleCreateDir,
    doCreateDirectory,
    handleDeleteDir,
    handleCreateDoc,
    handleCreateDocInDir,
    handleTreeNodeClick,
    handleDocAction,
    navigateToDoc,
    handleSelectVersion,
    handleRollback,
    handleAddComment,
    handleDeleteComment,
    handleExportDoc,
    handleExportCurrentDir,
    handleExportSuccess,
    handleExportError,
    handleShareDoc,
    goBack,
    consumeOpenVersionQuery,
    formatDateTime,
    formatRelativeTime,
  }
}
