<template>
  <div class="doc-editor h-full flex flex-col bg-gray-50">
    <!-- 顶部工具栏 -->
    <EditorToolbar
      :doc-title="docTitle"
      :is-title-dirty="isTitleDirty"
      :saving="saving"
      :saved="saved"
      :has-changes="hasChanges"
      :editor-mode="editorMode"
      :content-width-mode="contentWidthMode"
      :auto-save-enabled="autoSaveEnabled"
      :focus-mode="focusMode"
      :show-right-panel="!rightPanelCollapsed"
      @title-blur="handleTitleBlur"
      @go-back="goBack"
      @save="saveDoc"
      @change-mode="changeEditorMode"
      @change-width="(m: ContentWidthMode) => contentWidthMode = m"
      @toggle-auto-save="autoSaveEnabled = !autoSaveEnabled"
      @toggle-focus="focusMode = !focusMode"
      @toggle-command-palette="showCommandPalette = !showCommandPalette"
      @toggle-right-panel="rightPanelCollapsed = !rightPanelCollapsed"
      @show-versions="showVersionDrawer = true"
    />

    <!-- 主编辑区：三栏布局 -->
    <div ref="workspaceRef" class="flex-1 flex overflow-hidden relative min-w-0">
      <!-- 左侧目录树 -->
      <EditorSidebar
        v-if="showLeftSidebar"
        v-model:collapsed="leftSidebarCollapsed"
        :project-id="appStore.currentProjectId || ''"
        :directory-data="directoryData"
        :dir-loading="dirLoading"
        :current-doc-id="docId"
        :directory-id="directoryId"
        @node-click="handleTreeNodeClick"
        @create-dir="handleCreateDir"
        @create-doc="handleCreateDoc"
        @create-doc-in-dir="handleCreateDocInDir"
        @delete-dir="handleDeleteDir"
        @doc-action="handleDocAction"
        @share-doc="handleShareDoc"
        @export-dir="handleExportCurrentDir"
        @export-doc="handleExportDoc"
        @refresh-dirs="loadDirectories"
      />

      <!-- 左侧栏展开按钮 -->
      <button
        v-if="showLeftSidebar && leftSidebarCollapsed"
        class="sidebar-toggle left-toggle"
        @click="leftSidebarCollapsed = false"
      >
        <i class="fa-solid fa-chevron-right text-xs text-gray-400"></i>
      </button>

      <!-- 中间编辑器区域 -->
      <EditorMainArea
        ref="mainAreaRef"
        class="min-w-0 flex-1"
        :doc-title="docTitle"
        :doc-content="docContent"
        :editor-mode="editorMode"
        :content-width-mode="contentWidthMode"
        :last-saved-time="lastSavedTime"
        :word-count="wordCount"
        :line-count="lineCount"
        :reading-time="readingTime"
      />

      <!-- 中间区与右侧面板之间的宽度拖拽 -->
      <div
        v-if="showRightPanel && !rightPanelCollapsed"
        class="doc-right-split-handle shrink-0"
        :title="$t('doc.dragResizeRightPanel')"
        @mousedown.stop.prevent="onRightPanelResizeStart"
      />

      <!-- 右侧面板 -->
      <EditorRightPanel
        v-if="showRightPanel"
        v-model:collapsed="rightPanelCollapsed"
        :panel-width-px="rightPanelWidthForLayout"
        :doc="doc"
        :creator="creator"
        :updater="updater"
        :project-name="projectName"
        :directory-name="directoryName"
        :recent-versions="recentVersions"
        :comments="comments"
        :current-user-id="currentUserId"
        :current-user-name="currentUserName"
        :format-date-time="formatDateTime"
        :format-relative-time="formatRelativeTime"
        @show-all-versions="showVersionDrawer = true"
        @add-comment="handleAddComment"
        @delete-comment="handleDeleteComment"
      />

      <!-- 右侧面板展开按钮 -->
      <button
        v-if="showRightPanel && rightPanelCollapsed"
        class="sidebar-toggle right-toggle"
        @click="rightPanelCollapsed = false"
      >
        <i class="fa-solid fa-chevron-left text-xs text-gray-400"></i>
      </button>
    </div>

    <!-- 版本历史抽屉 -->
    <el-drawer v-model="showVersionDrawer" :title="$t('doc.versionHistoryTitle')" direction="rtl" size="420px" :with-header="true">
      <VersionTimeline
        :versions="versions"
        :current-version="currentVersionObj"
        @select="handleSelectVersion"
        @rollback="handleRollback"
      />
    </el-drawer>

    <!-- 新建目录弹窗 -->
    <el-dialog v-model="showCreateDir" :title="$t('doc.newDirectory')" width="400px" destroy-on-close>
      <el-form :model="newDirForm" label-width="80px">
        <el-form-item :label="$t('common.name')" required>
          <el-input v-model="newDirForm.name" :placeholder="$t('doc.namePlaceholder')" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDir = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newDirForm.name.trim()" @click="doCreateDirectory">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 导出对话框 -->
    <ExportDialog
      v-model="showExportDialog"
      :export-options="exportOptions"
      :doc-info="exportDocInfo"
      :batch-info="exportBatchInfo"
      @export-success="handleExportSuccess"
      @export-error="handleExportError"
    />

    <!-- 命令面板 -->
    <CommandPalette v-model:visible="showCommandPalette" :commands="paletteCommands" />

    <!-- 快捷键帮助 -->
    <ShortcutHelpDialog v-model="showShortcutHelp" :shortcuts="shortcuts" />

    <!-- 专注模式覆盖层 -->
    <FocusModeOverlay
      :focus-mode="focusMode"
      :doc-title="docTitle"
      :saving="saving"
      :saved="saved"
      :has-changes="hasChanges"
      :is-title-dirty="isTitleDirty"
      @exit="focusMode = false"
      @save="saveDoc"
    >
      <EditorMainArea
        :doc-title="docTitle"
        :doc-content="docContent"
        :editor-mode="editorMode"
        :content-width-mode="contentWidthMode"
        :last-saved-time="lastSavedTime"
        :word-count="wordCount"
        :line-count="lineCount"
        :reading-time="readingTime"
      />
    </FocusModeOverlay>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { useAppStore } from '@/stores/app'
