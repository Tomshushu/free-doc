<template>
  <aside
    v-if="!collapsed"
    class="bg-white border-l border-gray-200/60 flex flex-col shrink-0 overflow-hidden min-w-0 h-full"
    :style="{ width: `${panelWidthPx}px`, flex: `0 0 ${panelWidthPx}px` }"
  >
    <!-- 面板头部 -->
    <div class="p-3 border-b border-gray-100 flex items-center justify-between shrink-0">
      <div class="flex items-center bg-gray-100/80 rounded-lg p-0.5">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['panel-tab', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
        </button>
      </div>
      <button class="panel-close-btn" @click="$emit('update:collapsed', true)">
        <i class="fa-solid fa-xmark text-xs"></i>
      </button>
    </div>

    <!-- 信息面板 -->
    <div v-show="activeTab === 'info'" class="flex-1 overflow-y-auto p-4 space-y-5">
      <!-- 文档属性 -->
      <section>
        <h4 class="text-xs font-semibold text-gray-400 uppercase mb-3">{{ $t('doc.docProperties') }}</h4>
        <div class="space-y-2.5">
          <div class="info-row">
            <i class="fa-solid fa-user w-5 text-gray-300 text-center"></i>
            <span class="text-gray-400 w-16 shrink-0 text-xs">{{ $t('doc.creator') }}</span>
            <span class="text-gray-700 truncate text-sm">{{ creatorName }}</span>
          </div>
          <div class="info-row">
            <i class="fa-solid fa-folder w-5 text-gray-300 text-center"></i>
            <span class="text-gray-400 w-16 shrink-0 text-xs">{{ $t('doc.belongProject') }}</span>
            <span class="text-gray-700 truncate text-sm">{{ projectName }}</span>
          </div>
          <div class="info-row">
            <i class="fa-solid fa-folder-tree w-5 text-gray-300 text-center"></i>
            <span class="text-gray-400 w-16 shrink-0 text-xs">{{ $t('doc.belongDirectory') }}</span>
            <span class="text-gray-700 truncate text-sm">{{ directoryName }}</span>
          </div>
          <div class="info-row">
            <i class="fa-solid fa-calendar-plus w-5 text-gray-300 text-center"></i>
            <span class="text-gray-400 w-16 shrink-0 text-xs">{{ $t('doc.createTime') }}</span>
            <span class="text-gray-700 truncate text-sm">{{ formatDateTime(doc?.createTime) }}</span>
          </div>
          <div class="info-row">
            <i class="fa-solid fa-clock w-5 text-gray-300 text-center"></i>
            <span class="text-gray-400 w-16 shrink-0 text-xs">{{ $t('doc.lastUpdate') }}</span>
            <span class="text-gray-700 truncate text-sm">
              {{ updaterName }} ({{ formatDateTime(doc?.updateTime) }})
            </span>
          </div>
        </div>
      </section>

      <!-- 最近版本 -->
      <section>
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-xs font-semibold text-gray-400 uppercase">{{ $t('doc.versionRecords') }}</h4>
          <button class="text-xs text-primary hover:text-primary-dark transition-colors" @click="$emit('showAllVersions')">
            {{ $t('doc.viewAll') }}
          </button>
        </div>
        <div class="space-y-2">
          <div
            v-for="v in recentVersions"
            :key="v.versionId"
            class="version-mini-item p-2.5 rounded-lg border border-gray-100 hover:border-blue-200 cursor-pointer transition-all hover:shadow-sm"
            @click="$emit('showAllVersions')"
          >
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium">V{{ v.versionNum }}</span>
              <el-tag v-if="v.isCurrent" size="small" type="success" effect="plain" class="!h-5 !text-[10px]">{{ $t('common.current') }}</el-tag>
            </div>
            <p class="text-xs text-gray-400 mt-1">{{ formatRelativeTime(v.createTime) }}</p>
          </div>
          <div v-if="recentVersions.length === 0" class="text-xs text-gray-300 py-2 text-center">
            {{ $t('doc.noVersionRecords') }}
          </div>
        </div>
      </section>
    </div>

    <!-- 评论面板 -->
    <div v-show="activeTab === 'comment'" class="flex-1 overflow-y-auto p-4">
      <CommentList
        :comments="comments"
        :current-user-id="currentUserId"
        :current-user-name="currentUserName"
        @add="(payload) => $emit('addComment', payload)"
        @delete="(id) => $emit('deleteComment', id)"
      />
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommentList from '@/components/CommentList.vue'
import type { Doc, DocVersion, Comment, User } from '@/types'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  collapsed: boolean
  /** 展开时右栏宽度（像素），由 DocEdit 拖拽条控制 */
  panelWidthPx?: number
  doc: Doc | null
  creator: User | null
  updater: User | null
  projectName: string
  directoryName: string
  recentVersions: DocVersion[]
  comments: Comment[]
  currentUserId?: string
  currentUserName?: string
  formatDateTime: (date: string | undefined) => string
  formatRelativeTime: (date: string) => string
}>(), {
  panelWidthPx: 320,
})

defineEmits<{
  (e: 'update:collapsed', value: boolean): void
  (e: 'showAllVersions'): void
  (e: 'addComment', payload: { docId: string; content: string; parentCommentId?: string }): void
  (e: 'deleteComment', commentId: string): void
}>()

const activeTab = ref('info')

const tabs = computed(() => [
  { key: 'info', label: t('doc.info'), icon: 'fa-solid fa-circle-info' },
  { key: 'comment', label: t('doc.comments'), icon: 'fa-solid fa-comment' },
])

const creatorName = computed(() => props.creator?.userName || props.doc?.createUser || '-')
const updaterName = computed(() => props.updater?.userName || props.doc?.updateUser || props.creator?.userName || props.doc?.createUser || '-')
</script>

<style scoped>
.info-row {
  @apply flex items-center py-1.5 text-sm;
}

.panel-tab {
  @apply px-2.5 py-1 rounded-md text-xs text-gray-500 hover:text-gray-700 transition-all flex items-center gap-1 whitespace-nowrap;
}
.panel-tab.active {
  @apply bg-white text-primary shadow-sm font-medium;
}

.panel-close-btn {
  @apply w-6 h-6 rounded-md flex items-center justify-center text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-all;
}
</style>
