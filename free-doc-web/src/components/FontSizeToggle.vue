<template>
  <el-dropdown trigger="click" @command="handleFontSizeChange">
    <button
      :class="[
        'p-1.5 rounded-md hover:bg-gray-100 transition-colors',
        'text-gray-600',
        buttonClass
      ]"
      :title="$t('components.fontSizeLabel') + themeStore.getFontSizeName(themeStore.fontSize)"
    >
      <i :class="['fa-solid fa-text-height', iconSize]"></i>
    </button>
    <template #dropdown>
      <el-dropdown-menu class="!rounded-lg !p-1 !min-w-[120px]">
        <el-dropdown-item
          v-for="size in fontSizes"
          :key="size.value"
          :command="size.value"
          :class="[
            '!rounded-md !px-3 !py-2 !text-sm',
            themeStore.fontSize === size.value ? '!bg-blue-50 !text-blue-600' : ''
          ]"
        >
          <div class="flex items-center gap-2.5">
            <i :class="[size.icon, 'text-gray-500']"></i>
            <span>{{ size.label }}</span>
            <i v-if="themeStore.fontSize === size.value" class="fa-solid fa-check text-xs ml-auto text-blue-600"></i>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useThemeStore, type FontSize } from '@/stores/theme'

const { t } = useI18n()

interface Props {
  buttonClass?: string
  iconSize?: string
  showMessage?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  buttonClass: '',
  iconSize: 'text-sm',
  showMessage: true
})

const themeStore = useThemeStore()

const fontSizes = computed(() => [
  { value: 'small' as FontSize, label: t('components.fontSizeSmall'), icon: 'fa-solid fa-minus' },
  { value: 'medium' as FontSize, label: t('components.fontSizeMedium'), icon: 'fa-solid fa-font' },
  { value: 'large' as FontSize, label: t('components.fontSizeLarge'), icon: 'fa-solid fa-plus' }
])

function handleFontSizeChange(size: FontSize) {
  themeStore.setFontSize(size)

  if (props.showMessage) {
    ElMessage.success(t('components.fontSizeSetTo', { name: themeStore.getFontSizeName(size) }))
  }
}
</script>
