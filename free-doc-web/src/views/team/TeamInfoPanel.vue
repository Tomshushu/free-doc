<template>
  <div class="p-6 max-w-lg">
    <el-form :model="form" label-width="90px" size="large">
      <el-form-item :label="$t('team.teamName')">
        <el-input v-model="form.teamName" maxlength="30" show-word-limit />
      </el-form-item>
      <el-form-item :label="$t('team.teamIcon')">
        <IconPicker v-model="form.teamIcon" type="team" default-icon="fa-solid fa-users" />
      </el-form-item>
      <el-form-item :label="$t('team.teamDesc')">
        <el-input
          v-model="form.teamDesc"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          :placeholder="$t('team.teamDescPlaceholder')"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">
          <i class="fa-solid fa-save mr-1"></i>{{ $t('team.saveChanges') }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import IconPicker from '@/components/IconPicker.vue'
import type { Team } from '@/types'

const props = defineProps<{ team: Team | null }>()
const emit = defineEmits<{ (e: 'save', data: Partial<Team>): void }>()

const form = reactive({ teamName: '', teamIcon: 'fa-solid fa-users', teamDesc: '' })

watch(() => props.team, (val) => {
  if (val) {
    form.teamName = val.teamName
    form.teamIcon = val.teamIcon || 'fa-solid fa-users'
    form.teamDesc = val.teamDesc || ''
  }
}, { immediate: true })

function handleSave() {
  emit('save', { teamName: form.teamName, teamIcon: form.teamIcon, teamDesc: form.teamDesc })
}
</script>
