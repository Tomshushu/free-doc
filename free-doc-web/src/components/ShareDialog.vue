<template>
  <el-dialog
    v-model="visible"
    :title="$t('components.shareSettings')"
    width="480px"
    :close-on-click-modal="false"
    class="share-dialog"
    @close="handleClose"
  >
    <div class="space-y-5">
      <!-- 分享类型标识 -->
      <div class="flex items-center gap-3 p-3 bg-blue-50 rounded-xl">
        <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shrink-0">
          <i :class="[targetType === 'PROJECT' ? 'fa-solid fa-folder-open' : 'fa-solid fa-file-lines', 'text-white text-sm']"></i>
        </div>
        <div class="min-w-0">
          <div class="text-xs text-blue-500 font-medium">{{ targetType === 'PROJECT' ? $t('components.shareProject') : $t('components.shareDoc') }}</div>
          <div class="text-sm font-semibold text-gray-800 truncate">{{ targetName }}</div>
        </div>
      </div>

      <!-- 密码设置 -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <label class="text-sm font-medium text-gray-700">{{ $t('components.accessPassword') }}</label>
          <el-switch
            v-model="enablePassword"
            :active-text="$t('components.passwordRequired')"
            :inactive-text="$t('components.noPassword')"
            size="small"
          />
        </div>
        <el-input
          v-if="enablePassword"
          v-model="form.password"
          type="password"
          :placeholder="$t('components.passwordPlaceholder')"
          show-password
          size="large"
          class="!rounded-lg"
        />
      </div>

      <!-- 有效期设置 -->
      <div>
        <label class="text-sm font-medium text-gray-700 mb-2 block">{{ $t('components.validity') }}</label>
        <div class="grid grid-cols-4 gap-2">
          <div
            v-for="option in expireOptions"
            :key="option.value"
            :class="[
              'px-3 py-2 rounded-lg text-center text-sm cursor-pointer transition-all border',
              form.expireHours === option.value
                ? 'bg-blue-50 border-blue-300 text-blue-600 font-medium'
                : 'bg-white border-gray-200 text-gray-600 hover:border-blue-200 hover:bg-blue-50/50'
            ]"
            @click="form.expireHours = option.value"
          >
            {{ option.label }}
          </div>
        </div>
      </div>

      <!-- 已生成的链接 -->
      <div v-if="generatedLink" class="p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl border border-green-200">
        <div class="flex items-center gap-2 mb-2">
          <i class="fa-solid fa-circle-check text-green-500"></i>
          <span class="text-sm font-medium text-green-700">{{ $t('components.shareLinkGenerated') }}</span>
        </div>
        <div class="flex items-center gap-2">
          <el-input
            :model-value="generatedLink"
            readonly
            size="default"
            class="!rounded-lg flex-1"
          />
          <el-button type="primary" size="default" class="!rounded-lg !px-4" @click="copyLink">
            <i class="fa-solid fa-copy mr-1"></i>{{ $t('common.copy') }}
          </el-button>
        </div>
        <div v-if="form.expireHours && form.expireHours > 0" class="text-xs text-gray-500 mt-2">
          <i class="fa-regular fa-clock mr-1"></i>
          {{ $t('components.linkExpiresIn', { time: expireLabel }) }}
        </div>
        <div v-if="enablePassword" class="text-xs text-gray-500 mt-1">
          <i class="fa-solid fa-lock mr-1"></i>
          {{ $t('components.passwordAccessRequired') }}
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex items-center justify-end gap-3">
        <el-button @click="handleClose" class="!rounded-lg">{{ $t('common.cancel') }}</el-button>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="enablePassword && !form.password"
          class="!rounded-lg !px-6"
          @click="handleCreate"
        >
          <i v-if="!generatedLink" class="fa-solid fa-link mr-1.5"></i>
          {{ generatedLink ? $t('components.regenerate') : $t('components.generateLink') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { createShare } from '@/api/share'
import type { ShareCreateRequest } from '@/types'

const { t, locale } = useI18n()

const props = defineProps<{
  targetType: 'PROJECT' | 'DOC'
  targetId: string
  targetName: string
}>()

const visible = defineModel<boolean>('visible', { default: false })

const enablePassword = ref(true)
const loading = ref(false)
const generatedLink = ref('')

const form = reactive({
  password: '',
  expireHours: 24
})

const expireOptions = computed(() => [
  { label: t('components.oneDay'), value: 24 },
  { label: t('components.sevenDays'), value: 168 },
  { label: t('components.thirtyDays'), value: 720 },
  { label: t('components.permanent'), value: 0 }
])

const expireLabel = computed(() => {
  const opt = expireOptions.value.find(o => o.value === form.expireHours)
  return opt?.label || ''
})

async function handleCreate() {
  if (enablePassword.value && !form.password) {
    ElMessage.warning(t('components.pleaseEnterPassword'))
    return
  }

  loading.value = true
  try {
    const data: ShareCreateRequest = {
      targetType: props.targetType,
      targetId: props.targetId,
      password: enablePassword.value ? form.password : undefined,
      expireHours: form.expireHours || undefined
    }
    const result = await createShare(data)
    const baseUrl = window.location.origin
    generatedLink.value = `${baseUrl}/s/${result.shareCode}`

    // 生成包含详细信息的分享内容
    const shareContent = generateShareContent()
    const copied = await copyToClipboard(shareContent)
    if (copied) {
      ElMessage.success(t('components.shareLinkCopied'))
    } else {
      ElMessage.success(t('components.shareLinkGenerated2'))
    }
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function generateShareContent(): string {
  const baseUrl = window.location.origin
  const link = `${baseUrl}/s/${generatedLink.value.split('/').pop()}`

  let content = `${props.targetType === 'PROJECT' ? t('components.shareContentProject') : t('components.shareContentDoc')}：${props.targetName}\n`
  content += `${t('components.shareContentLink')}：${link}\n`

  if (enablePassword.value && form.password) {
    content += `${t('components.shareContentPassword')}${form.password}\n`
  }

  if (form.expireHours && form.expireHours > 0) {
    const expireTime = new Date(Date.now() + form.expireHours * 60 * 60 * 1000)
    const langMap: Record<string, string> = { 'zh-CN': 'zh-CN', 'zh-TW': 'zh-TW', en: 'en-US', ja: 'ja-JP', de: 'de-DE' }
    const dateLocale = langMap[locale.value] || 'en-US'
    const expireDate = expireTime.toLocaleString(dateLocale, {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', hour12: false,
    })
    content += `${t('components.shareContentValidUntil')}${expireDate}\n`
  } else {
    content += `${t('components.shareContentPermanent')}\n`
  }

  content += `\n${t('components.shareContentFrom')}`

  return content
}

async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    try {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.left = '-9999px'
      textarea.style.top = '-9999px'
      document.body.appendChild(textarea)
      textarea.select()
      textarea.setSelectionRange(0, textarea.value.length)
      const result = document.execCommand('copy')
      document.body.removeChild(textarea)
      return result
    } catch {
      return false
    }
  }
}

async function copyLink() {
  const shareContent = generateShareContent()
  const copied = await copyToClipboard(shareContent)
  if (copied) {
    ElMessage.success(t('components.shareInfoCopied'))
  } else {
    ElMessage.error(t('components.copyFailedManual'))
  }
}

function handleClose() {
  visible.value = false
  generatedLink.value = ''
  form.password = ''
  form.expireHours = 24
  enablePassword.value = true
}
</script>

<style scoped>
:deep(.el-dialog) {
  border-radius: 16px;
}
:deep(.el-dialog__header) {
  padding: 20px 24px 12px;
}
:deep(.el-dialog__body) {
  padding: 12px 24px;
}
:deep(.el-dialog__footer) {
  padding: 12px 24px 20px;
}
</style>
