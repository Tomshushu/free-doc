<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4 p-6">
    <div
      v-for="proj in projects"
      :key="proj.projectId"
      class="project-card p-4 rounded-lg border border-gray-100 hover:border-blue-200 hover:shadow-md cursor-pointer transition-all group relative"
      @click="$router.push(`/project/${proj.projectId}`)"
    >
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center shrink-0 group-hover:bg-blue-100 transition-colors">
          <i :class="[proj.projectIcon || 'fa-solid fa-folder', 'text-blue-500']"></i>
        </div>
        <div class="flex-1 min-w-0">
          <h4 class="font-medium text-gray-800 truncate text-sm">{{ proj.projectName }}</h4>
          <p class="text-xs text-gray-400 mt-1 line-clamp-1">{{ proj.projectDesc || $t('common.noDescription') }}</p>
          <p class="text-xs text-gray-300 mt-1">{{ formatDate(proj.createTime) }}</p>
        </div>
        <i class="fa-solid fa-chevron-right text-gray-200 group-hover:text-blue-300 text-xs shrink-0 mt-1"></i>
      </div>
    </div>

    <!-- 创建新项目卡片 -->
    <div
      class="project-card p-4 rounded-lg border border-dashed border-gray-300 hover:border-primary hover:bg-primary/5 cursor-pointer transition-all flex items-center justify-center gap-2 group min-h-[88px]"
      @click="showCreate = true"
    >
      <i class="fa-solid fa-plus text-gray-300 group-hover:text-primary text-lg"></i>
      <span class="text-sm text-gray-400 group-hover:text-primary font-medium">{{ $t('project.createNewProject') }}</span>
    </div>

    <div v-if="projects.length === 0 && !loading" class="col-span-full py-12 text-center text-gray-400">
      <i class="fa-solid fa-folder-open text-3xl mb-2 block opacity-40"></i>
      {{ $t('project.noProjectsInTeam') }}
    </div>

    <div v-if="loading" class="col-span-full py-12 text-center">
      <el-icon class="is-loading text-primary text-3xl"><Loading /></el-icon>
    </div>
  </div>

  <!-- 创建项目弹窗 -->
  <el-dialog v-model="showCreate" :title="$t('project.createNewProject')" width="420px" destroy-on-close>
    <el-form :model="form" label-width="80px" @submit.prevent="handleCreate">
      <el-form-item :label="$t('layout.projectName')" required>
        <el-input v-model="form.projectName" :placeholder="$t('project.projectNamePlaceholder')" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item :label="$t('layout.projectDesc')">
        <el-input v-model="form.projectDesc" type="textarea" :rows="3" :placeholder="$t('project.projectDescPlaceholder')" maxlength="200" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreate = false">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" :disabled="!form.projectName.trim()" @click="handleCreate">{{ $t('common.create') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Loading } from '@element-plus/icons-vue'
import { getTeamProjects, createProject } from '@/api/project'
import type { Project } from '@/types'

const props = defineProps<{ teamId: string }>()
const router = useRouter()
const { t } = useI18n()

const projects = ref<Project[]>([])
const loading = ref(false)
const showCreate = ref(false)
const form = ref({ projectName: '', projectDesc: '' })

watch(() => props.teamId, async (teamId) => {
  if (teamId) await load(teamId)
}, { immediate: true })

onMounted(async () => {
  if (props.teamId) await load(props.teamId)
})

async function load(teamId: string) {
  loading.value = true
  try {
    projects.value = await getTeamProjects(teamId)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!form.value.projectName.trim()) return
  try {
    const p = await createProject({
      projectName: form.value.projectName.trim(),
      projectDesc: form.value.projectDesc,
      teamId: props.teamId
    })
    ElMessage.success(t('project.projectCreated'))
    showCreate.value = false
    form.value = { projectName: '', projectDesc: '' }
    await load(props.teamId)
    router.push(`/workspace/${p.projectId}`)
  } catch (e: any) {
    ElMessage.error(e.message || t('common.createFailed'))
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
</style>