import { useAutoSave } from '@/composables/useAutoSave'
import { useDocEditor } from '@/composables/useDocEditor'
import { useEditorShortcuts } from '@/composables/useEditorShortcuts'
import type { EditorMode, ContentWidthMode } from '@/composables/useVditorEditor'
import ExportDialog from '@/components/ExportDialog.vue'
import VersionTimeline from '@/components/VersionTimeline.vue'
import EditorToolbar from './editor/EditorToolbar.vue'
import EditorSidebar from './editor/EditorSidebar.vue'
import EditorMainArea from './editor/EditorMainArea.vue'
import EditorRightPanel from './editor/EditorRightPanel.vue'
import FocusModeOverlay from './editor/FocusModeOverlay.vue'
import CommandPalette from './editor/CommandPalette.vue'
import ShortcutHelpDialog from './editor/ShortcutHelpDialog.vue'

const route = useRoute()
const { t } = useI18n()
const appStore = useAppStore()

// ===== Vditor 编辑器状态 =====
let vditor: Vditor | null = null
const mainAreaRef = ref<InstanceType<typeof EditorMainArea> | null>(null)
const editorMode = ref<EditorMode>('sv')
const contentWidthMode = ref<ContentWidthMode>('normal')

// ===== 面板状态 =====
const leftSidebarCollapsed = ref(false)
const rightPanelCollapsed = ref(true)
const focusMode = ref(false)
const showCommandPalette = ref(false)
const showShortcutHelp = ref(false)
const autoSaveEnabled = ref(true)
const showRightPanel = ref(true)

// ===== 中间区 / 右侧面板 宽度拖拽 =====
const WORKSPACE_RIGHT_FRACTION_KEY = 'freedoc-docedit-right-panel-fraction'
const RIGHT_PANEL_HANDLE_PX = 8
const RIGHT_PANEL_MIN_PX = 280
const RIGHT_PANEL_MAX_PX = 560
const EDITOR_CENTER_MIN_PX = 360
const LEFT_SIDEBAR_EXPANDED_PX = 256

