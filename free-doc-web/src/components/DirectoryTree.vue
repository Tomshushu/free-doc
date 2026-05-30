<template>
  <div class="directory-tree" @contextmenu.prevent>
    <div v-if="title" class="tree-header">
      <span class="text-xs text-gray-400 font-medium">{{ title }}</span>
      <div class="flex gap-1">
        <el-tooltip :content="$t('components.newDirectory')" placement="top">
          <el-button text size="small" @click="$emit('create-directory')">
            <i class="fa-solid fa-folder-plus text-blue-500 hover:text-blue-600"></i>
          </el-button>
        </el-tooltip>
        <el-tooltip :content="$t('components.newDoc')" placement="top">
          <el-button text size="small" @click="$emit('create-doc')">
            <i class="fa-solid fa-file-circle-plus text-blue-500 hover:text-blue-600"></i>
          </el-button>
        </el-tooltip>
        <el-tooltip :content="$t('common.refresh')" placement="top">
          <el-button text size="small" @click="$emit('refresh')">
            <i class="fa-solid fa-refresh text-blue-500 hover:text-blue-600"></i>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-8">
      <el-icon class="is-loading text-primary"><Loading /></el-icon>
    </div>

    <div v-else-if="data.length === 0" class="py-8 text-center text-gray-400">
      <i class="fa-solid fa-folder-open text-2xl mb-2 block"></i>
      <span class="text-xs">{{ $t('components.noDirectory') }}</span>
    </div>

    <el-tree
      v-else
      ref="treeRef"
      :data="treeData"
      :props="defaultProps"
      node-key="id"
      :default-expanded-keys="expandedKeys"
      :current-node-key="currentKey"
      :highlight-current="true"
      :expand-on-click-node="true"
      @node-click="handleNodeClick"
      @node-contextmenu="onContextMenu"
    >
      <template #default="{ node, data }">
        <div class="tree-node flex items-center w-full">
          <!-- 目录图标 -->
           <template v-if="!data.isDoc">
             <i
               :class="[
                 node.expanded ? 'fa-solid fa-folder-open' : 'fa-solid fa-folder',
                 activePathKeys.includes(data.id) ? 'text-primary' : 'text-amber-500',
                 'mr-2 text-sm transition-colors duration-200'
               ]"
             ></i>
           </template>
           <!-- 文档图标 -->
           <template v-else>
             <i :class="[data.docIcon || 'fa-regular fa-file-lines', 'text-blue-500 mr-2 text-sm opacity-80']"></i>
           </template>

           <!-- 名称 -->
           <span
             v-show="editingDirId !== data.id || data.isDoc"
             :class="[
               'flex-1 truncate text-sm cursor-pointer transition-colors duration-200',
               data.isDoc ? 'text-gray-600 font-normal' : 'font-bold text-gray-800',
               currentKey === data.id ? '!text-primary font-bold bg-blue-50/50 rounded px-1 -ml-1' : '',
               !data.isDoc && activePathKeys.includes(data.id) ? '!text-primary' : ''
             ]"
            >{{ node.label }}</span>

          <!-- 目录内联重命名 -->
          <el-input
            v-if="editingDirId === data.id && !data.isDoc"
            v-model="editingName"
            size="small"
            class="!w-32 mr-1"
            @blur="confirmRename(data)"
            @keyup.enter="confirmRename(data)"
            @keyup.escape="cancelRename"
          />

          <!-- 文档数量角标 -->
          <el-badge
            v-if="!data.isDoc && data.docCount > 0"
            :value="data.docCount"
            type="info"
            class="mr-1"
          />
        </div>
      </template>
    </el-tree>

    <!-- 右键菜单 -->
    <teleport to="body">
      <div
        v-show="ctxMenu.visible"
        class="ctx-menu"
        :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }"
        @click.stop
      >
        <!-- 文档菜单 -->
        <template v-if="ctxMenu.node?.isDoc">
          <div v-if="canEdit" class="ctx-item" @click="execCmd('edit-doc')">
            <i class="fa-solid fa-pen-to-square text-blue-500"></i>
            <span>{{ $t('components.editDoc') }}</span>
          </div>
          <div class="ctx-item" @click="execCmd('share-doc')">
            <i class="fa-solid fa-share-nodes text-indigo-500"></i>
            <span>{{ $t('components.shareDoc') }}</span>
          </div>
          <div class="ctx-item" @click="execCmd('export-doc')">
            <i class="fa-solid fa-file-export text-green-500"></i>
            <span>{{ $t('components.exportDoc') }}</span>
          </div>
          <div v-if="canEdit" class="ctx-divider"></div>
          <div v-if="canEdit" class="ctx-item ctx-danger" @click="execCmd('delete-doc')">
            <i class="fa-solid fa-trash-can text-red-500"></i>
            <span>{{ $t('components.deleteDoc') }}</span>
          </div>
        </template>
        <!-- 目录菜单 -->
        <template v-else-if="ctxMenu.node">
          <div v-if="canEdit" class="ctx-item" @click="execCmd('rename')">
            <i class="fa-solid fa-pen text-gray-500"></i>
            <span>{{ $t('components.rename') }}</span>
          </div>
          <div v-if="canEdit" class="ctx-item" @click="execCmd('create-doc')">
            <i class="fa-solid fa-file-circle-plus text-blue-500"></i>
            <span>{{ $t('components.newDoc') }}</span>
          </div>
          <div v-if="canEdit" class="ctx-item" @click="execCmd('add-sub-directory')">
            <i class="fa-solid fa-folder-plus text-amber-500"></i>
            <span>{{ $t('components.addSubDirectory') }}</span>
          </div>
          <div v-if="canEdit" class="ctx-divider"></div>
          <div class="ctx-item" @click="execCmd('export-current')">
            <i class="fa-solid fa-download text-indigo-500"></i>
            <span>{{ $t('components.exportCurrentDir') }}</span>
          </div>
          <div v-if="canEdit" class="ctx-divider"></div>
          <div v-if="canEdit" class="ctx-item ctx-danger" @click="execCmd('delete')">
            <i class="fa-solid fa-trash-can text-red-500"></i>
            <span>{{ $t('components.deleteDirectory') }}</span>
          </div>
        </template>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { Loading } from '@element-plus/icons-vue'
