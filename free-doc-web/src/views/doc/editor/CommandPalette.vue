<template>
  <Transition name="palette">
    <div v-if="visible" class="command-palette-overlay" @click.self="$emit('update:visible', false)">
      <div class="command-palette-container">
        <!-- 搜索框 -->
        <div class="palette-search">
          <i class="fa-solid fa-magnifying-glass text-gray-400"></i>
          <input
            ref="searchInputRef"
            v-model="query"
            type="text"
            :placeholder="$t('doc.searchCommand')"
            class="palette-input"
            @keydown.down.prevent="selectNext"
            @keydown.up.prevent="selectPrev"
            @keydown.enter.prevent="executeSelected"
            @keydown.esc="$emit('update:visible', false)"
          />
          <kbd class="palette-kbd">ESC</kbd>
        </div>

        <!-- 命令列表 -->
        <div class="palette-list">
          <template v-for="(group, category) in groupedCommands" :key="category">
            <div class="palette-category">{{ category }}</div>
            <button
              v-for="cmd in group"
              :key="cmd.id"
              :class="['palette-item', { active: selectedIndex === getFlatIndex(cmd) }]"
              @click="executeCommand(cmd)"
              @mouseenter="selectedIndex = getFlatIndex(cmd)"
            >
              <div class="flex items-center gap-3">
                <i :class="['w-5 text-center', cmd.icon || 'fa-solid fa-circle']"></i>
                <span>{{ cmd.label }}</span>
              </div>
              <kbd v-if="cmd.shortcut" class="palette-item-kbd">{{ cmd.shortcut }}</kbd>
            </button>
          </template>

          <div v-if="filteredCommands.length === 0" class="palette-empty">
            <i class="fa-solid fa-face-meh text-2xl text-gray-300 mb-2 block"></i>
            <span>{{ $t('doc.noMatchingCommand') }}</span>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Command {
  id: string
  label: string
  icon?: string
  shortcut?: string
  category?: string
  handler: () => void
}

const props = defineProps<{
  visible: boolean
  commands: Command[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const query = ref('')
const selectedIndex = ref(0)
const searchInputRef = ref<HTMLInputElement>()

const filteredCommands = computed(() => {
  if (!query.value) return props.commands
  const q = query.value.toLowerCase()
  return props.commands.filter(
    (cmd) => cmd.label.toLowerCase().includes(q) || cmd.category?.toLowerCase().includes(q),
  )
})

const groupedCommands = computed(() => {
  const groups: Record<string, Command[]> = {}
  for (const cmd of filteredCommands.value) {
    const cat = cmd.category || t('doc.other')
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(cmd)
  }
  return groups
})

function getFlatIndex(cmd: Command): number {
  return filteredCommands.value.indexOf(cmd)
}

function selectNext() {
  selectedIndex.value = Math.min(selectedIndex.value + 1, filteredCommands.value.length - 1)
}

function selectPrev() {
  selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
}

function executeSelected() {
  const cmd = filteredCommands.value[selectedIndex.value]
  if (cmd) executeCommand(cmd)
}

function executeCommand(cmd: Command) {
  cmd.handler()
  emit('update:visible', false)
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      query.value = ''
      selectedIndex.value = 0
      nextTick(() => searchInputRef.value?.focus())
    }
  },
)
</script>

<style scoped>
.command-palette-overlay {
  @apply fixed inset-0 z-50 flex items-start justify-center pt-[15vh];
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
}

.command-palette-container {
  @apply w-full max-w-lg bg-white rounded-2xl shadow-2xl border border-gray-200/60 overflow-hidden;
  animation: paletteIn 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes paletteIn {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.palette-search {
  @apply flex items-center gap-3 px-4 py-3.5 border-b border-gray-100;
}

.palette-input {
  @apply flex-1 bg-transparent outline-none text-sm text-gray-800 placeholder-gray-400;
}

.palette-kbd {
  @apply text-[10px] text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded font-mono;
}

.palette-list {
  @apply max-h-80 overflow-y-auto py-2;
}

.palette-category {
  @apply px-4 py-1.5 text-xs font-semibold text-gray-400 uppercase tracking-wider;
}

.palette-item {
  @apply w-full flex items-center justify-between px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer;
}
.palette-item.active {
  @apply bg-blue-50 text-primary;
}

.palette-item-kbd {
  @apply text-[10px] text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded font-mono;
}

.palette-empty {
  @apply py-8 text-center text-gray-400 text-sm;
}

/* 过渡 */
.palette-enter-active {
  transition: all 0.2s ease;
}
.palette-leave-active {
  transition: all 0.15s ease;
}
.palette-enter-from,
.palette-leave-to {
  opacity: 0;
}
</style>
