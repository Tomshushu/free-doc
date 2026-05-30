<template>
  <aside
    :class="[
      'bg-white border-r border-gray-200/60 transition-all duration-300 flex flex-col overflow-hidden shrink-0',
      collapsed ? 'w-0 !border-r-0' : 'w-64'
    ]"
  >
    <!-- 侧边栏头部 -->
    <div class="flex items-center justify-between p-3 border-b border-gray-100 shrink-0">
      <span class="text-xs font-semibold text-gray-400 uppercase tracking-wider">{{ $t('doc.directory') }}</span>
      <button class="sidebar-close-btn" @click="$emit('update:collapsed', true)">
        <i class="fa-solid fa-chevron-left text-[10px]"></i>
      </button>
    </div>

    <!-- 目录树 -->
    <div class="flex-1 overflow-y-auto">
      <DirectoryTree
        :key="'de-dt-' + projectId"
        :data="directoryData"
        :loading="dirLoading"
        :current-key="currentDocId"
        :expanded-keys="[directoryId]"
        title=""
        @node-click="(node: any) => $emit('nodeClick', node)"
        @create-directory="(dir?: any) => $emit('createDir', dir?.id)"
        @create-doc="(dirId?: string) => $emit('createDoc', dirId)"
        @add-sub-directory="(dir: any) => $emit('createDir', dir.id)"
        @create-doc-in-dir="(dirId: string) => $emit('createDocInDir', dirId)"
        @delete-directory="(dirId: string) => $emit('deleteDir', dirId)"
        @edit-doc="(doc: any) => $emit('docAction', doc)"
        @share-doc="(doc: any) => $emit('shareDoc', doc)"
        @export-current-dir="(dir: any) => $emit('exportDir', dir)"
        @export-doc="(doc: any) => $emit('exportDoc', doc)"
        @refresh="() => $emit('refreshDirs')"
      />
    </div>
  </aside>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import DirectoryTree from '@/components/DirectoryTree.vue'

defineProps<{
  collapsed: boolean
  projectId: string
  directoryData: any[]
  dirLoading: boolean
  currentDocId: string
  directoryId: string
}>()

const { t } = useI18n()

defineEmits<{
  (e: 'update:collapsed', value: boolean): void
  (e: 'nodeClick', node: any): void
  (e: 'createDir', parentPid?: string): void
  (e: 'createDoc', dirId?: string): void
  (e: 'createDocInDir', dirId: string): void
  (e: 'deleteDir', dirId: string): void
  (e: 'docAction', doc: any): void
  (e: 'shareDoc', doc: any): void
  (e: 'exportDir', dir: any): void
  (e: 'exportDoc', doc: any): void
  (e: 'refreshDirs'): void
}>()
</script>

<style scoped>
.sidebar-close-btn {
  @apply w-6 h-6 rounded-md flex items-center justify-center text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-all;
}
</style>
