<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-4">
      <span class="font-medium text-gray-700">{{ $t('team.memberList', { n: members.length }) }}</span>
      <el-button type="primary" size="small" @click="showAdd = true">
        <i class="fa-solid fa-user-plus mr-1"></i>{{ $t('team.inviteMember') }}
      </el-button>
    </div>

    <!-- 成员卡片列表 -->
    <div class="space-y-3">
      <div
        v-for="m in members"
        :key="m.id"
        class="member-row flex items-center gap-3 p-3 rounded-lg border border-gray-100 hover:bg-gray-50 transition-colors"
      >
        <el-avatar :size="36" class="bg-purple-100 !text-purple-600 shrink-0 font-semibold text-sm">
          {{ (m.userName || m.userId || '?').charAt(0).toUpperCase() }}
        </el-avatar>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="font-medium text-sm text-gray-800">{{ m.userName || m.userId || '-' }}</span>
            <el-tag v-if="m.type === 'OWNER'" size="small" type="warning">{{ $t('team.allOwner') }}</el-tag>
            <el-tag v-else-if="m.isDefault" size="small" type="success" effect="plain">{{ $t('team.defaultLabel') }}</el-tag>
            <el-tag v-else size="small" effect="plain">{{ $t('team.participant') }}</el-tag>
          </div>
          <p v-if="m.userName && m.userId" class="text-xs text-gray-400 mt-0.5">ID: {{ m.userId }}</p>
        </div>
        <!-- 角色切换（非 OWNER 可操作） -->
        <el-dropdown
          v-if="m.type !== 'OWNER'"
          trigger="click"
          @command="(cmd: string) => handleChangeRole(m, cmd)"
        >
          <el-button text size="small">
            {{ $t('team.switchRole') }}<i class="fa-solid fa-chevron-down ml-1 text-xs"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="PARTICIPANT">{{ $t('team.participant') }}</el-dropdown-item>
              <el-dropdown-item v-if="!m.isDefault" command="DEFAULT">{{ $t('team.setDefaultTeam') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-popconfirm
          v-if="m.type !== 'OWNER'"
          :title="$t('team.confirmRemoveMember')"
          :confirm-button-text="$t('common.confirm')"
          :cancel-button-text="$t('common.cancel')"
          @confirm="handleRemove(m)"
        >
          <template #reference>
            <el-button text type="danger" size="small">
              <i class="fa-solid fa-xmark mr-1"></i>{{ $t('common.remove') }}
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <div v-if="members.length === 0" class="text-center py-12 text-gray-400">
      <i class="fa-solid fa-users-slash text-3xl mb-2 block opacity-30"></i>
      {{ $t('team.noMembers') }}
    </div>

    <!-- 邀请弹窗 -->
    <el-dialog v-model="showAdd" :title="$t('team.inviteMember')" width="480px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item :label="$t('team.searchUser')">
          <el-select
            v-model="form.userId"
            filterable
            remote
            reserve-keyword
            :placeholder="$t('team.searchUserPlaceholder')"
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
          <el-select v-model="form.type" class="w-full">
            <el-option :label="$t('team.participant')" value="PARTICIPANT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAdd">{{ $t('team.inviteMember') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getTeamMembers, addTeamMember, removeTeamMember } from '@/api/team'
import { searchUsers as searchUsersApi } from '@/api/user'
import type { User, TeamMemberRequest } from '@/types'

const props = defineProps<{ teamId: string }>()
const { t } = useI18n()

interface TeamMember {
  id: string
  userId: string
  userName?: string
  type: 'OWNER' | 'PARTICIPANT'
  isDefault: boolean
}

const members = ref<TeamMember[]>([])
const showAdd = ref(false)
const form = reactive<TeamMemberRequest>({ userId: '', type: 'PARTICIPANT' })

// 用户搜索
const userOptions = ref<User[]>([])
const userSearchLoading = ref(false)

onMounted(load)

async function load() {
  try {
    const data: any[] = await getTeamMembers(props.teamId)
    // 尝试从 userId 解析用户名（如果后端返回了 userName）
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
  if (!form.userId) {
    ElMessage.warning(t('team.pleaseSelectUser'))
    return
  }
  try {
    await addTeamMember(props.teamId, { userId: form.userId, type: form.type })
    ElMessage.success(t('team.invited'))
    showAdd.value = false
    form.userId = ''
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || t('team.operationFailed'))
  }
}

async function handleRemove(m: TeamMember) {
  try {
    await removeTeamMember(props.teamId, m.userId)
    ElMessage.success(t('common.remove'))
    await load()
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

async function handleChangeRole(m: TeamMember, cmd: string) {
  if (cmd === 'PARTICIPANT') {
    ElMessage.info(t('team.roleSwitchNeedsBackend'))
  } else if (cmd === 'DEFAULT') {
    ElMessage.info(t('team.setDefaultNeedsBackend'))
  }
}
</script>