import { updateDirectoryName } from '@/api/directory'

const { t } = useI18n()

interface TreeNode {
  id: string
  name: string
  pid: string
  isDoc?: boolean
  docIcon?: string
  docCount?: number
  children?: TreeNode[]
}

const props = defineProps<{
  data: TreeNode[]
  loading?: boolean
  currentKey?: string
  expandedKeys?: string[]
  title?: string
  disableContextMenu?: boolean
  canEdit?: boolean // 新增：控制编辑/删除/创建等操作
}>()

const emit = defineEmits<{
  (e: 'node-click', node: TreeNode): void
  (e: 'create-directory'): void
  (e: 'create-doc'): void
  (e: 'add-sub-directory', dir: TreeNode): void
  (e: 'create-doc-in-dir', dirId: string): void
  (e: 'delete-directory', dirId: string): void
  (e: 'edit-doc', doc: any): void
  (e: 'share-doc', doc: TreeNode): void
  (e: 'export-current-dir', dir: TreeNode): void
  (e: 'export-doc', doc: TreeNode): void
  (e: 'refresh'): void
}>()

const treeRef = ref()
const defaultProps: Record<string, unknown> = {
  label: 'name',
  children: 'children',
  isLeaf: (data: TreeNode) => !!data.isDoc
}

// 目录重命名状态
const editingDirId = ref('')
const editingName = ref('')

// 右键菜单状态
const ctxMenu = ref<{
  visible: boolean
  x: number
  y: number
  node: TreeNode | null
}>({
  visible: false,
  x: 0,
  y: 0,
  node: null
})

const treeData = computed(() => buildTree(props.data))

// 计算当前选中节点的所有父级节点 ID
const activePathKeys = computed(() => {
  if (!props.currentKey || !props.data) return []
  const keys: string[] = []
  const idToPidMap = new Map<string, string>()

  props.data.forEach(item => {
    idToPidMap.set(item.id, item.pid)
  })

  let currentId = props.currentKey
  let pid = idToPidMap.get(currentId)

  while (pid && pid !== '0') {
    keys.push(pid)
    pid = idToPidMap.get(pid)
  }
  return keys
})

