<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/40">
    <!-- 顶部导航栏 -->
    <header class="bg-white/80 backdrop-blur-xl border-b border-gray-200/80 shadow-sm z-50">
      <div class="px-8 py-4">
        <div class="flex items-center justify-between">
          <!-- 左侧：Logo + 返回按钮 -->
          <div class="flex items-center gap-6">
            <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg">
                <i class="fa-solid fa-file-lines text-white text-lg"></i>
              </div>
              <div>
                <h1 class="text-xl font-bold text-gray-900">FreeDoc</h1>
                <p class="text-xs text-gray-500">{{ $t('common.appSlogan') }}</p>
              </div>
            </div>
            
            <div class="h-8 w-px bg-gray-200"></div>
            
            <button 
              class="flex items-center gap-2 px-4 py-2 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              @click="router.back()"
            >
              <i class="fa-solid fa-arrow-left"></i>
              <span>{{ $t('common.back') }}</span>
            </button>
          </div>

          <!-- 右侧：语言切换 + 用户信息 -->
          <div class="flex items-center gap-3">
            <!-- 语言切换 -->
            <LanguageToggle button-class="p-2.5" icon-size="text-lg" />

            <!-- 字体大小切换 -->
            <FontSizeToggle button-class="p-2.5" icon-size="text-lg" />

            <!-- 字体家族切换 -->
            <FontFamilyToggle button-class="p-2.5" icon-size="text-lg" />

            <!-- 用户下拉菜单 -->
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-50 px-3 py-2 rounded-lg transition-colors">
                <div class="w-9 h-9 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-sm font-bold shadow-sm">
                  {{ (userStore.user?.userName || '').charAt(0) || 'U' }}
                </div>
                <div class="text-left">
                  <p class="text-sm font-medium text-gray-900">{{ userStore.user?.userName }}</p>
                  <p class="text-xs text-gray-500">{{ userStore.user?.account }}</p>
                </div>
                <i class="fa-solid fa-chevron-down text-xs text-gray-400"></i>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="!rounded-xl !p-2 !min-w-[200px]">
                  <el-dropdown-item command="profile" class="!rounded-lg !px-3 !py-2.5">
                    <div class="flex items-center gap-3">
                      <i class="fa-solid fa-user text-gray-500"></i>
                      <span>{{ $t('common.profile') }}</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="!rounded-lg !px-3 !py-2.5 !text-red-500 hover:!bg-red-50">
                    <div class="flex items-center gap-3">
                      <i class="fa-solid fa-right-from-bracket"></i>
                      <span>{{ $t('common.logout') }}</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="flex-1 overflow-y-auto">
      <div class="max-w-4xl mx-auto px-8 py-8">
        <div class="bg-white rounded-xl shadow-sm border border-gray-100">
          <!-- 页面标题 -->
          <div class="px-6 py-4 border-b border-gray-100">
            <h1 class="text-xl font-bold text-gray-900 flex items-center gap-2">
              <i class="fa-solid fa-gear text-blue-600"></i>
              {{ $t('settings.systemSettings') }}
            </h1>
            <p class="text-sm text-gray-500 mt-1">{{ $t('settings.systemSettingsDesc') }}</p>
          </div>

          <div class="p-6">
            <el-tabs v-model="activeTab" class="settings-tabs">
              <!-- 外观设置 -->
              <el-tab-pane name="appearance">
                <template #label>
                  <span class="flex items-center gap-2">
                    <i class="fa-solid fa-palette"></i>
                    {{ $t('settings.appearance') }}
                  </span>
                </template>
                
                <div class="space-y-6">
                  <!-- 字体大小 -->
                  <div class="setting-item">
                    <div class="setting-header">
                      <h3 class="text-lg font-semibold text-gray-900">{{ $t('settings.fontSize') }}</h3>
                      <p class="text-sm text-gray-500">{{ $t('settings.fontSizeDesc') }}</p>
                    </div>
                    <div class="setting-content">
                      <el-radio-group v-model="fontSize" @change="handleFontSizeChange">
                        <el-radio value="small">{{ $t('settings.small') }}</el-radio>
                        <el-radio value="medium">{{ $t('settings.medium') }}</el-radio>
                        <el-radio value="large">{{ $t('settings.large') }}</el-radio>
                      </el-radio-group>
                    </div>
                  </div>

                  <!-- 字体家族 -->
                  <div class="setting-item">
                    <div class="setting-header">
                      <h3 class="text-lg font-semibold text-gray-900">{{ $t('settings.fontFamily') }}</h3>
                      <p class="text-sm text-gray-500">{{ $t('settings.fontFamilyDesc') }}</p>
                    </div>
                    <div class="setting-content">
                      <div class="grid grid-cols-2 gap-4 w-full">
                        <div
                          v-for="font in fontFamilies"
                          :key="font.value"
                          :class="[
                            'font-option',
                            fontFamily === font.value ? 'font-option-active' : ''
                          ]"
                          @click="handleFontFamilyChange(font.value)"
                        >
                          <div class="font-preview" :style="{ fontFamily: font.preview }">
                            <span class="font-sample">{{ font.sample }}</span>
                          </div>
                          <div class="font-info">
                            <h4 class="font-medium text-gray-900">{{ font.label }}</h4>
                            <p class="text-xs text-gray-500">{{ font.description }}</p>
                          </div>
                          <div v-if="fontFamily === font.value" class="font-check">
                            <i class="fa-solid fa-check text-blue-600"></i>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <!-- 通用设置 -->
              <el-tab-pane name="general">
                <template #label>
                  <span class="flex items-center gap-2">
                    <i class="fa-solid fa-sliders"></i>
                    {{ $t('settings.general') }}
                  </span>
                </template>
                
                <div class="space-y-6">
                  <!-- 语言设置 -->
                  <div class="setting-item">
                    <div class="setting-header">
                      <h3 class="text-lg font-semibold text-gray-900">{{ $t('settings.language') }}</h3>
                      <p class="text-sm text-gray-500">{{ $t('settings.languageDesc') }}</p>
                    </div>
                    <div class="setting-content">
                      <el-select v-model="language" style="width: 200px" @change="handleLanguageChange">
                        <el-option v-for="loc in localeOptions" :key="loc.value" :label="loc.label" :value="loc.value" />
                      </el-select>
                    </div>
                  </div>

                  <!-- 自动保存 -->
                  <div class="setting-item">
                    <div class="setting-header">
                      <h3 class="text-lg font-semibold text-gray-900">{{ $t('settings.autoSave') }}</h3>
                      <p class="text-sm text-gray-500">{{ $t('settings.autoSaveDesc') }}</p>
                    </div>
                    <div class="setting-content">
                      <el-switch v-model="autoSave" @change="handleAutoSaveChange" />
                    </div>
                  </div>

                  <!-- 通知设置 -->
                  <div class="setting-item">
                    <div class="setting-header">
                      <h3 class="text-lg font-semibold text-gray-900">{{ $t('settings.notification') }}</h3>
                      <p class="text-sm text-gray-500">{{ $t('settings.notificationDesc') }}</p>
                    </div>
                    <div class="setting-content">
                      <el-switch v-model="notifications" @change="handleNotificationChange" />
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <!-- 关于 -->
              <el-tab-pane name="about">
                <template #label>
                  <span class="flex items-center gap-2">
                    <i class="fa-solid fa-info-circle"></i>
                    {{ $t('settings.about') }}
                  </span>
                </template>
                
                <div class="text-center py-8">
                  <div class="w-20 h-20 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg">
                    <i class="fa-solid fa-file-lines text-white text-3xl"></i>
                  </div>
                  <h2 class="text-2xl font-bold text-gray-900 mb-2">FreeDoc</h2>
                  <p class="text-gray-500 mb-4">{{ $t('common.appSlogan') }}</p>
                  <div class="space-y-2 text-sm text-gray-600">
                    <p>{{ $t('settings.versionLabel') }}</p>
                    <p>{{ $t('settings.buildTime') }}{{ new Date().toLocaleDateString('zh-CN') }}</p>
                    <p>© 2024 FreeDoc Team. All rights reserved.</p>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore, type FontSize, type FontFamily } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { localeOptions, localeNames, type LocaleKey } from '@/i18n'
