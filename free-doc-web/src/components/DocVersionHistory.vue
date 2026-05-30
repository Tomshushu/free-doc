<template>
  <el-drawer
    :model-value="show"
    @update:model-value="(val: boolean) => emit('close', val)"
    :title="$t('components.versionHistory')"
    :size="480"
    direction="rtl"
    destroy-on-close
  >
    <div class="version-history">
      <div class="current-info mb-4 p-3 bg-blue-50 rounded-lg">
        <div class="flex items-center justify-between mb-1">
          <span class="text-xs font-medium text-blue-600">{{ $t('components.currentVersion') }}</span>
          <el-tag size="small" type="success">{{ $t('common.latest') }}</el-tag>
        </div>
        <p class="text-sm text-gray-600">{{ currentTime || '-' }}</p>
      </div>

      <div v-loading="loading">
        <template v-if="versions.length > 0">
          <TransitionGroup name="list" tag="div" class="space-y-2">
            <div
              v-for="(ver, idx) in versions"
              :key="ver.versionId"
              class="version-card"
              :class="{ 'is-current': ver.isCurrent, isPreviewing: previewId === ver.versionId }"
            >
              <div class="flex items-start gap-3">
                <div class="version-dot" :class="{ active: idx === 0 }"></div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-sm font-medium text-gray-700">
                      v{{ versions.length - idx }}
                      <el-tag v-if="ver.isCurrent" size="small" type="success" class="ml-1">{{ $t('common.current') }}</el-tag>
                    </span>
                    <span class="text-xs text-gray-400">{{ formatTime(ver.createTime) }}</span>
                  </div>
                  <p class="text-xs text-gray-400 truncate">
                    {{ ver.createUser || '-' }} · {{ formatSize(ver.docContent) }}
                    <template v-if="diffStats[ver.versionId]">
                      ·
                      <span :class="diffStats[ver.versionId].added > 0 ? 'text-green-500' : ''">+{{ diffStats[ver.versionId].added }}</span>
                      <span :class="diffStats[ver.versionId].removed > 0 ? 'text-red-500' : ''">-{{ diffStats[ver.versionId].removed }}</span>
                    </template>
                  </p>
                </div>
              </div>

              <div class="mt-2 flex gap-2 pl-[18px]">
                <el-button size="small" text type="primary" class="!text-xs" @click="toggleDiff(ver, idx)">
                  <i class="fa-solid fa-code-compare mr-1"></i>{{ previewId === ver.versionId ? $t('components.collapse') : $t('components.viewChanges') }}
                </el-button>
                <el-button
                  v-if="!ver.isCurrent"
                  size="small"
                  text
                  type="warning"
                  class="!text-xs !text-orange-500"
                  @click="handleRollback(ver)"
                >
                  <i class="fa-solid fa-clock-rotate-left mr-1"></i>{{ $t('components.rollback') }}
                </el-button>
              </div>

              <div v-if="previewId === ver.versionId && !diffLoading" class="diff-wrap mt-2 ml-[18px]">
                <div class="diff-head flex items-center px-3 py-1.5 bg-gray-100 border-b border-gray-200 text-xs font-medium text-gray-600">
                  <span><i class="fa-solid fa-file-lines mr-1"></i>{{ (ver.docContent || '').slice(0, 30) || $t('common.empty') }}</span>
                  <span v-if="prevContent !== null" class="text-gray-400 ml-auto">{{ $t('components.comparePrev') }}</span>
                </div>
                <div class="diff-body max-h-80 overflow-y-auto">
                  <template v-if="diffLines.length > 0">
                    <div v-for="(line, i) in diffLines" :key="i" :class="['diff-line', 'diff-' + line.type]">
                      <span class="ln-num">{{ line.num }}</span>
                      <pre class="ln-text"><code>{{ line.text }}</code></pre>
                    </div>
                  </template>
                  <div v-else class="p-4 text-center text-gray-400 text-sm">{{ $t('components.noDiff') }}</div>
                </div>
              </div>

              <div v-if="previewId === ver.versionId && diffLoading" class="mt-2 ml-[18px] p-6 text-center">
                <i class="fa-solid fa-spinner fa-spin text-primary text-xl block mb-2"></i>
                <span class="text-xs text-gray-400">{{ $t('components.calculatingDiff') }}</span>
              </div>
            </div>
          </TransitionGroup>
        </template>

        <div v-else-if="!loading" class="py-12 text-center text-gray-400">
          <i class="fa-solid fa-code-branch text-4xl mb-2 opacity-40 block"></i>
          <p class="text-sm">{{ $t('components.noVersionHistory') }}</p>
          <p class="text-xs mt-1">{{ $t('components.noVersionHistoryDesc') }}</p>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getDocVersions, getVersion, rollbackToVersion } from '@/api/doc'

