<template>
  <div class="p-6 max-w-6xl mx-auto">
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 mb-6 overflow-hidden">
      <div class="h-24 bg-gradient-to-r from-blue-500 via-blue-400 to-cyan-400 relative">
        <div class="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImEiIHBhdHRlcm5Vbml0cz0idXNlclNwYWNlT25Vc2UiIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoMjU1LDI1NSwyNTUsMC4xKSIvPjwvcGF0dGVybj48L2RlZnM+PHJlY3QgZmlsbD0idXJsKCNhKSIgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIvPjwvc3ZnPg==')] opacity-40"></div>
      </div>
      <div class="px-6 pb-6 -mt-10 relative z-10 flex items-end gap-4">
        <div
          class="w-20 h-20 rounded-xl shadow-lg border-4 border-white flex items-center justify-center text-2xl bg-gradient-to-br from-primary/10 to-primary/30"
        >
          <i :class="[parseIconWithStyle(project?.projectIcon || '').icon || 'fa-solid fa-folder', 'text-primary']"></i>
        </div>
        <div class="flex-1 min-w-0 pt-1">
          <h1 class="text-2xl font-bold text-gray-900 truncate">{{ project?.projectName }}</h1>
          <p class="text-gray-500 mt-1 line-clamp-1">{{ project?.projectDesc || $t('project.noProjectDesc') }}</p>
          <div class="flex items-center gap-4 mt-2 text-xs text-gray-400">
            <span><i class="fa-regular fa-calendar mr-1"></i>{{ $t('common.createdAt') }} {{ formatDate(project?.createTime) }}</span>
            <span><i class="fa-regular fa-user mr-1"></i>{{ project?.createUser }}</span>
          </div>
        </div>
        <div class="flex gap-2">
          <el-button type="primary" @click="$router.push(`/workspace/${projectId}`)">
            <i class="fa-solid fa-book-open mr-1"></i>{{ $t('project.enterWorkspace') }}
          </el-button>
          <el-popconfirm
            v-if="isProjectOwner"
            :title="$t('project.confirmDeleteProject')"
            :confirm-button-text="$t('common.confirmDelete')"
            :cancel-button-text="$t('common.cancel')"
            @confirm="handleDeleteProject"
          >
            <template #reference>
              <el-button type="danger" plain>
                <i class="fa-solid fa-trash-can mr-1"></i>{{ $t('project.deleteProject') }}
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <el-tabs v-model="activeTab" class="project-tabs">
        <el-tab-pane name="docs">
          <template #label>
            <span class="flex items-center gap-1.5"><i class="fa-solid fa-file-lines"></i>{{ $t('project.doc') }}</span>
          </template>
          <DocsList v-if="activeTab === 'docs'" :project-id="projectId" />
        </el-tab-pane>

        <el-tab-pane v-if="isProjectOwner" name="members">
          <template #label>
            <span class="flex items-center gap-1.5"><i class="fa-solid fa-users"></i>{{ $t('project.member') }}</span>
          </template>
          <MembersPanel :project-id="projectId" />
        </el-tab-pane>

        <el-tab-pane v-if="canEditProject" name="info">
          <template #label>
            <span class="flex items-center gap-1.5"><i class="fa-solid fa-gear"></i>{{ $t('common.settings') }}</span>
          </template>
          <InfoPanel :project="project" @save="handleSaveInfo" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getProjectById, updateProject, deleteProject, getProjectMembers, type ProjectMemberVO } from '@/api/project'
import { useUserStore } from '@/stores/user'
import DocsList from './DocsList.vue'
import MembersPanel from './MembersPanel.vue'
import InfoPanel from './InfoPanel.vue'
import { parseIconWithStyle } from '@/utils/projectStyle'
import type { Project } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const projectId = route.params.projectId as string

const project = ref<Project | null>(null)
const activeTab = ref('docs')
const currentMemberInfo = ref<ProjectMemberVO | null>(null)

const isProjectOwner = computed(() => currentMemberInfo.value?.type === 'OWNER')
const canEditProject = computed(() => {
  if (!currentMemberInfo.value) return false
  return currentMemberInfo.value.type === 'OWNER' || currentMemberInfo.value.permission === 'rw'
})

onMounted(async () => {
  const tab = route.query.tab as string
  if (tab) activeTab.value = tab
  await loadProject()
})

async function loadProject() {
  try {
    const [projectData, members] = await Promise.all([
      getProjectById(projectId),
      getProjectMembers(projectId)
    ])
    project.value = projectData
    currentMemberInfo.value = members.find((m: ProjectMemberVO) => m.userId === userStore.user?.userId) || null
  } catch (e) {
    console.error(e)
  }
}

async function handleSaveInfo(data: Partial<Project>) {
  try {
    await updateProject(projectId, data)
    ElMessage.success(t('common.saveSuccess'))
    await loadProject()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.saveFailed'))
  }
}

async function handleDeleteProject() {
  try {
    await deleteProject(projectId)
    ElMessage.success(t('project.projectDeleted'))
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.message || t('common.deleteFailed'))
  }
}

function formatDate(date: string | undefined): string {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.project-tabs :deep(.el-tabs__header) {
  margin: 0; padding: 0 20px; background-color: #fafafa; border-bottom: 1px solid #f0f0f0;
}
.project-tabs :deep(.el-tabs__item) {
  padding: 0 20px; height: 48px; line-height: 48px; font-size: 14px;
}
.project-tabs :deep(.el-tabs__content) { padding: 0; }
</style>
