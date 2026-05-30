<template>
  <div class="h-screen flex flex-col bg-gradient-to-br from-blue-50 via-white to-indigo-50">
    <!-- 顶部导航栏 - 简化版 -->
    <header class="h-14 bg-white/80 backdrop-blur-lg border-b border-gray-200 flex items-center justify-between px-6 shadow-sm z-20">
      <!-- 左侧：侧边栏切换 + Logo -->
      <div class="flex items-center gap-4">
        <el-button text class="!p-1.5" @click="appStore.toggleSidebar">
          <i :class="[appStore.sidebarCollapsed ? 'fa-solid fa-bars' : 'fa-solid fa-arrow-left-from-line', 'text-lg text-gray-600']"></i>
        </el-button>
        
        <div class="flex items-center gap-2.5 cursor-pointer hover:opacity-80 transition-opacity" @click="router.push('/')">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-md">
            <i class="fa-solid fa-file-lines text-white"></i>
          </div>
          <div>
            <h1 class="text-base font-bold text-gray-900">FreeDoc</h1>
            <p class="text-[10px] text-gray-500 leading-none">{{ $t('common.appSlogan') }}</p>
          </div>
        </div>
      </div>

      <!-- 右侧：用户信息 -->
      <div class="flex items-center gap-3">
        <!-- 语言切换 -->
        <LanguageToggle button-class="p-2" />

        <!-- 字体大小切换 -->
        <FontSizeToggle button-class="p-2" />

        <!-- 字体家族切换 -->
        <FontFamilyToggle button-class="p-2" />

        <!-- 用户下拉菜单 -->
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-50 px-2.5 py-1.5 rounded-lg transition-colors">
            <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-xs font-bold shadow-sm">
              {{ userStore.user?.userName?.charAt(0) || 'U' }}
            </div>
            <div class="text-left hidden lg:block">
              <p class="text-sm font-medium text-gray-900 leading-tight">{{ userStore.user?.userName }}</p>
              <p class="text-xs text-gray-500 leading-tight">{{ userStore.user?.account }}</p>
            </div>
            <i class="fa-solid fa-chevron-down text-xs text-gray-400 hidden lg:block"></i>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="!rounded-xl !p-2 !min-w-[180px]">
              <el-dropdown-item command="profile" class="!rounded-lg !px-3 !py-2.5">
                <div class="flex items-center gap-2.5">
                  <i class="fa-solid fa-user text-gray-500 w-4"></i>
                  <span>{{ $t('common.profile') }}</span>
                </div>
              </el-dropdown-item>
              <el-dropdown-item command="settings" class="!rounded-lg !px-3 !py-2.5">
                <div class="flex items-center gap-2.5">
                  <i class="fa-solid fa-gear text-gray-500 w-4"></i>
                  <span>{{ $t('common.systemSettings') }}</span>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout" class="!rounded-lg !px-3 !py-2.5 !text-red-500 hover:!bg-red-50">
                <div class="flex items-center gap-2.5">
                  <i class="fa-solid fa-right-from-bracket w-4"></i>
                  <span>{{ $t('common.logout') }}</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="flex-1 flex overflow-hidden relative">
      <!-- 左侧边栏：团队 + 项目 + 目录树（首页隐藏） -->
      <aside
        v-if="!isWorkspaceHome && !isProfileRoute"
        :class="[
          'bg-white/80 backdrop-blur-lg border-r border-gray-200 transition-all duration-300 flex flex-col z-10',
          appStore.sidebarCollapsed ? '!w-0 !overflow-hidden !border-r-0' : 'w-60'
        ]"
      >
        <!-- 团队列表 -->
        <div class="shrink-0 border-b border-gray-100">
          <div class="px-3 py-2 flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-400 tracking-wide">{{ $t('layout.team') }}</span>
            <el-button text size="small" class="!text-primary !hover:text-primary/80" @click="showCreateTeam = true">
              <i class="fa-solid fa-plus mr-0.5 text-xs"></i>{{ $t('layout.newTeam') }}
            </el-button>
          </div>

          <!-- 空状态 -->
          <div v-if="teams.length === 0" class="px-3 pb-3">
            <div class="rounded-lg border-2 border-dashed border-gray-200 p-4 text-center cursor-pointer hover:border-primary/40 hover:bg-primary/5 transition-all" @click="showCreateTeam = true">
              <i class="fa-solid fa-plus text-xl text-gray-300 mb-1 block"></i>
              <p class="text-xs text-gray-400">{{ $t('layout.clickToCreateTeam') }}</p>
            </div>
          </div>
        </div>

        <!-- 项目列表 -->
        <div class="shrink-0 border-t border-gray-100">
          <div class="px-3 py-2 flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-400 tracking-wide">{{ $t('layout.project') }}</span>
            <el-button v-if="isCurrentTeamOwner" text size="small" class="!text-primary !hover:text-primary/80" @click="showCreateProject = true">
              <i class="fa-solid fa-plus mr-0.5 text-xs"></i>{{ $t('layout.newProject') }}
            </el-button>
          </div>
          <div class="px-2 pb-2 space-y-0.5 max-h-36 overflow-y-auto">
            <!-- 项目空状态 -->
            <div v-if="projects.length === 0 && selectedTeamId && isCurrentTeamOwner" class="py-3 text-center">
              <div class="inline-flex items-center gap-1.5 rounded-full bg-blue-50 text-blue-600 text-xs px-3 py-1.5 cursor-pointer hover:bg-blue-100 transition-colors" @click="showCreateProject = true">
                <i class="fa-solid fa-plus text-[10px]"></i>{{ $t('layout.createFirstProject') }}
              </div>
            </div>
            <div v-if="projects.length === 0 && !selectedTeamId" class="py-3 text-center text-gray-400 text-xs">
              {{ $t('layout.selectTeamFirst') }}
            </div>
            <div
              v-for="project in projects"
              :key="project.projectId"
              :class="[
                'project-item px-3 py-2 rounded-lg cursor-pointer transition-all flex items-center gap-2 group',
                appStore.currentProjectId === project.projectId
                  ? 'bg-primary/10 text-primary'
                  : 'hover:bg-gray-50 text-gray-600'
              ]"
              @click="selectProject(project)"
            >
              <i :class="[parseIconWithStyle(project.projectIcon).icon || 'fa-solid fa-folder', 'text-sm']"></i>
              <span class="truncate text-sm flex-1">{{ project.projectName }}</span>
              <el-dropdown trigger="click" class="opacity-0 group-hover:opacity-100 !shrink-0" @command="(cmd: string) => handleProjectCommand(cmd, project)">
                <i class="fa-solid fa-ellipsis text-gray-300 hover:text-gray-500 text-xs cursor-pointer"></i>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="workspace"><i class="fa-solid fa-book-open mr-1 w-4"></i>{{ $t('layout.enterWorkspace') }}</el-dropdown-item>
                    <el-dropdown-item command="setting"><i class="fa-solid fa-gear mr-1 w-4"></i>{{ $t('layout.projectSettings') }}</el-dropdown-item>
                    <el-dropdown-item command="members"><i class="fa-solid fa-users mr-1 w-4"></i>{{ $t('layout.memberManagement') }}</el-dropdown-item>
                    <el-dropdown-item command="delete" divided><i class="fa-solid fa-trash-can mr-1 w-4 text-red-400"></i><span class="text-red-500">{{ $t('layout.deleteProject') }}</span></el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <!-- 选中项目的快捷操作栏 -->
          <div v-if="appStore.currentProjectId" class="px-3 pb-2 pt-1 flex gap-1.5">
            <button class="flex-1 flex items-center justify-center gap-1 rounded-md bg-green-50 text-green-600 text-[11px] py-1.5 px-2 cursor-pointer hover:bg-green-100 transition-colors"
                    @click="$router.push(`/workspace/${appStore.currentProjectId}`)">
              <i class="fa-solid fa-book-open text-[10px]"></i>{{ $t('layout.workspace') }}
            </button>
            <button class="flex-1 flex items-center justify-center gap-1 rounded-md bg-orange-50 text-orange-600 text-[11px] py-1.5 px-2 cursor-pointer hover:bg-orange-100 transition-colors"
                    @click="$router.push(`/project/${appStore.currentProjectId}?tab=members`)">
              <i class="fa-solid fa-users text-[10px]"></i>{{ $t('common.members') }}
            </button>
            <button class="flex-1 flex items-center justify-center gap-1 rounded-md bg-gray-50 text-gray-500 text-[11px] py-1.5 px-2 cursor-pointer hover:bg-gray-100 transition-colors"
                    @click="$router.push(`/project/${appStore.currentProjectId}`)">
              <i class="fa-solid fa-gear text-[10px]"></i>{{ $t('common.settings') }}
            </button>
          </div>
        </div>

        <!-- 目录树 (选中项目后显示，但在工作台/工作区页面不显示) -->
        <div v-if="appStore.currentProjectId && !isWorkspaceHome && !isWorkspaceRoute" class="flex-1 min-h-0 border-t border-gray-100 flex flex-col">
          <DirectoryTree
            :key="'ml-dt-' + appStore.currentProjectId"
            :data="directoryStore.directoryTreeData"
            :loading="dirLoading"
            :current-key="currentDocId"
            :expanded-keys="expandedDirKeys"
            :title="$t('layout.docDirectory')"
            @node-click="handleTreeNodeClick"
            @create-directory="showCreateDirDialog()"
            @create-doc="showCreateDocDialog()"
            @add-sub-directory="(dir) => showCreateDirDialog(dir.id)"
            @create-doc-in-dir="(dirId) => showCreateDocDialog(dirId)"
            @delete-directory="handleDeleteDirectory"
            @edit-doc="handleDocAction"
            @share-doc="handleShareDoc"
            @export-current-dir="handleExportCurrentDir"
            @export-doc="handleExportDoc"
            @refresh="loadDirectories"
          />
        </div>

        <!-- 未选项目时的提示 -->
        <div v-else class="flex-1 flex items-center justify-center p-6">
          <div class="text-center text-gray-400">
            <i class="fa-solid fa-folder-tree text-3xl mb-2 block opacity-30"></i>
            <p class="text-xs">{{ $t('layout.selectProjectFirst') }}</p>
          </div>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="flex-1 overflow-hidden min-w-0">
        <router-view />
      </main>
    </div>

    <!-- 页脚 -->
    <footer class="shrink-0 text-center py-1.5 text-[11px] text-gray-400 border-t border-gray-100">
      © 2025-{{ currentYear }} tomshushu. Licensed under the MIT License.
      <a href="https://github.com/Tomshushu/free-doc" target="_blank" rel="noopener noreferrer" class="inline-flex items-center gap-1 ml-2 text-gray-400 hover:text-gray-600 transition-colors">
        <svg class="w-4 h-4" viewBox="0 0 16 16" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>
        GitHub
      </a>
    </footer>

    <!-- 创建团队弹窗 -->
    <el-dialog v-model="showCreateTeam" :title="$t('layout.createTeam')" width="480px" destroy-on-close>
      <el-form :model="newTeamForm" label-width="80px">
        <el-form-item :label="$t('layout.teamName')" required>
          <el-input v-model="newTeamForm.teamName" :placeholder="$t('layout.teamNamePlaceholder')" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('layout.teamIcon')">
          <IconPicker v-model="newTeamForm.teamIcon" type="team" default-icon="fa-solid fa-users" />
        </el-form-item>
        <el-form-item :label="$t('layout.teamDesc')">
          <el-input v-model="newTeamForm.teamDesc" type="textarea" :rows="3" :placeholder="$t('layout.teamDescPlaceholder')" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTeam = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newTeamForm.teamName.trim()" @click="createTeam">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 创建项目弹窗 -->
    <el-dialog v-model="showCreateProject" :title="$t('layout.createProject')" width="520px" destroy-on-close>
      <el-form :model="newProject" label-width="80px" @submit.prevent="createProject">
        <el-form-item :label="$t('layout.projectName')" required>
          <el-input v-model="newProject.projectName" :placeholder="$t('layout.projectNamePlaceholder')" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('layout.projectIcon')">
          <IconPicker v-model="newProject.projectIcon" type="project" default-icon="fa-solid fa-folder" />
        </el-form-item>
        <el-form-item :label="$t('layout.cardStyle')">
          <ProjectStylePicker v-model="newProject.projectStyle" :icon="newProject.projectIcon || 'fa-solid fa-folder'" />
        </el-form-item>
        <el-form-item :label="$t('layout.projectDesc')">
          <el-input v-model="newProject.projectDesc" type="textarea" :rows="3" :placeholder="$t('layout.projectDescPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateProject = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newProject.projectName.trim()" @click="createProject">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 新建目录弹窗 -->
    <el-dialog v-model="showCreateDir" :title="$t('layout.createDirectory')" width="400px" destroy-on-close>
      <el-form :model="newDirForm" label-width="80px">
        <el-form-item :label="$t('layout.parentDirectory')">
          <el-cascader
            v-if="directories.length > 0"
            v-model="parentDirPath"
            :options="dirCascadeOptions"
            :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
            clearable
            :placeholder="$t('layout.rootDirectory')"
            class="w-full"
          />
          <span v-else class="text-sm text-gray-400">{{ $t('layout.rootDirectory') }}</span>
        </el-form-item>
        <el-form-item :label="$t('layout.directoryName')" required>
          <el-input v-model="newDirForm.name" :placeholder="$t('layout.directoryNamePlaceholder')" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDir = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newDirForm.name.trim()" @click="createDirectory">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 新建文档弹窗 -->
    <el-dialog v-model="showCreateDoc" :title="$t('layout.createDoc')" width="450px" destroy-on-close>
      <el-form :model="newDocForm" label-width="80px">
        <el-form-item :label="$t('layout.belongDirectory')">
          <el-select v-model="newDocForm.directoryId" class="w-full" :placeholder="$t('layout.selectDirectoryPlaceholder')">
            <el-option
              v-for="dir in flatDirectories"
              :key="dir.id"
              :label="getDirLabel(dir)"
              :value="dir.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('layout.docTitle')" required>
          <el-input v-model="newDocForm.docTitle" :placeholder="$t('layout.docTitlePlaceholder')" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDoc = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!newDocForm.docTitle.trim()" @click="createDoc">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

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
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useDirectoryStore } from '@/stores/directory'
import DirectoryTree from '@/components/DirectoryTree.vue'
import ExportDialog from '@/components/ExportDialog.vue'
import LanguageToggle from '@/components/LanguageToggle.vue'
import FontSizeToggle from '@/components/FontSizeToggle.vue'
import FontFamilyToggle from '@/components/FontFamilyToggle.vue'
import { getUserTeams, getTeamById, addTeamMember as addTeamMemberApi, removeTeamMember as removeTeamMemberApi, createTeam as createTeamApi, deleteTeam as deleteTeamApi, getTeamStats, type TeamStatsVO } from '@/api/team'
import { getTeamProjects, createProject as createProjectApi, deleteProject as deleteProjectApi, getProjectById } from '@/api/project'
import {
  getProjectDirectories,
  createDirectory as createDirectoryApi,
  deleteDirectory as deleteDirectoryApi
} from '@/api/directory'
import { getDirectoryDocs, createDoc as createDocApi, deleteDoc as deleteDocApi, getDocById } from '@/api/doc'
import type { Team, Project, Directory, ExportOptions, BatchExportInfo } from '@/types'
import IconPicker from '@/components/IconPicker.vue'
import ProjectStylePicker from '@/components/ProjectStylePicker.vue'
import { parseIconWithStyle, buildIconWithStyle } from '@/utils/projectStyle'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const userStore = useUserStore()
const appStore = useAppStore()
const directoryStore = useDirectoryStore()

