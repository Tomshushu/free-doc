<template>
  <!-- 加载中 -->
  <div v-if="loading" class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
    <i class="fa-solid fa-circle-notch fa-spin text-3xl text-blue-500"></i>
  </div>

  <!-- 分享不存在 -->
  <div v-else-if="!shareInfo" class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
    <div class="text-center">
      <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-4">
        <i class="fa-solid fa-link-slash text-gray-400 text-3xl"></i>
      </div>
      <h2 class="text-xl font-bold text-gray-800 mb-2">{{ $t('share.shareNotExist') }}</h2>
      <p class="text-sm text-gray-500">{{ $t('share.shareNotExistDesc') }}</p>
    </div>
  </div>

  <!-- 链接已失效 -->
  <div v-else-if="shareInfo.isExpired" class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
    <div class="text-center">
      <div class="w-20 h-20 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-4">
        <i class="fa-solid fa-clock text-red-400 text-3xl"></i>
      </div>
      <h2 class="text-xl font-bold text-gray-800 mb-2">{{ $t('share.shareExpired') }}</h2>
      <p class="text-sm text-gray-500">{{ $t('share.shareExpiredDesc') }}</p>
    </div>
  </div>

  <!-- 密码验证 -->
  <div v-else-if="needVerify" class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-indigo-50">
    <div class="w-full max-w-md px-6">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-600 shadow-lg mb-4">
          <i class="fa-solid fa-book text-white text-2xl"></i>
        </div>
        <h1 class="text-3xl font-bold text-gray-900">FreeDoc</h1>
      </div>
      <div class="bg-white/90 backdrop-blur-lg rounded-2xl shadow-xl p-8 border border-gray-100">
        <div class="text-center mb-6">
          <div class="w-14 h-14 rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-600 flex items-center justify-center mx-auto mb-4 shadow-lg shadow-blue-100">
            <i class="fa-solid fa-lock text-white text-xl"></i>
          </div>
          <h2 class="text-xl font-bold text-gray-900">{{ shareTitle }}</h2>
          <p class="text-sm text-gray-500 mt-1">{{ $t('share.passwordRequired') }}</p>
        </div>
        <el-input
          v-model="inputPassword"
          type="password"
          :placeholder="$t('share.passwordPlaceholder')"
          size="large"
          show-password
          class="!rounded-xl mb-4"
          @keyup.enter="handleVerify"
        />
        <el-button
          type="primary"
          size="large"
          class="w-full !rounded-xl !font-semibold"
          :loading="verifyLoading"
          @click="handleVerify"
        >
          {{ $t('share.verifyAccess') }}
        </el-button>
      </div>
      <p class="mt-8 text-sm text-gray-400 text-center">&copy; 2026 FreeDoc. All rights reserved.</p>
    </div>
  </div>

  <!-- 已验证，显示内容 -->
  <ShareProjectView v-else-if="shareInfo.targetType === 'PROJECT'" :share-info="shareInfo" :share-code="shareCode" />
  <ShareDocView v-else-if="shareInfo.targetType === 'DOC'" :share-info="shareInfo" :share-code="shareCode" />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import ShareProjectView from './ShareProjectView.vue'
import ShareDocView from './ShareDocView.vue'
import { useThemeStore } from '@/stores/theme'
import { getSharePublicInfo, verifyShare } from '@/api/share'
import type { ShareVO } from '@/types'

const route = useRoute()
const { t } = useI18n()
const shareCode = route.params.shareCode as string
const themeStore = useThemeStore()

// session key 格式：share_verified_{shareCode}
const SESSION_KEY = `share_verified_${shareCode}`

const loading = ref(true)
const shareInfo = ref<ShareVO | null>(null)
const needVerify = ref(false)
const inputPassword = ref('')
const verifyLoading = ref(false)

const shareTitle = computed(() => {
  if (!shareInfo.value) return ''
  return shareInfo.value.targetType === 'PROJECT' ? shareInfo.value.projectName : shareInfo.value.docTitle
})

/** 检查是否已在当前会话中验证过 */
function hasVerified(): boolean {
  return sessionStorage.getItem(SESSION_KEY) === '1'
}

/** 标记当前会话已验证 */
function markVerified() {
  sessionStorage.setItem(SESSION_KEY, '1')
}

onMounted(async () => {
  // 初始化主题设置
  themeStore.init()
  
  try {
    shareInfo.value = await getSharePublicInfo(shareCode)

    // 已过期，不显示任何内容（由 v-else-if="shareInfo.isExpired" 处理）
    if (shareInfo.value.isExpired) return

    // 需要密码但尚未验证过 → 显示密码输入框
    if (shareInfo.value.needPassword && !hasVerified()) {
      needVerify.value = true
      return
    }

    // 无需密码 或 已验证过 → 直接展示内容
    needVerify.value = false
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

async function handleVerify() {
  if (!inputPassword.value) {
    ElMessage.warning(t('share.pleaseEnterPassword'))
    return
  }
  verifyLoading.value = true
  try {
    const result = await verifyShare(shareCode, inputPassword.value)
    // 同时存储 token 和验证标记
    sessionStorage.setItem('shareToken', result.shareCode || shareCode)
    markVerified()
    needVerify.value = false
  } catch (e: any) {
    console.error(e)
  } finally {
    verifyLoading.value = false
  }
}
</script>
