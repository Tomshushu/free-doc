<template>
  <div class="project-style-picker">
    <div class="text-sm text-gray-600 mb-2">{{ $t('components.cardStyle') }}</div>
    <div class="grid grid-cols-5 gap-2">
      <div
        v-for="style in projectStyles"
        :key="style.value"
        class="style-item rounded-lg border-2 cursor-pointer transition-all hover:scale-105 overflow-hidden"
        :class="modelValue === style.value ? 'border-primary shadow-md ring-2 ring-primary/20' : 'border-gray-200 hover:border-gray-300'"
        @click="selectStyle(style.value)"
      >
        <div class="h-14 relative" :class="style.bgClass">
          <div class="absolute inset-0 flex items-center justify-center">
            <i :class="[icon || 'fa-solid fa-folder', 'text-lg', style.iconClass]"></i>
          </div>
        </div>
        <div class="px-1.5 py-1 bg-white text-center">
          <span class="text-[10px] font-medium" :class="modelValue === style.value ? 'text-primary' : 'text-gray-500'">
            {{ style.label }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { projectStyles } from '@/utils/projectStyle'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  modelValue?: string
  icon?: string
}>(), {
  modelValue: 'default',
  icon: 'fa-solid fa-folder'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

function selectStyle(value: string) {
  emit('update:modelValue', value)
}
</script>