const { t } = useI18n()

interface Ver {
  versionId: string
  docContent: string
  isCurrent: boolean
  createTime: string
  createUser: string
}

interface DiffStat { added: number; removed: number }
interface DLine { type: string; num: number; text: string }

const props = defineProps<{
  docId: string
  show?: boolean
}>()

const emit = defineEmits<{
  (e: 'close', val: boolean): void
  (e: 'rollback'): void
}>()

const loading = ref(false)
const diffLoading = ref(false)
const versions = ref<Ver[]>([])
const currentTime = ref<string>('-')
const previewId = ref<string | null>(null)
const prevContent = ref<string | null>(null)
const diffStats = reactive<Record<string, DiffStat>>({})
const diffLines = ref<DLine[]>([])

watch(() => props.show, (val) => {
  if (val) {
    loadVersions()
    closePreview()
  }
})

function closePreview() {
  previewId.value = null
  prevContent.value = null
  diffLines.value = []
}

async function loadVersions() {
  loading.value = true
  try {
    const list = await getDocVersions(props.docId)
    versions.value = list || []
    const cur = list.find((v) => v.isCurrent)
    if (cur) currentTime.value = cur.createTime
  } catch (e) {
    console.error(e)
    ElMessage.error(t('components.loadVersionFailed'))
  } finally {
    loading.value = false
  }
}

async function toggleDiff(ver: Ver, idx: number) {
  if (previewId.value === ver.versionId) {
    closePreview()
    return
  }

  previewId.value = ver.versionId
  diffLoading.value = true

  try {
    const detail = await getVersion(ver.versionId)
    let prevStr: string | null = null
    if (idx + 1 < versions.value.length) {
      try {
        const p = await getVersion(versions.value[idx + 1].versionId)
        prevStr = p.docContent || ''
      } catch (_) {}
    }
    prevContent.value = prevStr
    calcDiff(prevStr, detail.docContent || '', ver.versionId)
  } catch (_) {
    ElMessage.error(t('components.loadFailed'))
  } finally {
    diffLoading.value = false
  }
}

function calcDiff(oldS: string | null, newS: string, vid: string) {
  const oldLs = oldS ? oldS.split('\n') : []
  const newLs = newS.split('\n')
  let lines: DLine[] = []
  let oi = 0, nj = 0, ln = 1, add = 0, rm = 0

  while (oi < oldLs.length || nj < newLs.length) {
    const ol = oi < oldLs.length ? oldLs[oi] : null
    const nl = nj < newLs.length ? newLs[nj] : null
    if (ol === null) {
      lines.push({ type: 'add', num: ln++, text: nl || '' }); add++; nj++
    } else if (nl === null) {
      lines.push({ type: 'remove', num: ln++, text: ol }); rm++; oi++
    } else if (ol === nl) {
      lines.push({ type: 'normal', num: ln++, text: ol }); oi++; nj++
    } else if (newLs.indexOf(ol, nj) === nj && newLs.indexOf(ol, nj) >= 0) {
      lines.push({ type: 'remove', num: ln++, text: ol }); rm++; oi++
    } else if (oldLs.indexOf(nl, oi) === oi && oldLs.indexOf(nl, oi) >= 0) {
      lines.push({ type: 'add', num: ln++, text: nl }); add++; nj++
    } else {
      lines.push({ type: 'remove', num: ln++, text: ol })
      lines.push({ type: 'add', num: ln++, text: nl })
      rm++; add++; oi++; nj++
    }
  }
  diffLines.value = lines
  diffStats[vid] = { added: add, removed: rm }
}

