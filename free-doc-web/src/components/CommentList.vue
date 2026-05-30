<template>
  <div class="comment-list">
    <!-- 评论输入 -->
    <div v-if="showInput" class="comment-input mb-4">
      <div class="flex gap-2">
        <el-avatar :size="32" class="bg-primary shrink-0">
          {{ currentUser?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="flex-1">
          <el-input
            ref="inputRef"
            v-model="content"
            type="textarea"
            :rows="3"
            :placeholder="placeholder"
            resize="none"
          />
          <div class="flex justify-between items-center mt-2">
            <el-button v-if="replyTo" text size="small" @click="cancelReply">
              {{ $t('components.cancelReply') }}
            </el-button>
            <div class="flex gap-2">
              <el-button size="small" @click="showInput = false">{{ $t('common.cancel') }}</el-button>
              <el-button type="primary" size="small" @click="submitComment">{{ $t('components.publish') }}</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div v-if="comments.length === 0 && !showInput" class="empty-state py-6 text-center text-gray-400">
      <i class="fa-solid fa-comment-dots text-2xl mb-2 block"></i>
      <span class="text-sm">{{ $t('components.noComments') }}</span>
    </div>

    <TransitionGroup name="list" tag="div" class="space-y-4">
      <div
        v-for="comment in commentTree"
        :key="comment.commentId"
        class="comment-item bg-white rounded-lg border border-gray-100 p-4 hover:shadow-sm transition-shadow"
      >
        <!-- 主评论 -->
        <div class="flex gap-3">
          <el-avatar :size="36" class="bg-primary shrink-0">
            {{ getAvatar(comment.createBy) }}
          </el-avatar>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <span class="font-medium text-gray-800 text-sm">{{ comment.createBy || $t('components.anonymousUser') }}</span>
              <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
              <el-tag v-if="comment._isOwner" size="small" type="warning">{{ $t('components.author') }}</el-tag>
            </div>
            <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap break-words">
              {{ comment.content }}
            </p>
            <div class="flex gap-4 mt-2">
              <button
                class="text-xs text-gray-400 hover:text-primary flex items-center gap-1 transition-colors"
                @click="startReply(comment)"
              >
                <i class="fa-solid fa-reply"></i>{{ $t('components.reply') }}
              </button>
              <el-popconfirm
                v-if="canDelete(comment)"
                :title="$t('components.confirmDeleteComment')"
                :confirm-button-text="$t('common.confirm')"
                :cancel-button-text="$t('common.cancel')"
                @confirm="handleDelete(comment)"
              >
                <template #reference>
                  <button class="text-xs text-gray-400 hover:text-red-500 flex items-center gap-1 transition-colors">
                    <i class="fa-solid fa-trash-can"></i>{{ $t('common.delete') }}
                  </button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>

        <!-- 嵌套回复 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies ml-12 mt-3 space-y-3 pl-3 border-l-2 border-gray-100">
          <div
            v-for="reply in comment.replies"
            :key="reply.commentId"
            class="reply-item flex gap-2"
          >
            <el-avatar :size="28" class="bg-blue-100 text-blue-600 shrink-0 text-xs">
              {{ getAvatar(reply.createBy) }}
            </el-avatar>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-0.5">
                <span class="font-medium text-gray-700 text-xs">{{ reply.createBy || $t('components.anonymousUser') }}</span>
                <span class="text-xs text-gray-300">{{ formatTime(reply.createTime) }}</span>
                <span v-if="reply.replyToName" class="text-xs text-gray-400">
                  {{ $t('components.reply') }} <span class="text-primary">{{ reply.replyToName }}</span>
                </span>
              </div>
              <p class="text-sm text-gray-600 leading-relaxed break-words">{{ reply.content }}</p>
              <div class="flex gap-3 mt-1.5">
                <button
                  class="text-xs text-gray-400 hover:text-primary flex items-center gap-1"
                  @click="startReply(comment, reply)"
                >
                  <i class="fa-solid fa-reply"></i>{{ $t('components.reply') }}
                </button>
                <el-popconfirm
                  v-if="canDelete(reply)"
                  :title="$t('components.confirmDeleteReply')"
                  @confirm="handleDelete(reply)"
                >
                  <template #reference>
                    <button class="text-xs text-gray-400 hover:text-red-500 flex items-center gap-1">
                      <i class="fa-solid fa-trash-can"></i>{{ $t('common.delete') }}
                    </button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </div>

        <!-- 行内回复输入 -->
        <div
          v-if="replyingId === comment.commentId"
          class="inline-reply ml-12 mt-3 pl-3 border-l-2 border-primary/30"
        >
          <div class="flex gap-2">
            <el-avatar :size="28" class="bg-primary shrink-0 text-xs">
              {{ currentUser?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="flex-1">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="$t('components.replyPlaceholder')"
                resize="none"
                autofocus
              />
              <div class="flex justify-end gap-2 mt-1.5">
                <el-button size="small" text @click="cancelReply">{{ $t('common.cancel') }}</el-button>
                <el-button type="primary" size="small" @click="submitReply(comment)">{{ $t('components.reply') }}</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </TransitionGroup>
  </div>

  <!-- 底部触发按钮（当 showInput=false 时显示） -->
  <div
    v-if="!showInput"
    class="comment-trigger mt-4 p-3 rounded-lg border border-dashed border-gray-200 cursor-pointer hover:border-primary hover:bg-blue-50/50 transition-all"
    @click="showInput = true"
  >
    <div class="flex items-center gap-2 text-gray-400">
      <i class="fa-solid fa-pen-to-square"></i>
      <span class="text-sm">{{ $t('components.writeComment') }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Comment {
  commentId: string
  docId: string
  parentCommentId?: string | null
  content: string
  createBy: string
  createTime: string
  updateTime?: string
  replies?: Comment[]
  _isOwner?: boolean
  replyToName?: string
}

const props = defineProps<{
  comments: Comment[]
  currentUserId?: string
  currentUser?: string
  placeholder?: string
  replyTo?: string
}>()

const emit = defineEmits<{
  (e: 'add', payload: { docId: string; content: string; parentCommentId?: string }): void
  (e: 'delete', commentId: string): void
}>()

const showInput = ref(false)
const content = ref('')
const inputRef = ref()
const replyingId = ref<string | null>(null)
const replyContent = ref('')
const replyTargetUser = ref('')

interface CommentNode extends Comment {
  replies?: Comment[]
}

// 构建嵌套评论树
const commentTree = computed<CommentNode[]>(() => {
  const map: Record<string, CommentNode> = {}
  const roots: CommentNode[] = []

  props.comments.forEach((c) => {
    map[c.commentId] = { ...c, replies: [] }
  })

  props.comments.forEach((c) => {
    if (!c.parentCommentId || c.parentCommentId === '0') {
      roots.push(map[c.commentId])
    } else {
      const parent = map[c.parentCommentId]
      if (parent) {
        parent.replies!.push(map[c.commentId])
      }
    }
  })

  return roots
})

function startReply(parent: Comment, targetReply?: Comment) {
  replyingId.value = parent.commentId
  replyContent.value = ''
  replyTargetUser.value = targetReply ? (targetReply.createBy || t('components.anonymousUser')) : ''
  nextTick(() => {})
}

function cancelReply() {
  replyingId.value = null
  replyContent.value = ''
  replyTargetUser.value = ''
}

async function submitComment() {
  const trimmed = content.value.trim()
  if (!trimmed) return

  try {
    await emit('add', {
      docId: props.comments[0]?.docId || '',
      content: trimmed
    })
    content.value = ''
    showInput.value = false
    ElMessage.success(t('components.commentSuccess'))
  } catch (e) {
    // handled by parent
  }
}

async function submitReply(parent: Comment) {
  const trimmed = replyContent.value.trim()
  if (!trimmed) return

  try {
    await emit('add', {
      docId: props.comments[0]?.docId || '',
      content: trimmed,
      parentCommentId: parent.commentId
    })
    cancelReply()
    ElMessage.success(t('components.replySuccess'))
  } catch (e) {
    // handled by parent
  }
}

async function handleDelete(comment: Comment) {
  emit('delete', comment.commentId)
}

function canDelete(comment: Comment): boolean {
  return !!props.currentUserId && comment.createBy === props.currentUserId
}

function getAvatar(name: string): string {
  return name?.charAt(0)?.toUpperCase() || 'U'
}

function formatTime(date: string): string {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMs / 3600000)

  if (diffMin < 1) return t('common.justNow')
  if (diffMin < 60) return t('common.minutesAgo', { n: diffMin })
  if (diffHour < 24) return t('common.hoursAgo', { n: diffHour })

  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.25s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.comment-input :deep(.el-textarea__inner) {
  border-radius: 8px;
}
</style>
