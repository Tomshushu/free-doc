<template>
  <el-config-provider :locale="elLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import en from 'element-plus/es/locale/lang/en'
import ja from 'element-plus/es/locale/lang/ja'
import de from 'element-plus/es/locale/lang/de'

const { locale, t } = useI18n()

const localeMap: Record<string, typeof zhCn> = {
  'zh-CN': zhCn,
  'zh-TW': zhTw,
  en,
  ja,
  de,
}

const elLocale = computed(() => localeMap[locale.value] || en)

watch(locale, () => {
  document.title = t('common.appName') + ' - ' + t('common.appSlogan')
}, { immediate: true })
</script>

<style>
</style>
