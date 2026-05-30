<template>
  <div class="doc-view h-full w-full flex bg-gray-50 overflow-hidden">
    <aside class="w-64 bg-white border-r border-gray-200 flex flex-col shrink-0 overflow-hidden">
      <div class="px-4 py-3 border-b border-gray-100 text-xs text-gray-400 space-y-1">
        <div v-if="currentTeamName" class="flex items-center gap-1">
          <span class="cursor-pointer hover:text-primary" @click="$router.push('/')">{{ $t('doc.workspace') }}</span>
          <i class="fa-solid fa-chevron-right text-[8px]"></i>
          <span class="cursor-pointer hover:text-primary" @click="$router.push('/team/' + appStore.currentTeamId)">{{ currentTeamName }}</span>
        </div>
        <template v-if="projectInfo">
          <span v-if="currentTeamName"><i class="fa-solid fa-chevron-right text-[8px] mr-1"></i></span>
          <span>{{ projectInfo.projectName }}</span>
        </template>
      </div>

      <DirectoryTree :key="'dv-dt-' + appStore.currentProjectId" :data="directoryTreeData"
        :loading="dirLoading" :current-key="docId" :expanded-keys="expandedDirKeys"
        :title="$t('doc.docDirectory')" @node-click="handleNodeClick" @create-directory="handleCreateDir"
        @create-doc="handleCreateDoc" @add-sub-directory="(dir) => handleCreateDir(dir.id)"
        @create-doc-in-dir="(dirId) => handleCreateDoc(dirId)" @delete-directory="handleDeleteDir"
        @edit-doc="handleDocAction" @refresh="loadDirectories" />
    </aside>

    <main class="flex-1 min-w-0 w-0 flex flex-col bg-white relative">
      <header class="absolute top-0 left-0 right-0 h-[72px] bg-white border-b border-gray-100 px-8 shrink-0 flex items-center justify-between z-20">
        <div class="min-w-0 flex-1">
          <div v-if="directoryName" class="text-[11px] text-blue-500 mb-0.5">{{ projectInfo?.projectName }} / {{ directoryName }}</div>
          <h1 class="text-2xl font-bold text-gray-900 truncate mb-1.5 leading-tight">{{ doc?.docTitle || $t('doc.noTitle') }}</h1>
          <div class="flex items-center justify-between text-[11px] text-gray-400">
            <div class="flex items-center gap-4">
              <span class="flex items-center gap-1">
                <i class="fa-regular fa-user opacity-70"></i>
                {{ creator?.userName || doc?.createUser || '-' }} {{ $t('common.createdAt') }} {{ formatDate(doc?.createTime) }}
              </span>
            </div>
            <div class="flex items-center gap-4">
              <span class="flex items-center gap-1">
                <i class="fa-solid fa-rotate-right opacity-70"></i>
                {{ updater?.userName || doc?.updateUser || creator?.userName || doc?.createUser || '-' }} {{ $t('common.updatedAt') }} {{ formatDateTime(doc?.updateTime) }}
              </span>
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3 shrink-0 ml-8">
          <el-button size="default" class="!rounded-lg !px-4" @click="openShareDoc">
            <i class="fa-solid fa-share-nodes mr-1.5"></i>{{ $t('doc.share') }}
          </el-button>
          <el-button type="primary" size="default" class="!rounded-lg !px-5 !font-semibold !shadow-sm !shadow-blue-100 hover:!shadow-md hover:!shadow-blue-200 transition-all active:scale-95" @click="goEdit">
            <i class="fa-solid fa-pen-to-square mr-1.5"></i>{{ $t('doc.editDoc') }}
          </el-button>
          <el-button size="default" class="!rounded-lg !px-4" @click="goVersionHistory">
            <i class="fa-solid fa-clock-rotate-left mr-1.5"></i>{{ $t('doc.versionHistory') }}
          </el-button>
        </div>
      </header>

      <div v-if="loading" class="flex-1 mt-[72px] bg-white p-5 text-center overflow-y-auto">
        <i class="fa-solid fa-circle-notch fa-spin text-2xl mb-3 block"></i>{{ $t('doc.loading') }}
      </div>
      <article v-else-if="!doc" class="flex-1 mt-[72px] bg-white flex items-center justify-center overflow-y-auto">
        <div class="text-center text-gray-400"><i class="fa-solid fa-file-exclamation text-3xl mb-3 block opacity-30"></i><p>{{ $t('doc.docNotExist') }}</p></div>
      </article>
      <!-- 核心内容区域 -->
      <template v-else>
        <article v-if="doc?.docContent" ref="contentRef" class="flex-1 mt-[72px] min-w-0 min-h-0 bg-gray-50 flex flex-col overflow-y-auto overflow-x-hidden">
          <div class="markdown-wrapper min-w-0 flex-1 flex flex-col">
            <ApiMarkdownRenderer class="flex-1 w-full" :content="doc?.docContent" />
          </div>
        </article>
        <div v-else class="flex-1 mt-[72px] bg-white flex items-center justify-center overflow-y-auto">
          <div class="text-center text-gray-400">
            <i class="fa-solid fa-feather-pointed text-4xl mb-4 opacity-20 block"></i>
            <p class="text-base mb-2">{{ $t('doc.noDocContent') }}</p>
            <p class="text-xs opacity-60">{{ $t('doc.clickToEdit') }}</p>
          </div>
        </div>
      </template>
    </main>

    <!-- 版本历史抽屉 -->
    <el-drawer
      v-model="showVersionDrawer"
      :title="$t('doc.versionHistoryTitle')"
      direction="rtl"
      size="420px"
      :with-header="true"
    >
      <VersionTimeline
        :versions="versions"
        :current-version="currentVersionObj"
        @select="handleSelectVersion"
        @rollback="handleRollback"
      />
    </el-drawer>

    <!-- 分享对话框 -->
    <ShareDialog
      v-model:visible="showShareDialog"
      :target-type="shareTargetType"
      :target-id="shareTargetId"
      :target-name="shareTargetName"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import DirectoryTree from '@/components/DirectoryTree.vue'
