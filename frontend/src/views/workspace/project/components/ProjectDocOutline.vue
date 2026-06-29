<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import ProjectDocOutlineNode from './ProjectDocOutlineNode.vue'
import {
  buildHeadingTree,
  extractHeadingFlatList,
  findActiveHeadingId,
} from '@/utils/docOutline'

const props = defineProps({
  editor: {
    type: Object,
    default: null,
  },
})

const tree = ref([])
const activeId = ref(null)
const collapsedIds = ref(new Set())

const refreshOutline = () => {
  if (!props.editor?.state) {
    tree.value = []
    activeId.value = null
    return
  }

  const flatList = extractHeadingFlatList(props.editor.state.doc)
  tree.value = buildHeadingTree(flatList)
  activeId.value = findActiveHeadingId(flatList, props.editor.state.selection.from)
}

const scheduleRefresh = () => {
  requestAnimationFrame(refreshOutline)
}

const bindEditor = () => {
  unbindEditor()
  if (!props.editor) return

  props.editor.on('update', scheduleRefresh)
  props.editor.on('selectionUpdate', scheduleRefresh)
  props.editor.on('transaction', scheduleRefresh)
  scheduleRefresh()
}

const unbindEditor = () => {
  if (!props.editor) return
  props.editor.off('update', scheduleRefresh)
  props.editor.off('selectionUpdate', scheduleRefresh)
  props.editor.off('transaction', scheduleRefresh)
}

const handleSelect = (item) => {
  if (!props.editor?.view) return
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
    if (editor) nextTick(bindEditor)
    else {
      unbindEditor()
      tree.value = []
      activeId.value = null
    }
  },
  { immediate: true },
)

onBeforeUnmount(unbindEditor)
</script>

<template>
  <aside class="ps-doc-outline" aria-label="文档目录">
    <header class="ps-doc-outline__header">
      <span class="ps-doc-outline__title">目录</span>
    </header>

    <div v-if="!tree.length" class="ps-doc-outline__empty" aria-hidden="true" />

    <nav v-else class="ps-doc-outline__nav">
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
    </nav>
  </aside>
</template>
