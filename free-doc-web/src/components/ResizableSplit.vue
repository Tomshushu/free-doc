<template>
  <div class="flex h-full">
    <div :style="{ width: leftWidth + 'px' }" class="h-full overflow-hidden shrink-0">
      <slot name="left" />
    </div>

    <div class="w-4 shrink-0"></div>

    <div
      class="w-[3px] hover:w-[6px] bg-gray-300/60 hover:bg-blue-400 cursor-col-resize transition-all duration-200 z-20 shrink-0 rounded-full"
      :class="{ 'bg-blue-400 w-[6px]': isDragging }"
      @mousedown="onMouseDown"
    />

    <div class="flex-1 h-full overflow-hidden min-w-0">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'

const props = defineProps<{
  initialWidth?: number
  minWidth?: number
  maxWidth?: number
}>()

const leftWidth = ref(props.initialWidth ?? 280)
const isDragging = ref(false)
let startX = 0
let startWidth = 0

function onMouseDown(e: MouseEvent) {
  e.preventDefault()
  isDragging.value = true
  startX = e.clientX
  startWidth = leftWidth.value
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function onMouseMove(e: MouseEvent) {
  if (!isDragging.value) return
  const delta = e.clientX - startX
  let newWidth = startWidth + delta

  const min = props.minWidth ?? 200
  const max = props.maxWidth ?? 500
  newWidth = Math.max(min, Math.min(max, newWidth))

  leftWidth.value = newWidth
}

function onMouseUp() {
  if (isDragging.value) {
    isDragging.value = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
}

onBeforeUnmount(() => {
  if (isDragging.value) {
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
})
</script>