import ApiMarkdownRenderer from '@/components/ApiMarkdownRenderer.vue'
import VersionTimeline from '@/components/VersionTimeline.vue'
import ShareDialog from '@/components/ShareDialog.vue'
import { getDocById } from '@/api/doc'
import { getUserById } from '@/api/user'
import { deleteDirectory as deleteDirectoryApi } from '@/api/directory'
import { deleteDoc as deleteDocApi } from '@/api/doc'
import { getProjectById } from '@/api/project'
import { getTeamById } from '@/api/team'
import { getDocVersions, rollbackToVersion } from '@/api/version'
import { useAppStore } from '@/stores/app'
import { useDirectoryStore } from '@/stores/directory'
import type { Doc, Project, User, DocVersion } from '@/types'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const appStore = useAppStore()
const directoryStore = useDirectoryStore()
const docId = route.params.docId as string
const doc = ref<Doc | null>(null)
const creator = ref<User | null>(null)
const updater = ref<User | null>(null)

// ===== 版本历史 =====
const showVersionDrawer = ref(false)
const versions = ref<DocVersion[]>([])
const currentVersionObj = computed(() => versions.value.find(v => v.isCurrent))

// ===== 分享功能 =====
const showShareDialog = ref(false)
const shareTargetType = ref<'PROJECT' | 'DOC'>('DOC')
const shareTargetId = ref('')
const shareTargetName = ref('')

const dirLoading = computed(() => directoryStore.loading)
const loading = ref(true)
const expandedDirKeys = computed(() => directoryStore.defaultExpandedKeys)
const contentRef = ref<HTMLElement>()
const projectInfo = ref<Project | null>(null)
const currentTeamName = ref('')
const directoryTreeData = computed(() => directoryStore.directoryTreeData)
const directoryName = computed(() => {
  if (!doc.value?.directoryId) return ''
  const dir = directoryStore.directories.find(d => d.id === doc.value?.directoryId)
  return dir?.name || ''
})

onMounted(async () => { await Promise.all([loadDoc(), loadDirectories()]) })
watch(() => route.params.docId, async (newId) => {
  if (newId && newId !== docId) { await loadDocFor(newId as string); await loadDirectories() }
}, { flush: 'post' })
watch(() => appStore.currentProjectId, async () => {}, { flush: 'post' })

