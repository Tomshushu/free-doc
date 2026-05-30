<template>
  <main class="flex-1 flex flex-col overflow-hidden bg-white">
    <div ref="splitRowRef" class="flex-1 min-w-0 flex overflow-hidden">
      <!-- 编辑器区域 -->
      <div
        ref="editorContainerRef"
        class="min-w-0 overflow-hidden editor-container"
        :class="['mode-' + contentWidthMode, { 'is-split-preview': editorMode === 'sv' }]"
        :style="editorPaneStyle"
      >
        <!-- Vditor 挂载点 -->
        <div ref="vditorRef" class="h-full w-full"></div>
      </div>

      <!-- 分屏：可拖拽调整预览区宽度 -->
      <template v-if="editorMode === 'sv'">
        <div
          class="split-resize-handle shrink-0"
          :title="$t('doc.dragResizePreview')"
          @mousedown.stop.prevent="onSplitResizeStart"
        />
        <div
          class="markdown-wrapper editor-split-preview shrink-0"
          :style="previewPaneStyle"
        >
          <ApiMarkdownRenderer :content="docContent" />
        </div>
      </template>
    </div>

    <!-- 底部状态栏 -->
    <div class="status-bar h-7 bg-gray-50/80 backdrop-blur-sm border-t border-gray-100 px-4 flex items-center justify-between text-xs text-gray-400 shrink-0">
      <div class="flex items-center gap-4">
        <span><i class="fa-solid fa-file-lines mr-1"></i>{{ docTitle || $t('doc.noTitle') }}</span>
        <span v-if="lastSavedTime !== '-'"><i class="fa-regular fa-clock mr-1"></i>{{ lastSavedTime }}</span>
      </div>
      <div class="flex items-center gap-4">
        <span>{{ wordCount }} {{ $t('common.word') }}</span>
        <span>{{ lineCount }} {{ $t('common.line') }}</span>
        <span>~{{ readingTime }} {{ $t('common.minuteRead') }}</span>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import ApiMarkdownRenderer from '@/components/ApiMarkdownRenderer.vue'
import type { EditorMode, ContentWidthMode } from '@/composables/useVditorEditor'

const { t } = useI18n()

/** 存预览区宽度占主行（含分割条）的比例，避免换显示器后像素失效 */
const SPLIT_PREVIEW_FRACTION_KEY = 'freedoc-editor-split-preview-fraction'
const LEGACY_SPLIT_PREVIEW_PCT_KEY = 'freedoc-editor-split-preview-percent'

const SPLITTER_PX = 8
const PREVIEW_MIN_PX = 280
const EDITOR_MIN_PX = 240
const PREVIEW_MAX_PX = 1100
const DEFAULT_PREVIEW_FRACTION = 0.58

const props = defineProps<{
  docTitle: string
  docContent: string
  editorMode: EditorMode
  contentWidthMode: ContentWidthMode
  lastSavedTime: string
  wordCount: number
  lineCount: number
  readingTime: number
}>()

const vditorRef = ref<HTMLElement>()
const editorContainerRef = ref<HTMLElement>()
const splitRowRef = ref<HTMLElement>()
/** 分屏模式下预览区宽度（像素）；null 表示尚未根据行宽初始化 */
const splitPreviewWidthPx = ref<number | null>(null)

let splitRowResizeObserver: ResizeObserver | null = null

const editorPaneStyle = computed(() => ({
  flex: '1 1 0%',
  minWidth: `${EDITOR_MIN_PX}px`,
}))

const previewPaneStyle = computed(() => {
  const w = splitPreviewWidthPx.value
  if (w == null) {
    return {
      flex: '0 1 42%',
      minWidth: `${PREVIEW_MIN_PX}px`,
      maxWidth: `${PREVIEW_MAX_PX}px`,
    }
  }
  return {
    flex: `0 0 ${w}px`,
    width: `${w}px`,
    maxWidth: `${PREVIEW_MAX_PX}px`,
  }
})

