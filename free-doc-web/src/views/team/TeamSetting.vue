<template>
  <div class="p-6 max-w-5xl mx-auto">
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 mb-6 overflow-hidden">
      <div class="h-20 bg-gradient-to-r from-purple-500 to-indigo-500 relative">
        <div class="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImEiIHBhdHRlcm5Vbml0cz0idXNlclNwYWNlT25Vc2UiIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoMjU1LDI1NSwyNTUsMC4xKSIvPjwvcGF0dGVybj48L2RlZnM+PHJlY3QgZmlsbD0idXJsKCNhKSIgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIvPjwvc3ZnPg==')] opacity-30"></div>
      </div>
      <div class="px-6 pb-6 -mt-8 relative z-10 flex items-end gap-4">
        <div class="w-16 h-16 rounded-xl shadow-lg border-4 border-white flex items-center justify-center text-xl bg-gradient-to-br from-purple-50 to-indigo-50">
          <i :class="[team?.teamIcon || 'fa-solid fa-users', 'text-purple-500']"></i>
        </div>
        <div class="flex-1 min-w-0 pt-1">
          <h1 class="text-xl font-bold text-gray-900 truncate">{{ team?.teamName }}</h1>
          <p class="text-gray-500 text-sm mt-0.5 line-clamp-1">{{ team?.teamDesc || $t('common.noDescription') }}</p>
        </div>
        <el-button v-if="isTeamOwner" text size="small" @click="activeTab = 'info'">
          <i class="fa-solid fa-pen mr-1"></i>{{ $t('common.edit') }}
        </el-button>
        <el-popconfirm
          v-if="isTeamOwner"
          :title="$t('team.confirmDeleteTeam')"
          :confirm-button-text="$t('common.confirmDelete')"
          :cancel-button-text="$t('common.cancel')"
          @confirm="handleDeleteTeam"
        >
          <template #reference>
            <el-button text size="small" type="danger">
              <i class="fa-solid fa-trash-can mr-1"></i>{{ $t('common.delete') }}
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <el-tabs v-model="activeTab" class="team-tabs">
        <el-tab-pane name="projects">
          <template #label>
            <span class="flex items-center gap-1.5"><i class="fa-solid fa-folder-tree"></i>{{ $t('layout.project') }}</span>
          </template>
          <TeamProjectsList :team-id="teamId" />
        </el-tab-pane>

        <el-tab-pane v-if="isTeamOwner" name="members">
          <template #label>
            <span class="flex items-center gap-1.5"><i class="fa-solid fa-users"></i>{{ $t('common.members') }}</span>
          </template>
          <MembersPanel :team-id="teamId" />
        </el-tab-pane>

        <el-tab-pane v-if="isTeamOwner" name="info">
          <template #label>
            <span class="flex items-center gap-1.5"><i class="fa-solid fa-gear"></i>{{ $t('common.settings') }}</span>
          </template>
          <TeamInfoPanel :team="team" @save="handleSaveInfo" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getTeamById, getTeamMembers, updateTeam, deleteTeam as deleteTeamApi, type TeamMemberVO } from '@/api/team'
import { useUserStore } from '@/stores/user'
import TeamProjectsList from './TeamProjectsList.vue'
import MembersPanel from './MembersPanel.vue'
import TeamInfoPanel from './TeamInfoPanel.vue'
import type { Team } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const teamId = route.params.teamId as string

const team = ref<Team | null>(null)
const activeTab = ref('projects')
const currentMemberInfo = ref<TeamMemberVO | null>(null)

const isTeamOwner = computed(() => currentMemberInfo.value?.type === 'OWNER')

onMounted(async () => {
  const tab = route.query.tab as string
  if (tab) activeTab.value = tab
  await loadTeam()
})

async function loadTeam() {
  try {
    const [teamData, members] = await Promise.all([
      getTeamById(teamId),
      getTeamMembers(teamId)
    ])
    team.value = teamData
    currentMemberInfo.value = members.find((m: TeamMemberVO) => m.userId === userStore.user?.userId) || null
  } catch (e) {
    console.error(e)
  }
}

async function handleSaveInfo(data: Partial<Team>) {
  try {
    await updateTeam(teamId, data)
    ElMessage.success(t('common.saveSuccess'))
    await loadTeam()
  } catch (e: any) {
    ElMessage.error(e.message || t('common.saveFailed'))
  }
}

async function handleDeleteTeam() {
  try {
    await deleteTeamApi(teamId)
    ElMessage.success(t('team.teamDeleted'))
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.message || t('common.deleteFailed'))
  }
}
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.team-tabs :deep(.el-tabs__header) {
  margin: 0; padding: 0 20px; background-color: #fafafa; border-bottom: 1px solid #f0f0f0;
}
.team-tabs :deep(.el-tabs__item) {
  padding: 0 20px; height: 48px; line-height: 48px; font-size: 14px;
}
.team-tabs :deep(.el-tabs__content) { padding: 0; }
</style>
