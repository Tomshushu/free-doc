<template>
  <div class="icon-picker">
    <div class="flex items-center gap-2 mb-2">
      <div
        class="w-10 h-10 rounded-lg border-2 flex items-center justify-center cursor-pointer transition-all"
        :class="modelValue ? 'border-primary/40 bg-primary/5' : 'border-gray-200 bg-gray-50'"
        @click="showPicker = !showPicker"
      >
        <i :class="[modelValue || defaultIcon, 'text-lg', modelValue ? 'text-primary' : 'text-gray-400']"></i>
      </div>
      <div class="flex-1">
        <span class="text-sm text-gray-600">{{ modelValue || $t('components.selectIcon') }}</span>
      </div>
      <el-button text size="small" @click="showPicker = !showPicker">
        <i class="fa-solid fa-palette"></i>
      </el-button>
    </div>

    <transition name="slide-down">
      <div v-if="showPicker" class="icon-grid border border-gray-200 rounded-lg p-3 bg-white shadow-lg">
        <div class="grid grid-cols-6 gap-2">
          <div
            v-for="icon in icons"
            :key="icon.value"
            class="icon-item w-10 h-10 rounded-lg border-2 flex items-center justify-center cursor-pointer transition-all hover:scale-110"
            :class="modelValue === icon.value ? 'border-primary bg-primary/10 shadow-sm' : 'border-gray-100 hover:border-gray-300 hover:bg-gray-50'"
            :title="icon.label"
            @click="selectIcon(icon.value)"
          >
            <i :class="[icon.value, modelValue === icon.value ? 'text-primary' : 'text-gray-600']"></i>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  modelValue?: string
  defaultIcon?: string
  type?: 'user' | 'project' | 'team'
}>(), {
  defaultIcon: 'fa-solid fa-user',
  type: 'user'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const showPicker = ref(false)

const userIcons = [
  { value: 'fa-solid fa-user', label: t('components.iconUser') },
  { value: 'fa-solid fa-user-tie', label: t('components.iconBusiness') },
  { value: 'fa-solid fa-user-graduate', label: t('components.iconStudent') },
  { value: 'fa-solid fa-user-astronaut', label: t('components.iconAstronaut') },
  { value: 'fa-solid fa-user-ninja', label: t('components.iconNinja') },
  { value: 'fa-solid fa-user-secret', label: t('components.iconAgent') },
  { value: 'fa-solid fa-user-shield', label: t('components.iconSecurity') },
  { value: 'fa-solid fa-user-gear', label: t('components.iconAdmin') },
  { value: 'fa-solid fa-user-pen', label: t('components.iconEdit') },
  { value: 'fa-solid fa-user-check', label: t('components.iconCert') },
  { value: 'fa-solid fa-user-plus', label: t('components.iconAdd') },
  { value: 'fa-solid fa-user-lock', label: t('components.iconLock') },
]

const projectIcons = [
  { value: 'fa-solid fa-folder', label: t('components.iconFolder') },
  { value: 'fa-solid fa-code', label: t('components.iconCode') },
  { value: 'fa-solid fa-book', label: t('components.iconBook') },
  { value: 'fa-solid fa-lightbulb', label: t('components.iconIdea') },
  { value: 'fa-solid fa-rocket', label: t('components.iconLaunch') },
  { value: 'fa-solid fa-flask', label: t('components.iconExperiment') },
  { value: 'fa-solid fa-cube', label: t('components.iconModule') },
  { value: 'fa-solid fa-diagram-project', label: t('components.iconProject') },
  { value: 'fa-solid fa-microchip', label: t('components.iconTech') },
  { value: 'fa-solid fa-palette', label: t('components.iconDesign') },
  { value: 'fa-solid fa-chart-line', label: t('components.iconData') },
  { value: 'fa-solid fa-shield-halved', label: t('components.iconSecurity') },
]

const teamIcons = [
  { value: 'fa-solid fa-users', label: t('components.iconTeam') },
  { value: 'fa-solid fa-people-group', label: t('components.iconGroup') },
  { value: 'fa-solid fa-user-group', label: t('components.iconSquad') },
  { value: 'fa-solid fa-people-arrows', label: t('components.iconCollab') },
  { value: 'fa-solid fa-handshake', label: t('components.iconCoop') },
  { value: 'fa-solid fa-building', label: t('components.iconEnterprise') },
  { value: 'fa-solid fa-landmark', label: t('components.iconOrg') },
  { value: 'fa-solid fa-sitemap', label: t('components.iconArch') },
  { value: 'fa-solid fa-network-wired', label: t('components.iconNetwork') },
  { value: 'fa-solid fa-house-chimney', label: t('components.iconFamily') },
  { value: 'fa-solid fa-school', label: t('components.iconSchool') },
  { value: 'fa-solid fa-hospital', label: t('components.iconMedical') },
]

const icons = props.type === 'project' ? projectIcons : props.type === 'team' ? teamIcons : userIcons

function selectIcon(icon: string) {
  emit('update:modelValue', icon)
  showPicker.value = false
}
</script>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
.icon-grid {
  max-height: 200px;
  overflow-y: auto;
}
</style>
