<template>
  <div class="version-timeline">
    <!-- 版本对比弹窗 -->
    <el-dialog
      v-model="comparing"
      :title="`${$t('components.diffCompare')}：V${compareVersion?.versionNum} ${baseVersion ? 'vs V' + baseVersion.versionNum : '(' + $t('components.noPrevVersion') + ')'}`"
      width="85%"
      class="compare-dialog !rounded-xl"
      destroy-on-close
      append-to-body
    >
      <div v-if="compareVersion" class="compare-view p-1">
        <!-- Diff 视图 -->
        <div class="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
          <div class="px-3 py-2 bg-gray-50 text-xs font-medium text-gray-500 border-b flex justify-between items-center">
            <div class="flex items-center gap-4">
              <span><i class="fa-solid fa-arrows-left-right mr-1"></i>{{ $t('components.diffCompare') }}</span>
              <div class="flex gap-3">
                <span class="flex items-center gap-1"><i class="w-2 h-2 bg-red-400 rounded-full"></i> {{ $t('components.deleted') }}</span>
                <span class="flex items-center gap-1"><i class="w-2 h-2 bg-green-400 rounded-full"></i> {{ $t('components.added') }}</span>
              </div>
            </div>
            <div class="text-[10px] text-gray-400">
              {{ baseVersion ? $t('components.compareBase', { version: baseVersion.versionNum }) : $t('components.noPrevVersion') }}
            </div>
          </div>
          <div class="diff-content p-4 max-h-[60vh] overflow-y-auto font-mono text-sm leading-relaxed bg-white">
            <div
              v-for="(line, idx) in diffLines"
              :key="idx"
              :class="[
                line.type === 'added' ? 'bg-green-50 text-green-700' :
                line.type === 'removed' ? 'bg-red-50 text-red-700 line-through opacity-70' :
                'text-gray-600',
                'px-2 py-0.5 border-l-4 mb-px',
                line.type === 'added' ? 'border-green-400' :
                line.type === 'removed' ? 'border-red-400' :
                'border-transparent'
              ]"
            >
              <div class="flex">
                <span class="select-none w-10 shrink-0 opacity-30 text-right pr-3 mr-3 border-r border-gray-100">{{ line.num }}</span>
                <span class="whitespace-pre-wrap break-all flex-1">{{ line.text || ' ' }}</span>
              </div>
            </div>
            <div v-if="diffLines.length === 0" class="text-center py-12 text-gray-400">
              <i class="fa-solid fa-check-circle text-3xl mb-3 block opacity-20"></i>
              {{ $t('components.noContentChange') }}
            </div>
          </div>
        </div>

        <!-- 弹窗内部的恢复按钮（双重保险） -->
        <div class="mt-6 flex justify-center pb-2">
          <el-button 
            v-if="!compareVersion?.isCurrent" 
            type="primary" 
            size="large"
            class="!rounded-xl !px-12 !py-6 !font-bold shadow-lg transform hover:scale-105 transition-all"
            @click="handleRollbackFromDialog"
          >
            <i class="fa-solid fa-rotate-left mr-2 text-lg"></i>{{ $t('components.restoreToVersion', { version: compareVersion?.versionNum }) }}
          </el-button>
          <div v-else class="text-gray-400 text-sm italic">
            <i class="fa-solid fa-info-circle mr-1"></i>{{ $t('components.alreadyCurrentVersion') }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="flex justify-end gap-3 px-1">
          <el-button @click="comparing = false" class="!rounded-lg">{{ $t('components.closeWindow') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 版本时间线 -->
    <div class="timeline-wrapper relative">
      <div v-if="versions.length === 0" class="py-8 text-center text-gray-400">
        <i class="fa-solid fa-clock-rotate-left text-2xl mb-2 block"></i>
        {{ $t('components.noVersionRecords') }}
      </div>

      <div v-else class="relative pl-8">
        <!-- 时间线竖线 -->
        <div class="absolute left-[11px] top-2 bottom-2 w-0.5 bg-gray-200"></div>

        <div
          v-for="(ver, index) in versions"
          :key="ver.versionId"
          class="timeline-node relative pb-5 last:pb-0 group"
        >
          <!-- 圆点 -->
          <div
            :class="[
              'absolute left-[-23px] w-[22px] h-[22px] rounded-full border-2 flex items-center justify-center z-10',
              ver.isCurrent ? 'border-primary bg-primary shadow-md shadow-primary/30' : 'border-gray-300 bg-white'
            ]"
          >
            <i v-if="ver.isCurrent" class="fa-solid fa-check text-white text-[9px]"></i>
            <div v-else class="w-2 h-2 rounded-full bg-gray-300"></div>
          </div>

          <!-- 卡片 -->
          <div
            :class="[
              'rounded-lg border p-4 transition-all cursor-pointer',
              ver.isCurrent
                ? 'border-primary bg-blue-50/30 shadow-sm'
                : (comparing && compareVersion?.versionId === ver.versionId)
                  ? 'border-blue-400 bg-blue-50/50 shadow-md'
                  : 'border-gray-100 bg-white hover:border-blue-200 hover:shadow-sm'
            ]"
            @click="handleCardClick(ver)"
          >
            <div class="flex items-start justify-between">
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-gray-800 text-sm">
                    V{{ ver.versionNum }}
                  </span>
                  <el-tag
                    v-if="index === versions.length - 1"
                    size="small"
                    type="info"
                    effect="plain"
                  >
                    {{ $t('common.initial') }}
                  </el-tag>
                  <el-tag
                    v-if="ver.isCurrent"
                    size="small"
                    type="success"
                    effect="dark"
                  >
                    {{ $t('common.current') }}
                  </el-tag>
                </div>
                <div class="text-xs text-gray-400 mt-1.5 flex items-center gap-3">
                  <span><i class="fa-regular fa-clock mr-1"></i>{{ formatDate(ver.createTime) }}</span>
                  <span><i class="fa-regular fa-user mr-1"></i>{{ ver.createUser }}</span>
                </div>
              </div>

              <div v-if="ver.isCurrent" class="flex items-center">
                <el-tag size="small" type="success" effect="light" class="!rounded-md">
                  {{ $t('components.currentVersion') }}
                </el-tag>
              </div>
            </div>

            <!-- 内容预览 -->
            <div class="mt-3 text-xs text-gray-500 bg-gray-50/50 rounded-lg px-3 py-2 line-clamp-2 border border-gray-100/50">
              {{ getContentPreview(ver.docContent) }}
            </div>

          <!-- 操作按钮：显式按钮 -->
          <div class="mt-4 pt-3 border-t border-gray-100 flex items-center justify-end">
            <el-button 
                size="small" 
                class="!rounded-md !px-4 w-full"
                type="primary"
                plain
                @click.stop="openCompare(ver)"
              >
                <i class="fa-solid fa-magnifying-glass mr-1.5 text-[10px]"></i>{{ ver.isCurrent ? $t('common.refresh') : $t('components.viewAndRestore') }}
              </el-button>
          </div>

            <!-- 内容哈希标识 -->
            <div class="mt-2 flex items-center justify-between">
              <span class="text-[10px] text-gray-300 font-mono">
                SHA256: {{ ver.contentHash?.slice(0, 12) }}...
              </span>
              <el-tag
                v-if="hasDiff(ver)"
                size="small"
                type="info"
                effect="plain"
                class="!h-5 !text-[9px] !px-1.5"
              >
                {{ $t('components.changed') }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DocVersion } from '@/types'

const { t } = useI18n()

const props = defineProps<{
  versions: DocVersion[]
  currentVersion?: DocVersion
}>()

const emit = defineEmits<{
  (e: 'select', version: DocVersion): void
  (e: 'rollback', versionId: string): void
}>()

const comparing = ref(false)
const baseVersion = ref<DocVersion | null>(null)
const compareVersion = ref<DocVersion | null>(null)

interface DiffLine {
  num: number
  text: string
  type: 'same' | 'added' | 'removed'
}

const diffLines = computed<DiffLine[]>(() => {
  if (!compareVersion.value) return []
  // 对比选中版本与上一个版本
  const oldText = baseVersion.value?.docContent?.split('\n') || []
  const newText = compareVersion.value.docContent?.split('\n') || []

  // 简单的逐行 diff 算法
  return simpleLcsDiff(oldText, newText)
})

function simpleLcsDiff(oldLines: string[], newLines: string[]): DiffLine[] {
  const result: DiffLine[] = []
  let oldIdx = 0
  let newIdx = 0
  let lineNum = 1

  while (oldIdx < oldLines.length || newIdx < newLines.length) {
    if (oldIdx >= oldLines.length) {
      // 剩余都是新增
      result.push({ num: lineNum++, text: newLines[newIdx], type: 'added' })
      newIdx++
    } else if (newIdx >= newLines.length) {
      // 剩余都是删除
      result.push({ num: lineNum++, text: oldLines[oldIdx], type: 'removed' })
      oldIdx++
    } else if (oldLines[oldIdx] === newLines[newIdx]) {
      result.push({ num: lineNum++, text: oldLines[oldIdx], type: 'same' })
      oldIdx++
      newIdx++
    } else {
      // 尝试向前查找匹配
      let foundNew = -1
      for (let i = newIdx + 1; i < newLines.length && i <= newIdx + 3; i++) {
        if (newLines[i] === oldLines[oldIdx]) {
          foundNew = i
          break
        }
      }

      if (foundNew !== -1) {
        while (newIdx < foundNew) {
          result.push({ num: lineNum++, text: newLines[newIdx], type: 'added' })
          newIdx++
        }
      } else {
        result.push({ num: lineNum++, text: oldLines[oldIdx], type: 'removed' })
        oldIdx++
      }
    }
  }

  return result
}

function openCompare(version: DocVersion) {
  // 查找选中版本在列表中的索引
  const index = props.versions.findIndex(v => v.versionId === version.versionId)
  
  // 对比相邻版本：选中版本(compare) vs 上一个版本(base)
  // 注意：versions 数组通常是按时间倒序排列的（最新的在前）
  compareVersion.value = version
  
  // 如果有上一个版本（索引更大），则作为基准
  if (index !== -1 && index < props.versions.length - 1) {
    baseVersion.value = props.versions[index + 1]
  } else {
    // 如果是初始版本，基准设为空
    baseVersion.value = null
  }
  
  comparing.value = true
}

function handleRollbackFromDialog() {
  if (compareVersion.value) {
    emit('rollback', compareVersion.value.versionId)
    comparing.value = false
  }
}

function handleCardClick(version: DocVersion) {
  openCompare(version)
  
  // 触发 select 事件，允许父组件做额外处理（如预览）
  emit('select', version)
}

function hasDiff(ver: DocVersion): boolean {
  return !!(ver.diffContent && ver.diffContent.trim())
}

function formatDate(date: string | undefined): string {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getContentPreview(content: string | undefined): string {
  if (!content) return t('common.empty')
  // 只去除一些极端的标记，保留基本结构
  const text = content.replace(/[#*`>\-\[\]]/g, ' ').replace(/\s+/g, ' ').trim()
  return text.slice(0, 80) + (text.length > 80 ? '...' : '')
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.diff-content div:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.compare-view {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
