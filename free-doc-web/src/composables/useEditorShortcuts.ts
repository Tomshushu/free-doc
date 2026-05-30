import { onMounted, onBeforeUnmount, type Ref } from 'vue'

export interface ShortcutAction {
  id: string
  label: string
  shortcut?: string
  category?: string
  handler: () => void
}

export function useEditorShortcuts(options: {
  saveFn: () => Promise<void>
  toggleFocusMode: () => void
  toggleCommandPalette: () => void
  toggleRightPanel: () => void
  toggleLeftSidebar: () => void
  goBack: () => void
  editorMode: Ref<string>
}) {
  const {
    saveFn,
    toggleFocusMode,
    toggleCommandPalette,
    toggleRightPanel,
    toggleLeftSidebar,
    goBack,
  } = options

  const shortcuts: ShortcutAction[] = [
    { id: 'save', label: '保存文档', shortcut: 'Ctrl+S', category: '文件', handler: () => saveFn() },
    { id: 'focus', label: '专注模式', shortcut: 'F11', category: '视图', handler: toggleFocusMode },
    { id: 'palette', label: '命令面板', shortcut: 'Ctrl+/', category: '工具', handler: toggleCommandPalette },
    { id: 'right-panel', label: '右侧面板', shortcut: 'Ctrl+J', category: '视图', handler: toggleRightPanel },
    { id: 'left-sidebar', label: '左侧目录', shortcut: 'Ctrl+B', category: '视图', handler: toggleLeftSidebar },
    { id: 'back', label: '返回', shortcut: 'Alt+←', category: '导航', handler: goBack },
  ]

  function handleKeydown(e: KeyboardEvent) {
    const ctrl = e.ctrlKey || e.metaKey

    // Ctrl+S 保存
    if (ctrl && e.key === 's') {
      e.preventDefault()
      saveFn()
      return
    }

    // F11 专注模式
    if (e.key === 'F11') {
      e.preventDefault()
      toggleFocusMode()
      return
    }

    // Ctrl+/ 命令面板
    if (ctrl && e.key === '/') {
      e.preventDefault()
      toggleCommandPalette()
      return
    }

    // Ctrl+J 右侧面板
    if (ctrl && e.key === 'j') {
      e.preventDefault()
      toggleRightPanel()
      return
    }

    // Ctrl+B 左侧目录
    if (ctrl && e.key === 'b') {
      e.preventDefault()
      toggleLeftSidebar()
      return
    }

    // Alt+← 返回
    if (e.altKey && e.key === 'ArrowLeft') {
      e.preventDefault()
      goBack()
      return
    }

    // Esc 退出专注模式
    if (e.key === 'Escape') {
      // 由专注模式组件自行处理
    }
  }

  onMounted(() => {
    document.addEventListener('keydown', handleKeydown, true)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('keydown', handleKeydown, true)
  })

  return {
    shortcuts,
  }
}
