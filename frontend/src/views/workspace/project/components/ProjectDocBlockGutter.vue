<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import ProjectDocFormatPicker from './ProjectDocFormatPicker.vue'
import { openAppTableSize } from '@/composables/appPrompt'
import { DOC_BLOCK_FORMATS, applyBlockFormatToRow, deleteTableAtRow, getBlockFormatsForRow, insertTableAtRow } from '@/constants/docBlockFormats'

const props = defineProps({
  editor: {
    type: Object,
    default: null,
  },
  canWrite: {
    type: Boolean,
    default: true,
  },
})

const gutterRef = ref(null)
const rows = ref([])
const gutterHeight = ref(0)
const activeRowIndex = ref(null)
const openRowIndex = ref(null)
const pickerAnchor = ref(null)
const selectedRow = shallowRef(null)

let proseMirrorEl = null
let resizeObserver = null
let mutationObserver = null

const measureRows = () => {
  const gutterEl = gutterRef.value
  if (!props.editor?.view?.dom || !gutterEl) {
    rows.value = []
    gutterHeight.value = 0
    return
  }

  const prose = props.editor.view.dom
  const gutterRect = gutterEl.getBoundingClientRect()
  const blockElements = Array.from(prose.children)

  rows.value = blockElements.map((element, index) => {
    const rect = element.getBoundingClientRect()
    let pos = 0
    try {
      pos = props.editor.view.posAtDOM(element, 0)
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
  requestAnimationFrame(measureRows)
}

const bindEditor = () => {
  unbindEditor()
  if (!props.editor?.view?.dom) return

  proseMirrorEl = props.editor.view.dom

  props.editor.on('update', scheduleMeasure)
  props.editor.on('selectionUpdate', scheduleMeasure)
  props.editor.on('transaction', scheduleMeasure)

  resizeObserver = new ResizeObserver(scheduleMeasure)
  resizeObserver.observe(proseMirrorEl)

  mutationObserver = new MutationObserver(scheduleMeasure)
  mutationObserver.observe(proseMirrorEl, { childList: true, subtree: true, characterData: true })

  scheduleMeasure()
  nextTick(measureRows)
}

const unbindEditor = () => {
  if (props.editor) {
    props.editor.off('update', scheduleMeasure)
    props.editor.off('selectionUpdate', scheduleMeasure)
    props.editor.off('transaction', scheduleMeasure)
  }
  resizeObserver?.disconnect()
  mutationObserver?.disconnect()
  resizeObserver = null
  mutationObserver = null
  proseMirrorEl = null
}

const pickerFormats = ref(DOC_BLOCK_FORMATS)

const openPicker = (row, event) => {
  if (!props.canWrite) return
  selectedRow.value = row
  openRowIndex.value = row.index
  pickerFormats.value = getBlockFormatsForRow(props.editor, row)
  const rect = event.currentTarget.getBoundingClientRect()
  pickerAnchor.value = {
    top: Math.max(12, rect.top - 4),
    left: rect.right + 10,
  }
}

const closePicker = () => {
  pickerAnchor.value = null
  openRowIndex.value = null
  selectedRow.value = null
}

const handleFormatSelect = async (formatId) => {
  if (!selectedRow.value || !props.editor) return

  if (formatId === 'table') {
    const size = await openAppTableSize()
    if (!size) return
    insertTableAtRow(props.editor, selectedRow.value, size)
  } else if (formatId === 'deleteTable') {
    deleteTableAtRow(props.editor, selectedRow.value)
  } else {
    applyBlockFormatToRow(props.editor, selectedRow.value, formatId)
  }

  nextTick(scheduleMeasure)
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
    if (editor) nextTick(bindEditor)
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
  unbindEditor()
})
</script>

<template>
  <div
    ref="gutterRef"
    class="ps-block-gutter"
    :style="{ height: `${gutterHeight}px` }"
    aria-label="块插入区域"
    @mouseleave="activeRowIndex = null"
  >
    <div
      v-for="row in rows"
      :key="row.index"
      class="ps-block-gutter__row"
      :class="{ 'is-active': openRowIndex === row.index }"
      :style="{ top: `${row.top}px`, height: `${row.height}px` }"
      @mouseenter="activeRowIndex = row.index"
    >
      <button
        v-show="canWrite && (activeRowIndex === row.index || openRowIndex === row.index)"
        type="button"
        class="ps-block-gutter__btn"
        :aria-label="`设置第 ${row.index + 1} 行格式`"
        @click="openPicker(row, $event)"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" aria-hidden="true">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
      </button>
    </div>

    <ProjectDocFormatPicker
      v-if="pickerAnchor"
      :anchor="pickerAnchor"
      :formats="pickerFormats"
      @select="handleFormatSelect"
      @close="closePicker"
    />
  </div>
</template>
