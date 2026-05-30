<template>
  <div class="share-project-view h-screen flex flex-col overflow-hidden bg-gradient-to-br from-blue-50 via-white to-indigo-50">
    <!-- 顶部导航栏 -->
    <header class="h-12 bg-white/80 backdrop-blur-lg border-b border-gray-200/80 flex items-center justify-between px-5 shrink-0 z-20">
      <div class="flex items-center gap-2.5 cursor-pointer" @click="goToHomepage">
        <div class="w-7 h-7 rounded-lg bg-gradient-to-r from-blue-500 to-indigo-600 flex items-center justify-center shadow-sm">
          <i class="fa-solid fa-book text-white text-[11px]"></i>
        </div>
        <span class="text-base font-bold text-gray-800">FreeDoc</span>
        <span class="text-gray-300 mx-0.5">|</span>
        <span class="text-sm text-gray-600 truncate max-w-[300px]">{{ shareInfo?.projectName || $t('share.project') }}</span>
      </div>
      <div class="flex items-center gap-2 shrink-0">
        <el-tag size="small" type="info" effect="plain" class="!rounded-md !text-xs !h-5 shrink-0">
          <i class="fa-solid fa-share-nodes mr-1 text-xs"></i>{{ $t('share.readonlyPreview') }}
        </el-tag>
        <el-tag size="small" effect="plain" class="!rounded-md !text-xs !h-5 shrink-0">
          <i class="fa-regular fa-clock mr-1 text-xs"></i>{{ $t('share.expireTime') }}{{ formatExpireTime(shareInfo?.expireTime) }}
        </el-tag>
        
        <!-- 语言切换按钮 -->
        <LanguageToggle button-class="p-1.5" icon-size="text-xs" />

        <!-- 字体大小切换按钮 -->
        <FontSizeToggle button-class="p-1.5" icon-size="text-xs" />
        
        <!-- 字体家族切换按钮 -->
        <FontFamilyToggle button-class="p-1.5" icon-size="text-xs" />
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="flex-1 pt-0.5 pb-0 px-1 sm:px-2 lg:px-3 min-h-0 overflow-auto">
      <!-- 居中卡片容器 -->
      <div class="mx-auto w-full max-w-[1500px] h-[calc(100vh-58px)] bg-white rounded-2xl shadow-xl overflow-hidden flex">
        <ResizableSplit class="flex-1 w-full" :initial-width="260" :min-width="200" :max-width="400">
          <template #left>
            <div class="h-full flex flex-col">
              <!-- 搜索框 + 标题栏 -->
              <div class="px-3 pt-2.5 pb-2 border-b border-gray-100 shrink-0 space-y-2">
                <div class="flex items-center justify-between">
                  <h2 class="text-xs font-semibold text-gray-700">{{ $t('share.docDirectory') }}</h2>
                  <span v-if="searchKeyword && filteredDirectoryData.length !== directoryData.length" class="text-[10px] text-gray-400">
                    {{ filteredDirectoryData.length }} / {{ directoryData.filter(d => d.isDoc).length }}
                  </span>
                </div>
                <el-input
                  v-model="searchKeyword"
                  :placeholder="$t('share.searchDocPlaceholder')"
                  size="small"
                  clearable
                  prefix-icon="Search"
                  class="!rounded-lg search-input"
                />
              </div>

              <!-- 目录树 -->
              <div class="flex-1 overflow-y-auto min-h-0">
                <template v-if="searchKeyword && filteredDirectoryData.length === 0">
                  <div class="text-center text-gray-400 text-sm py-8">
                    <i class="fa-solid fa-search mb-2 block opacity-30 text-2xl"></i>
                    {{ $t('share.noMatchingDoc') }}
                  </div>
                </template>
                <DirectoryTree
                  v-else
                  :key="'share-dt-' + shareCode + (searchKeyword ? '-s' : '')"
                  :data="filteredDirectoryData"
                  :loading="dirLoading"
                  :current-key="currentDocId || ''"
                  :expanded-keys="expandedKeys"
                  :disable-context-menu="true"
                  title=""
                  @node-click="handleTreeNodeClick"
                />
              </div>
            </div>
          </template>

          <template #right>
            <div class="h-full flex flex-col overflow-hidden pl-6">
              <!-- 空状态 -->
              <div v-if="!currentDoc" class="flex-1 flex items-center justify-center">
                <div class="text-center text-gray-400">
                  <i class="fa-regular fa-file-lines text-5xl mb-3 opacity-50 block"></i>
                  <p class="text-sm">{{ $t('share.selectDocFromLeft') }}</p>
                </div>
              </div>

              <!-- 文档内容 -->
              <div v-else class="flex-1 flex flex-col overflow-hidden">
                <!-- 文档头部 -->
                <header class="shrink-0 py-4 pr-7 border-b border-gray-100 flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <h1 class="text-lg font-bold text-gray-900">{{ currentDoc.docTitle }}</h1>
                    <div class="flex items-center justify-between mt-1.5 text-[11px] text-gray-400">
                      <div class="flex items-center gap-4">
                        <span class="flex items-center gap-1">
                          <i class="fa-regular fa-user opacity-70"></i>
                          {{ currentDoc.createUserName || currentDoc.createUser || '-' }} {{ $t('common.createdAt') }} {{ formatDate(currentDoc.createTime) }}
                        </span>
                      </div>
                      <div class="flex items-center gap-4 pr-2">
                        <span v-if="currentDoc.updateTime && currentDoc.updateTime !== currentDoc.createTime" class="flex items-center gap-1">
                          <i class="fa-solid fa-rotate-right opacity-70"></i>
                          {{ currentDoc.updateUserName || currentDoc.updateUser || '-' }} {{ $t('common.updatedAt') }} {{ formatDateTime(currentDoc.updateTime) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </header>

                <!-- 文档正文 -->
                <article class="flex-1 overflow-y-auto">
                  <div class="markdown-wrapper">
                    <ApiMarkdownRenderer :content="currentDoc.docContent || ''" />
                  </div>
                </article>
              </div>
            </div>
          </template>
        </ResizableSplit>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="shrink-0 text-center py-1.5 text-[11px] text-gray-400 border-t border-gray-100">
      © 2025-{{ currentYear }} tomshushu. Licensed under the MIT License.
      <a href="https://github.com/Tomshushu/free-doc" target="_blank" rel="noopener noreferrer" class="inline-flex items-center gap-1 ml-2 text-gray-400 hover:text-gray-600 transition-colors">
        <svg class="w-4 h-4" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>
        GitHub
      </a>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import DirectoryTree from '@/components/DirectoryTree.vue'
import ResizableSplit from '@/components/ResizableSplit.vue'
import ApiMarkdownRenderer from '@/components/ApiMarkdownRenderer.vue'
import LanguageToggle from '@/components/LanguageToggle.vue'
import FontSizeToggle from '@/components/FontSizeToggle.vue'
import FontFamilyToggle from '@/components/FontFamilyToggle.vue'
import { useThemeStore } from '@/stores/theme'
import { getShareProjectContent, getShareDocContent } from '@/api/share'
import type { ShareVO, ShareDocItem, Directory } from '@/types'

const router = useRouter()
const { t } = useI18n()
const themeStore = useThemeStore()
const currentYear = new Date().getFullYear()

const props = defineProps<{
  shareInfo: ShareVO
  shareCode: string
}>()

// ===== 基础数据 =====
const currentDocId = ref<string | null>(null)
const currentDoc = ref<ShareDocItem | null>(null)
const docs = ref<ShareDocItem[]>([])
const directories = ref<Directory[]>([])

// ===== 搜索 =====
const searchKeyword = ref('')

// ===== 目录树 =====
const directoryData = ref<any[]>([])
const dirLoading = ref(false)
const expandedKeys = ref<string[]>([])

/** 过滤后的目录数据：只保留匹配搜索关键词的文档节点及其父级目录 */
const filteredDirectoryData = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return directoryData.value

  // 找出所有匹配的文档 ID
  const matchedDocIds = new Set<string>()
  // 找出包含匹配文档的目录 ID（需要保留父级）
  const parentDirIds = new Set<string>()

  for (const node of directoryData.value) {
    if (node.isDoc) {
      if ((node.name || '').toLowerCase().includes(keyword)) {
        matchedDocIds.add(node.id)
        // 记录其所属目录
        if (node.pid) parentDirIds.add(node.pid)
      }
    }
  }

  // 递归收集所有祖先目录 ID
  function collectAncestors(dirId: string): void {
    parentDirIds.add(dirId)
    for (const node of directoryData.value) {
      if (!node.isDoc && node.id === dirId && node.pid && !parentDirIds.has(node.pid)) {
        collectAncestors(node.pid)
      }
    }
  }

  for (const dirId of Array.from(parentDirIds)) {
    collectAncestors(dirId)
  }

  return directoryData.value.filter((node: any) => {
    if (node.isDoc) return matchedDocIds.has(node.id)
    return parentDirIds.has(node.id)
  })
})

