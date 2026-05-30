<template>
  <div class="doc-search p-6">
    <div class="max-w-4xl mx-auto">
      <!-- 搜索区域 -->
      <div class="search-header mb-8 text-center">
        <h1 class="text-2xl font-bold text-gray-800 mb-4">{{ $t('doc.docSearch') }}</h1>
        <div class="search-box mx-auto max-w-2xl relative">
          <el-input
            ref="searchInputRef"
            v-model="keyword"
            size="large"
            :placeholder="$t('doc.searchPlaceholder')"
            :prefix-icon="Search"
            clearable
            autofocus
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="flex items-center justify-center gap-3 mt-4">
          <el-radio-group v-model="searchType" size="small" @change="handleSearch">
            <el-radio-button value="all"><i class="fa-solid fa-globe mr-1"></i>{{ $t('doc.all') }}</el-radio-button>
            <el-radio-button value="title"><i class="fa-solid fa-heading mr-1"></i>{{ $t('doc.titleOnly') }}</el-radio-button>
            <el-radio-button value="content"><i class="fa-solid fa-align-left mr-1"></i>{{ $t('doc.content') }}</el-radio-button>
          </el-radio-group>
        </div>
        <!-- 搜索范围提示 -->
        <p v-if="projectId" class="text-xs text-gray-400 mt-3">
          <i class="fa-solid fa-folder mr-1"></i>{{ $t('doc.searchScope') }}
        </p>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="flex flex-col items-center py-16 gap-4">
        <el-icon class="is-loading text-5xl text-primary"><Loading /></el-icon>
        <span class="text-gray-400 text-sm">{{ $t('doc.searching') }}</span>
      </div>

      <!-- 搜索结果 -->
      <template v-else-if="results.length > 0">
        <!-- 结果统计 -->
        <div class="result-stats flex items-center justify-between mb-4 px-2">
          <span class="text-sm text-gray-500">
            {{ $t('doc.foundResults', { n: total }) }}
          </span>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="sizes, prev, pager, next"
            :page-sizes="[10, 20, 50]"
            small
            background
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>

        <!-- 搜索结果列表 -->
        <TransitionGroup name="list" tag="div" class="space-y-4">
          <article
            v-for="(item, index) in results"
            :key="item.docId + '-' + index"
            class="result-card bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-200 transition-all cursor-pointer group"
            @click="router.push(`/doc/${item.docId}`)"
          >
            <!-- 标题（高亮） -->
            <header class="mb-2">
              <h3
                class="text-lg font-semibold text-gray-900 inline-block group-hover:text-primary transition-colors"
                v-html="highlight(item.docTitle, keyword)"
              ></h3>
              <span class="ml-2 text-xs text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity">
                {{ $t('doc.clickToView') }}
              </span>
            </header>

            <!-- 内容预览（高亮） -->
            <p
              class="text-gray-600 text-sm leading-relaxed mb-3"
              v-html="getPreview(item.docContent)"
            ></p>

            <!-- 元信息 -->
            <footer class="flex items-center gap-4 text-xs text-gray-400 border-t border-gray-50 pt-3 mt-1">
              <span class="flex items-center gap-1">
                <i class="fa-regular fa-folder"></i>
                {{ item.directoryName || $t('common.uncategorized') }}
              </span>
              <span class="flex items-center gap-1">
                <i class="fa-regular fa-clock"></i>
                {{ formatRelativeTime(item.updateTime) }}
              </span>
              <span class="flex items-center gap-1 ml-auto">
                <i class="fa-regular fa-user"></i>
                {{ item.createUser || $t('common.unknown') }}
              </span>
            </footer>
          </article>
        </TransitionGroup>

        <!-- 底部分页 -->
        <div class="flex justify-end mt-6">
          <el-pagination
            v-model:current-page="currentPage"
            :total="total"
            :page-size="pageSize"
            layout="prev, pager, next"
            small
            background
            @current-change="handlePageChange"
          />
        </div>
      </template>

      <!-- 无结果 -->
      <div v-else-if="hasSearched && !loading" class="empty-state py-20 text-center">
        <i class="fa-solid fa-magnifying-glass-location text-5xl text-gray-200 mb-4 block"></i>
        <h3 class="text-lg font-medium text-gray-600 mb-1">{{ $t('doc.noResults') }}</h3>
        <p class="text-gray-400 text-sm mb-4">
          {{ $t('doc.noResultsDesc') }}
        </p>
        <!-- 搜索建议 -->
        <div class="inline-flex flex-wrap justify-center gap-2 max-w-md mx-auto">
          <el-button
            v-for="suggestion in suggestions"
            :key="suggestion"
            size="small"
            round
            @click="keyword = suggestion; handleSearch()"
          >
            {{ suggestion }}
          </el-button>
        </div>
      </div>

      <!-- 初始状态 -->
      <div v-else class="py-20 text-center text-gray-400">
        <i class="fa-solid fa-magnifying-glass text-5xl text-gray-200 mb-4 block"></i>
        <p>{{ $t('doc.startSearch') }}</p>
        <p class="text-xs mt-2">{{ $t('doc.startSearchDesc') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Search, Loading } from '@element-plus/icons-vue'
import { searchDocs } from '@/api/doc'
import type { PageResult, Doc } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 支持项目限定搜索
const projectId = ref<string | undefined>(undefined)

const searchInputRef = ref<any>()
const keyword = ref('')
const searchType = ref('all')
const loading = ref(false)
const hasSearched = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 结果
const results = ref<Doc[]>([])
const total = ref(0)

// 搜索建议
const suggestions = computed(() => [t('doc.searchSuggestion1'), t('doc.searchSuggestion2'), t('doc.searchSuggestion3'), t('doc.searchSuggestion4'), t('doc.searchSuggestion5'), t('doc.searchSuggestion6'), t('doc.searchSuggestion7')])

onMounted(() => {
  // 从路由获取项目ID
  if (route.query.projectId) {
    projectId.value = route.query.projectId as string
  }
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
    handleSearch()
  }
})

