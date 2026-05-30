import { ref, watch, nextTick, type Ref } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const vditorAny = Vditor as any

export type EditorMode = 'sv' | 'ir'
export type ContentWidthMode = 'narrow' | 'normal' | 'wide' | 'full'

export function useVditorEditor(options: {
  vditorRef: Ref<HTMLElement | undefined>
  editorContainerRef: Ref<HTMLElement | undefined>
  content: Ref<string>
  originalContent: Ref<string>
  hasChanges: Ref<boolean>
  editorMode: Ref<EditorMode>
  contentWidthMode: Ref<ContentWidthMode>
  onUpdateStats: (content: string) => void
}) {
  const {
    vditorRef,
    content,
    originalContent,
    hasChanges,
    editorMode,
    onUpdateStats,
  } = options

  let vditor: any = null
  const isReady = ref(false)

  function initVditor() {
    if (!vditorRef.value) return
    if (vditor) vditor.destroy()
    isReady.value = false

    // sv 模式下 Vditor 实际使用 ir 模式，分屏预览由外部 ApiMarkdownRenderer 实现
    const vditorMode = editorMode.value === 'sv' ? 'ir' : editorMode.value

    vditor = new Vditor(vditorRef.value, {
      height: '100%',
      mode: vditorMode,
      placeholder: '开始编写你的 Markdown 文档...',
      value: content.value,
      cache: { enable: false },
      toolbar: [
        'emoji',
        'headings',
        {
          name: 'bold',
          tip: '粗体',
          icon: '<svg class="vditor-icon" viewBox="0 0 1024 1024"><path d="M320 832c-17.696 0-32-14.304-32-32V224c0-17.664 14.304-32 32-32h352c105.6 0 192 86.4 192 192 0 52.8-21.6 100.8-56 135.2-34.4 34.4-82.4 56-136 56H320zm0-64h288c70.4 0 128-57.6 128-128 0-35.2-14.4-67.2-37.6-90.4-23.2-23.2-55.2-37.6-90.4-37.6H320v256zm-160-128c-17.696 0-32-14.304-32-32s14.304-32 32-32h192v64H160z"></path></svg>',
          handler(e: Event) {
            e.preventDefault()
            const textarea = vditor?.getElement()?.querySelector('textarea') as HTMLTextAreaElement
            if (textarea) {
              const start = textarea.selectionStart
              const end = textarea.selectionEnd
              if (start !== end) {
                const selected = textarea.value.substring(start, end)
                vditor?.insertValue('**' + selected + '**', true)
                return
              }
            }
            vditor?.insertValue('**\u200B**', true)
          },
        } as any,
        {
          name: 'italic',
          tip: '斜体',
          icon: '<svg class="vditor-icon" viewBox="0 0 1024 1024"><path d="M384 832h384v64H384zM256 736c0 17.664 14.304 32 32 32h320c52.8 0 96-43.2 96-96 0-26.4-10.4-50.4-27.2-68-16.8-17.6-41.6-28-68.8-28H320v64h112c17.664 0 32 14.336 32 32v32h-32v64h128v64H256zM352 288v64h128c17.664 0 32-14.336 32-32s-14.336-32-32-32H352z"></path></svg>',
          handler(e: Event) {
            e.preventDefault()
            const textarea = vditor?.getElement()?.querySelector('textarea') as HTMLTextAreaElement
            if (textarea) {
              const start = textarea.selectionStart
              const end = textarea.selectionEnd
              if (start !== end) {
                const selected = textarea.value.substring(start, end)
                vditor?.insertValue('*' + selected + '*', true)
                return
              }
            }
            vditor?.insertValue('*\u200B*', true)
          },
        } as any,
        {
          name: 'strike',
          tip: '删除线',
          icon: '<svg class="vditor-icon" viewBox="0 0 1024 1024"><path d="M896 512c0-52.8-21.6-100.8-56-135.2-34.4-34.4-82.4-56-136-56H384c-17.6 0-32 14.4-32 32 0 8 2.4 15.2 7.2 21.6 23.2 30.4 56 57.6 95.2 73.6 39.2 16 82.4 24.8 125.6 24.8 70.4 0 128-57.6 128-128h-128c-17.6 0-32-14.4-32-32s14.4-32 32-32h128c35.2 0 67.2-14.4 90.4-37.6 23.2-23.2 37.6-55.2 37.6-90.4 0-70.4-57.6-128-128-128H320c-105.6 0-192 86.4-192 192 0 52.8 21.6 100.8 56 135.2 34.4 34.4 82.4 56 136 56h320c17.6 0 32-14.4 32-32s-14.4-32-32-32H384c-35.2 0-67.2 14.4-90.4 37.6-23.2 23.2-37.6 55.2-37.6 90.4 0 35.2 14.4 67.2 37.6 90.4 23.2 23.2 55.2 37.6 90.4 37.6h320c70.4 0 128-57.6 128-128z"></path></svg>',
          handler(e: Event) {
            e.preventDefault()
            const textarea = vditor?.getElement()?.querySelector('textarea') as HTMLTextAreaElement
            if (textarea) {
              const start = textarea.selectionStart
              const end = textarea.selectionEnd
              if (start !== end) {
                const selected = textarea.value.substring(start, end)
                vditor?.insertValue('~~' + selected + '~~', true)
                return
              }
            }
            vditor?.insertValue('~~\u200B~~', true)
          },
        } as any,
        '|',
        'list',
        'ordered-list',
        'check',
        '|',
        'quote',
        'line',
        {
          name: 'code',
          tip: '代码块',
          icon: '<svg class="vditor-icon" viewBox="0 0 1024 1024"><path d="M416 672c-16.64 0-32-15.36-32-32s15.36-32 32-32 32 15.36 32 32-15.36 32-32 32zm320-32c0 16.64-15.36 32-32 32s-32-15.36-32-32 15.36-32 32-32 32 15.36 32 32zm-320-64c-16.64 0-32-15.36-32-32s15.36-32 32-32 32 15.36 32 32-15.36 32-32 32zm320-32c0 16.64-15.36 32-32 32s-32-15.36-32-32 15.36-32 32-32 32 15.36 32 32zm-320-64c-16.64 0-32-15.36-32-32s15.36-32 32-32 32 15.36 32 32-15.36 32-32 32zm-118.4-76.8L339.2 259.2c-25.6-25.6-67.2-25.6-92.8 0l-128 128c-25.6 25.6-25.6 67.2 0 92.8l118.4 118.4c25.6 25.6 67.2 25.6 92.8 0l128-128c25.6-25.6 25.6-67.2 0-92.8zM390.4 515.2L272 633.6l-41.6-41.6 118.4-118.4 41.6 41.6z"></path></svg>',
          handler(e: Event) {
            e.preventDefault()
            const textarea = vditor?.getElement()?.querySelector('textarea') as HTMLTextAreaElement
            if (textarea) {
              const start = textarea.selectionStart
              const end = textarea.selectionEnd
              if (start !== end) {
                const selected = textarea.value.substring(start, end)
                vditor?.insertValue('```\n' + selected + '\n```', true)
                return
              }
            }
            vditor?.insertValue('```\n\n```', true)
          },
        } as any,
        {
          name: 'inline-code',
          tip: '行内代码',
          icon: '<svg class="vditor-icon" viewBox="0 0 1024 1024"><path d="M448 832c-17.696 0-32-14.304-32-32s14.304-32 32-32h128c17.696 0 32 14.304 32 32s-14.304 32-32 32H448zm384-128c-17.696 0-32-14.304-32-32s14.304-32 32-32h128c17.696 0 32 14.304 32 32s-14.304 32-32 32H832zM320 320c-17.696 0-32-14.304-32-32s14.304-32 32-32h384c17.696 0 32 14.304 32 32s-14.304 32-32 32H320zM256 544c-17.696 0-32-14.304-32-32s14.304-32 32-32h512c17.696 0 32 14.304 32 32s-14.304 32-32 32H256zM150.272 675.616l-32.64-33.088c-28.256-28.608-28.256-74.752 0-103.36l179.648-182.272c28.256-28.608 74.176-28.608 102.432 0l26.336 26.72c28.256 28.608 28.256 74.752 0 103.36l-179.648 182.272c-28.256 28.608-74.176 28.608-102.432 0l-23.296-23.632z"></path></svg>',
          handler(e: Event) {
            e.preventDefault()
            const textarea = vditor?.getElement()?.querySelector('textarea') as HTMLTextAreaElement
            if (textarea) {
              const start = textarea.selectionStart
              const end = textarea.selectionEnd
              if (start !== end) {
                const selected = textarea.value.substring(start, end)
                vditor?.insertValue('`' + selected + '`', true)
                return
              }
            }
            vditor?.insertValue('`\u200B`', true)
          },
        } as any,
        '|',
        'link',
        'table',
        '|',
        'undo',
        'redo',
        'fullscreen',
        {
          name: 'more',
          toolbar: ['export', 'outline', 'preview'],
        },
      ],
      preview: {
        markdown: { toc: true },
        hljs: { lineNumber: true, style: 'github' },
      },
      upload: {
        url: '/api/upload/image',
        fieldName: 'file[]',
        linkToImgUrl: '/api/upload/image',
        handler() {
          return null
        },
      },
      after: () => {
        isReady.value = true
        if (vditor) {
          onUpdateStats(vditor.getValue())
        }
      },
      input(value) {
        content.value = value
        hasChanges.value = value !== originalContent.value
        onUpdateStats(value)
      },
    })
  }

  function destroyVditor() {
    if (vditor) {
      vditor.destroy()
      vditor = null
      isReady.value = false
    }
  }

  function getVditor(): Vditor | null {
    return vditor
  }

  function setValue(val: string) {
    if (vditor) vditor.setValue(val)
  }

  function getValue(): string {
    return vditor?.getValue() || ''
  }

  return {
    isReady,
    initVditor,
    destroyVditor,
    getVditor,
    setValue,
    getValue,
  }
}
