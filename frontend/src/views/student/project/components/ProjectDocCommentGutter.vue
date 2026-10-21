<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  getEditorView,
  getEditorViewDom,
  isEditorDestroyed,
  safeEditorOff,
  safeEditorOn,
} from '@/utils/tiptapSafe'

const props = defineProps({
  editor: {
    type: Object,
    default: null,
  },
  commentCountByPos: {
    type: Object,
    default: () => ({}),
  },
  activeAnchorPos: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['comment-block'])

const gutterRef = ref(null)
const rows = ref([])
const gutterHeight = ref(0)
const activeRowIndex = ref(null)

let proseMirrorEl = null
let resizeObserver = null
let mutationObserver = null
let unmounted = false
/** @type {number | null} */
let rafId = null

const getCommentCount = (pos) => props.commentCountByPos?.[pos] ?? 0

const measureRows = () => {
  const gutterEl = gutterRef.value
  const dom = getEditorViewDom(props.editor)
  if (!dom || !gutterEl) {
    rows.value = []
    gutterHeight.value = 0
    return
  }

  const prose = dom
  const gutterRect = gutterEl.getBoundingClientRect()
  const blockElements = Array.from(prose.children)
  const view = getEditorView(props.editor)

  rows.value = blockElements.map((element, index) => {
    const rect = element.getBoundingClientRect()
    let pos = 0
    try {
      pos = view?.posAtDOM(element, 0) ?? 0
    } catch {
      pos = 0
    }

    return {
      index,
      top: rect.top - gutterRect.top,
      height: Math.max(rect.height, 24),
      pos,
    }
  })

  if (blockElements.length === 0) {
    gutterHeight.value = prose.getBoundingClientRect().height
    return
  }

  const lastRect = blockElements[blockElements.length - 1].getBoundingClientRect()
  gutterHeight.value = Math.max(lastRect.bottom - gutterRect.top, prose.getBoundingClientRect().height)
}

const scheduleMeasure = () => {
  if (unmounted) return
  if (rafId != null) cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(() => {
    rafId = null
    if (!unmounted) measureRows()
  })
}

const bindEditor = () => {
  unbindEditor()
  const dom = getEditorViewDom(props.editor)
  if (!dom) return

  proseMirrorEl = dom

  safeEditorOn(props.editor, 'update', scheduleMeasure)
  safeEditorOn(props.editor, 'selectionUpdate', scheduleMeasure)
  safeEditorOn(props.editor, 'transaction', scheduleMeasure)

  resizeObserver = new ResizeObserver(scheduleMeasure)
  resizeObserver.observe(proseMirrorEl)

  mutationObserver = new MutationObserver(scheduleMeasure)
  mutationObserver.observe(proseMirrorEl, { childList: true, subtree: true, characterData: true })

  scheduleMeasure()
  nextTick(measureRows)
}

const unbindEditor = () => {
  safeEditorOff(props.editor, 'update', scheduleMeasure)
  safeEditorOff(props.editor, 'selectionUpdate', scheduleMeasure)
  safeEditorOff(props.editor, 'transaction', scheduleMeasure)
  resizeObserver?.disconnect()
  mutationObserver?.disconnect()
  resizeObserver = null
  mutationObserver = null
  proseMirrorEl = null
}

const handleCommentClick = (row, event) => {
  event.stopPropagation()
  emit('comment-block', row)
}

watch(
  () => props.editor?.state?.doc?.childCount,
  () => {
    scheduleMeasure()
  },
)

watch(
  () => props.editor,
  (editor) => {
    if (editor && !isEditorDestroyed(editor)) nextTick(bindEditor)
    else {
      unbindEditor()
      rows.value = []
      gutterHeight.value = 0
    }
  },
  { immediate: true },
)

onMounted(() => {
  nextTick(scheduleMeasure)
})

onBeforeUnmount(() => {
  unmounted = true
  if (rafId != null) cancelAnimationFrame(rafId)
  unbindEditor()
})
</script>

<template>
  <div
    ref="gutterRef"
    class="ps-comment-gutter"
    :style="{ height: `${gutterHeight}px` }"
    aria-label="块评论区域"
    @mouseleave="activeRowIndex = null"
  >
    <div
      v-for="row in rows"
      :key="row.index"
      class="ps-comment-gutter__row"
      :class="{
        'has-comments': getCommentCount(row.pos) > 0,
        'is-panel-active': activeAnchorPos != null && activeAnchorPos === row.pos,
      }"
      :style="{ top: `${row.top}px`, height: `${row.height}px` }"
      @mouseenter="activeRowIndex = row.index"
    >
      <button
        type="button"
        class="ps-comment-gutter__btn"
        :class="{ 'is-visible': activeRowIndex === row.index || getCommentCount(row.pos) > 0 }"
        :aria-label="`评论第 ${row.index + 1} 行${getCommentCount(row.pos) ? `，${getCommentCount(row.pos)} 条` : ''}`"
        @click="handleCommentClick(row, $event)"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
        </svg>
        <span v-if="getCommentCount(row.pos)" class="ps-comment-gutter__badge">{{ getCommentCount(row.pos) }}</span>
      </button>
    </div>
  </div>
</template>
