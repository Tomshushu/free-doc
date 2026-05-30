<template>
  <div class="share-doc-view min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50 flex flex-col">
    <!-- 顶部导航栏 -->
    <header class="h-14 bg-white/80 backdrop-blur-lg border-b border-gray-200 flex items-center justify-between px-6 sticky top-0 z-20 shadow-sm">
      <div class="flex items-center gap-3 cursor-pointer" @click="goToHomepage">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-r from-blue-500 to-indigo-600 flex items-center justify-center">
          <i class="fa-solid fa-book text-white text-xs"></i>
        </div>
        <span class="text-lg font-bold text-gray-800">FreeDoc</span>
        <span class="text-gray-300 mx-2">|</span>
        <span class="text-sm text-gray-600">{{ shareInfo?.docTitle || $t('share.doc') }}</span>
        <el-tag size="small" type="info" class="!rounded-md ml-1">{{ $t('share.sharedBy', { name: shareInfo?.shareUserName || $t('common.unknown') }) }}</el-tag>
      </div>
      <div class="text-xs text-gray-400 flex items-center gap-4">
        <span><i class="fa-solid fa-share-nodes mr-1"></i>{{ $t('share.readonlyPreview') }}</span>
        <span><i class="fa-regular fa-clock mr-1"></i>{{ $t('share.expireTime') }}{{ formatExpireTime(shareInfo?.expireTime) }}</span>
        
        <!-- 语言切换按钮 -->
        <LanguageToggle button-class="p-1.5" icon-size="text-sm" />

        <!-- 字体大小切换按钮 -->
        <FontSizeToggle button-class="p-1.5" icon-size="text-sm" />
        
        <!-- 字体家族切换按钮 -->
        <FontFamilyToggle button-class="p-1.5" icon-size="text-sm" />
      </div>
    </header>

    <!-- 加载中 -->
    <div v-if="loading" class="flex items-center justify-center" style="min-height: calc(100vh - 56px)">
      <i class="fa-solid fa-circle-notch fa-spin text-3xl text-blue-500"></i>
    </div>

    <!-- 文档内容 -->
    <div v-else class="px-[20%] py-4">
      <header class="mb-3">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ doc?.docTitle || $t('share.noTitle') }}</h1>
        <div class="flex items-center gap-4 text-xs text-gray-400">
          <span><i class="fa-regular fa-user mr-1"></i>{{ doc?.createUserName || '-' }} {{ $t('common.createdAt') }} {{ formatDate(doc?.createTime) }}</span>
          <span v-if="doc?.updateTime"><i class="fa-solid fa-rotate-right mr-1"></i>{{ doc?.updateUserName || '-' }} {{ $t('common.updatedAt') }} {{ formatDate(doc?.updateTime) }}</span>
        </div>
      </header>
      <div v-if="doc?.docContent" class="markdown-wrapper min-w-0 flex-1 flex flex-col">
        <ApiMarkdownRenderer :content="doc.docContent" />
      </div>
      <div v-else class="text-center text-gray-400 py-20">
        <i class="fa-solid fa-feather-pointed text-4xl mb-4 opacity-20 block"></i>
        <p>{{ $t('share.noDocContent') }}</p>
      </div>
    </div>

    <div class="fixed bottom-3 right-4 text-xs text-gray-300 pointer-events-none">{{ $t('share.sharedByFreeDoc') }}</div>

    <!-- 页脚 -->
    <footer class="text-center py-1.5 text-[11px] text-gray-400 border-t border-gray-100 mt-auto">
      © 2025-{{ currentYear }} tomshushu. Licensed under the MIT License.
      <a href="https://github.com/Tomshushu/free-doc" target="_blank" rel="noopener noreferrer" class="inline-flex items-center gap-1 ml-2 text-gray-400 hover:text-gray-600 transition-colors">
        <svg class="w-4 h-4" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>
        GitHub
      </a>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import ApiMarkdownRenderer from '@/components/ApiMarkdownRenderer.vue'
import LanguageToggle from '@/components/LanguageToggle.vue'
import FontSizeToggle from '@/components/FontSizeToggle.vue'
import FontFamilyToggle from '@/components/FontFamilyToggle.vue'
import { useThemeStore } from '@/stores/theme'
import { getShareDocContent } from '@/api/share'
import type { ShareVO, ShareDocItem } from '@/types'

const router = useRouter()
const { t, locale } = useI18n()
const themeStore = useThemeStore()
const currentYear = new Date().getFullYear()

const props = defineProps<{
  shareInfo: ShareVO
  shareCode: string
}>()

const loading = ref(true)
const doc = ref<ShareDocItem | null>(null)

function goToHomepage() {
  router.push('/')
}

onMounted(async () => {
  // 初始化主题设置
  themeStore.init()
  
  try {
    doc.value = await getShareDocContent(props.shareCode, props.shareInfo.targetId)
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

function formatDate(d?: string) {
  return d ? new Date(d).toLocaleDateString(locale.value) : ''
}

function formatExpireTime(d?: string) {
  if (!d) return t('common.permanent')
  return new Date(d).toLocaleString(locale.value)
}
</script>