const workspaceRef = ref<HTMLElement | null>(null)
/** 右栏展开时的宽度（px）；null 表示尚未按当前窗口初始化 */
const rightPanelWidthPx = ref<number | null>(null)
let workspaceResizeObserver: ResizeObserver | null = null

const rightPanelWidthForLayout = computed(() => rightPanelWidthPx.value ?? 320)

function readRightPanelFraction(): number {
  try {
    const s = localStorage.getItem(WORKSPACE_RIGHT_FRACTION_KEY)
    if (s == null) return 320 / 1280
    const n = Number.parseFloat(s)
    if (!Number.isFinite(n) || n < 0.16 || n > 0.55) return 320 / 1280
    return n
  } catch {
    return 320 / 1280
  }
}

function estimateLeftSidebarUsedPx(): number {
  if (!showLeftSidebar.value) return 0
  return leftSidebarCollapsed.value ? 0 : LEFT_SIDEBAR_EXPANDED_PX
}

function clampRightPanelWidthPx(rowW: number, px: number): number {
  if (rowW <= RIGHT_PANEL_HANDLE_PX) return RIGHT_PANEL_MIN_PX
  const leftUsed = estimateLeftSidebarUsedPx()
  const inner = rowW - leftUsed - RIGHT_PANEL_HANDLE_PX
  const maxR = Math.min(RIGHT_PANEL_MAX_PX, inner - EDITOR_CENTER_MIN_PX)
  const minR = Math.min(RIGHT_PANEL_MIN_PX, Math.max(220, maxR))
  return Math.round(Math.min(maxR, Math.max(minR, px)))
}

function initRightPanelWidthFromWorkspace() {
  if (!workspaceRef.value || rightPanelCollapsed.value || !showRightPanel.value) return
  const rowW = workspaceRef.value.getBoundingClientRect().width
  if (rowW <= 0) return
  if (rightPanelWidthPx.value != null) {
    rightPanelWidthPx.value = clampRightPanelWidthPx(rowW, rightPanelWidthPx.value)
    return
  }
  const fr = readRightPanelFraction()
  rightPanelWidthPx.value = clampRightPanelWidthPx(rowW, Math.round(rowW * fr))
}

function ensureWorkspaceResizeObserver() {
  if (workspaceResizeObserver || typeof ResizeObserver === 'undefined') return
  workspaceResizeObserver = new ResizeObserver(() => {
    if (!rightPanelCollapsed.value && showRightPanel.value) initRightPanelWidthFromWorkspace()
  })
}

function attachWorkspaceResizeObserver() {
  const el = workspaceRef.value
  if (!el) return
  ensureWorkspaceResizeObserver()
  workspaceResizeObserver?.observe(el)
}

function detachWorkspaceResizeObserver() {
  workspaceResizeObserver?.disconnect()
  workspaceResizeObserver = null
}

