<template>
  <div class="editor-toolbar h-13 bg-white/80 backdrop-blur-xl border-b border-gray-200/60 px-4 flex items-center justify-between shrink-0 z-30">
    <!-- 左侧：返回 + 标题 -->
    <div class="flex items-center gap-3 min-w-0">
      <el-tooltip :content="$t('doc.backAlt')" placement="bottom" :show-after="500">
        <button
          class="toolbar-btn"
          @click="$emit('goBack')"
        >
          <i class="fa-solid fa-arrow-left"></i>
        </button>
      </el-tooltip>

      <div class="w-px h-5 bg-gray-200"></div>

      <!-- 文档标题 -->
      <div class="flex items-center gap-2 min-w-0">
        <el-input
          ref="titleInputRef"
          :model-value="docTitle"
          class="!w-auto max-w-xs title-input"
          :placeholder="$t('doc.inputDocTitle')"
          :class="{ 'is-dirty': isTitleDirty }"
          @update:model-value="$emit('update:docTitle', $event)"
          @blur="$emit('titleBlur')"
          @keyup.enter="(target: any) => target.blur()"
        />
      </div>

      <!-- 保存状态指示器 -->
      <transition name="fade" mode="out-in">
        <div v-if="saving" class="save-indicator saving">
          <i class="fa-solid fa-spinner fa-spin"></i>
          <span>{{ $t('common.saving') }}</span>
        </div>
        <div v-else-if="hasChanges" class="save-indicator unsaved">
          <i class="fa-solid fa-circle"></i>
          <span>{{ $t('common.unsaved') }}</span>
        </div>
        <div v-else-if="saved" class="save-indicator saved">
          <i class="fa-solid fa-circle-check"></i>
          <span>{{ $t('common.saved') }}</span>
        </div>
      </transition>
    </div>

    <!-- 右侧：工具按钮组 -->
    <div class="flex items-center gap-1.5 shrink-0">
      <!-- 编辑模式切换 -->
      <div class="hidden lg:flex items-center bg-gray-100/80 rounded-lg p-0.5">
        <button
          v-for="mode in editorModes"
          :key="mode.value"
          :class="['mode-btn', { active: editorMode === mode.value }]"
          :title="mode.label"
          @click="$emit('changeMode', mode.value)"
        >
          <i :class="mode.icon"></i>
          <span class="hidden xl:inline">{{ mode.label }}</span>
        </button>
      </div>

      <div class="w-px h-5 bg-gray-200 mx-1.5 hidden lg:block"></div>

      <!-- 宽度模式 -->
      <div class="hidden xl:flex items-center bg-gray-100/80 rounded-lg p-0.5">
        <button
          v-for="wm in widthModes"
          :key="wm.value"
          :class="['mode-btn text-xs', { active: contentWidthMode === wm.value }]"
          :title="wm.label"
          @click="$emit('changeWidth', wm.value)"
        >
          {{ wm.label }}
        </button>
      </div>

      <div class="w-px h-5 bg-gray-200 mx-1.5"></div>

      <!-- 自动保存开关 -->
      <el-tooltip :content="autoSaveEnabled ? $t('doc.autoSaveOn') : $t('doc.autoSaveOff')" placement="bottom" :show-after="500">
        <button
          :class="['toolbar-btn', { active: autoSaveEnabled }]"
          @click="$emit('toggleAutoSave')"
        >
          <i class="fa-solid fa-rotate"></i>
        </button>
      </el-tooltip>

      <!-- 专注模式 -->
      <el-tooltip :content="$t('doc.focusModeShortcut')" placement="bottom" :show-after="500">
        <button
          :class="['toolbar-btn', { active: focusMode }]"
          @click="$emit('toggleFocus')"
        >
          <i class="fa-solid fa-expand"></i>
        </button>
      </el-tooltip>

      <!-- 命令面板 -->
      <el-tooltip :content="$t('doc.commandPalette')" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="$emit('toggleCommandPalette')">
          <i class="fa-solid fa-terminal"></i>
        </button>
      </el-tooltip>

      <!-- 右侧面板 -->
      <el-tooltip :content="$t('doc.rightPanel')" placement="bottom" :show-after="500">
        <button
          :class="['toolbar-btn', { active: showRightPanel }]"
          @click="$emit('toggleRightPanel')"
        >
          <i class="fa-solid fa-sidebar-flip"></i>
        </button>
      </el-tooltip>

      <div class="w-px h-5 bg-gray-200 mx-1"></div>

      <!-- 版本历史 -->
      <el-tooltip :content="$t('doc.versionHistory')" placement="bottom" :show-after="500">
        <button class="toolbar-btn" @click="$emit('showVersions')">
          <i class="fa-solid fa-clock-rotate-left"></i>
        </button>
      </el-tooltip>

      <!-- 保存按钮 -->
      <button
        class="save-btn"
        :disabled="!hasChanges && !isTitleDirty"
        @click="$emit('save')"
      >
        <i class="fa-solid fa-cloud-arrow-up"></i>
        <span>{{ $t('common.save') }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { EditorMode, ContentWidthMode } from '@/composables/useVditorEditor'

defineProps<{
  docTitle: string
  isTitleDirty: boolean
  saving: boolean
  saved: boolean
  hasChanges: boolean
  editorMode: EditorMode
  contentWidthMode: ContentWidthMode
  autoSaveEnabled: boolean
  focusMode: boolean
  showRightPanel: boolean
}>()

defineEmits<{
  (e: 'update:docTitle', value: string): void
  (e: 'titleBlur'): void
  (e: 'goBack'): void
  (e: 'save'): void
  (e: 'changeMode', mode: EditorMode): void
  (e: 'changeWidth', mode: ContentWidthMode): void
  (e: 'toggleAutoSave'): void
  (e: 'toggleFocus'): void
  (e: 'toggleCommandPalette'): void
  (e: 'toggleRightPanel'): void
  (e: 'showVersions'): void
}>()

const { t } = useI18n()

const editorModes = computed(() => [
  { value: 'sv' as EditorMode, label: t('doc.splitMode'), icon: 'fa-solid fa-columns' },
  { value: 'ir' as EditorMode, label: t('doc.irMode'), icon: 'fa-solid fa-bolt' },
])

const widthModes = computed(() => [
  { value: 'narrow' as ContentWidthMode, label: t('doc.narrow') },
  { value: 'normal' as ContentWidthMode, label: t('doc.standard') },
  { value: 'wide' as ContentWidthMode, label: t('doc.wide') },
  { value: 'full' as ContentWidthMode, label: t('doc.fullWidth') },
])
</script>

<style scoped>
.editor-toolbar {
  height: 52px;
}

/* 工具按钮 */
.toolbar-btn {
  @apply w-8 h-8 rounded-lg flex items-center justify-center text-gray-500 hover:bg-gray-100 hover:text-gray-700 transition-all text-sm;
}
.toolbar-btn.active {
  @apply bg-blue-50 text-primary;
}

/* 模式切换按钮 */
.mode-btn {
  @apply px-2.5 py-1 rounded-md text-xs text-gray-500 hover:text-gray-700 transition-all flex items-center gap-1 whitespace-nowrap;
}
.mode-btn.active {
  @apply bg-white text-primary shadow-sm font-medium;
}

/* 保存按钮 */
.save-btn {
  @apply h-8 px-4 rounded-lg bg-primary text-white text-sm font-medium flex items-center gap-1.5 hover:bg-primary-dark transition-all disabled:opacity-40 disabled:cursor-not-allowed;
}

/* 保存状态指示器 */
.save-indicator {
  @apply flex items-center gap-1.5 text-xs px-2 py-0.5 rounded-full transition-all;
}
.save-indicator.saving {
  @apply text-blue-500 bg-blue-50;
}
.save-indicator.unsaved {
  @apply text-amber-500 bg-amber-50;
}
.save-indicator.unsaved i {
  font-size: 6px;
}
.save-indicator.saved {
  @apply text-green-500 bg-green-50;
}

/* 标题输入框 */
.title-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}
.title-input :deep(.el-input__inner) {
  font-weight: 600;
}
.title-input.is-dirty :deep(.el-input__inner) {
  color: var(--el-color-warning);
}
.title-input :deep(.el-input__inner)::placeholder {
  font-weight: 400;
  color: #9ca3af;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>