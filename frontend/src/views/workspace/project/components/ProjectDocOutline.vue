<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import ProjectDocOutlineNode from './ProjectDocOutlineNode.vue'
import AppScrollArea from '@/components/AppScrollArea.vue'
import {
  buildHeadingTree,
  extractHeadingFlatList,
  findActiveHeadingId,
} from '@/utils/docOutline'
import {
  isEditorDestroyed,
  safeEditorOff,
  safeEditorOn,
} from '@/utils/tiptapSafe'

const props = defineProps({
  editor: {
    type: Object,
    default: null,
  },
})

const tree = ref([])
const activeId = ref(null)
const collapsedIds = ref(new Set())
let unmounted = false
/** @type {number | null} */
let rafId = null

const refreshOutline = () => {
  if (isEditorDestroyed(props.editor) || !props.editor?.state) {
    tree.value = []
    activeId.value = null
    return
  }

  const flatList = extractHeadingFlatList(props.editor.state.doc)
  tree.value = buildHeadingTree(flatList)
  activeId.value = findActiveHeadingId(flatList, props.editor.state.selection.from)
}

const scheduleRefresh = () => {
  if (unmounted) return
  if (rafId != null) cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(() => {
    rafId = null
    if (!unmounted) refreshOutline()
  })
}

const bindEditor = () => {
  unbindEditor()
  if (isEditorDestroyed(props.editor)) return

  safeEditorOn(props.editor, 'update', scheduleRefresh)
  safeEditorOn(props.editor, 'selectionUpdate', scheduleRefresh)
  safeEditorOn(props.editor, 'transaction', scheduleRefresh)
  scheduleRefresh()
}

const unbindEditor = () => {
  safeEditorOff(props.editor, 'update', scheduleRefresh)
  safeEditorOff(props.editor, 'selectionUpdate', scheduleRefresh)
  safeEditorOff(props.editor, 'transaction', scheduleRefresh)
}

const handleSelect = (item) => {
  if (isEditorDestroyed(props.editor)) return
  props.editor
    .chain()
    .focus()
    .setTextSelection(item.pos + 1)
    .scrollIntoView()
    .run()
  activeId.value = item.id
}

const handleToggle = (id) => {
  const next = new Set(collapsedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  collapsedIds.value = next
}

watch(
  () => props.editor,
  (editor) => {
    collapsedIds.value = new Set()
    if (editor && !isEditorDestroyed(editor)) nextTick(bindEditor)
    else {
      unbindEditor()
      tree.value = []
      activeId.value = null
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unmounted = true
  if (rafId != null) cancelAnimationFrame(rafId)
  unbindEditor()
})
</script>

<template>
  <aside class="ps-doc-outline" aria-label="文档目录">
    <header class="ps-doc-outline__header">
      <span class="ps-doc-outline__title">目录</span>
    </header>

    <div v-if="!tree.length" class="ps-doc-outline__empty" aria-hidden="true" />

    <AppScrollArea v-else tag="nav" class="ps-doc-outline__nav" axis="y" flex hover-reveal>
      <ul class="ps-outline-tree">
        <ProjectDocOutlineNode
          v-for="item in tree"
          :key="item.id"
          :item="item"
          :active-id="activeId"
          :collapsed-ids="collapsedIds"
          @select="handleSelect"
          @toggle="handleToggle"
        />
      </ul>
    </AppScrollArea>
  </aside>
</template>
