<template>
  <el-dropdown trigger="click" @command="handleLocaleChange">
    <button :class="buttonClass || 'p-2 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors'" :title="`语言：${currentLocaleName}`">
      <i class="fa-solid fa-globe"></i>
      <span v-if="showLabel" class="ml-1 text-xs">{{ currentLocaleName }}</span>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="locale in localeOptions"
          :key="locale.value"
          :command="locale.value"
          :class="{ 'is-active': currentLocale === locale.value }"
        >
          <div class="flex items-center justify-between w-full min-w-[140px]">
            <span>{{ locale.label }}</span>
            <i v-if="currentLocale === locale.value" class="fa-solid fa-check text-blue-600 ml-2"></i>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { localeOptions, localeNames, type LocaleKey } from '@/i18n'

defineProps<{
  buttonClass?: string
  iconSize?: string
  showLabel?: boolean
}>()

const { locale, t } = useI18n()

const currentLocale = computed(() => locale.value as LocaleKey)
const currentLocaleName = computed(() => localeNames[currentLocale.value])

function handleLocaleChange(lang: LocaleKey) {
  locale.value = lang
  localStorage.setItem('locale', lang)
  document.documentElement.setAttribute('lang', lang)
  ElMessage.success(t('settings.languageSetTo', { name: localeNames[lang] }))
}
</script>
