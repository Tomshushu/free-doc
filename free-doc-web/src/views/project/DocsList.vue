<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-4">
      <el-input
        v-model="keyword"
        :placeholder="$t('project.searchDocPlaceholder')"
        clearable
        class="!w-64"
        :prefix-icon="Search"
      />
      <div class="flex items-center gap-3">
        <span class="text-sm text-gray-400">{{ $t('project.totalDocs', { n: filteredDocs.length }) }}</span>
      </div>
    </div>

    <template v-if="loading">
      <div class="text-center py-12">
        <el-icon class="is-loading text-3xl text-primary"><Loading /></el-icon>
      </div>
    </template>

    <template v-else-if="filteredDocs.length > 0">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="doc in filteredDocs"
          :key="doc.docId"
          class="group border border-gray-100 rounded-lg p-4 hover:border-blue-200 hover:shadow-md transition-all relative"
        >
          <!-- 操作按钮 -->
          <div class="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity z-10" @click.stop>
            <el-dropdown trigger="click" @command="(cmd: string) => handleDocCommand(cmd, doc)">
              <el-button text circle size="small" class="!bg-white/80 shadow-sm">
                <i class="fa-solid fa-ellipsis-vertical text-xs"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit"><i class="fa-solid fa-pen-to-square mr-1"></i>{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="delete" divided><i class="fa-solid fa-trash-can mr-1 text-red-500"></i><span class="text-red-500">{{ $t('common.delete') }}</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="flex items-start gap-3 cursor-pointer" @click="$router.push(`/doc/${doc.docId}/view`)">
            <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center shrink-0 group-hover:bg-blue-100 transition-colors">
              <i :class="[doc.docIcon || 'fa-solid fa-file-lines', 'text-primary']"></i>
            </div>
            <div class="flex-1 min-w-0 pr-6">
              <h4 class="font-medium text-gray-800 text-sm truncate">{{ doc.docTitle }}</h4>
              <p class="text-xs text-gray-400 mt-1 line-clamp-2">{{ getPreview(doc.docContent) }}</p>
              <div class="flex items-center gap-2 mt-2 text-xs text-gray-300">
                <span>{{ formatDate(doc.updateTime) }}</span>
                <span><i class="fa-regular fa-user mr-0.5"></i>{{ doc.createUser }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="py-12 text-center text-gray-400">
      <i class="fa-solid fa-folder-open text-3xl mb-2 block opacity-50"></i>
      {{ keyword ? $t('project.noMatchingDocs') : $t('project.noDocsYet') }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Search, Loading } from '@element-plus/icons-vue'
import { getProjectDocs, deleteDoc } from '@/api/doc'

const props = defineProps<{ projectId: string }>()

const router = useRouter()
const { t } = useI18n()
const docs = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')

const filteredDocs = computed(() => {
  if (!keyword.value.trim()) return docs.value
  const kw = keyword.value.trim().toLowerCase()
  return docs.value.filter((d: any) => d.docTitle.toLowerCase().includes(kw))
})

onMounted(async () => {
  await loadDocs()
})

async function loadDocs() {
  loading.value = true
  try {
    docs.value = await getProjectDocs(props.projectId)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleDocCommand(cmd: string, doc: any) {
  if (cmd === 'edit') {
    router.push(`/doc/${doc.docId}`)
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(t('project.confirmDeleteDoc2', { name: doc.docTitle }), t('project.confirmDeleteLabel'), {
      type: 'warning',
      confirmButtonText: t('common.confirmDelete'),
      cancelButtonText: t('common.cancel'),
      confirmButtonClass: 'el-button--danger'
    }).then(async () => {
      try {
        await deleteDoc(doc.docId)
        ElMessage.success(t('layout.docDeleted'))
        await loadDocs()
      } catch (e: any) {
        ElMessage.error(e.message || t('common.deleteFailed'))
      }
    }).catch(() => {})
  }
}

function getPreview(content?: string): string {
  if (!content) return ''
  return content.replace(/[#*`>\-\[\]]/g, '').slice(0, 80)
}

function formatDate(date?: string): string {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
