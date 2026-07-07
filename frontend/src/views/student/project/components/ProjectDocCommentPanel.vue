<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import AppScrollArea from '@/components/AppScrollArea.vue'
import { DOCUMENT_COMMENT_CONTENT_MAX_LENGTH } from '@/constants/fieldLimits'
import { getCurrentUserId } from '@/utils/currentUser.js'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  comments: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
  submitting: {
    type: Boolean,
    default: false,
  },
  activeCommentId: {
    type: [String, Number],
    default: null,
  },
  pendingQuoteText: {
    type: String,
    default: '',
  },
  hasAnchor: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'select', 'delete', 'publish'])

const draft = ref('')
const composerRef = ref(null)

const currentUserId = computed(() => {
  const id = getCurrentUserId()
  return id == null ? null : String(id)
})

const canPublish = computed(() => draft.value.trim().length > 0 && !props.submitting)

const formatCommentTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const mins = String(date.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hours}:${mins}`
}

const canDelete = (comment) => {
  if (currentUserId.value == null || comment?.authorId == null) return false
  return String(comment.authorId) === currentUserId.value
}

const focusComposer = () => {
  nextTick(() => {
    composerRef.value?.focus()
  })
}

const handleSelect = (comment) => {
  emit('select', comment)
}

const handleDelete = (comment, event) => {
  event.stopPropagation()
  emit('delete', comment)
}

const handlePublish = () => {
  const content = draft.value.trim()
  if (!content || props.submitting) return
  emit('publish', content)
}

const handleComposerKeydown = (event) => {
  if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
    event.preventDefault()
    handlePublish()
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) focusComposer()
    else draft.value = ''
  },
)

watch(
  () => props.pendingQuoteText,
  () => {
    if (props.open) focusComposer()
  },
)

defineExpose({ focusComposer, clearDraft: () => { draft.value = '' } })
</script>

<template>
  <aside
    class="ps-doc-comments"
    :class="{ 'is-open': open }"
    aria-label="文档评论"
  >
    <header class="ps-doc-comments__header">
      <div class="ps-doc-comments__header-main">
        <span class="ps-doc-comments__title">本段评论</span>
        <span v-if="comments.length" class="ps-doc-comments__count">{{ comments.length }}</span>
      </div>
      <button
        type="button"
        class="ps-doc-comments__close"
        aria-label="关闭评论区"
        @click="emit('close')"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
    </header>

    <div class="ps-doc-comments__body">
      <p v-if="!hasAnchor" class="ps-doc-comments__state">请从段落右侧点击评论按钮</p>
      <p v-else-if="loading" class="ps-doc-comments__state">加载评论…</p>
      <p v-else-if="errorMessage && !comments.length" class="ps-doc-comments__state ps-doc-comments__state--error">{{ errorMessage }}</p>
      <p v-else-if="!comments.length" class="ps-doc-comments__state">本段暂无评论</p>

      <AppScrollArea v-else tag="div" class="ps-doc-comments__list" axis="y" flex hover-reveal>
        <article
          v-for="comment in comments"
          :key="comment.id"
          class="ps-doc-comment-item"
          :class="{ 'is-active': activeCommentId != null && String(activeCommentId) === String(comment.id) }"
          role="button"
          tabindex="0"
          @click="handleSelect(comment)"
          @keydown.enter.prevent="handleSelect(comment)"
        >
          <div class="ps-doc-comment-item__head">
            <span class="ps-doc-comment-item__author">{{ comment.authorName || '用户' }}</span>
            <time class="ps-doc-comment-item__time">{{ formatCommentTime(comment.createDate) }}</time>
          </div>

          <p class="ps-doc-comment-item__content">{{ comment.content }}</p>

          <button
            v-if="canDelete(comment)"
            type="button"
            class="ps-doc-comment-item__delete"
            :disabled="submitting"
            @click="handleDelete(comment, $event)"
          >
            删除
          </button>
        </article>
      </AppScrollArea>
    </div>

    <footer v-if="hasAnchor" class="ps-doc-comments__composer">
      <p v-if="pendingQuoteText" class="ps-doc-comments__composer-quote">{{ pendingQuoteText }}</p>
      <div class="ps-doc-comments__composer-box">
        <textarea
          ref="composerRef"
          v-model="draft"
          class="ps-doc-comments__composer-input"
          rows="3"
          placeholder="输入评论…"
          :maxlength="DOCUMENT_COMMENT_CONTENT_MAX_LENGTH"
          :disabled="submitting"
          @keydown="handleComposerKeydown"
        />
        <button
          type="button"
          class="ps-doc-comments__composer-send"
          :disabled="!canPublish"
          aria-label="发布评论"
          @click="handlePublish"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" fill="currentColor" stroke="none" />
          </svg>
        </button>
      </div>
      <p v-if="errorMessage && comments.length" class="ps-doc-comments__composer-error">{{ errorMessage }}</p>
    </footer>
  </aside>
</template>
