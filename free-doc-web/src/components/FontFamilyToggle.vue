<template>
  <el-dropdown trigger="click" @command="handleFontFamilyChange">
    <button
      :class="buttonClass || 'p-2 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors'"
      :title="$t('components.fontFamilyLabel') + themeStore.getFontFamilyName(themeStore.fontFamily)"
    >
      <i :class="['fa-solid fa-font', iconSize || 'text-base']"></i>
    </button>
    <template #dropdown>
      <el-dropdown-menu class="!rounded-xl !p-2 !min-w-[220px]">
        <el-dropdown-item
          v-for="font in fontFamilies"
          :key="font.value"
          :command="font.value"
          :class="themeStore.fontFamily === font.value ? '!bg-blue-50 !text-blue-600' : ''"
          class="!rounded-lg !px-3 !py-3"
        >
          <div class="flex items-center justify-between w-full">
            <div class="flex items-center gap-3">
              <div class="flex flex-col">
                <span class="font-medium text-sm">{{ font.label }}</span>
                <span :style="{ fontFamily: font.preview }" class="text-xs text-gray-500">{{ font.sampleText }}</span>
              </div>
            </div>
            <i
              v-if="themeStore.fontFamily === font.value"
              class="fa-solid fa-check text-blue-600 text-sm"
            ></i>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useThemeStore, type FontFamily } from '@/stores/theme'

const { t } = useI18n()

interface Props {
  buttonClass?: string
  iconSize?: string
}

defineProps<Props>()

const themeStore = useThemeStore()

const fontFamilies = computed(() => [
  {
    value: 'system' as FontFamily,
    label: t('components.systemFont'),
    preview: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    sample: 'Aa',
    sampleText: t('components.systemFontSample')
  },
  {
    value: 'serif' as FontFamily,
    label: t('components.serifFont'),
    preview: '"Times New Roman", "宋体", serif',
    sample: 'Aa',
    sampleText: t('components.serifFontSample')
  },
  {
    value: 'kai' as FontFamily,
    label: t('components.kaiFont'),
    preview: '"楷体", "KaiTi", "STKaiti", serif',
    sample: 'Aa',
    sampleText: t('components.kaiFontSample')
  },
  {
    value: 'hei' as FontFamily,
    label: t('components.heiFont'),
    preview: '"黑体", "SimHei", "Microsoft YaHei", sans-serif',
    sample: 'Aa',
    sampleText: t('components.heiFontSample')
  },
  {
    value: 'song' as FontFamily,
    label: t('components.songFont'),
    preview: '"宋体", "SimSun", "NSimSun", serif',
    sample: 'Aa',
    sampleText: t('components.songFontSample')
  }
])

function handleFontFamilyChange(fontFamily: FontFamily) {
  themeStore.setFontFamily(fontFamily)
  ElMessage.success(t('components.fontFamilySetTo', { name: themeStore.getFontFamilyName(fontFamily) }))
}
</script>
