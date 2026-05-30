<template>
  <div class="project-workspace h-full overflow-auto bg-gradient-to-br from-blue-50 via-white to-indigo-50 pt-0.5 pb-0 px-1 sm:px-2 lg:px-3">
    <!-- 居中卡片容器 -->
    <div class="mx-auto w-full max-w-[1500px] h-[calc(100%-10px)] bg-white rounded-2xl shadow-xl overflow-hidden flex">
      <ResizableSplit class="flex-1 w-full" :initial-width="260" :min-width="200" :max-width="400">
        <template #left>
          <div class="h-full flex flex-col">
            <!-- 搜索框 + 标题栏 -->
            <div class="px-3 pt-2.5 pb-2 border-b border-gray-100 shrink-0 space-y-2">
              <div class="flex items-center justify-between">
                <h2 class="text-xs font-semibold text-gray-700">{{ $t('layout.docDirectory') }}</h2>
<el-dropdown trigger="click" @command="handleDirCommand">
  <div class="cursor-pointer p-1.5 rounded hover:bg-gray-100 transition-colors text-gray-500 hover:text-gray-700 flex items-center justify-center">
    <i class="fa-solid fa-gear text-[16px]"></i>
  </div>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item command="share">
        <i class="fa-solid fa-share-nodes mr-2 text-indigo-500"></i>{{ $t('project.shareProject') }}
      </el-dropdown-item>
      <el-dropdown-item command="exportProject">
        <i class="fa-solid fa-file-export mr-2 text-green-500"></i>{{ $t('project.exportProject') }}
      </el-dropdown-item>
      <el-dropdown-item v-if="canEditProject" command="createDir" divided>
        <i class="fa-solid fa-folder-plus mr-2 text-blue-500"></i>{{ $t('project.newDirectory') }}
      </el-dropdown-item>
      <el-dropdown-item v-if="canEditProject" command="createDoc">
        <i class="fa-solid fa-file-circle-plus mr-2"></i>{{ $t('project.newDoc') }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
              </div>
              <el-input
                v-model="searchKeyword"
                :placeholder="$t('project.searchDocPlaceholder')"
                size="small"
                clearable
                class="!rounded-lg"
              >
                <template #prefix>
                  <div class="h-full flex items-center pl-0.5">
                    <i class="fa-solid fa-search text-gray-400 text-[12px]"></i>
                  </div>
                </template>
              </el-input>
              <span v-if="searchKeyword && filteredDirectoryData.length !== directoryData.length" class="text-[10px] text-gray-400 block -mt-1">
                {{ filteredDirectoryData.filter(d => d.isDoc).length }} / {{ directoryData.filter(d => d.isDoc).length }}
              </span>
            </div>

            <!-- 目录树 -->
            <div class="flex-1 overflow-y-auto min-h-0">
              <template v-if="searchKeyword && filteredDirectoryData.length === 0">
                <div class="text-center text-gray-400 text-sm py-8">
                  <i class="fa-solid fa-search mb-2 block opacity-30 text-2xl"></i>
                  {{ $t('project.noMatchingDoc') }}
                </div>
              </template>
              <DirectoryTree
                v-else
                :key="'pw-dt-' + projectId + (searchKeyword ? '-s' : '')"
                :data="filteredDirectoryData"
                :loading="dirLoading"
                :current-key="currentDocId || ''"
                :expanded-keys="expandedKeys"
                :can-edit="canEditProject"
                title=""
                @node-click="handleTreeNodeClick"
                @create-directory="handleCreateDir"
                @create-doc="handleCreateDoc"
                @add-sub-directory="handleCreateDir"
                @create-doc-in-dir="(dirId) => handleCreateDocInDir(dirId)"
                @delete-directory="handleDeleteDir"
                @edit-doc="handleDocAction"
                @share-doc="handleShareDoc"
                @export-current-dir="handleExportCurrentDir"
                @export-doc="handleExportDoc"
                @refresh="loadDirectories"
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
                <p class="text-sm">{{ $t('project.selectDocFromLeft') }}</p>
              </div>
            </div>

            <!-- 文档内容 -->
            <div v-else class="flex-1 flex flex-col overflow-hidden min-w-0">
              <!-- 文档头部 -->
              <header class="shrink-0 py-4 pr-7 border-b border-gray-100 flex items-start justify-between min-w-0">
                <div class="flex-1 min-w-0">
                  <h1 class="text-lg font-bold text-gray-900">{{ currentDoc.docTitle }}</h1>
                  <div class="flex items-center justify-between mt-1.5 text-[11px] text-gray-400">
                    <div class="flex items-center gap-4">
                      <span class="flex items-center gap-1">
                        <i class="fa-regular fa-user opacity-70"></i>
                        {{ creator?.userName || currentDoc.createUserName || currentDoc.createUser || '-' }} {{ $t('common.createdAt') }} {{ formatDate(currentDoc.createTime) }}
                      </span>
                    </div>
                    <div class="flex items-center gap-4 pr-2">
                      <span class="flex items-center gap-1">
                        <i class="fa-solid fa-rotate-right opacity-70"></i>
                        {{ updater?.userName || currentDoc.updateUserName || currentDoc.updateUser || creator?.userName || currentDoc.createUser || '-' }} {{ $t('common.updatedAt') }} {{ formatDateTime(currentDoc.updateTime) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="pl-4 shrink-0 pt-1 flex gap-2">
<el-button size="default" class="!rounded-lg !px-4" @click="openShareDoc">
  <i class="fa-solid fa-share-nodes mr-1.5"></i>{{ $t('project.share') }}
</el-button>
<el-button size="default" class="!rounded-lg !px-4" @click="showVersionDrawer = true">
  <i class="fa-solid fa-clock-rotate-left mr-1.5"></i>{{ $t('project.versionHistory') }}
</el-button>
<el-button 
  v-if="canEditProject" 
  type="primary" 
  size="default" 
  class="!rounded-lg !font-semibold !shadow-sm !shadow-blue-200 hover:!shadow-md hover:!shadow-blue-300 transition-shadow" 
  @click="openEditDialog(currentDoc.docId)"
>
  <i class="fa-solid fa-pen-to-square mr-1.5"></i>{{ $t('project.editDoc') }}
</el-button>
<el-button 
  v-else
  size="default" 
  class="!rounded-lg !px-4" 
  disabled
  :title="$t('project.editDocNoPermission')"
>
  <i class="fa-solid fa-pen-to-square mr-1.5 text-gray-400"></i>{{ $t('project.editDoc') }}
</el-button>
                </div>
              </header>

              <!-- 文档正文 -->
              <article class="flex-1 overflow-y-auto min-w-0">
                <div class="markdown-wrapper min-w-0 w-full">
                  <ApiMarkdownRenderer class="min-w-0 w-full" :content="currentDoc.docContent || ''" />
                </div>
              </article>
            </div>
          </div>
        </template>
      </ResizableSplit>
    </div>

    <!-- ========== 博客园风格编辑器全屏覆盖 ========== -->
    <Transition name="editor-fade">
      <div v-if="showEditorDialog" class="fixed inset-0 z-50 flex flex-col bg-[#f0f2f5]">
        <!-- 顶部导航栏 -->
        <div class="shrink-0 flex items-center justify-between px-5 py-2.5 bg-white border-b border-gray-200 shadow-sm z-10">
          <div class="flex items-center gap-3">
            <button class="p-1.5 rounded hover:bg-gray-100 transition-colors text-gray-500 hover:text-gray-700" :title="$t('common.back')" @click="closeEditorDialog">
              <i class="fa-solid fa-arrow-left text-sm"></i>
            </button>
            <span class="text-sm font-medium text-gray-700">{{ $t('project.editDocTitle') }}</span>
          </div>
          <div class="flex items-center gap-3 text-xs text-gray-400">
            <span>{{ editWordCount }} {{ $t('common.word') }} · {{ editLineCount }} {{ $t('common.line') }}</span>
            <span v-if="editLastSavedTime !== '-'">{{ $t('project.lastSaved') }} {{ editLastSavedTime }}</span>
          </div>
        </div>

        <!-- 表单区 -->
        <div class="shrink-0 bg-white mx-4 mt-3 rounded-lg border border-gray-200 shadow-sm overflow-hidden">
          <!-- 标题 -->
          <div class="px-4 py-3 border-b border-gray-100 flex items-center gap-4">
            <input
              v-model="editTitle"
              class="flex-1 text-lg font-semibold text-gray-900 outline-none border-none placeholder:text-gray-300 placeholder:text-lg"
              :placeholder="$t('project.docTitlePlaceholder')"
              @input="editHasChanges = true"
            />
          </div>
        </div>

        <!-- 编辑器主体：左编辑 + 右预览（可拖拽调整） -->
        <div ref="editorSplitRef" class="flex-1 mx-4 mt-3 mb-3 flex overflow-hidden" @mousemove="onEditorSplitMove" @mouseup="onEditorSplitEnd" @mouseleave="onEditorSplitEnd">
          <!-- 左侧编辑区 -->
          <div class="bg-white rounded-l-lg border border-gray-200 shadow-sm overflow-hidden flex flex-col" :style="{ width: editorLeftPct + '%' }">
            <!-- 简易工具栏 -->
            <div class="shrink-0 flex items-center gap-1 px-2 py-1.5 bg-[#fafafa] border-b border-[#e8e8e8] relative">
              <button v-for="btn in mdToolbar" :key="btn.label" :title="btn.label"
                class="md-toolbar-btn" @click="btn.label === t('project.mdEmoji') ? toggleEmojiPicker($event) : insertMdSyntax(btn)">
                <i v-if="btn.icon" :class="btn.icon"></i>
                <span v-else>{{ btn.label }}</span>
              </button>
            </div>
            <!-- 表情包选择器面板 -->
            <Teleport to="body">
              <div v-if="showEmojiPicker"
              class="emoji-picker-panel fixed z-[10000] bg-white rounded-xl shadow-2xl border border-gray-200 overflow-hidden"
                :style="{ left: emojiPickerPos.x + 'px', top: emojiPickerPos.y + 'px' }"
                @click.stop>
                <!-- 分类标签 -->
                <div class="flex items-center gap-0.5 px-2 py-1.5 bg-gray-50 border-b border-gray-100">
                  <button v-for="(cat, catKey) in emojiCategories" :key="catKey"
                    class="px-2 py-1 rounded-md text-xs font-medium transition-colors"
                    :class="activeEmojiCat === catKey ? 'bg-blue-100 text-blue-700' : 'text-gray-500 hover:bg-gray-200 hover:text-gray-700'"
                    @click="activeEmojiCat = catKey">
                    {{ cat.icon }}
                  </button>
                </div>
                <!-- 表情网格 -->
                <div class="grid grid-cols-10 gap-0.5 p-2 max-h-[240px] overflow-y-auto">
                  <button v-for="(emoji, code) in emojiCategories[activeEmojiCat].items" :key="code"
                    class="w-8 h-8 flex items-center justify-center text-lg rounded hover:bg-blue-50 transition-colors cursor-pointer"
                    :title="code"
                    @click="insertEmoji(emoji, code)">
                    {{ emoji }}
                  </button>
                </div>
                <div class="px-3 py-1.5 border-t border-gray-100 text-[11px] text-gray-400 bg-gray-50">
                  {{ $t('project.emojiHint') }}
                </div>
              </div>
            </Teleport>
            <textarea
              ref="mdTextareaRef"
              v-model="previewContent"
              class="md-textarea"
              :placeholder="$t('project.markdownPlaceholder')"
              spellcheck="false"
              @input="onMdInput"
              @keydown.tab.prevent="handleMdTab"
              @scroll="syncMdScroll"
            ></textarea>
          </div>
          <!-- 拖拽分割条 -->
          <div class="w-1.5 cursor-col-resize bg-gray-100 hover:bg-blue-300 transition-colors flex-shrink-0 relative z-10"
            @mousedown.prevent="onEditorSplitStart">
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-1 h-8 rounded-full bg-gray-300"></div>
          </div>
          <!-- 右侧：ApiMarkdownRenderer 预览 -->
          <div class="flex-1 bg-white rounded-r-lg border border-l-0 border-gray-200 shadow-sm overflow-y-auto min-w-0">
            <div class="editor-preview-area">
              <ApiMarkdownRenderer :content="previewContent" />
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="shrink-0 flex items-center justify-between px-5 py-2.5 bg-white border-t border-gray-200">
          <!-- 左侧：保存状态 + 自动保存开关 -->
          <div class="flex items-center gap-4">
            <div :class="[
              'flex items-center gap-1.5 px-2.5 py-1 rounded text-xs font-medium transition-all',
              editSaving ? 'bg-amber-50 text-amber-600' :
              editSaved ? 'bg-emerald-50 text-emerald-600' :
              editHasChanges ? 'bg-orange-50 text-orange-600' : 'bg-gray-50 text-gray-400'
            ]">
              <span v-if="editSaving" class="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse"></span>
              <i v-else-if="editSaved" class="fa-solid fa-check text-[10px]"></i>
              <span v-else-if="editHasChanges" class="w-1.5 h-1.5 rounded-full bg-orange-400"></span>
              {{ editSaving ? $t('common.saving') : editSaved ? $t('common.saved') : editHasChanges ? $t('common.unsaved') : '' }}
            </div>

            <label class="flex items-center gap-1.5 cursor-pointer select-none text-xs text-gray-500 hover:text-gray-700">
              <span>{{ $t('project.autoSave') }}</span>
              <div
                :class="autoSaveEnabled ? 'bg-blue-500' : 'bg-gray-300'"
                class="relative w-8 h-[18px] rounded-full transition-colors"
                @click="autoSaveEnabled = !autoSaveEnabled"
              >
                <div
                  :class="autoSaveEnabled ? 'translate-x-[17px]' : 'translate-x-[2px]'"
                  class="absolute top-[2px] w-[14px] h-[14px] rounded-full transition-transform"
                  style="background-color: #fff; box-shadow: 0 0 1px rgba(0,0,0,0.1);"
                ></div>
              </div>
            </label>
          </div>

          <!-- 右侧：操作按钮 -->
          <div class="flex items-center gap-2">
            <button
              class="px-4 py-1.5 text-sm text-gray-600 hover:text-gray-800 bg-gray-100 hover:bg-gray-200 rounded transition-colors"
              @click="closeEditorDialog"
            >
              {{ $t('common.cancel') }}
            </button>
            <button
              class="px-4 py-1.5 text-sm text-white bg-blue-600 hover:bg-blue-700 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="editSaving || !editHasChanges"
              @click="saveEditedDoc"
            >
              <i class="fa-solid fa-floppy-disk mr-1 text-[11px]"></i>{{ $t('common.save') }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 新建目录弹窗 -->
    <el-dialog v-model="showCreateDir" :title="$t('project.newDirectory')" width="400px" destroy-on-close>
      <el-form :model="newDirForm" label-width="80px">
        <el-form-item :label="$t('common.name')" required>
          <el-input v-model="newDirForm.name" :placeholder="$t('project.directoryNamePlaceholder')" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDir = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newDirForm.name.trim()" @click="doCreateDirectory">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 新建文档弹窗 -->
    <el-dialog v-model="showCreateDoc" :title="$t('project.newDoc')" width="400px" destroy-on-close>
      <el-form :model="newDocForm" label-width="80px">
        <el-form-item :label="$t('common.title')" required>
          <el-input v-model="newDocForm.title" :placeholder="$t('project.docTitlePlaceholder2')" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDoc = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newDocForm.title.trim()" @click="doCreateDocument">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 版本历史抽屉 -->
    <el-drawer
      v-model="showVersionDrawer"
      :title="$t('project.versionHistoryTitle')"
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

    <!-- 导出对话框 -->
    <ExportDialog
      v-model="showExportDialog"
      :export-options="exportOptions"
      :doc-info="exportDocInfo"
      :batch-info="exportBatchInfo"
      @export-success="handleExportSuccess"
      @export-error="handleExportError"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DirectoryTree from '@/components/DirectoryTree.vue'
import ResizableSplit from '@/components/ResizableSplit.vue'
import ApiMarkdownRenderer from '@/components/ApiMarkdownRenderer.vue'
import VersionTimeline from '@/components/VersionTimeline.vue'
import ShareDialog from '@/components/ShareDialog.vue'
import ExportDialog from '@/components/ExportDialog.vue'
import { getDocById, getDocByIdWithUser, getProjectDocs, createDoc, updateDoc, deleteDoc } from '@/api/doc'
import { getProjectById, getProjectMembers } from '@/api/project'
import {
  getProjectDirectories,
  createDirectory as createDirectoryApi,
  deleteDirectory as deleteDirectoryApi
} from '@/api/directory'
import { getDocVersions, rollbackToVersion } from '@/api/version'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { Doc, Directory, User, DocVersion, ExportOptions, BatchExportInfo } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const appStore = useAppStore()
const userStore = useUserStore()

// ===== 基础数据 =====
const projectId = route.params.projectId as string
const projectName = computed(() => appStore.currentProjectName)
const currentDocId = ref<string | null>(null)
const currentDoc = ref<Doc | null>(null)
const creator = ref<User | null>(null)
const updater = ref<User | null>(null)

// ===== 项目成员权限 =====
const projectMembers = ref<any[]>([])
const currentMemberInfo = ref<any>(null)

// ===== 版本历史 =====
const showVersionDrawer = ref(false)
const versions = ref<DocVersion[]>([])
const currentVersionObj = computed(() => versions.value.find(v => v.isCurrent))

// ===== 项目成员权限 =====
const isProjectOwner = computed(() => currentMemberInfo.value?.type === 'OWNER')
const canEditProject = computed(() => {
  if (!currentMemberInfo.value) return false
  return currentMemberInfo.value.type === 'OWNER' || currentMemberInfo.value.permission === 'rw'
})

// ===== 目录树 =====
const directoryData = ref<any[]>([])
const dirLoading = ref(false)
const expandedKeys = ref<string[]>([])

// ===== 新建目录/文档弹窗 =====
const showCreateDir = ref(false)
const newDirForm = ref({ name: '', pid: '0' })
const showCreateDoc = ref(false)
const newDocForm = ref({ title: '', directoryId: '' })

// ===== 编辑器弹窗 =====
const showEditorDialog = ref(false)

// 文档标题
const editTitle = ref('')
let editOriginalTitle = ''

// 预览内容（实时同步到 ApiMarkdownRenderer）
const previewContent = ref('')

// 编辑器左右分割拖拽
const editorSplitRef = ref<HTMLElement>()
const editorLeftPct = ref(50)
let editorSplitDragging = false
let editorSplitStartX = 0
let editorSplitStartPct = 0

function onEditorSplitStart(e: MouseEvent) {
  editorSplitDragging = true
  editorSplitStartX = e.clientX
  editorSplitStartPct = editorLeftPct.value
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

function onEditorSplitMove(e: MouseEvent) {
  if (!editorSplitDragging) return
  const container = editorSplitRef.value
  if (!container) return
  const dx = e.clientX - editorSplitStartX
  const containerWidth = container.clientWidth - 6
  const dPct = (dx / containerWidth) * 100
  editorLeftPct.value = Math.min(Math.max(15, editorSplitStartPct + dPct), 85)
}

function onEditorSplitEnd() {
  if (!editorSplitDragging) return
  editorSplitDragging = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

// Markdown 纯源码编辑器
const mdTextareaRef = ref<HTMLTextAreaElement>()
interface MdToolbarBtn { label: string; icon?: string; syntax?: string; openLen?: number; selectLen?: number }
const mdToolbar = computed(() => [
  { label: 'H1', icon: '', syntax: '# ', openLen: 2 },
  { label: 'H2', icon: '', syntax: '## ', openLen: 3 },
  { label: 'H3', icon: '', syntax: '### ', openLen: 4 },
  { label: 'H4', icon: '', syntax: '#### ', openLen: 5 },
  { label: 'H5', icon: '', syntax: '##### ', openLen: 6 },
  { label: 'H6', icon: '', syntax: '###### ', openLen: 7 },
  { label: '|', icon: '' },
  { label: 'B', icon: 'fa-solid fa-bold', syntax: '****', openLen: 2 },
  { label: 'I', icon: 'fa-solid fa-italic', syntax: '**', openLen: 1 },
  { label: 'BI', icon: 'fa-solid fa-bold', syntax: '******', openLen: 3 },
  { label: 'S', icon: 'fa-solid fa-strikethrough', syntax: '~~~~', openLen: 2 },
  { label: 'U', icon: 'fa-solid fa-underline', syntax: '<u></u>', openLen: 3 },
  { label: 'M', icon: 'fa-solid fa-highlighter', syntax: '====', openLen: 2 },
  { label: '|', icon: '' },
  { label: t('project.mdList'), icon: 'fa-solid fa-list-ul', syntax: '- ' },
  { label: t('project.mdOrderedList'), icon: 'fa-solid fa-list-ol', syntax: '1. ' },
  { label: t('project.mdTaskList'), icon: 'fa-regular fa-square-check', syntax: '- [ ] ' },
  { label: t('project.mdQuote'), icon: 'fa-solid fa-quote-left', syntax: '> ' },
  { label: '|', icon: '' },
  { label: t('project.mdCode'), icon: 'fa-solid fa-code', syntax: '``', openLen: 1 },
  { label: t('project.mdCodeBlock'), icon: 'fa-solid fa-file-code', syntax: '\n```\n\n```\n', openLen: 5 },
  { label: t('project.mdTable'), icon: 'fa-solid fa-table', syntax: '\n| ' + t('project.mdCol1') + ' | ' + t('project.mdCol2') + ' | ' + t('project.mdCol3') + ' |\n|------|------|------|\n|      |      |      |' },
  { label: '|', icon: '' },
  { label: t('project.mdLink'), icon: 'fa-solid fa-link', syntax: '[](url)', openLen: 1, selectLen: 3 },
  { label: t('project.mdImage'), icon: 'fa-solid fa-image', syntax: '![](url)', openLen: 2, selectLen: 3 },
  { label: '|', icon: '' },
  { label: t('project.mdSup'), icon: 'fa-solid fa-superscript', syntax: '^^', openLen: 1 },
  { label: t('project.mdSub'), icon: 'fa-solid fa-subscript', syntax: '~~', openLen: 1 },
  { label: '|', icon: '' },
  { label: t('project.mdHorizontalRule'), icon: 'fa-solid fa-minus', syntax: '\n---\n' },
  { label: '|', icon: '' },
  { label: t('project.mdEmoji'), icon: 'fa-regular fa-face-smile', syntax: ':smile:', openLen: 1, selectLen: 5 },
])

// 表情包选择器
const showEmojiPicker = ref(false)
const activeEmojiCat = ref('people')
const emojiPickerPos = ref({ x: 0, y: 0 })

const emojiCategories: Record<string, { icon: string; items: Record<string, string> }> = {
  people: {
    icon: '😀',
    items: {
      smile: '😊', grin: '😁', joy: '😂', rofl: '🤣', wink: '😉',
      blush: '😊', heart_eyes: '😍', kissing_heart: '😘', thinking: '🤔',
      neutral_face: '😐', expressionless: '😑', unamused: '😒', sweat: '😓',
      sad: '😢', cry: '😭', angry: '😠', rage: '🤬',
      smirk: '😏', tongue: '😛', tongue_out_wink: '😜', zipper_mouth: '🤐',
      sleepy: '😪', dizzy: '😵', face_with_cowboy_hat: '🤠', party: '🥳',
      cold_face: '🥶', hot_face: '🥵', nauseated: '🤢', sneezing: '🤧',
      hugging: '🤗', clapping_hands: '👏', waving_hand: '👋', ok_hand: '👌',
      thumbsup: '👍', thumbsdown: '👎', fist: '✊', palm: '🖐️',
    }
  },
  nature: {
    icon: '🌿',
    items: {
      dog: '🐶', cat: '🐱', mouse: '🐭', rabbit: '🐰', bear: '🐻',
      panda: '🐼', koala: '🐨', tiger: '🐯', lion: '🦁', cow: '🐮',
      pig: '🐷', frog: '🐸', snake: '🐍', bird: '🐦', chicken: '🐔',
      penguin: '🐧', butterfly: '🦋', bug: '🐛', snail: '🐌', bee: '🐝',
      rose: '🌹', tulip: '🌷', cherry_blossom: '🌸', sunflower: '🌻', bouquet: '💐',
      apple: '🍎', banana: '🍌', watermelon: '🍉', grape: '🍇', strawberry: '🍓',
      peach: '🍑', cherries: '🍒', leaf: '🍃', seedling: '🌱', evergreen_tree: '🌲',
    }
  },
  food: {
    icon: '🍕',
    items: {
      coffee: '☕', tea: '🍵', beer: '🍺', wine: '🍷', cocktail: '🍸',
      cake: '🎂', cookie: '🍪', candy: '🍬', lollipop: '🍭', chocolate_bar: '🍫',
      pizza: '🍔', fries: '🍟', taco: '🌮', sushi: '🍣', rice: '🍚',
      egg: '🥚', milk: '🥛', bread: '🍞', croissant: '🥐', pancake: '🥞',
      ice_cream: '🍨', icecream: '🍦', dumpling: '🥟', sandwich: '🥪', salad: '🥗',
      popcorn: '🍿', cheese: '🧀', bacon: '🥓', steak: '🥩', shrimp: '🦐',
    }
  },
  activity: {
    icon: '⚽',
    items: {
      soccer: '⚽', basketball: '🏀', football: '🏈', baseball: '⚾', tennis: '🎾',
      volleyball: '🏐', rugby_football: '🏉', golf: '⛳', checkered_flag: '🏁', trophy: '🏆',
      musical_keyboard: '🎹', guitar: '🎸', violin: '🎻', drum: '🥁', headphones: '🎧',
      video_game: '🎮', joystick: '🕹️', dart: '🎯', bowling: '🎳', fishing_pole_and_fish: '🎣',
      bicycle: '🚲', car: '🚗', bus: '🚌', airplane: '✈️', rocket: '🚀',
      fire: '🔥', star: '⭐', sparkles: '✨', boom: '💥', collision: '💥',
    }
  },
  symbol: {
    icon: '❤️',
    items: {
      heart: '❤️', orange_heart: '🧡', yellow_heart: '💛', green_heart: '💚',
      blue_heart: '💙', purple_heart: '💜', broken_heart: '💔', two_hearts: '💕',
      '100': '💯', copyright: '©️', registered: '®️', tm: '™️',
      check: '✅', x: '❌', warning: '⚠️', bulb: '💡',
      lock: '🔒', unlock: '🔓', key: '🔑', link: '🔗',
      arrow_up: '⬆️', arrow_down: '⬇️', arrow_left: '⬅️', arrow_right: '➡️',
      recycle: '♻️', no_entry: '⛔', exclamation: '❗', question: '❓',
    }
  }
}

function toggleEmojiPicker(e: MouseEvent) {
  if (showEmojiPicker.value) {
    showEmojiPicker.value = false
    return
  }
  const btn = (e.target as HTMLElement).closest('.md-toolbar-btn')
  if (!btn) return
  const rect = btn.getBoundingClientRect()
  emojiPickerPos.value = {
    x: Math.min(rect.left, window.innerWidth - 360),
    y: rect.bottom + 4
  }
  showEmojiPicker.value = true
  activeEmojiCat.value = 'people'
}

function insertEmoji(emoji: string, code: string) {
  const ta = mdTextareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const text = ta.value
  const scrollTop = ta.scrollTop

  const insertStr = `:${code}:`
  const newText = text.slice(0, start) + insertStr + text.slice(end)
  previewContent.value = newText
  editHasChanges.value = true
  showEmojiPicker.value = false

  nextTick(() => {
    ta.setSelectionRange(start + insertStr.length, start + insertStr.length)
    ta.focus()
    ta.scrollTop = scrollTop
    updateStats(newText)
  })
}

function closeEmojiPicker(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.md-toolbar-btn') && !target.closest('.emoji-picker-panel')) {
    showEmojiPicker.value = false
  }
}

// 内容
let editOriginalContent = ''
let editSessionOriginalContent = ''
let editSessionVersionCreated = false
let autoSaveTimer: ReturnType<typeof setInterval> | null = null

// 状态
const editSaving = ref(false)
const editSaved = ref(false)
const editHasChanges = ref(false)
const autoSaveEnabled = ref(false)
const editLastSavedTime = ref('-')

// 统计
const editWordCount = ref(0)
const editLineCount = ref(0)

// 当前正在编辑的 docId
const editingDocId = ref('')

// ===== 搜索 =====
const searchKeyword = ref('')

/** 过滤后的目录数据 */
const filteredDirectoryData = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return directoryData.value

  const matchedDocIds = new Set<string>()
  const parentDirIds = new Set<string>()

  for (const node of directoryData.value) {
    if (node.isDoc) {
      if ((node.name || '').toLowerCase().includes(keyword)) {
        matchedDocIds.add(node.id)
        if (node.pid) parentDirIds.add(node.pid)
      }
    }
  }

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

// ===== 分享功能 =====
const showShareDialog = ref(false)
const shareTargetType = ref<'PROJECT' | 'DOC'>('PROJECT')
const shareTargetId = ref('')
const shareTargetName = ref('')

function handleDirCommand(command: string) {
  switch (command) {
    case 'share':
      openShareProject()
      break
    case 'exportProject':
      handleExportProject()
      break
    case 'createDir':
      handleCreateDir()
      break
    case 'createDoc':
      handleCreateDoc()
      break
  }
}

function openShareProject() {
  shareTargetType.value = 'PROJECT'
  shareTargetId.value = projectId
  shareTargetName.value = projectName.value || t('share.project')
  showShareDialog.value = true
}

function openShareDoc() {
  if (!currentDoc.value) {
    ElMessage.warning(t('project.selectDocFromLeft'))
    return
  }
  shareTargetType.value = 'DOC'
  shareTargetId.value = currentDoc.value.docId
  shareTargetName.value = currentDoc.value.docTitle || t('share.doc')
  showShareDialog.value = true
}

function handleShareDoc(doc: any) {
  shareTargetType.value = 'DOC'
  shareTargetId.value = doc.id
  shareTargetName.value = doc.name || t('share.doc')
  showShareDialog.value = true
}

// ===== 导出功能 =====
const showExportDialog = ref(false)
const exportOptions = ref<ExportOptions>({
  targetId: '',
  targetType: 'document',
  format: 'md'
})
const exportDocInfo = ref<any>(null)
const exportBatchInfo = ref<BatchExportInfo>({
  isDirectory: false,
  estimatedFileCount: 0
})

async function handleExportDoc(doc: any) {
  try {
    // 获取文档详细信息
    const docData = await getDocById(doc.id)
    
    exportOptions.value = {
      targetId: doc.id,
      targetType: 'document',
      format: 'md'
    }
    
    exportDocInfo.value = {
      docId: docData.docId,
      docTitle: docData.docTitle,
      docContent: docData.docContent,
      createTime: docData.createTime,
      updateTime: docData.updateTime
    }
    
    exportBatchInfo.value = {
      isDirectory: false
    }
    
    showExportDialog.value = true
    
  } catch (error: any) {
    console.error('Failed to get document info:', error)
    ElMessage.error(t('project.getDocInfoFailed'))
  }
}

async function handleExportCurrentDir(dir: any) {
  try {
    // 计算当前目录及所有子目录下的文档数量（递归）
    const estimatedCount = await estimateDirectoryFileCount(dir.id, true)
    
    exportOptions.value = {
      targetId: dir.id,
      targetType: 'directory',
      format: 'md',
      recursive: true  // 修改为true，实现级联导出
    }
    
    exportDocInfo.value = null
    
    exportBatchInfo.value = {
      isDirectory: true,
      directoryName: dir.name,
      recursive: true,  // 修改为true，实现级联导出
      estimatedFileCount: estimatedCount
    }
    
    showExportDialog.value = true
    
  } catch (error: any) {
    console.error('Failed to get directory info:', error)
    ElMessage.error(t('project.getDirInfoFailed'))
  }
}

async function handleExportProject() {
  try {
    // 计算整个项目的文档数量
    const totalDocs = directoryData.value.filter((item: any) => item.isDoc).length
    
    exportOptions.value = {
      targetId: projectId,
      targetType: 'project',
      format: 'md',
      recursive: true
    }
    
    exportDocInfo.value = null
    
    exportBatchInfo.value = {
      isDirectory: true,
      directoryName: projectName.value || t('share.project'),
      recursive: true,
      estimatedFileCount: totalDocs
    }
    
    showExportDialog.value = true
    
  } catch (error: any) {
    console.error('Failed to get project info:', error)
    ElMessage.error(t('project.getProjectInfoFailed'))
  }
}

async function estimateDirectoryFileCount(directoryId: string, recursive: boolean): Promise<number> {
  // 简单估算：遍历目录树计算文档数量
  let count = 0
  
  const countInDirectory = (dirId: string, includeSubdirs: boolean) => {
    directoryData.value.forEach((item: any) => {
      if (item.pid === dirId) {
        if (item.isDoc) {
          count++
        } else if (includeSubdirs) {
          countInDirectory(item.id, true)
        }
      }
    })
  }
  
  countInDirectory(directoryId, recursive)
  return count
}

function handleExportSuccess() {
  ElMessage.success(t('project.exportSuccess'))
  showExportDialog.value = false
}

function handleExportError(error: string) {
  ElMessage.error(t('project.exportFailed') + `: ${error}`)
}

// ========== 生命周期 ==========
onMounted(async () => {
  appStore.setCurrentProject(projectId)

  await Promise.all([
    loadProject(),
    loadDirectories()
  ])

  await nextTick()

  const docIdFromUrl = route.query.docId as string
  if (docIdFromUrl) {
    loadDoc(docIdFromUrl, false).catch(e => console.error('Failed to load specified document:', e))
  } else {
    loadFirstDoc().catch(e => console.error('Failed to load first document:', e))
  }

  restoreExpandedKeys()

  document.addEventListener('keydown', handleKeydown, true)
  document.addEventListener('click', closeEmojiPicker)
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  stopAutoSave()
  document.removeEventListener('keydown', handleKeydown, true)
  document.removeEventListener('click', closeEmojiPicker)
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

watch(() => route.params.projectId, async (newId) => {
  if (newId && newId !== projectId) {
    appStore.setCurrentProject(newId as string)
    await Promise.all([
      loadProject(),
      loadDirectories()
    ])
    await loadFirstDoc()
  }
}, { flush: 'post' })

// ========== 加载数据 ==========

async function loadProject() {
  try {
    const [projectData, members] = await Promise.all([
      getProjectById(projectId),
      getProjectMembers(projectId)
    ])
    appStore.setProjectName(projectData.projectName)
    appStore.setCurrentTeam(projectData.teamId)
    
    // 找到当前用户的成员信息
    currentMemberInfo.value = members.find(
      (m: any) => m.userId === userStore.user?.userId
    ) || null
    projectMembers.value = members
  } catch (e) {
    console.error('Failed to load project info:', e)
  }
}

async function loadDirectories() {
  dirLoading.value = true
  try {
    const [dirs, docs] = await Promise.all([
      getProjectDirectories(projectId),
      getProjectDocs(projectId)
    ])

    directoryData.value = [
      ...dirs.map(d => ({ ...d, isDoc: false })),
      ...docs.filter(d => d.directoryId).map(d => ({
        id: d.docId,
        name: d.docTitle,
        pid: d.directoryId,
        isDoc: true,
        docIcon: d.docIcon
      }))
    ]

    const rootDirs = dirs.filter(d => d.pid === '0' || !d.pid)
    expandedKeys.value = rootDirs.map(d => d.id)
  } catch (e) {
    console.error('Failed to load directory:', e)
    ElMessage.error(t('project.loadDirFailed'))
  } finally {
    dirLoading.value = false
  }
}

async function loadFirstDoc() {
  try {
    // 优先使用已加载的目录树数据，避免额外API请求
    const docsFromTree = directoryData.value.filter(d => d.isDoc)
    if (docsFromTree.length > 0) {
      await loadDoc(docsFromTree[0].id, true)
      return
    }
    // 回退：如果目录树尚未加载完成，重新拉取
    const docs = await getProjectDocs(projectId)
    if (docs.length > 0) await loadDoc(docs[0].docId, true) // 更新URL
  } catch (e) {
    console.error('Failed to load document:', e)
  }
}

async function loadDoc(docId: string, updateUrl = true) {
  try {
    // 使用包含用户信息的新API
    const doc = await getDocByIdWithUser(docId)
    currentDoc.value = doc
    currentDocId.value = docId

    // 加载版本历史
    loadVersions(docId)

    // 直接使用API返回的用户信息，无需额外请求
    creator.value = {
      userId: doc.createUser,
      userName: doc.createUserName,
      userIcon: '',
      account: doc.createUserAccount || '',
      createTime: doc.createTime
    } as any

    if (doc.updateUser) {
      updater.value = {
        userId: doc.updateUser,
        userName: doc.updateUserName,
        userIcon: '',
        account: doc.updateUserAccount || '',
        createTime: doc.updateTime
      } as any
    } else {
      updater.value = null
    }

    // 更新URL，保持文档状态（保留现有query参数）
    if (updateUrl) {
      const currentQuery = { ...route.query }
      router.replace({ query: { ...currentQuery, docId } })
    }
  } catch (e) {
    console.error('Failed to load document:', e)
    ElMessage.error(t('project.loadDocFailed'))
  }
}

async function loadVersions(docId: string) {
  try {
    versions.value = await getDocVersions(docId)
  } catch (e) {
    console.error('Failed to load version:', e)
  }
}

async function handleSelectVersion(version: DocVersion) {
  if (!currentDoc.value) return
  // 这里可以预览版本内容，但目前主要用于回滚
  console.log('Selected version:', version)
}

async function handleRollback(versionId: string) {
  if (!currentDoc.value) return
  const version = versions.value.find(v => v.versionId === versionId)
  const versionNum = version ? version.versionNum : ''
  
  try {
    await ElMessageBox.confirm(t('project.confirmRollback', { versionNum }), t('project.rollbackVersion'), {
      confirmButtonText: t('project.confirmRollbackBtn'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    await rollbackToVersion(currentDoc.value.docId, versionId)
    ElMessage.success(t('project.rollbackSuccess'))
    showVersionDrawer.value = false
    await loadDoc(currentDoc.value.docId)
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Rollback failed:', e)
      ElMessage.error(t('project.rollbackFailed'))
    }
  }
}

// ========== 目录树交互 ==========
function handleTreeNodeClick(node: any) {
  if (node.isDoc) {
    loadDoc(node.id)
  }
}

async function handleCreateDir(parentDir?: any) {
  newDirForm.value = { name: '', pid: parentDir?.id || '0' }
  showCreateDir.value = true
}

async function doCreateDirectory() {
  if (!newDirForm.value.name.trim()) return

  try {
    await createDirectoryApi({
      name: newDirForm.value.name.trim(),
      pid: newDirForm.value.pid,
      projectId
    })
    ElMessage.success(t('common.createSuccess'))
    showCreateDir.value = false
    await loadDirectories()
  } catch (e) {
    console.error('Failed to create directory:', e)
    ElMessage.error(t('project.createDirFailed'))
  }
}

async function handleDeleteDir(dirId: string) {
  try {
    await ElMessageBox.confirm(t('project.confirmDeleteDir'), t('common.confirm'), { type: 'warning' })
    await deleteDirectoryApi(dirId)
    ElMessage.success(t('common.deleteSuccess'))
    await loadDirectories()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('Failed to delete directory:', e)
      ElMessage.error(t('project.deleteDirFailed'))
    }
  }
}

async function handleCreateDoc(dir?: any) {
  newDocForm.value = { title: '', directoryId: dir?.id || '' }
  showCreateDoc.value = true
}

async function handleCreateDocInDir(dirId: string) {
  newDocForm.value = { title: '', directoryId: dirId }
  showCreateDoc.value = true
}

async function doCreateDocument() {
  if (!newDocForm.value.title.trim()) return

  try {
    const doc = await createDoc({
      docTitle: newDocForm.value.title.trim(),
      directoryId: newDocForm.value.directoryId || undefined,
      projectId
    })
    ElMessage.success(t('common.createSuccess'))
    showCreateDoc.value = false
    await loadDirectories()
    await loadDoc(doc.docId)
  } catch (e) {
    console.error('Failed to create document:', e)
    ElMessage.error(t('project.createDocFailed'))
  }
}

// ========== 文档操作 ==========
function handleDocAction(doc: any) {
  if (doc._delete) {
    ElMessageBox.confirm(t('project.confirmDeleteDoc'), t('project.deleteConfirm'), {
      type: 'warning',
      confirmButtonText: t('common.confirmDelete'),
      cancelButtonText: t('common.cancel')
    }).then(async () => {
      try {
        await deleteDoc(doc.id)
        ElMessage.success(t('layout.docDeleted'))
await loadProject()
      } catch (e: any) {
        ElMessage.error(e.message || t('common.deleteFailed'))
      }
    }).catch(() => {})
  } else {
    openEditDialog(doc.id)
  }
}

// ========== 编辑器 ==========
async function openEditDialog(docId: string) {
  editingDocId.value = docId

  const doc = await getDocById(docId)
  editTitle.value = doc.docTitle
  editOriginalContent = doc.docContent || ''
  editOriginalTitle = doc.docTitle
  editSessionOriginalContent = doc.docContent || ''
  previewContent.value = doc.docContent || ''

  showEditorDialog.value = true
  editHasChanges.value = false
  editSaved.value = false
  editLastSavedTime.value = '-'
  editSessionVersionCreated = false

  await nextTick()
  updateStats(previewContent.value || '')
  startAutoSave()
}

/** 关闭编辑器（有更改时确认） */
function closeEditorDialog() {
  if (editHasChanges.value) {
    ElMessageBox.confirm(t('project.unsavedChangesConfirm'), t('common.confirm'), {
      type: 'warning',
    }).then(() => doCloseEditor()).catch(() => {})
  } else {
    doCloseEditor()
  }
}

async function doCloseEditor() {
  await saveAndCreateVersion()
  showEditorDialog.value = false
  stopAutoSave()
  previewContent.value = ''
  editSessionOriginalContent = ''
  editSessionVersionCreated = false
  loadDirectories()
}

// ========== 编辑器 ==========

/** Markdown 工具栏：插入语法 */
function insertMdSyntax(btn: MdToolbarBtn) {
  if (!btn.syntax || btn.label === '|') return
  const ta = mdTextareaRef.value
  if (!ta) return

  const start = ta.selectionStart
  const end = ta.selectionEnd
  const text = ta.value
  const selected = text.substring(start, end)
  const scrollTop = ta.scrollTop

  let insertText: string
  let cursorPos: number
  let selectEnd: number | undefined

  if (selected && btn.openLen !== undefined) {
    const prefix = btn.syntax.slice(0, btn.openLen)
    const suffix = btn.syntax.slice(btn.openLen)
    insertText = prefix + selected + suffix
    cursorPos = start + insertText.length
  } else if (btn.openLen !== undefined) {
    insertText = btn.syntax
    cursorPos = start + btn.openLen
    if (btn.selectLen !== undefined) {
      selectEnd = cursorPos + btn.selectLen
    }
  } else {
    insertText = btn.syntax
    cursorPos = start + insertText.length
  }

  const newText = text.slice(0, start) + insertText + text.slice(end)
  previewContent.value = newText
  editHasChanges.value = true

  nextTick(() => {
    if (selectEnd !== undefined) {
      ta.setSelectionRange(cursorPos, selectEnd)
    } else {
      ta.setSelectionRange(cursorPos, cursorPos)
    }
    ta.focus()
    ta.scrollTop = scrollTop
    updateStats(newText)
  })
}

/** Markdown 输入处理 */
function onMdInput() {
  const val = previewContent.value
  editHasChanges.value = val !== editOriginalContent || editTitle.value !== editOriginalTitle
  updateStats(val)
}

/** Markdown Tab 键插入空格 */
function handleMdTab() {
  const ta = mdTextareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  const text = ta.value
  const scrollTop = ta.scrollTop
  previewContent.value = text.slice(0, start) + '  ' + text.slice(start)
  nextTick(() => {
    ta.setSelectionRange(start + 2, start + 2)
    ta.scrollTop = scrollTop
    updateStats(ta.value)
  })
}

/** Markdown textarea 滚动同步 */
function syncMdScroll(e: Event) {
  // 可扩展：同步右侧预览滚动
}

/** 保存文档（第一次保存创建版本，后续保存覆盖当前版本） */
async function saveEditedDoc() {
  if (!editingDocId.value) return
  if (!editHasChanges.value) return

  editSaving.value = true
  editSaved.value = false

  try {
    const content = previewContent.value

    if (editSessionVersionCreated) {
      await updateDoc({
        docId: editingDocId.value,
        docTitle: editTitle.value,
        docContent: content,
        createVersion: false,
        updateCurrentVersion: true
      })
    } else {
      await updateDoc({
        docId: editingDocId.value,
        docTitle: editTitle.value,
        docContent: content,
        createVersion: true,
        updateCurrentVersion: false
      })
      editSessionVersionCreated = true
    }
    
    if (currentDoc.value && currentDoc.value.docId === editingDocId.value) {
      currentDoc.value.docTitle = editTitle.value
      currentDoc.value.docContent = content
    }
    
    editOriginalContent = content
    editOriginalTitle = editTitle.value
    editHasChanges.value = false
    editSaved.value = true
    editLastSavedTime.value = new Date().toLocaleTimeString()
    ElMessage.success(t('common.saveSuccess'))
  } catch (e) {
    console.error('Save failed:', e)
    ElMessage.error(t('common.saveFailed'))
  } finally {
    editSaving.value = false
  }
}

/** 关闭编辑器时确保版本已创建 */
async function saveAndCreateVersion() {
  if (!editingDocId.value) return
  if (editSessionVersionCreated) return

  const content = previewContent.value || ''
  const title = editTitle.value
  const contentChanged = content !== editSessionOriginalContent || title !== editOriginalTitle
  if (!contentChanged) return

  editSaving.value = true
  try {
    await updateDoc({
      docId: editingDocId.value,
      docTitle: title,
      docContent: content,
      createVersion: true,
      updateCurrentVersion: false
    })

    if (currentDoc.value && currentDoc.value.docId === editingDocId.value) {
      currentDoc.value.docTitle = title
      currentDoc.value.docContent = content
    }

    editOriginalContent = content
    editOriginalTitle = title
    editHasChanges.value = false
    editSessionVersionCreated = true
  } catch (e) {
    console.error('Save version failed:', e)
  } finally {
    editSaving.value = false
  }
}

// ========== 自动保存 ==========
function startAutoSave() {
  stopAutoSave()
  if (!autoSaveEnabled.value) return
  autoSaveTimer = setInterval(() => {
    if (autoSaveEnabled.value && editHasChanges.value && !editSaving.value) {
      saveEditedDoc()
    }
  }, 30000)
}

function stopAutoSave() {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

watch(autoSaveEnabled, (val) => {
  if (val) {
    startAutoSave()
  } else {
    stopAutoSave()
  }
})

// ========== 统计 ==========
function updateStats(value: string) {
  editWordCount.value = value.replace(/\s/g, '').length
  editLineCount.value = value ? value.split('\n').length : 0
}

// ========== 跳转（保留兼容）==========
function navigateToDoc(docId: string) {
  openEditDialog(docId)
}

// ========== 快捷键 ==========
function handleKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    saveEditedDoc()
  }
}

// ========== 浏览器关闭/页面离开 ==========
function handleBeforeUnload() {
  if (!showEditorDialog.value || !editingDocId.value) return

  const content = previewContent.value || ''
  const title = editTitle.value
  const contentChanged = content !== editSessionOriginalContent || title !== editOriginalTitle
  if (!contentChanged) return

  const token = localStorage.getItem('token')
  if (!token) return

  try {
    fetch('/api/doc', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        docId: editingDocId.value,
        docTitle: title,
        docContent: content,
        createVersion: !editSessionVersionCreated,
        updateCurrentVersion: editSessionVersionCreated
      }),
      keepalive: true
    })
  } catch (e) {
    // beforeunload 中无法可靠处理错误，静默忽略
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

// ========== 状态持久化 ==========
const EXPANDED_KEYS_STORAGE_KEY = `project-${projectId}-expanded-keys`

function saveExpandedKeys() {
  localStorage.setItem(EXPANDED_KEYS_STORAGE_KEY, JSON.stringify(expandedKeys.value))
}

function restoreExpandedKeys() {
  try {
    const saved = localStorage.getItem(EXPANDED_KEYS_STORAGE_KEY)
    if (saved) {
      expandedKeys.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('Failed to restore expanded state:', e)
  }
}

// 监听展开状态变化并保存
watch(expandedKeys, () => {
  saveExpandedKeys()
}, { deep: true })
</script>

<style scoped lang="scss">
.project-workspace {
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

.doc-header h1 {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  letter-spacing: -0.02em;
}

// ========== 编辑器动画样式 ==========
.editor-fade-enter-active { transition: opacity 0.2s ease; }
.editor-fade-leave-active { transition: opacity 0.15s ease; }
.editor-fade-enter-from,
.editor-fade-leave-to { opacity: 0; }

/* 编辑器右侧预览区 */
.editor-preview-area {
  padding: 24px 28px;
  min-height: 100%;

  :deep(.api-markdown-shell) {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
  }
  :deep(.api-markdown-shell:not(.no-toc)) {
    grid-template-columns: minmax(0, 1fr);
  }
  :deep(.api-markdown-toc) {
    display: none !important;
  }
}

/* ========== Markdown 纯源码编辑器 ========== */
.md-textarea {
  flex: 1;
  width: 100%;
  min-height: 0;
  padding: 16px 20px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  background: #fff;
  tab-size: 2;

  &::placeholder {
    color: #c0c4cc;
  }

  &:focus {
    box-shadow: none;
  }

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: #d0d5dd;
    border-radius: 3px;
    &:hover { background: #a0aab5; }
  }
}

.md-toolbar-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  color: #5f6368;
  background: transparent;
  cursor: pointer;
  transition: all 0.15s;

  i { font-size: 13px; }

  &:hover {
    background: #e8eaed;
    color: #202124;
  }

  &:active {
    transform: scale(0.95);
  }

  &[disabled] {
    opacity: 0.4;
    cursor: not-allowed;
  }
}
</style>
