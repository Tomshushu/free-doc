<template>
  <div class="p-6 max-w-lg">
    <el-form :model="form" label-width="100px" size="large">
      <el-form-item :label="$t('project.projectNameLabel')">
        <el-input v-model="form.projectName" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item :label="$t('project.projectIconLabel')">
        <IconPicker v-model="form.projectIcon" type="project" default-icon="fa-solid fa-folder" />
      </el-form-item>
      <el-form-item :label="$t('project.cardStyleLabel')">
        <ProjectStylePicker v-model="form.projectStyle" :icon="form.projectIcon || 'fa-solid fa-folder'" />
      </el-form-item>
      <el-form-item :label="$t('project.projectDescLabel')">
        <el-input
          v-model="form.projectDesc"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          :placeholder="$t('project.projectDescPlaceholder2')"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">
          <i class="fa-solid fa-save mr-1"></i>{{ $t('project.saveChanges') }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import IconPicker from '@/components/IconPicker.vue'
import ProjectStylePicker from '@/components/ProjectStylePicker.vue'
import { parseIconWithStyle, buildIconWithStyle } from '@/utils/projectStyle'
import type { Project } from '@/types'

const props = defineProps<{ project: Project | null }>()
const emit = defineEmits<{ (e: 'save', data: Partial<Project>): void }>()

const form = reactive({ projectName: '', projectIcon: 'fa-solid fa-folder', projectStyle: 'default', projectDesc: '' })

watch(() => props.project, (val) => {
  if (val) {
    form.projectName = val.projectName
    const parsed = parseIconWithStyle(val.projectIcon)
    form.projectIcon = parsed.icon
    form.projectStyle = parsed.style
    form.projectDesc = val.projectDesc || ''
  }
}, { immediate: true })

function handleSave() {
  const iconWithStyle = buildIconWithStyle(form.projectIcon, form.projectStyle)
  emit('save', { projectName: form.projectName, projectIcon: iconWithStyle, projectDesc: form.projectDesc })
}
</script>
