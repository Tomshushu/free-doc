<template>
  <Transition name="focus-overlay">
    <div v-if="focusMode" class="focus-mode-overlay" @keydown.esc="$emit('exit')">
      <!-- 顶部迷你工具栏 -->
      <div class="focus-top-bar">
        <div class="flex items-center gap-2">
          <el-tooltip :content="$t('doc.exitFocusMode')" placement="bottom" :show-after="300">
            <button class="focus-btn" @click="$emit('exit')">
              <i class="fa-solid fa-compress"></i>
            </button>
          </el-tooltip>
          <span class="text-sm text-gray-500 font-medium">{{ docTitle }}</span>
        </div>
        <div class="flex items-center gap-2">
          <transition name="fade" mode="out-in">
            <span v-if="saving" class="focus-status saving">
              <i class="fa-solid fa-spinner fa-spin"></i> {{ $t('common.saving') }}
            </span>
            <span v-else-if="hasChanges" class="focus-status unsaved">
              <i class="fa-solid fa-circle" style="font-size: 6px;"></i> {{ $t('common.unsaved') }}
            </span>
            <span v-else class="focus-status saved">
              <i class="fa-solid fa-circle-check"></i> {{ $t('common.saved') }}
            </span>
          </transition>
          <button class="focus-btn primary" :disabled="!hasChanges && !isTitleDirty" @click="$emit('save')">
            <i class="fa-solid fa-cloud-arrow-up"></i>
          </button>
        </div>
      </div>
      <!-- 专注模式下的内容插槽 -->
      <div class="focus-content">
        <slot></slot>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  focusMode: boolean
  docTitle: string
  saving: boolean
  saved: boolean
  hasChanges: boolean
  isTitleDirty: boolean
}>()

defineEmits<{
  (e: 'exit'): void
  (e: 'save'): void
}>()
</script>

<style scoped>
.focus-mode-overlay {
  @apply fixed inset-0 z-50 bg-white flex flex-col;
}

.focus-top-bar {
  @apply h-12 px-4 flex items-center justify-between border-b border-gray-100 bg-white/80 backdrop-blur-xl shrink-0;
}

.focus-content {
  @apply flex-1 overflow-hidden;
}

.focus-btn {
  @apply w-8 h-8 rounded-lg flex items-center justify-center text-gray-500 hover:bg-gray-100 transition-all text-sm;
}
.focus-btn.primary {
  @apply bg-primary text-white hover:bg-primary-dark;
}
.focus-btn.primary:disabled {
  @apply opacity-40 cursor-not-allowed;
}

.focus-status {
  @apply text-xs flex items-center gap-1.5 px-2 py-0.5 rounded-full;
}
.focus-status.saving {
  @apply text-blue-500 bg-blue-50;
}
.focus-status.unsaved {
  @apply text-amber-500 bg-amber-50;
}
.focus-status.saved {
  @apply text-green-500 bg-green-50;
}

/* 过渡动画 */
.focus-overlay-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.focus-overlay-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.focus-overlay-enter-from {
  opacity: 0;
  transform: scale(0.98);
}
.focus-overlay-leave-to {
  opacity: 0;
  transform: scale(1.02);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