async function handleRollback(ver: Ver) {
  const vn = versions.value.length - versions.value.indexOf(ver)
  try {
    await ElMessageBox.confirm(t('components.confirmRollback', { version: vn }), t('components.confirmRollbackLabel'),
      { type: 'warning', confirmButtonText: t('components.confirmRollbackLabel') })
    await rollbackToVersion(props.docId, ver.versionId)
    ElMessage.success(t('common.success'))
    emit('close', false)
    emit('rollback')
  } catch (_: any) { /* 取消 */ }
}

function formatSize(c?: string): string {
  if (!c) return '0B'
  try {
    const b = new Blob([c]).size
    return b < 1024 ? b + 'B' : (b / 1024).toFixed(1) + 'KB'
  } catch { return '-' }
}

function formatTime(d?: string): string {
  if (!d) return '-'
  const s = Date.now() - new Date(d).getTime()
  const m = Math.floor(s / 60000)
  if (m < 1) return t('common.justNow')
  if (m < 60) return t('common.minutesAgo', { n: m })
  const h = Math.floor(m / 60)
  return h < 24 ? t('common.hoursAgo', { n: h }) : t('common.daysAgo', { n: Math.floor(h / 24) })
}
</script>

<style scoped>
.version-history { padding: 0; }
.current-info { border-left: 3px solid #3b82f6; }
.version-card {
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: all .15s ease;
}
.version-card:hover { border-color:#e0e0e0; box-shadow:0 2px 8px rgba(0,0,0,.05); }
.version-card.is-current{ background:#f8fafc; border-color:#86efac; }
.version-card.isPreviewing{ border-color:#93c5fd; }
.version-dot{
  width:10px;height:10px;border-radius:50%;
  background:#d1d5db;margin-top:5px;flex-shrink:0;
}
.version-dot.active{
  background:#22c55e;
  box-shadow:0 0 0 3px rgba(34,197,94,.2);
}
.diff-wrap{border:1px solid #e5e7eb;border-radius:6px;overflow:hidden;}
.diff-head{position:sticky;top:0;z-index:1}
.diff-line{
  display:flex;align-items:stretch;min-height:20px;
  font-family:'SF Mono',Consolas,'Liberation Mono',Menlo,monospace;
  font-size:12px;line-height:20px;
}
.diff-line:hover{background:rgba(0,0,0,.02)}
.ln-num{
  display:inline-flex;align-items:center;justify-content:center;
  width:40px;min-width:40px;color:#8b949e;background:transparent;
  user-select:none;border-right:1px solid #eaeaea;flex-shrink:0;font-size:11px;
}
.ln-text{flex:1;margin:0;padding:0 10px;white-space:pre-wrap;word-break:break-all;overflow-x:auto}
.ln-text code{font-size:inherit;background:none}
.diff-add .ln-num{background:#dafbe5;color:#16a34a;border-right-color:#a7f3d0}
.diff-add .ln-text{background:#ecfdf5;color:#15803d}
.diff-remove .ln-num{background:#ffe4e6;color:#dc2626;border-right-color:#fecaca}
.diff-remove .ln-text{background:#fef2f2;color:#b91c1c;text-decoration:line-through;opacity:.7}
.diff-normal .ln-num{background:#fafafa}
.diff-normal .ln-text{background:#fff;color:#374151}
.list-enter-active,.list-leave-active{transition:all .25s ease}
.list-enter-from{opacity:0;transform:translateX(-10px)}
.list-leave-to{opacity:0;transform:translateX(10px)}
</style>
