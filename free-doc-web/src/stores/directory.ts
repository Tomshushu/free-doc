import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProjectDirectories } from '@/api/directory'
import type { Directory } from '@/types'

export const useDirectoryStore = defineStore('directory', () => {
  // 状态
  const directories = ref<Directory[]>([])
  const loading = ref(false)
  const activeProjectId = ref<string | null>(null)

  // 请求去重：防止并发 load 同一项目导致竞态
  let _loadingProjectId: string | null = null
  let _pendingProjectId: string | null = null

  // 计算属性：树形结构数据
  interface DirNode extends Directory {
    isDoc?: boolean
    children?: DirNode[]
  }

  const directoryTreeData = computed<DirNode[]>(() => {
    const map: Record<string, DirNode> = {}
    const roots: DirNode[] = []

    directories.value.forEach(d => {
      map[d.id] = { ...d, children: [] }
    })

    directories.value.forEach(d => {
      const node = map[d.id]
      if (!d.pid || d.pid === '0' || !map[d.pid]) {
        roots.push(node)
      } else {
        map[d.pid].children!.push(node)
      }
    })

    return roots
  })

  // 默认展开的 key（顶层节点）
  const defaultExpandedKeys = computed(() => {
    return directories.value
      .filter(d => d.pid === '0' || !d.pid)
      .map(d => d.id)
  })

  /**
   * 加载指定项目的目录数据
   * @param projectId 项目ID
   * @param force 是否强制重新加载（即使项目ID相同也重新请求）
   */
  async function load(projectId: string, force = false) {
    if (!projectId) return

    // 去重：如果正在加载同一项目，跳过（防止并发竞态）
    if (_loadingProjectId === projectId) {
      // 记录为待加载，当前请求完成后会自动触发
      _pendingProjectId = projectId
      return
    }

    // 如果项目ID相同且不强制，跳过（始终请求最新数据确保一致性）
    loading.value = true
    activeProjectId.value = projectId
    _loadingProjectId = projectId

    try {
      directories.value = await getProjectDirectories(projectId)
    } catch (e) {
      console.error('加载目录失败:', e)
      directories.value = []
    } finally {
      _loadingProjectId = null
      loading.value = false
      // 如果有等待的请求，执行它
      if (_pendingProjectId && _pendingProjectId !== projectId) {
        const pending = _pendingProjectId
        _pendingProjectId = null
        await load(pending, force)
      }
    }
  }

  /** 强制刷新当前项目的目录 */
  async function refresh() {
    if (activeProjectId.value) {
      await load(activeProjectId.value, true)
    }
  }

  /** 清空目录（切换团队/退出时调用） */
  function clear() {
    directories.value = []
    activeProjectId.value = null
    loading.value = false
  }

  return {
    directories,
    loading,
    activeProjectId,
    directoryTreeData,
    defaultExpandedKeys,
    load,
    refresh,
    clear
  }
})