// ===== 团队 & 项目数据 =====
const teams = ref<Team[]>([])
const projects = ref<Project[]>([])
const selectedTeamId = ref<string>('')
const currentTeamName = computed(() => teams.value.find(t => t.teamId === selectedTeamId.value)?.teamName || '')
const currentTeamStats = ref<TeamStatsVO | null>(null)
const isCurrentTeamOwner = computed(() => currentTeamStats.value?.currentUserType === 'OWNER')

// ===== 目录 & 文档（使用统一 Store）=====
// 本地目录相关变量已迁移到 directoryStore，以下为兼容引用
const directories = computed(() => directoryStore.directories)
const dirLoading = computed(() => directoryStore.loading)
const currentDocId = computed(() => route.name === 'DocEdit' ? route.params.docId as string : '')
const expandedDirKeys = computed(() => directoryStore.defaultExpandedKeys)
const isWorkspaceRoute = computed(() => route.name === 'ProjectWorkspace')
const isWorkspaceHome = computed(() => route.name === 'Workspace' || route.name === 'ProjectWorkspace')
const isProfileRoute = computed(() => route.name === 'Profile')

// ===== 弹窗状态 =====
const showCreateProject = ref(false)
const showCreateDir = ref(false)
const showCreateDoc = ref(false)
const showCreateTeam = ref(false)
const showExportDialog = ref(false)