function buildTree(nodes: TreeNode[]): TreeNode[] {
  const map: Record<string, TreeNode> = {}
  const roots: TreeNode[] = []

  nodes.forEach((n) => {
    map[n.id] = { ...n, children: n.children || [] }
  })

  nodes.forEach((n) => {
    if (!n.pid || n.pid === '0' || !map[n.pid]) {
      roots.push(map[n.id])
    } else {
      map[n.pid].children!.push(map[n.id])
    }
  })

  return roots
}

function handleNodeClick(data: TreeNode) {
  emit('node-click', data)
}

function onContextMenu(event: Event, data: TreeNode) {
  if (props.disableContextMenu) {
    return
  }

  const e = event as MouseEvent
  e.preventDefault()
  e.stopPropagation()
  ctxMenu.value = {
    visible: true,
    x: e.clientX,
    y: e.clientY,
    node: data
  }
}

function closeCtxMenu() {
  ctxMenu.value.visible = false
}

async function execCmd(cmd: string) {
  const node = ctxMenu.value.node
  if (!node) return
  closeCtxMenu()

  switch (cmd) {
    case 'edit-doc':
      emit('edit-doc', node)
      break
    case 'share-doc':
      emit('share-doc', node)
      break
    case 'export-doc':
      emit('export-doc', node)
      break
    case 'delete-doc':
      try {
        await ElMessageBox.confirm(t('components.confirmDeleteDoc'), t('components.deleteConfirm'), { type: 'warning' })
        emit('edit-doc', { ...node, _delete: true })
      } catch { /* 取消 */ }
      break
    case 'rename':
      editingDirId.value = node.id
      editingName.value = node.name
      break
    case 'create-doc':
      emit('create-doc-in-dir', node.id)
      break
    case 'add-sub-directory':
      emit('add-sub-directory', node)
      break
    case 'export-current':
      emit('export-current-dir', node)
      break
    case 'delete':
      try {
        await ElMessageBox.confirm(t('components.confirmDeleteDir'), t('components.deleteConfirm'), { type: 'warning' })
        emit('delete-directory', node.id)
      } catch { /* 取消 */ }
      break
  }
}

// 点击其他地方关闭菜单
function onDocClick(e: MouseEvent) {
  if (ctxMenu.value.visible) {
    const target = e.target as HTMLElement
    if (!target.closest('.ctx-menu')) {
      closeCtxMenu()
    }
  }
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
  document.addEventListener('contextmenu', onDocClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('contextmenu', onDocClick)
})

// ====== 目录重命名 ======
async function confirmRename(data: TreeNode) {
  if (!editingName.value.trim()) {
    cancelRename()
    return
  }
  if (editingName.value.trim() === data.name) {
    cancelRename()
    return
  }
  try {
    await updateDirectoryName(data.id, editingName.value.trim())
    ElMessage.success(t('components.renameSuccess'))
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e.message || t('components.renameFailed'))
  }
  cancelRename()
}

function cancelRename() {
  editingDirId.value = ''
  editingName.value = ''
}

defineExpose({ treeRef })

watch(
  () => props.data,
  (newData) => {
    if (treeRef.value && newData) {
      treeRef.value.setData(newData)
    }
  },
  { deep: true }
)
</script>

<style scoped>
.directory-tree {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.tree-node {
  padding: 4px 0;
}

.tree-node:hover .text-gray-600 {
  color: #1e293b;
}

::v-deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 6px;
  margin: 1px 8px;
  transition: all 0.2s;
}

::v-deep(.el-tree-node__content:hover) {
  background-color: #f1f5f9 !important;
}

::v-deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #eff6ff !important;
}

/* 文档节点微调 */
.tree-node:has(.fa-regular) {
  opacity: 0.95;
}

/* 右键菜单 */
.ctx-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08);
  padding: 6px 0;
  min-width: 180px;
  animation: ctxFadeIn 0.12s ease;
}

@keyframes ctxFadeIn {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}

.ctx-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  transition: background 0.12s;
}

.ctx-item:hover {
  background: #f5f7fa;
}

.ctx-item i {
  width: 16px;
  text-align: center;
  font-size: 14px;
}

.ctx-danger {
  color: #f56c6c;
}

.ctx-danger:hover {
  background: #fef0f0;
}

.ctx-divider {
  height: 1px;
  background: #ebeef5;
  margin: 4px 8px;
}
</style>
