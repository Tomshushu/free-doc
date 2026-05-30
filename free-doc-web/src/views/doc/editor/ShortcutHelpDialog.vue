<template>
  <el-dialog
    v-model="visible"
    :title="$t('doc.shortcuts')"
    width="480px"
    class="shortcut-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="space-y-6">
      <div v-for="group in groupedShortcuts" :key="group.category">
        <h4 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">{{ group.category }}</h4>
        <div class="space-y-2">
          <div
            v-for="s in group.items"
            :key="s.id"
            class="flex items-center justify-between py-2 px-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <span class="text-sm text-gray-700">{{ s.label }}</span>
            <kbd class="shortcut-kbd">{{ s.shortcut }}</kbd>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ShortcutAction } from '@/composables/useEditorShortcuts'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  shortcuts: ShortcutAction[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const groupedShortcuts = computed(() => {
  const groups: Record<string, ShortcutAction[]> = {}
  for (const s of props.shortcuts) {
    const cat = s.category || t('doc.other')
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(s)
  }
  return Object.entries(groups).map(([category, items]) => ({ category, items }))
})
</script>

<style scoped>
.shortcut-kbd {
  @apply text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded-md font-mono border border-gray-200;
}
</style>