function onRightPanelResizeStart(e: MouseEvent) {
  const row = workspaceRef.value
  if (!row || rightPanelCollapsed.value || !showRightPanel.value) return
  const rowW = row.getBoundingClientRect().width
  if (rowW <= 0) return
  initRightPanelWidthFromWorkspace()
  const startX = e.clientX
  const startW = rightPanelWidthPx.value ?? clampRightPanelWidthPx(rowW, 320)

  const onMove = (ev: MouseEvent) => {
    const w = row.getBoundingClientRect().width || rowW
    rightPanelWidthPx.value = clampRightPanelWidthPx(w, startW + (ev.clientX - startX))
  }
  const onUp = () => {
    window.removeEventListener('mousemove', onMove)
    window.removeEventListener('mouseup', onUp)
    document.removeEventListener('mouseleave', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    const rw = row.getBoundingClientRect().width
    if (rw > 0 && rightPanelWidthPx.value != null) {
      try {
        const fr = Math.min(0.55, Math.max(0.16, rightPanelWidthPx.value / rw))
        localStorage.setItem(WORKSPACE_RIGHT_FRACTION_KEY, String(Math.round(fr * 10000) / 10000))
      } catch {
        /* ignore */
      }
    }
  }
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  window.addEventListener('mousemove', onMove, { passive: true })
  window.addEventListener('mouseup', onUp)
  document.addEventListener('mouseleave', onUp)
}

watch(
  [() => rightPanelCollapsed.value, () => showRightPanel.value, () => leftSidebarCollapsed.value, () => showLeftSidebar.value],
  () => {
    nextTick(() => {
      if (!showRightPanel.value || rightPanelCollapsed.value) return
      initRightPanelWidthFromWorkspace()
      attachWorkspaceResizeObserver()
    })
  },
)

// ===== 文档编辑器业务逻辑 =====
const {
  docId, doc, creator, updater, docTitle, docContent, originalContent, directoryId,
  saving, saved, hasChanges, isTitleDirty, lastSavedTime,
  wordCount, lineCount, readingTime,
  directoryData, dirLoading, showCreateDir, newDirForm,
  versions, showVersionDrawer, currentVersionObj, recentVersions,
  comments, showExportDialog, exportOptions, exportDocInfo, exportBatchInfo,
  currentUserId, currentUserName, projectName, directoryName,
  loadDoc, loadDocFor, loadDirectories, loadVersions, loadComments,
  saveDoc, handleTitleBlur, updateStats,
  handleCreateDir, doCreateDirectory, handleDeleteDir,
  handleCreateDoc, handleCreateDocInDir, handleTreeNodeClick, handleDocAction,
  handleSelectVersion, handleRollback,
  handleAddComment, handleDeleteComment,
  handleExportDoc, handleExportCurrentDir, handleExportSuccess, handleExportError, handleShareDoc,
  goBack, consumeOpenVersionQuery, formatDateTime, formatRelativeTime,
} = useDocEditor({
  vditorSetValue: (val: string) => { if (vditor) vditor.setValue(val) },
  vditorGetValue: () => vditor?.getValue() || '',
  vditorDestroy: () => { if (vditor) { vditor.destroy(); vditor = null } },
})

// ===== 布局计算 =====
const showLeftSidebar = computed(() => !!appStore.currentProjectId)

// ===== Vditor 初始化 =====
function initVditor() {
  const vditorEl = mainAreaRef.value?.vditorRef
  if (!vditorEl) return
  if (vditor) { vditor.destroy(); vditor = null }

  const vditorMode = editorMode.value === 'sv' ? 'ir' : editorMode.value

  vditor = new Vditor(vditorEl, {
    height: '100%',
    mode: vditorMode,
    placeholder: t('doc.markdownPlaceholder'),
    value: docContent.value,
    cache: { enable: false },
    toolbar: [
      'emoji', 'headings', 'bold', 'italic', 'strike', '|',
      'list', 'ordered-list', 'check', '|',
      'quote', 'line', 'code', 'inline-code', '|',
      'link', 'table', '|',
      'undo', 'redo', 'fullscreen',
      { name: 'more', toolbar: ['export', 'outline', 'preview'] },
    ],
    preview: {
      markdown: { toc: true },
      hljs: { lineNumber: true, style: 'github' },
    },
    upload: {
      url: '/api/upload/image',
      fieldName: 'file[]',
      linkToImgUrl: '/api/upload/image',
      handler() { return null },
    },
    after: () => {
      if (vditor) updateStats(vditor.getValue())
    },
    input(value: string) {
      docContent.value = value
      hasChanges.value = value !== originalContent.value
      updateStats(value)
    },
  })
}

// ===== 自动保存 =====
const { startAutoSave, stopAutoSave } = useAutoSave({
  hasChanges,
  isTitleDirty,
  saveFn: saveDoc,
  enabled: autoSaveEnabled,
  intervalSeconds: ref(30),
})

// ===== 快捷键 =====
function toggleFocusMode() { focusMode.value = !focusMode.value }
function toggleCommandPaletteFn() { showCommandPalette.value = !showCommandPalette.value }
function toggleRightPanel() { rightPanelCollapsed.value = !rightPanelCollapsed.value }
function toggleLeftSidebar() { leftSidebarCollapsed.value = !leftSidebarCollapsed.value }

const { shortcuts } = useEditorShortcuts({
  saveFn: saveDoc,
  toggleFocusMode,
  toggleCommandPalette: toggleCommandPaletteFn,
  toggleRightPanel,
  toggleLeftSidebar,
  goBack,
  editorMode,
})

// ===== 命令面板命令 =====
const paletteCommands = computed(() => [
  { id: 'save', label: t('doc.saveDoc'), icon: 'fa-solid fa-cloud-arrow-up', shortcut: 'Ctrl+S', category: t('doc.categoryFile'), handler: saveDoc },
  { id: 'focus', label: t('doc.focusMode'), icon: 'fa-solid fa-expand', shortcut: 'F11', category: t('doc.categoryView'), handler: toggleFocusMode },
  { id: 'right-panel', label: t('doc.toggleRightPanel'), icon: 'fa-solid fa-table-columns', shortcut: 'Ctrl+J', category: t('doc.categoryView'), handler: toggleRightPanel },
  { id: 'left-sidebar', label: t('doc.toggleLeftDir'), icon: 'fa-solid fa-bars', shortcut: 'Ctrl+B', category: t('doc.categoryView'), handler: toggleLeftSidebar },
  { id: 'versions', label: t('doc.viewVersionHistory'), icon: 'fa-solid fa-clock-rotate-left', category: t('doc.categoryVersion'), handler: () => { showVersionDrawer.value = true } },
  { id: 'shortcuts', label: t('doc.viewShortcuts'), icon: 'fa-solid fa-keyboard', category: t('doc.categoryHelp'), handler: () => { showShortcutHelp.value = true } },
  { id: 'mode-sv', label: t('doc.switchToSplit'), icon: 'fa-solid fa-columns', category: t('doc.categoryEditor'), handler: () => changeEditorMode('sv') },
  { id: 'mode-ir', label: t('doc.switchToIR'), icon: 'fa-solid fa-bolt', category: t('doc.categoryEditor'), handler: () => changeEditorMode('ir') },
])

// ===== 编辑模式切换 =====
function changeEditorMode(mode: EditorMode) {
  if (editorMode.value !== mode) {
    editorMode.value = mode
    nextTick(() => initVditor())
  }
}

// ===== 生命周期 =====
onMounted(async () => {
  await Promise.all([loadDoc(), loadDirectories(), loadVersions(), loadComments()])
  await nextTick()
  initVditor()
  consumeOpenVersionQuery()
  startAutoSave()
  nextTick(() => {
    if (showRightPanel.value && !rightPanelCollapsed.value) {
      initRightPanelWidthFromWorkspace()
      attachWorkspaceResizeObserver()
    }
  })
})

onBeforeUnmount(() => {
  detachWorkspaceResizeObserver()
  stopAutoSave()
  if (vditor) { vditor.destroy(); vditor = null }
})

// 监听路由变化
watch(() => route.params.docId, async (newId) => {
  if (newId && newId !== docId) {
    stopAutoSave()
    await loadDocFor(newId as string)
    await nextTick()
    initVditor()
    consumeOpenVersionQuery()
    startAutoSave()
  }
}, { flush: 'post' })

// 监听项目切换
watch(() => appStore.currentProjectId, async (newProjectId) => {
  if (!newProjectId) return
  await loadDirectories()
  const { useDirectoryStore } = await import('@/stores/directory')
  const directoryStore = useDirectoryStore()
  await directoryStore.load(newProjectId)
  if (!doc.value) return
  const docProjectId = doc.value.projectId
  if (docProjectId && docProjectId !== newProjectId) {
    stopAutoSave()
    doc.value = null
    docContent.value = ''
    originalContent.value = ''
    hasChanges.value = false
    if (vditor) { vditor.destroy(); vditor = null }
  }
}, { flush: 'post' })
</script>

<style scoped>
.sidebar-toggle {
  @apply absolute top-3 z-10 bg-white border border-gray-200 shadow-sm hover:bg-gray-50 transition-all;
}
.left-toggle {
  @apply left-0 rounded-r-lg p-1.5;
}
.right-toggle {
  @apply right-0 rounded-l-lg p-1.5;
}

.doc-right-split-handle {
  width: 8px;
  flex: 0 0 8px;
  z-index: 25;
  cursor: col-resize;
  touch-action: none;
  position: relative;
  background: transparent;
}
.doc-right-split-handle::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  transform: translateX(-50%);
  background: #e2e8f0;
  border-radius: 1px;
  transition: background 0.15s, width 0.15s;
}
.doc-right-split-handle:hover::after {
  width: 4px;
  background: #a5b4fc;
}
</style>