// ===== 导出相关状态 =====
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

// 表单
const newProject = ref({ projectName: '', projectIcon: 'fa-solid fa-folder', projectStyle: 'default', projectDesc: '' })
const newDirForm = ref({ name: '', pid: '0' })
const parentDirPath = ref<string | undefined>(undefined)
const newDocForm = ref({ docTitle: '', directoryId: '', docIcon: 'fa-solid fa-file-lines', docContent: '' })
const newTeamForm = ref({ teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' })
const currentYear = new Date().getFullYear()

onMounted(async () => {
  await loadTeams()
  // 如果当前在项目页面，恢复项目状态
  await restoreProjectFromRoute()
})

// 监听路由变化，恢复项目状态
watch(() => route.params.projectId, (projectId) => {
  if (projectId && typeof projectId === 'string') {
    restoreProjectFromRoute()
  }
}, { immediate: false })

// 从路由恢复项目状态
async function restoreProjectFromRoute() {
  const projectId = route.params.projectId as string
  if (projectId) {
    if (appStore.currentProjectId !== projectId) {
      appStore.setCurrentProject(projectId)
    }
    // 如果没有项目名称，说明是刷新页面，需要重新获取
    if (!appStore.currentProjectName) {
      try {
        const project = await getProjectById(projectId)
        appStore.setProjectName(project.projectName)
        if (project.teamId) {
          appStore.setCurrentTeam(project.teamId)
          selectedTeamId.value = project.teamId
        }
      } catch (e) {
        console.error('Failed to restore project info:', e)
      }
    }
  }
}

// 监听 Store 中的团队变化，同步本地状态
watch(() => appStore.currentTeamId, (teamId) => {
  if (teamId && teamId !== selectedTeamId.value) {
    selectedTeamId.value = teamId
  }
})

// 团队切换时自动加载项目列表
let hasLoadedProjects = false
watch(selectedTeamId, async (teamId) => {
  if (teamId) {
    // 首次加载不清空，后续切换团队时才清空项目状态
    if (hasLoadedProjects) {
      appStore.setCurrentProject(null)
      directoryStore.clear()
    }
    await loadProjects(teamId)
    hasLoadedProjects = true
  }
})

// 监听项目变化，通过 Store 自动刷新目录树（所有使用此 Store 的组件都会同步更新）
watch(() => appStore.currentProjectId, async (newProjectId) => {
  if (newProjectId) {
    await directoryStore.load(newProjectId)
  } else {
    directoryStore.clear()
  }
})

// ===== 团队操作 =====
async function loadTeams() {
  try {
    teams.value = await getUserTeams()
    if (teams.value.length > 0) {
      const firstTeam = teams.value[0]
      selectedTeamId.value = firstTeam.teamId
      appStore.setCurrentTeam(firstTeam.teamId)
      // watch 会自动触发 loadProjects
    }
  } catch (e) {
    console.error(e)
  }
}

async function handleTeamChange(teamId: string) {
  // 只更新值，watch 会自动触发 loadProjects
  // 不需要手动调用，避免重复请求
}

/** 点击切换团队 */
function switchTeam(team: Team) {
  selectedTeamId.value = team.teamId
  appStore.setCurrentTeam(team.teamId)
}

/** 团队下拉操作 */
function handleTeamAction(cmd: string, team: Team) {
  if (cmd === 'setting') {
    router.push(`/team/${team.teamId}`)
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(
      t('layout.confirmDeleteTeam', { name: team.teamName }),
      t('common.dangerAction'),
      { type: 'warning', confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel') }
    ).then(() => handleDeleteTeam()).catch(() => {})
  }
}

// ===== 团队操作 =====
async function createTeam() {
  if (!newTeamForm.value.teamName.trim()) return
  try {
    const team = await createTeamApi({
      teamName: newTeamForm.value.teamName.trim(),
      teamIcon: newTeamForm.value.teamIcon,
      teamDesc: newTeamForm.value.teamDesc
    })
    ElMessage.success(t('layout.teamCreated'))
    showCreateTeam.value = false
    newTeamForm.value = { teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' }
    await loadTeams()
    selectedTeamId.value = team.teamId
    appStore.setCurrentTeam(team.teamId)
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

async function handleDeleteTeam() {
  if (!selectedTeamId.value) return
  try {
    await deleteTeamApi(selectedTeamId.value)
    ElMessage.success(t('layout.teamDeleted'))
    selectedTeamId.value = ''
    appStore.setCurrentTeam(null)
    newTeamForm.value = { teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' }
    await loadTeams()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.deleteFailed'))
  }
}

// ===== 项目操作 =====
async function loadProjects(teamId: string) {
  try {
    const [projectList, stats] = await Promise.all([
      getTeamProjects(teamId),
      getTeamStats(teamId)
    ])
    projects.value = projectList
    currentTeamStats.value = stats
  } catch (e) {
    console.error(e)
  }
}

function selectProject(project: Project) {
  // 如果已是当前项目则跳过（避免重复加载）
  if (appStore.currentProjectId === project.projectId) return
  
  appStore.setCurrentProject(project.projectId)
  appStore.setProjectName(project.projectName)
  // watch(currentProjectId) 会自动触发 directoryStore.load()，无需手动调用
  router.push(`/workspace/${project.projectId}`)
}

function handleProjectCommand(cmd: string, project: Project) {
  if (cmd === 'setting') {
    router.push(`/project/${project.projectId}`)
  } else if (cmd === 'workspace') {
    selectProject(project)
  } else if (cmd === 'members') {
    router.push(`/project/${project.projectId}?tab=members`)
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(
      t('layout.confirmDeleteProject', { name: project.projectName }),
      t('common.dangerAction'),
      { type: 'warning', confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel') }
    ).then(async () => {
      try {
        await deleteProjectApi(project.projectId)
        ElMessage.success(t('layout.projectDeleted'))
        if (appStore.currentProjectId === project.projectId) {
          appStore.setCurrentProject(null)
          directoryStore.clear()
          router.push('/')
        }
        await loadProjects(selectedTeamId.value)
      } catch (e: any) {
        ElMessage.error(e.message || t('common.deleteFailed'))
      }
    }).catch(() => {})
  }
}

async function createProject() {
  if (!newProject.value.projectName.trim()) return
  try {
    const iconWithStyle = buildIconWithStyle(newProject.value.projectIcon, newProject.value.projectStyle)
    const p = await createProjectApi({
      projectName: newProject.value.projectName.trim(),
      projectIcon: iconWithStyle,
      projectDesc: newProject.value.projectDesc,
      teamId: selectedTeamId.value
    })
    ElMessage.success(t('common.create'))
    showCreateProject.value = false
    newProject.value = { projectName: '', projectIcon: 'fa-solid fa-folder', projectStyle: 'default', projectDesc: '' }

    // 先设置项目信息到 store
    appStore.setCurrentProject(p.projectId)
    appStore.setProjectName(p.projectName)

    // 重新加载项目列表
    await loadProjects(selectedTeamId.value)

    // 跳转到项目工作区
    router.push(`/workspace/${p.projectId}`)
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

// ===== 目录树相关 =====

const flatDirectories = computed(() => {
  return directories.value.filter(d => true)
})

const dirCascadeOptions = computed(() =>
  directories.value.map(d => ({ id: d.id, name: d.name }))
)

function getDirLabel(dir: Directory): string {
  let labels = [dir.name]
  let pid = dir.pid
  let depth = 0
  while (pid && pid !== '0' && depth < 5) {
    const parent = directories.value.find(d => d.id === pid)
    if (!parent) break
    labels.unshift(parent.name)
    pid = parent.pid
    depth++
  }
  return labels.join(' / ')
}

async function loadDirectories() {
  if (appStore.currentProjectId) {
    await directoryStore.load(appStore.currentProjectId)
  }
}

function handleTreeNodeClick(node: any) {
  if (node.isDoc) {
    router.push(`/doc/${node.id}/view`)
  }
  // 点击目录可以展开/折叠（el-tree 已自动处理）
}

// ===== 目录 CRUD =====
function showCreateDirDialog(parentPid?: string) {
  newDirForm.value = { name: '', pid: parentPid || '0' }
  parentDirPath.value = parentPid
  showCreateDir.value = true
}

async function createDirectory() {
  if (!newDirForm.value.name.trim()) return
  try {
    await createDirectoryApi({
      name: newDirForm.value.name.trim(),
      pid: newDirForm.value.pid,
      projectId: appStore.currentProjectId || ''
    })
    ElMessage.success(t('layout.directoryCreated'))
    showCreateDir.value = false
    await loadDirectories()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

async function handleDeleteDirectory(dirId: string) {
  try {
    await deleteDirectoryApi(dirId)
    ElMessage.success(t('common.confirm'))
    await loadDirectories()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.deleteFailed'))
  }
}

// ===== 文档操作 =====
function showCreateDocDialog(directoryId?: string) {
  newDocForm.value = {
    docTitle: '',
    directoryId: directoryId || '',
    docIcon: 'fa-solid fa-file-lines',
    docContent: ''
  }
  showCreateDoc.value = true
}

async function createDoc() {
  if (!newDocForm.value.docTitle.trim()) return
  try {
    const res = await createDocApi({
      docTitle: newDocForm.value.docTitle.trim(),
      directoryId: newDocForm.value.directoryId,
      docIcon: newDocForm.value.docIcon,
      docContent: ''
    })
    ElMessage.success(t('layout.docCreated'))
    showCreateDoc.value = false
    router.push(`/doc/${res.docId}`)
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
  }
}

function handleDocAction(doc: any) {
  if (doc._delete) {
    ElMessageBox.confirm(t('layout.confirmDeleteDoc'), t('common.confirm'), { type: 'warning' }).then(async () => {
      try {
        await deleteDocApi(doc.id)
        ElMessage.success(t('layout.docDeleted'))
        await loadDirectories()
      } catch (e: any) {
        ElMessage.error(e.message || t('common.deleteFailed'))
      }
    }).catch(() => {})
  } else {
    router.push(`/doc/${doc.id}`)
  }
}

// ===== 导出处理函数 =====
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
    console.error(t('layout.getDocInfoFailed') + ':', error)
    ElMessage.error(t('layout.getDocInfoFailed'))
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
    console.error(t('layout.getDirInfoFailed') + ':', error)
    ElMessage.error(t('layout.getDirInfoFailed'))
  }

}

function handleShareDoc(doc: any) {
  // 分享功能暂未实现，跳转到文档页面
  router.push(`/doc/${doc.id}`)
}

async function estimateDirectoryFileCount(directoryId: string, recursive: boolean): Promise<number> {
  // 简单估算：遍历目录树计算文档数量
  let count = 0
  
  const countInDirectory = (dirId: string, includeSubdirs: boolean) => {
    directoryStore.directoryTreeData.forEach((item: any) => {
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
  ElMessage.success(t('layout.exportSuccess'))
  showExportDialog.value = false
}

function handleExportError(error: string) {
  ElMessage.error(t('layout.exportFailed', { error }))
}

// ===== 命令处理 =====
function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'logout') {
    ElMessageBox.confirm(t('layout.confirmLogout'), t('common.confirm'), { type: 'warning' }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.team-select :deep(.el-select__wrapper) {
  border-radius: 8px;
}

.project-item:hover {
  transform: translateX(2px);
}

/* 自定义滚动条 */
:deep(::-webkit-scrollbar) {
  width: 4px;
}
:deep(::-webkit-scrollbar-thumb) {
  background-color: #e5e7eb;
  border-radius: 4px;
}
:deep(::-webkit-scrollbar-thumb:hover) {
  background-color: #d1d5db;
}
</style>