function readStoredFraction(): number {
  try {
    const frRaw = localStorage.getItem(SPLIT_PREVIEW_FRACTION_KEY)
    if (frRaw != null) {
      const n = Number.parseFloat(frRaw)
      if (Number.isFinite(n) && n >= 0.18 && n <= 0.88) return n
    }
    const legacy = localStorage.getItem(LEGACY_SPLIT_PREVIEW_PCT_KEY)
    if (legacy != null) {
      const p = Number.parseFloat(legacy)
      if (Number.isFinite(p)) return Math.min(0.88, Math.max(0.18, p / 100))
    }
  } catch {
    /* ignore */
  }
  return DEFAULT_PREVIEW_FRACTION
}

function clampPreviewWidthPx(rowWidth: number, px: number): number {
  if (rowWidth <= SPLITTER_PX) return PREVIEW_MIN_PX
  const inner = rowWidth - SPLITTER_PX
  const maxPreview = Math.min(PREVIEW_MAX_PX, inner - EDITOR_MIN_PX)
  const minPreview = Math.min(PREVIEW_MIN_PX, Math.max(120, maxPreview))
  return Math.round(Math.min(maxPreview, Math.max(minPreview, px)))
}

/** 首次进入分屏时从 localStorage 比例初始化；已有宽度时随窗口变化只做 clamp */
function initSplitPreviewWidth() {
  const row = splitRowRef.value
  if (!row || props.editorMode !== 'sv') return
  const rowW = row.getBoundingClientRect().width
  if (rowW <= 0) return
  if (splitPreviewWidthPx.value != null) {
    splitPreviewWidthPx.value = clampPreviewWidthPx(rowW, splitPreviewWidthPx.value)
    return
  }
  const fr = readStoredFraction()
  splitPreviewWidthPx.value = clampPreviewWidthPx(rowW, Math.round(rowW * fr))
}

function onSplitRowResize() {
  initSplitPreviewWidth()
}

function ensureSplitResizeObserver() {
  if (splitRowResizeObserver || typeof ResizeObserver === 'undefined') return
  splitRowResizeObserver = new ResizeObserver(() => onSplitRowResize())
}

function attachSplitResizeObserver() {
  const row = splitRowRef.value
  if (!row || props.editorMode !== 'sv') return
  ensureSplitResizeObserver()
  splitRowResizeObserver?.observe(row)
}

function detachSplitResizeObserver() {
  splitRowResizeObserver?.disconnect()
  splitRowResizeObserver = null
}

