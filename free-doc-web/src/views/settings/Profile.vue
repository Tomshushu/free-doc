<template>
  <div class="p-6">
    <div class="max-w-2xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100">
        <div class="p-6 border-b border-gray-100">
          <h1 class="text-xl font-bold text-gray-900">{{ $t('common.profile') }}</h1>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane :label="$t('profile.basicInfo')" name="info">
            <div class="p-6">
              <div class="flex items-center gap-6 mb-8">
                <div class="w-20 h-20 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-2xl shadow-lg">
                  <i :class="[userForm.userIcon || 'fa-solid fa-user', 'text-2xl']"></i>
                </div>
                <div>
                  <h2 class="text-lg font-semibold text-gray-900">{{ userStore.user?.userName }}</h2>
                  <p class="text-gray-500 text-sm">{{ userStore.user?.account }}</p>
                </div>
              </div>

              <el-form :model="userForm" label-width="100px">
                <el-form-item :label="$t('workspace.username')">
                  <el-input v-model="userForm.userName" maxlength="30" show-word-limit />
                </el-form-item>
                <el-form-item :label="$t('layout.teamIcon')">
                  <IconPicker v-model="userForm.userIcon" type="user" default-icon="fa-solid fa-user" />
                </el-form-item>
                <el-form-item :label="$t('workspace.account')">
                  <el-input :value="userStore.user?.account" disabled />
                </el-form-item>
                <el-form-item :label="$t('profile.registerTime')">
                  <el-input :value="formatDate(userStore.user?.createTime)" disabled />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingInfo" @click="handleSaveInfo">
                    <i class="fa-solid fa-save mr-1"></i>{{ $t('team.saveChanges') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="$t('profile.changePassword')" name="password">
            <div class="p-6 max-w-md">
              <el-form :model="passwordForm" label-width="100px">
                <el-form-item :label="$t('profile.currentPassword')">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password :placeholder="$t('profile.currentPasswordPlaceholder')" />
                </el-form-item>
                <el-form-item :label="$t('profile.newPassword')">
                  <el-input v-model="passwordForm.newPassword" type="password" show-word-limit minlength="6" maxlength="20" :placeholder="$t('profile.newPasswordPlaceholder')" />
                </el-form-item>
                <el-form-item :label="$t('profile.confirmPassword')">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password :placeholder="$t('profile.confirmPasswordPlaceholder')" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
                    <i class="fa-solid fa-lock mr-1"></i>{{ $t('profile.changePassword') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword } from '@/api/user'
import IconPicker from '@/components/IconPicker.vue'

const { t } = useI18n()
const userStore = useUserStore()

const activeTab = ref('info')

const savingInfo = ref(false)
const changingPassword = ref(false)

const userForm = reactive({
  userName: userStore.user?.userName || '',
  userIcon: userStore.user?.userIcon || 'fa-solid fa-user'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(async () => {
  if (!userStore.user?.account || !userStore.user?.createTime) {
    await userStore.fetchUser()
    userForm.userName = userStore.user?.userName || ''
    userForm.userIcon = userStore.user?.userIcon || 'fa-solid fa-user'
  }
})

async function handleSaveInfo() {
  if (!userForm.userName.trim()) {
    ElMessage.warning(t('login.pleaseEnterUsername'))
    return
  }
  savingInfo.value = true
  try {
    await updateUserInfo({
      userName: userForm.userName.trim(),
      userIcon: userForm.userIcon || undefined
    })
    ElMessage.success(t('common.saveSuccess'))
    await userStore.fetchUser()
    userForm.userName = userStore.user?.userName || ''
    userForm.userIcon = userStore.user?.userIcon || 'fa-solid fa-user'
  } catch (e: any) {
    ElMessage.error(e.message || t('common.saveFailed'))
  } finally {
    savingInfo.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword) {
    ElMessage.warning(t('profile.pleaseEnterCurrentPassword'))
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning(t('profile.pleaseEnterNewPassword'))
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning(t('login.passwordLength'))
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning(t('login.passwordMismatch'))
    return
  }

  changingPassword.value = true
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success(t('profile.passwordChangeSuccess'))
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch (e: any) {
    ElMessage.error(e.message || t('common.modifyFailed'))
  } finally {
    changingPassword.value = false
  }
}

function formatDate(date: string | undefined): string {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>
