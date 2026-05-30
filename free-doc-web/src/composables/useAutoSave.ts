import { ref, watch, onBeforeUnmount, type Ref } from 'vue'

export function useAutoSave(options: {
  hasChanges: Ref<boolean>
  isTitleDirty: Ref<boolean>
  saveFn: () => Promise<void>
  enabled?: Ref<boolean>
  intervalSeconds?: Ref<number>
}) {
  const {
    hasChanges,
    isTitleDirty,
    saveFn,
    enabled = ref(true),
    intervalSeconds = ref(30),
  } = options

  let autoSaveTimer: ReturnType<typeof setInterval> | null = null

  function startAutoSave() {
    stopAutoSave()
    if (!enabled.value) return
    autoSaveTimer = setInterval(() => {
      if (hasChanges.value || isTitleDirty.value) {
        saveFn()
      }
    }, intervalSeconds.value * 1000)
  }

  function stopAutoSave() {
    if (autoSaveTimer) {
      clearInterval(autoSaveTimer)
      autoSaveTimer = null
    }
  }

  watch(enabled, (val) => {
    if (val) startAutoSave()
    else stopAutoSave()
  })

  onBeforeUnmount(() => {
    stopAutoSave()
  })

  return {
    startAutoSave,
    stopAutoSave,
  }
}