function onSplitResizeStart(e: MouseEvent) {
  const row = splitRowRef.value
  if (!row || props.editorMode !== 'sv') return
  const rowW = row.getBoundingClientRect().width
  if (rowW <= 0) return
  if (splitPreviewWidthPx.value == null) initSplitPreviewWidth()
  const startX = e.clientX
  const startW = splitPreviewWidthPx.value ?? clampPreviewWidthPx(rowW, Math.round(rowW * DEFAULT_PREVIEW_FRACTION))

  const onMove = (ev: MouseEvent) => {
    const w = row.getBoundingClientRect().width || rowW
    splitPreviewWidthPx.value = clampPreviewWidthPx(w, startW + (ev.clientX - startX))
  }
  const onUp = () => {
    window.removeEventListener('mousemove', onMove)
    window.removeEventListener('mouseup', onUp)
    document.removeEventListener('mouseleave', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    const rw = row.getBoundingClientRect().width
    if (rw > 0 && splitPreviewWidthPx.value != null) {
      try {
        const fr = Math.min(0.88, Math.max(0.18, splitPreviewWidthPx.value / rw))
        localStorage.setItem(SPLIT_PREVIEW_FRACTION_KEY, String(Math.round(fr * 10000) / 10000))
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
  () => props.editorMode,
  (mode) => {
    if (mode !== 'sv') {
      detachSplitResizeObserver()
      return
    }
    nextTick(() => {
      initSplitPreviewWidth()
      attachSplitResizeObserver()
    })
  },
  { immediate: true },
)

onMounted(() => {
  nextTick(() => {
    if (props.editorMode === 'sv') {
      initSplitPreviewWidth()
      attachSplitResizeObserver()
    }
  })
})

onBeforeUnmount(() => {
  detachSplitResizeObserver()
})

defineExpose({ vditorRef, editorContainerRef })
</script>

<style scoped>
/* 编辑器容器 */
.editor-container :deep(.vditor) {
  border: none !important;
  padding: 8px 20px 20px;
}

/* 分屏：拖拽分割条（需高于 Vditor 吸顶工具栏 z-index:10） */
.split-resize-handle {
  width: 8px;
  flex: 0 0 8px;
  z-index: 30;
  cursor: col-resize;
  touch-action: none;
  background: transparent;
  position: relative;
}
.split-resize-handle::after {
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
.split-resize-handle:hover::after {
  width: 4px;
  background: #a5b4fc;
}

/* 分屏预览面板（宽度由 :style flex 与 max-width 控制） */
.editor-split-preview {
  border-left: 1px solid #e5e7eb;
  background: #f8fafc;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 分屏预览内强制显示 TOC */
.editor-split-preview :deep(.api-markdown-toc) {
  display: flex !important;
}
.editor-split-preview :deep(.api-markdown-shell:not(.no-toc)) {
  grid-template-columns: minmax(0, 1fr) 240px !important;
}

/* 编辑器工具栏吸顶 */
.editor-container :deep(.vditor-toolbar) {
  position: sticky;
  top: 0;
  z-index: 10;
  background: white;
  margin: -8px -20px 0;
  padding: 8px 20px;
  backdrop-filter: blur(8px);
}

/* ===== 内容宽度模式控制 ===== */
.editor-container :deep(.vditor-content),
.editor-container :deep(.vditor-ir),
.editor-container :deep(.vditor-wysiwyg),
.editor-container :deep(.vditor-preview) {
  margin: 0 auto;
  padding: 0 24px;
  box-sizing: border-box;
  width: 100%;
}

.editor-container.mode-narrow :deep(.vditor-content),
.editor-container.mode-narrow :deep(.vditor-ir),
.editor-container.mode-narrow :deep(.vditor-wysiwyg),
.editor-container.mode-narrow :deep(.vditor-preview) {
  max-width: 720px;
}

.editor-container.mode-normal :deep(.vditor-content),
.editor-container.mode-normal :deep(.vditor-ir),
.editor-container.mode-normal :deep(.vditor-wysiwyg),
.editor-container.mode-normal :deep(.vditor-preview) {
  max-width: 900px;
}

.editor-container.mode-wide :deep(.vditor-content),
.editor-container.mode-wide :deep(.vditor-ir),
.editor-container.mode-wide :deep(.vditor-wysiwyg),
.editor-container.mode-wide :deep(.vditor-preview) {
  max-width: 1200px;
}

.editor-container.mode-full :deep(.vditor-content),
.editor-container.mode-full :deep(.vditor-ir),
.editor-container.mode-full :deep(.vditor-wysiwyg),
.editor-container.mode-full :deep(.vditor-preview) {
  max-width: 100%;
}

/* ===== 表格样式优化 ===== */
.editor-container :deep(.vditor-ir table),
.editor-container :deep(.vditor-wysiwyg table),
.editor-container :deep(.vditor-preview table) {
  width: 100% !important;
  min-width: 100% !important;
  table-layout: auto !important;
  border-collapse: collapse;
  margin: 16px 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.editor-container :deep(.vditor-ir table th),
.editor-container :deep(.vditor-wysiwyg table th),
.editor-container :deep(.vditor-preview table th) {
  background: #f8fafc;
  font-weight: 700;
  color: #0f172a;
  text-align: left;
  padding: 11px 14px;
  border-bottom: 2px solid #e2e8f0;
  font-size: 12.5px;
  white-space: nowrap;
}

.editor-container :deep(.vditor-ir table td),
.editor-container :deep(.vditor-wysiwyg table td),
.editor-container :deep(.vditor-preview table td) {
  padding: 10px 14px;
  border-bottom: 1px solid #f1f5f9;
  color: #374151;
  word-wrap: break-word;
}

.editor-container :deep(.vditor-ir table tbody tr:hover),
.editor-container :deep(.vditor-wysiwyg table tbody tr:hover),
.editor-container :deep(.vditor-preview table tbody tr:hover) {
  background: #fafbfc;
}

.editor-container :deep(.vditor-ir table tbody tr:last-child td),
.editor-container :deep(.vditor-wysiwyg table tbody tr:last-child td),
.editor-container :deep(.vditor-preview table tbody tr:last-child td) {
  border-bottom: none;
}
</style>