import LanguageToggle from '@/components/LanguageToggle.vue'
import FontSizeToggle from '@/components/FontSizeToggle.vue'
import FontFamilyToggle from '@/components/FontFamilyToggle.vue'

const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()
const activeTab = ref('appearance')

// 设置项 - 从 store 中获取当前值
const fontSize = ref<FontSize>(themeStore.fontSize)
const fontFamily = ref<FontFamily>(themeStore.fontFamily)
const language = ref(localStorage.getItem('locale') || 'en')
const autoSave = ref(true)
const notifications = ref(false)

onMounted(() => {
  // 同步 store 中的字体设置
  fontSize.value = themeStore.fontSize
  fontFamily.value = themeStore.fontFamily
})

const { locale, t } = useI18n()

// 字体家族选项
const fontFamilies = computed(() => [
  {
    value: 'system' as FontFamily,
    label: t('settings.systemFont'),
    description: t('settings.systemFontDesc'),
    preview: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    sample: t('settings.systemFontSample')
  },
  {
    value: 'serif' as FontFamily,
    label: t('settings.serifFont'),
    description: t('settings.serifFontDesc'),
    preview: '"Times New Roman", "宋体", serif',
    sample: t('settings.serifFontSample')
  },
  {
    value: 'kai' as FontFamily,
    label: t('settings.kaiFont'),
    description: t('settings.kaiFontDesc'),
    preview: '"楷体", "KaiTi", "STKaiti", serif',
    sample: t('settings.kaiFontSample')
  },
  {
    value: 'hei' as FontFamily,
    label: t('settings.heiFont'),
    description: t('settings.heiFontDesc'),
    preview: '"黑体", "SimHei", "Microsoft YaHei", sans-serif',
    sample: t('settings.heiFontSample')
  },
  {
    value: 'song' as FontFamily,
    label: t('settings.songFont'),
    description: t('settings.songFontDesc'),
    preview: '"宋体", "SimSun", "NSimSun", serif',
    sample: t('settings.songFontSample')
  }
])