// 监听路由查询变化
watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val as string
    handleSearch()
  }
})

async function handleSearch() {
  if (!keyword.value.trim()) return

  loading.value = true
  hasSearched.value = true
  results.value = []
  total.value = 0

  try {
    const res: PageResult<Doc> = await searchDocs(
      keyword.value.trim(),
      currentPage.value,
      pageSize.value,
      projectId.value
    )
    results.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  handleSearch()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  handleSearch()
}

/** 高亮匹配文本 */
function highlight(text: string, kw: string): string {
  if (!kw || !text) return escapeHtml(text)
  const escaped = escapeHtml(kw)
  const regex = new RegExp(`(${escaped.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark class="bg-yellow-200/70 text-red-700 font-semibold rounded px-0.5">$1</mark>')
}

function escapeHtml(str: string): string {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

/** 获取带高亮的预览 */
function getPreview(content: string): string {
  if (!content) return ''
  // 去除 markdown 标记
  let text = content.replace(/[#*`>\-\[\]!|]/g, '').trim()

  if (keyword.value && text.toLowerCase().includes(keyword.value.toLowerCase())) {
    const idx = text.toLowerCase().indexOf(keyword.value.toLowerCase())
    const start = Math.max(0, idx - 60)
    const end = Math.min(text.length, idx + keyword.value.length + 120)
    const preview =
      (start > 0 ? '... ' : '') +
      text.slice(start, end) +
      (end < text.length ? ' ...' : '')
    return highlight(preview, keyword.value)
  }

  return escapeHtml(text.slice(0, 180)) + (text.length > 180 ? ' ...' : '')
}

function formatRelativeTime(date: string): string {
  if (!date) return '-'
  const diff = Date.now() - new Date(date).getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return t('common.justNow')
  if (min < 60) return t('common.minutesAgo', { n: min })
  const hr = Math.floor(min / 60)
  if (hr < 24) return t('common.hoursAgo', { n: hr })
  const day = Math.floor(hr / 24)
  if (day < 30) return t('common.daysAgo', { n: day })
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.25s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 搜索结果预览内容换行 */
.result-preview {
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
}</style>