// ========== 导航 ==========
function goToHomepage() {
  router.push('/')
}

// ========== 生命周期 ==========
onMounted(async () => {
  // 初始化主题设置
  themeStore.init()
  await loadProjectContent()
})

// ========== 加载数据 ==========
async function loadProjectContent() {
  dirLoading.value = true
  try {
    const data = await getShareProjectContent(props.shareCode)
    docs.value = data.docs
    directories.value = data.directories

    // 构建目录树数据
    directoryData.value = [
      ...directories.value.map(d => ({ ...d, isDoc: false })),
      ...docs.value.filter(d => d.directoryId).map(d => ({
        id: d.docId,
        name: d.docTitle,
        pid: d.directoryId,
        isDoc: true,
        docIcon: d.docIcon
      }))
    ]

    // 默认展开根级目录
    const rootDirs = directories.value.filter(d => d.pid === '0' || !d.pid)
    expandedKeys.value = rootDirs.map(d => d.id)

    // 先让目录树渲染，再异步加载第一个文档（不阻塞UI）
    await nextTick()
    
    // 自动加载第一个文档（不await，让目录树先展示）
    if (docs.value.length > 0) {
      loadDoc(docs.value[0].docId).catch(e => console.error('Failed to load shared document:', e))
    }
  } catch (e) {
    console.error('Failed to load shared project content:', e)
  } finally {
    dirLoading.value = false
  }
}

async function loadDoc(docId: string) {
  try {
    const doc = await getShareDocContent(props.shareCode, docId)
    currentDoc.value = doc
    currentDocId.value = docId
  } catch (e) {
    console.error('Failed to load document:', e)
  }
}

// ========== 目录树交互 ==========
function handleTreeNodeClick(node: any) {
  if (node.isDoc) {
    loadDoc(node.id)
  }
}

// ========== 工具函数 ==========
function formatDateTime(dateStr?: string) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}/${month}/${day} ${hour}:${minute}:${second}`
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}/${month}/${day} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`
}

function formatExpireTime(dateStr?: string) {
  if (!dateStr) return t('common.permanent')
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}/${month}/${day} ${hour}:${minute}:${second}`
}
</script>

<style scoped lang="scss">
.share-project-view {
  ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  ::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 3px;
    &:hover { background: #909399; }
  }

  ::-webkit-scrollbar-track { background: transparent; }
}

.search-input {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
}


</style>
