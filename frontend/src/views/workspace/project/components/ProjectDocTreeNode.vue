<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { WORKSPACE_NODE_TYPE } from '@/constants/workspace'

const props = defineProps({
  node: { type: Object, required: true },
  activeDocId: { type: [String, Number], default: null },
  expandedFolders: { type: Object, required: true },
  renamingNodeId: { type: [String, Number], default: null },
  depth: { type: Number, default: 0 },
})

const emit = defineEmits([
  'select',
  'toggle-folder',
  'node-contextmenu',
  'commit-rename',
  'cancel-rename',
])

const isFolder = (node) => node.nodeType === WORKSPACE_NODE_TYPE.FOLDER
const isDocument = (node) => node.nodeType === WORKSPACE_NODE_TYPE.DOCUMENT
const isSheet = (node) => node.nodeType === WORKSPACE_NODE_TYPE.SHEET
const isLeaf = (node) => isDocument(node) || isSheet(node)

const isRenaming = computed(() =>
  props.renamingNodeId != null && String(props.renamingNodeId) === String(props.node.id),
)

const isLeafActive = computed(() =>
  String(props.activeDocId) === String(props.node.id),
)

const renameInputRef = ref(null)
const renameValue = ref('')

watch(isRenaming, async (renaming) => {
  if (!renaming) return
  renameValue.value = props.node.title || ''
  await nextTick()
  const input = renameInputRef.value
  if (!input) return
  input.focus()
  input.select()
})

const handleRowContextMenu = (event, node) => {
  emit('node-contextmenu', { event, node })
}

const handleRenameBlur = () => {
  emit('commit-rename', props.node.id, renameValue.value)
}

const handleRenameCancel = () => {
  emit('cancel-rename', props.node.id)
}

const handleRenameKeydown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    renameInputRef.value?.blur()
  } else if (event.key === 'Escape') {
    event.preventDefault()
    handleRenameCancel()
  }
}
</script>

<template>
  <div class="ps-doc-node" :style="{ paddingLeft: depth ? '0' : undefined }">
    <div
      v-if="isFolder(node) && isRenaming"
      class="ps-doc-node-row ps-doc-node-row--renaming"
      @contextmenu="handleRowContextMenu($event, node)"
    >
      <svg
        class="ps-doc-chevron"
        :class="{ collapsed: !expandedFolders.has(node.id) }"
        width="14"
        height="14"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
      <svg class="ps-doc-folder-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
      </svg>
      <input
        ref="renameInputRef"
        v-model="renameValue"
        type="text"
        class="ps-doc-node-rename-input"
        @blur="handleRenameBlur"
        @keydown="handleRenameKeydown"
        @click.stop
      >
    </div>

    <button
      v-else-if="isFolder(node)"
      type="button"
      class="ps-doc-node-row"
      @click="emit('toggle-folder', node.id)"
      @contextmenu="handleRowContextMenu($event, node)"
    >
      <svg
        class="ps-doc-chevron"
        :class="{ collapsed: !expandedFolders.has(node.id) }"
        width="14"
        height="14"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
      <svg class="ps-doc-folder-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
      </svg>
      <span class="ps-doc-node-title">{{ node.title }}</span>
    </button>

    <div
      v-else-if="isLeaf(node) && isRenaming"
      class="ps-doc-node-row ps-doc-node-row--doc ps-doc-node-row--renaming"
      :class="{ active: isLeafActive }"
      @contextmenu="handleRowContextMenu($event, node)"
    >
      <span class="ps-doc-chevron-placeholder" aria-hidden="true" />
      <svg
        v-if="isSheet(node)"
        width="15"
        height="15"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <rect x="3" y="3" width="18" height="18" rx="2" />
        <line x1="3" y1="9" x2="21" y2="9" />
        <line x1="3" y1="15" x2="21" y2="15" />
        <line x1="9" y1="3" x2="9" y2="21" />
        <line x1="15" y1="3" x2="15" y2="21" />
      </svg>
      <svg
        v-else
        width="15"
        height="15"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
      </svg>
      <input
        ref="renameInputRef"
        v-model="renameValue"
        type="text"
        class="ps-doc-node-rename-input"
        @blur="handleRenameBlur"
        @keydown="handleRenameKeydown"
        @click.stop
      >
    </div>

    <button
      v-else-if="isLeaf(node)"
      type="button"
      class="ps-doc-node-row ps-doc-node-row--doc"
      :class="{ active: isLeafActive }"
      @click="emit('select', node.id)"
      @contextmenu="handleRowContextMenu($event, node)"
    >
      <span class="ps-doc-chevron-placeholder" aria-hidden="true" />
      <svg
        v-if="isSheet(node)"
        width="15"
        height="15"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <rect x="3" y="3" width="18" height="18" rx="2" />
        <line x1="3" y1="9" x2="21" y2="9" />
        <line x1="3" y1="15" x2="21" y2="15" />
        <line x1="9" y1="3" x2="9" y2="21" />
        <line x1="15" y1="3" x2="15" y2="21" />
      </svg>
      <svg
        v-else
        width="15"
        height="15"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
      </svg>
      <span class="ps-doc-node-title">{{ node.title }}</span>
    </button>

    <div
      v-if="isFolder(node) && expandedFolders.has(node.id) && node.children?.length"
      class="ps-doc-node-children"
    >
      <ProjectDocTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :active-doc-id="activeDocId"
        :expanded-folders="expandedFolders"
        :renaming-node-id="renamingNodeId"
        :depth="depth + 1"
        @select="emit('select', $event)"
        @toggle-folder="emit('toggle-folder', $event)"
        @node-contextmenu="emit('node-contextmenu', $event)"
        @commit-rename="(nodeId, title) => emit('commit-rename', nodeId, title)"
        @cancel-rename="(nodeId) => emit('cancel-rename', nodeId)"
      />
    </div>
  </div>
</template>
