<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="520px"
    :before-close="handleClose"
    :close-on-click-modal="!exporting"
    :close-on-press-escape="!exporting"
    class="export-dialog"
  >
    <div class="export-content">
      <!-- 导出格式选择 -->
      <div v-if="!exporting && !exportCompleted" class="mb-4">
        <h4 class="text-sm font-medium text-gray-700 mb-2">{{ $t('components.selectExportFormat') }}</h4>
        <div class="grid grid-cols-2 gap-3">
          <div
            v-for="format in exportFormats"
            :key="format.value"
            :class="[
              'export-format-item',
              selectedFormat === format.value ? 'selected' : ''
            ]"
            @click="selectedFormat = format.value"
          >
            <div class="flex items-center gap-3">
              <div :class="['format-icon', format.colorClass]">
                <i :class="format.icon"></i>
              </div>
              <div class="flex-1">
                <div class="font-medium text-sm">{{ format.label }}</div>
                <div class="text-xs text-gray-500">{{ format.description }}</div>
              </div>
            </div>
            <i v-if="selectedFormat === format.value" class="fa-solid fa-check text-blue-600"></i>
          </div>
        </div>
      </div>

      <!-- 导出信息 -->
      <div v-if="!exporting && !exportCompleted" class="mb-4">
        <h4 class="text-sm font-medium text-gray-700 mb-2">
          {{ exportOptions.targetType === 'document' ? $t('components.docInfo') : $t('components.dirInfo') }}
        </h4>
        <div class="bg-gray-50 rounded-lg p-3">
          <div class="flex items-center gap-2 mb-2">
            <i :class="exportOptions.targetType === 'document' ? 'fa-regular fa-file-lines text-blue-500' : 'fa-solid fa-folder text-amber-500'"></i>
            <span class="font-medium text-sm">{{ displayName }}</span>
          </div>
          <div class="text-xs text-gray-500">
            <template v-if="exportOptions.targetType === 'document'">
              <span>{{ $t('common.createTime') }}{{ formatDate(docInfo?.createTime) }}</span>
              <span class="ml-4">{{ $t('common.updateTime') }}{{ formatDate(docInfo?.updateTime) }}</span>
            </template>
            <template v-else>
              <span>{{ $t('components.exportType') }}{{ exportOptions.recursive ? $t('components.exportDirRecursive') : $t('components.exportTypeCurrent') }}</span>
              <span v-if="batchInfo.estimatedFileCount" class="ml-4">
                {{ $t('components.estimatedFiles') }}{{ batchInfo.estimatedFileCount }}
              </span>
            </template>
          </div>
        </div>

        <!-- 合并选项（仅目录和项目导出时显示） -->
        <div v-if="exportOptions.targetType === 'directory' || exportOptions.targetType === 'project'" class="mt-3">
          <el-checkbox v-model="mergeDocuments" size="large">
            <span class="text-sm">{{ $t('components.mergeToSingle') }}</span>
            <span class="text-xs text-gray-500 ml-2">{{ $t('components.mergeDesc') }}</span>
          </el-checkbox>
        </div>
      </div>

      <!-- 导出进度 -->
      <div v-if="exporting" class="mb-4">
        <h4 class="text-sm font-medium text-gray-700 mb-3">{{ $t('components.exportProgress') }}</h4>
        <div class="bg-blue-50 rounded-lg p-4">
          <!-- 进度条 -->
          <div class="mb-3">
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium text-blue-700">
                {{ progressInfo.status === 'PROCESSING' ? $t('components.exporting') : $t('components.preparing') }}
              </span>
              <span class="text-sm text-blue-600">{{ Math.round(progressInfo.progress) }}%</span>
            </div>
            <el-progress
              :percentage="Math.round(progressInfo.progress)"
              :status="progressInfo.status === 'FAILED' ? 'exception' : undefined"
              :stroke-width="8"
              :show-text="false"
            />
          </div>

          <!-- 批量导出详细信息 -->
          <div v-if="exportOptions.targetType === 'directory' || exportOptions.targetType === 'project'" class="text-sm text-blue-600">
            <div class="flex justify-between mb-1">
              <span>{{ $t('components.processedFiles') }}</span>
              <span>{{ progressInfo.processedFiles }} / {{ progressInfo.totalFiles }}</span>
            </div>
            <div v-if="progressInfo.currentFile" class="text-xs text-blue-500 truncate">
              {{ $t('components.currentFile') }}{{ progressInfo.currentFile }}
            </div>
          </div>
        </div>
      </div>

      <!-- 导出完成 -->
      <div v-if="exportCompleted" class="mb-4">
        <div class="bg-green-50 rounded-lg p-4 text-center">
          <i class="fa-solid fa-check-circle text-green-500 text-2xl mb-2"></i>
          <div class="text-sm font-medium text-green-700 mb-1">{{ $t('components.exportComplete') }}</div>
          <div class="text-xs text-green-600">
            {{ $t('components.exportCompleteDesc') }}
          </div>
        </div>
      </div>

      <!-- 导出错误 -->
      <div v-if="exportError" class="mb-4">
        <div class="bg-red-50 rounded-lg p-4">
          <div class="flex items-start gap-3">
            <i class="fa-solid fa-exclamation-triangle text-red-500 mt-0.5"></i>
            <div class="flex-1">
              <div class="text-sm font-medium text-red-700 mb-1">{{ $t('components.exportFailed') }}</div>
              <div class="text-xs text-red-600 mb-3">{{ exportError }}</div>
              <el-button
                size="small"
                type="danger"
                plain
                @click="retryExport"
              >
                <i class="fa-solid fa-redo mr-1"></i>
                {{ $t('common.retry') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="handleClose" :disabled="exporting">
          {{ exportCompleted ? $t('common.close') : $t('common.cancel') }}
        </el-button>
        <el-button
          v-if="!exporting && !exportCompleted && !exportError"
          type="primary"
          :disabled="!selectedFormat"
          @click="handleExport"
        >
          <i class="fa-solid fa-download mr-1"></i>
          {{ $t('components.startExport') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  exportDocument,
  exportDirectory,
  exportDocumentAsync,
  exportDirectoryAsync,
  exportProjectAsync,
  getExportTaskStatus,
  getExportTaskResult,
  downloadFile,
  extractFilename,
  type ExportFormat,
  type ExportProgress
} from '@/api/export'
import type { ExportOptions, BatchExportInfo } from '@/types'

const { t } = useI18n()

interface DocInfo {
  docId: string
  docTitle: string
  docContent: string
  createTime?: string
  updateTime?: string
}

interface Props {
  modelValue: boolean
  exportOptions: ExportOptions
  docInfo?: DocInfo | null
  batchInfo?: BatchExportInfo
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'export-success'): void
  (e: 'export-error', error: string): void
}

const props = withDefaults(defineProps<Props>(), {
  batchInfo: () => ({
    isDirectory: false,
    estimatedFileCount: 0
  })
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 导出状态
const selectedFormat = ref<ExportFormat>('md')
const exporting = ref(false)
const exportCompleted = ref(false)
const exportError = ref('')
const currentTaskId = ref('')
const mergeDocuments = ref(false)  // 是否合并为单个文档

// 进度信息
const progressInfo = ref<ExportProgress>({
  taskId: '',
  totalFiles: 0,
  processedFiles: 0,
  progress: 0,
  status: 'PENDING'
})

// 轮询定时器
let progressTimer: ReturnType<typeof setTimeout> | null = null

const exportFormats = computed(() => [
  {
    value: 'md' as ExportFormat,
    label: 'Markdown',
    description: t('components.markdownFormat'),
    icon: 'fa-brands fa-markdown',
    colorClass: 'text-gray-600 bg-gray-100'
  },
  {
    value: 'html' as ExportFormat,
    label: 'HTML',
    description: t('components.htmlFormat'),
    icon: 'fa-brands fa-html5',
    colorClass: 'text-orange-600 bg-orange-100'
  },
  {
    value: 'pdf' as ExportFormat,
    label: 'PDF',
    description: t('components.pdfFormat'),
    icon: 'fa-solid fa-file-pdf',
    colorClass: 'text-red-600 bg-red-100'
  },
  {
    value: 'docx' as ExportFormat,
    label: t('components.wordFormat'),
    description: t('components.wordFormatDesc'),
    icon: 'fa-solid fa-file-word',
    colorClass: 'text-blue-600 bg-blue-100'
  }
])

const dialogTitle = computed(() => {
  if (props.exportOptions.targetType === 'document') {
    return t('components.exportDoc')
  } else {
    return props.exportOptions.recursive ? t('components.exportDirRecursive') : t('components.exportDirCurrent')
  }
})

const displayName = computed(() => {
  if (props.exportOptions.targetType === 'document') {
    return props.docInfo?.docTitle || t('components.unknownDoc')
  } else {
    return props.batchInfo.directoryName || t('components.unknownDir')
  }
})

const selectedFormatLabel = computed(() => {
  const format = exportFormats.value.find(f => f.value === selectedFormat.value)
  return format?.label || selectedFormat.value.toUpperCase()
})

function resetState() {
  exporting.value = false
  exportCompleted.value = false
  exportError.value = ''
  currentTaskId.value = ''
  progressInfo.value = {
    taskId: '',
    totalFiles: 0,
    processedFiles: 0,
    progress: 0,
    status: 'PENDING'
  }
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

function handleClose() {
  if (exporting.value) {
    ElMessage.warning(t('components.exportInProgress'))
    return
  }
  visible.value = false
  resetState()
  selectedFormat.value = 'md'
  mergeDocuments.value = false  // 重置合并选项
}

async function handleExport() {
  if (!selectedFormat.value) {
    ElMessage.warning(t('components.selectExportFormat'))
    return
  }

  resetState()
  exporting.value = true
  exportError.value = ''

  try {
    if (props.exportOptions.targetType === 'document') {
      await handleDocumentExport()
    } else if (props.exportOptions.targetType === 'project') {
      await handleProjectExport()
    } else {
      await handleDirectoryExport()
    }
  } catch (error: any) {
    console.error('Export failed:', error)
    const errorMessage = error.response?.data?.message || error.message || t('components.exportFailed')
    exportError.value = errorMessage
    exporting.value = false
    emit('export-error', errorMessage)
  }
}

async function handleDocumentExport() {
  const docId = props.exportOptions.targetId

  try {
    // 对于单文档，优先使用同步导出
    progressInfo.value.progress = 10
    progressInfo.value.status = 'PROCESSING'

    const blob = await exportDocument(docId, selectedFormat.value)

    progressInfo.value.progress = 90

    // 生成文件名
    const filename = generateFilename(props.docInfo?.docTitle || 'document', selectedFormat.value)

    // 下载文件
    downloadFile(blob, filename)

    progressInfo.value.progress = 100
    progressInfo.value.status = 'COMPLETED'

    exporting.value = false
    exportCompleted.value = true

    ElMessage.success(t('components.docExportSuccess'))
    emit('export-success')

    // 3秒后自动关闭对话框
    setTimeout(() => {
      if (exportCompleted.value) {
        handleClose()
      }
    }, 3000)

  } catch (error: any) {
    // 如果同步导出失败，尝试异步导出
    if (error.response?.status === 413 || error.message?.includes('timeout')) {
      await handleDocumentAsyncExport(docId)
    } else {
      throw error
    }
  }
}

async function handleDocumentAsyncExport(docId: string) {
  try {
    // 启动异步导出
    const taskId = await exportDocumentAsync(docId, selectedFormat.value)
    currentTaskId.value = taskId
    progressInfo.value.taskId = taskId
    progressInfo.value.totalFiles = 1

    // 开始轮询进度
    startProgressPolling()

  } catch (error) {
    throw error
  }
}

async function handleDirectoryExport() {
  const directoryId = props.exportOptions.targetId
  const recursive = props.exportOptions.recursive || false

  try {
    // 批量导出使用异步方式
    const taskId = await exportDirectoryAsync(directoryId, selectedFormat.value, recursive, mergeDocuments.value)
    currentTaskId.value = taskId
    progressInfo.value.taskId = taskId
    progressInfo.value.totalFiles = props.batchInfo.estimatedFileCount || 0

    // 开始轮询进度
    startProgressPolling()

  } catch (error) {
    throw error
  }
}

async function handleProjectExport() {
  const projectId = props.exportOptions.targetId

  try {
    // 项目导出使用异步方式
    const taskId = await exportProjectAsync(projectId, selectedFormat.value, mergeDocuments.value)
    currentTaskId.value = taskId
    progressInfo.value.taskId = taskId
    progressInfo.value.totalFiles = props.batchInfo.estimatedFileCount || 0

    // 开始轮询进度
    startProgressPolling()

  } catch (error) {
    throw error
  }
}

function startProgressPolling() {
  if (progressTimer) {
    clearInterval(progressTimer)
  }

  progressTimer = setInterval(async () => {
    try {
      // 后端返回的是 { code: 200, data: "COMPLETED", message: "success" }
      // 或者 { code: 200, data: "PROCESSING:5/10", message: "success" }
      const statusString = await getExportTaskStatus(currentTaskId.value)

      console.log('Polling task status:', currentTaskId.value, statusString)

      // 解析状态字符串
      if (statusString.startsWith('PROCESSING:')) {
        // 格式: "PROCESSING:5/10"
        const parts = statusString.split(':')
        if (parts.length === 2) {
          const [processed, total] = parts[1].split('/').map(Number)

          // 更新进度信息
          progressInfo.value.status = 'PROCESSING'
          progressInfo.value.processedFiles = processed
          progressInfo.value.totalFiles = total

          // 计算进度百分比
          if (total > 0) {
            progressInfo.value.progress = Math.min(95, (processed / total) * 100)
          } else {
            progressInfo.value.progress = 10
          }

          console.log('Progress update:', processed, '/', total, progressInfo.value.progress + '%')
        }
      } else if (statusString === 'PROCESSING') {
        // 处理中，但没有详细进度信息
        progressInfo.value.status = 'PROCESSING'
        if (progressInfo.value.progress < 90) {
          progressInfo.value.progress += 10
        }
      } else if (statusString === 'COMPLETED') {
        // 任务完成，下载结果
        console.log('Task complete, starting download')
        await downloadTaskResult()
      } else if (statusString === 'FAILED') {
        // 任务失败
        throw new Error(t('components.exportTaskFailed'))
      } else if (statusString === 'NOT_FOUND') {
        // 任务不存在
        throw new Error(t('components.exportTaskNotFound'))
      }

    } catch (error: any) {
      console.error('Failed to get export progress:', error)
      if (progressTimer) {
        clearInterval(progressTimer)
        progressTimer = null
      }

      const errorMessage = error.message || t('components.exportProgressFailed')
      exportError.value = errorMessage
      exporting.value = false
      emit('export-error', errorMessage)
    }
  }, 1000) // 每秒轮询一次
}

async function downloadTaskResult() {
  try {
    if (progressTimer) {
      clearInterval(progressTimer)
      progressTimer = null
    }

    const blob = await getExportTaskResult(currentTaskId.value)

    // 生成文件名
    let filename: string
    if (props.exportOptions.targetType === 'document') {
      filename = generateFilename(props.docInfo?.docTitle || 'document', selectedFormat.value)
    } else {
      // 如果是合并导出，使用选择的格式；否则使用zip
      const fileFormat = mergeDocuments.value ? selectedFormat.value : 'zip'
      filename = generateFilename(props.batchInfo.directoryName || 'directory', fileFormat)
    }

    // 下载文件
    downloadFile(blob, filename)

    progressInfo.value.progress = 100
    progressInfo.value.status = 'COMPLETED'

    exporting.value = false
    exportCompleted.value = true

    ElMessage.success(t('components.exportComplete'))
    emit('export-success')

    // 3秒后自动关闭对话框
    setTimeout(() => {
      if (exportCompleted.value) {
        handleClose()
      }
    }, 3000)

  } catch (error: any) {
    console.error('Failed to download export result:', error)
    const errorMessage = error.message || t('components.downloadResultFailed')
    exportError.value = errorMessage
    exporting.value = false
    emit('export-error', errorMessage)
  }
}

function generateFilename(baseName: string, format: string): string {
  // 清理文件名中的特殊字符
  const cleanName = baseName.replace(/[<>:"/\\|?*]/g, '_')
  const timestamp = new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')

  if (format === 'zip') {
    return `${cleanName}_${timestamp}.zip`
  } else {
    return `${cleanName}_${timestamp}.${format}`
  }
}

async function retryExport() {
  exportError.value = ''
  await handleExport()
}

function formatDate(dateStr?: string) {
  if (!dateStr) return t('common.unknown')
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 监听对话框关闭，清理定时器
watch(visible, (newVisible) => {
  if (!newVisible) {
    resetState()
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
})
</script>

<style scoped>
.export-format-item {
  @apply border border-gray-200 rounded-lg p-3 cursor-pointer transition-all duration-200 hover:border-blue-300 hover:bg-blue-50/30;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.export-format-item.selected {
  @apply border-blue-500 bg-blue-50;
}

.format-icon {
  @apply w-8 h-8 rounded-lg flex items-center justify-center text-sm;
}

.export-dialog :deep(.el-dialog__header) {
  @apply pb-4;
}

.export-dialog :deep(.el-dialog__body) {
  @apply pt-0;
}
</style>