async function loadDoc() {
  loading.value = true
  try {
    const data = await getDocById(docId)
    doc.value = data
    loadProjectInfo()
    loadVersions(docId)
    if (data && data.createUser) {
      loadCreatorInfo(data.createUser)
    }
    if (data && data.updateUser) {
      loadUpdaterInfo(data.updateUser)
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
async function loadDocFor(id: string) {
  try {
    const d = await getDocById(id)
    doc.value = d
    loadVersions(id)
    if (d && d.createUser) {
      loadCreatorInfo(d.createUser)
    }
    if (d && d.updateUser) {
      loadUpdaterInfo(d.updateUser)
    }
  }
  catch (e) { console.error(e) }
}

async function loadVersions(id: string) {
  try {
    versions.value = await getDocVersions(id)
  } catch (e) {
    console.error('Failed to load version:', e)
  }
}

async function handleSelectVersion(version: DocVersion) {
  // 预览版本逻辑可以后续添加，目前点击主要看回滚
  console.log('Selected version:', version)
}

async function handleRollback(versionId: string) {
  if (!doc.value) return
  const version = versions.value.find(v => v.versionId === versionId)
  const versionNum = version ? version.versionNum : ''
  
  try {
    await ElMessageBox.confirm(t('doc.confirmRestoreVersion', { version: versionNum }), t('doc.restoreVersion'), {
      confirmButtonText: t('doc.confirmRestore'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    await rollbackToVersion(doc.value.docId, versionId)
    ElMessage.success(t('doc.rollbackSuccess'))
    showVersionDrawer.value = false
    await loadDoc()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Rollback failed:', e)
      ElMessage.error(t('doc.rollbackFailed'))
    }
  }
}

function openShareDoc() {
  if (!doc.value) {
    ElMessage.warning(t('doc.docNotExistWarning'))
    return
  }
  shareTargetType.value = 'DOC'
  shareTargetId.value = doc.value.docId
  shareTargetName.value = doc.value.docTitle || t('share.doc')
  showShareDialog.value = true
}
async function loadCreatorInfo(userId: string) {
  try {
    creator.value = await getUserById(userId)
  } catch {
    creator.value = null
  }
}
async function loadUpdaterInfo(userId: string) {
  try {
    updater.value = await getUserById(userId)
  } catch {
    updater.value = null
  }
}
async function loadDirectories() {
  if (appStore.currentProjectId) await directoryStore.load(appStore.currentProjectId)
}
async function loadProjectInfo() {
  if (appStore.currentProjectId) {
    try {
      projectInfo.value = await getProjectById(appStore.currentProjectId)
      if (projectInfo.value?.teamId) currentTeamName.value = (await getTeamById(projectInfo.value.teamId)).teamName
    } catch {}
  }
}

function handleNodeClick(node: any) { if (node.isDoc && node.id !== docId) router.push(`/doc/${node.id}/view`) }
function handleCreateDir(parentId?: string) { 
  ElMessage.info(t('doc.goToWorkspace')) 
}
function handleCreateDoc(dir?: string) { router.push({ path: '/doc/new', query: { dir: dir || '' } }) }
function handleDeleteDir(id: string) {
  ElMessageBox.confirm(t('common.confirmDelete'),t('common.confirm'),{type:'warning'}).then(async()=>{await deleteDirectoryApi(id);ElMessage.success(t('doc.deleted'));await loadDirectories()}).catch(()=>{})
}
function handleDocAction(d: any) {
  if (d._delete) ElMessageBox.confirm(t('common.confirmDelete'),t('common.confirm'),{type:'warning'}).then(async()=>{await deleteDocApi(d.id);ElMessage.success(t('doc.deleted'));router.push('/')}).catch(()=>{})
  else router.push(`/doc/${d.id}`)
}
function goEdit() { router.push(`/doc/${docId}`) }
function goVersionHistory() { showVersionDrawer.value = true }
function formatDate(d?:string){return d?new Date(d).toLocaleDateString(locale.value):''}
function formatDateTime(d?:string){return d?new Date(d).toLocaleString(locale.value):''}
</script>

<style scoped>
/* 移动端适配等可以在此扩展 */
</style>
