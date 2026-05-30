<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-4">
      <span class="font-medium text-gray-700">{{ $t('project.memberList', { n: members.length }) }}</span>
      <el-button type="primary" size="small" @click="showAddMember = true">
        <i class="fa-solid fa-plus mr-1"></i>{{ $t('project.addMember') }}
      </el-button>
    </div>

    <el-table :data="members" stripe>
      <el-table-column :label="$t('workspace.userId')" prop="userId">
        <template #default="{ row }">
          <div class="flex items-center gap-2">
            <el-avatar :size="28" class="bg-blue-100 !text-blue-600 !text-xs font-semibold shrink-0">
              {{ (row.userName || row.userId || '?').charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="min-w-0">
              <span class="font-medium text-sm">{{ row.userName || row.userId }}</span>
              <p v-if="row.userName && row.userId" class="text-xs text-gray-400 truncate max-w-[120px]">{{ row.userId }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.role')" width="110">
        <template #default="{ row }">
          <el-select
            v-if="row.type !== 'OWNER'"
            :model-value="row.type"
            size="small"
            @change="(val: 'OWNER' | 'PARTICIPANT') => handleChangeRole(row, val)"
          >
            <el-option :label="$t('common.participant')" value="PARTICIPANT" />
            <el-option :label="$t('common.owner')" value="OWNER" />
          </el-select>
          <el-tag v-else type="warning" size="small">{{ $t('common.owner') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.permission')" width="100">
        <template #default="{ row }">
          <el-select
            v-if="row.type !== 'OWNER'"
            :model-value="row.permission"
            size="small"
            @change="(val: 'r' | 'rw') => handleChangePermission(row, val)"
          >
            <el-option :label="$t('common.readOnly')" value="r" />
            <el-option :label="$t('common.readWrite')" value="rw" />
          </el-select>
          <el-tag v-else type="success" size="small">{{ $t('common.readWrite') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.action')" width="80">
        <template #default="{ row }">
          <el-button
            v-if="row.type !== 'OWNER'"
            text
            type="danger"
            size="small"
            @click="handleRemove(row)"
          >{{ $t('common.remove') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加成员弹窗 -->
    <el-dialog v-model="showAddMember" :title="$t('project.addMember')" width="480px" destroy-on-close>
      <el-form :model="newMember" label-width="80px">
        <el-form-item :label="$t('project.searchUser')">
          <el-select
            v-model="newMember.userId"
            filterable
            remote
            reserve-keyword
            :placeholder="$t('project.searchUserPlaceholder')"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            class="w-full"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="`${u.userName} (${u.account})`"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.role')">
          <el-select v-model="newMember.type" class="w-full">
            <el-option :label="$t('common.participant')" value="PARTICIPANT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.permission')">
          <el-select v-model="newMember.permission" class="w-full">
            <el-option :label="$t('common.readOnly')" value="r" />
            <el-option :label="$t('common.readWrite')" value="rw" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddMember = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAdd">{{ $t('common.add') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getProjectMembers, addProjectMember, removeProjectMember, updateProjectMember } from '@/api/project'
import { searchUsers as searchUsersApi } from '@/api/user'
import type { User, ProjectUser, ProjectMemberRequest } from '@/types'

const props = defineProps<{ projectId: string }>()
const { t } = useI18n()

interface ProjectMember extends ProjectUser {
  userName?: string
}

const members = ref<ProjectMember[]>([])
const showAddMember = ref(false)
const newMember = reactive<ProjectMemberRequest>({ userId: '', type: 'PARTICIPANT', permission: 'r' })

// 用户搜索
const userOptions = ref<User[]>([])
const userSearchLoading = ref(false)

onMounted(loadMembers)

async function loadMembers() {
  try {
    const data: ProjectUser[] = await getProjectMembers(props.projectId)
    members.value = data.map((m: any) => ({
      ...m,
      userName: m.user?.userName || m.userName || null
    }))
  } catch (e) {
    console.error(e)
  }
}

async function searchUsers(keyword: string) {
  if (!keyword) {
    userOptions.value = []
    return
  }
  userSearchLoading.value = true
  try {
    userOptions.value = await searchUsersApi(keyword)
  } catch (e) {
    console.error(e)
  } finally {
    userSearchLoading.value = false
  }
}

async function handleAdd() {
  if (!newMember.userId) {
    ElMessage.warning(t('project.pleaseSelectUser'))
    return
  }
  try {
    await addProjectMember(props.projectId, { ...newMember })
    ElMessage.success(t('project.addSuccess'))
    showAddMember.value = false
    newMember.userId = ''
    newMember.permission = 'r'
    await loadMembers()
  } catch (e: any) {
    ElMessage.error(e.message || t('project.addFailed'))
  }
}

async function handleRemove(row: ProjectMember) {
  try {
    await ElMessageBox.confirm(t('project.confirmRemoveMember', { name: row.userName || row.userId }), t('common.confirm'), { type: 'warning' })
    await removeProjectMember(props.projectId, row.userId)
    ElMessage.success(t('common.remove'))
    await loadMembers()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleChangeRole(row: ProjectMember, val: 'OWNER' | 'PARTICIPANT') {
  try {
    await updateProjectMember(props.projectId, row.userId, { type: val })
    ElMessage.success(t('project.roleModified'))
    row.type = val
  } catch (e: any) {
    ElMessage.error(e.message || t('common.modifyFailed'))
    await loadMembers()
  }
}

async function handleChangePermission(row: ProjectMember, val: 'r' | 'rw') {
  try {
    await updateProjectMember(props.projectId, row.userId, { permission: val })
    ElMessage.success(t('project.permissionModified'))
    row.permission = val
  } catch (e: any) {
    ElMessage.error(e.message || t('common.modifyFailed'))
    await loadMembers()
  }
}
</script>