function handleFontSizeChange(size: string | number | boolean | undefined) {
  if (!size) return
  themeStore.setFontSize(size as FontSize)
  fontSize.value = size as FontSize
  ElMessage.success(t('settings.fontSizeSetTo', { name: themeStore.getFontSizeName(size as FontSize) }))
}

function handleFontFamilyChange(family: FontFamily) {
  themeStore.setFontFamily(family)
  fontFamily.value = family
  ElMessage.success(t('settings.fontFamilySetTo', { name: themeStore.getFontFamilyName(family) }))
}

function handleLanguageChange(lang: string) {
  locale.value = lang
  language.value = lang
  localStorage.setItem('locale', lang)
  document.documentElement.setAttribute('lang', lang)
  ElMessage.success(t('settings.languageSetTo', { name: localeNames[lang as LocaleKey] }))
}

function handleAutoSaveChange(enabled: string | number | boolean) {
  ElMessage.success(enabled ? t('settings.autoSaveEnabled') : t('settings.autoSaveDisabled'))
}

function handleNotificationChange(enabled: string | number | boolean) {
  ElMessage.success(enabled ? t('settings.notificationEnabled') : t('settings.notificationDisabled'))
}

function handleUserCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm(t('layout.confirmLogout'), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.setting-item {
  @apply border border-gray-100 rounded-lg p-4;
}

.setting-header {
  @apply mb-4;
}

.setting-content {
  @apply flex items-center;
}

.theme-option {
  @apply relative border-2 border-gray-200 rounded-lg p-4 cursor-pointer transition-all hover:border-blue-300;
}

.theme-option-active {
  @apply border-blue-500 bg-blue-50;
}

.theme-preview {
  @apply flex items-center justify-center h-12 mb-3;
}

.theme-info {
  @apply text-center;
}

.theme-check {
  @apply absolute top-2 right-2;
}

.font-option {
  @apply relative border-2 border-gray-200 rounded-lg p-4 cursor-pointer transition-all hover:border-blue-300;
}

.font-option-active {
  @apply border-blue-500 bg-blue-50;
}

.font-preview {
  @apply flex items-center justify-center h-12 mb-3;
}

.font-sample {
  @apply text-lg font-medium;
}

.font-info {
  @apply text-center;
}

.font-check {
  @apply absolute top-2 right-2;
}

.settings-tabs :deep(.el-tabs__header) {
  @apply mb-6;
}

.settings-tabs :deep(.el-tabs__item) {
  @apply text-base;
}
</style>