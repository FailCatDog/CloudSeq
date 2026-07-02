<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ProjectDocContextMenu from './ProjectDocContextMenu.vue'
import ProjectDocTreeNode from './ProjectDocTreeNode.vue'
import { WORKSPACE_NODE_TYPE } from '@/constants/workspace'

const props = defineProps({
  rootTitle: {
    type: String,
    default: '项目文档',
  },
  nodes: {
    type: Array,
    default: () => [],
  },
  activeDocId: {
    type: [String, Number],
    default: null,
  },
  expandedFolders: {
    type: Object,
    required: true,
  },
  renamingNodeId: {
    type: [String, Number],
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

const emit = defineEmits([
  'create-document',
  'create-sheet',
  'create-folder',
  'select',
  'toggle-folder',
  'rename-node',
  'commit-rename',
  'cancel-rename',
  'delete-node',
])

const ICON_FOLDER = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>`
const ICON_DOC = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>`
const ICON_SHEET = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg>`
const ICON_RENAME = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z"/></svg>`
const ICON_DELETE = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>`
const ICON_OPEN = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>`

const rootExpanded = ref(true)
const showPopover = ref(false)
const addBtnRef = ref(null)
const popoverRef = ref(null)

const menuOpen = ref(false)
const menuX = ref(0)
const menuY = ref(0)
const menuTarget = ref(null)

const closeMenu = () => {
  menuOpen.value = false
  menuTarget.value = null
}

const isFolderNode = (node) => node?.nodeType === WORKSPACE_NODE_TYPE.FOLDER
const isDocumentNode = (node) => node?.nodeType === WORKSPACE_NODE_TYPE.DOCUMENT
const isSheetNode = (node) => node?.nodeType === WORKSPACE_NODE_TYPE.SHEET

const menuItems = computed(() => {
  const target = menuTarget.value
  if (!target) return []

  const createItems = (parentId) => [
    { key: 'new-folder', label: '新建文件夹', icon: ICON_FOLDER, parentId },
    { key: 'new-document', label: '新建文档', icon: ICON_DOC, parentId },
    { key: 'new-sheet', label: '新建表格', icon: ICON_SHEET, parentId },
  ]

  if (target.type === 'root') {
    return createItems(null)
  }

  if (target.type === 'blank') {
    return createItems(null)
  }

  const node = target.node
  if (!node) return []

  if (isFolderNode(node)) {
    return [
      ...createItems(node.id),
      { key: 'divider-1', divider: true },
      { key: 'rename', label: '重命名', icon: ICON_RENAME, nodeId: node.id },
      { key: 'delete', label: '删除', icon: ICON_DELETE, nodeId: node.id, danger: true },
    ]
  }

  if (isDocumentNode(node) || isSheetNode(node)) {
    return [
      { key: 'open', label: '打开', icon: ICON_OPEN, nodeId: node.id },
      { key: 'divider-1', divider: true },
      { key: 'rename', label: '重命名', icon: ICON_RENAME, nodeId: node.id },
      { key: 'delete', label: '删除', icon: ICON_DELETE, nodeId: node.id, danger: true },
    ]
  }

  return []
})

const openMenu = (event, target) => {
  event.preventDefault()
  event.stopPropagation()
  closePopover()
  menuTarget.value = target
  menuX.value = event.clientX
  menuY.value = event.clientY
  menuOpen.value = true
}

const handleRootContextMenu = (event) => {
  openMenu(event, { type: 'root' })
}

const handleBlankContextMenu = (event) => {
  if (props.loading || props.errorMessage) return
  openMenu(event, { type: 'blank' })
}

const handleNodeContextMenu = (payload) => {
  openMenu(payload.event, { type: 'node', node: payload.node })
}

const handleMenuSelect = (key) => {
  const target = menuTarget.value
  if (!target) return

  const item = menuItems.value.find((entry) => entry.key === key)
  if (!item) return

  if (key === 'new-folder') {
    emit('create-folder', item.parentId ?? null)
    return
  }
  if (key === 'new-document') {
    emit('create-document', item.parentId ?? null)
    return
  }
  if (key === 'new-sheet') {
    emit('create-sheet', item.parentId ?? null)
    return
  }
  if (key === 'open') {
    emit('select', item.nodeId)
    return
  }
  if (key === 'rename') {
    emit('rename-node', item.nodeId)
    return
  }
  if (key === 'delete') {
    emit('delete-node', item.nodeId)
  }
}

const toggleRoot = () => {
  toggleFolderInternal()
}

const toggleFolderInternal = () => {
  rootExpanded.value = !rootExpanded.value
}

const togglePopover = () => {
  showPopover.value = !showPopover.value
}

const closePopover = () => {
  showPopover.value = false
}

const handleCreateSheet = () => {
  closePopover()
  emit('create-sheet', null)
}

const handleCreateDocument = () => {
  closePopover()
  emit('create-document', null)
}

const handleCreateFolder = () => {
  closePopover()
  emit('create-folder', null)
}

const handleClickOutside = (event) => {
  if (!showPopover.value) return
  const target = event.target
  if (addBtnRef.value?.contains(target)) return
  if (popoverRef.value?.contains(target)) return
  closePopover()
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <section class="ps-doc-tree" aria-label="项目文档">
    <div class="ps-doc-root">
      <button
        type="button"
        class="ps-doc-root-row"
        :aria-expanded="rootExpanded"
        @click="toggleRoot"
        @contextmenu="handleRootContextMenu"
      >
        <svg
          class="ps-doc-chevron"
          :class="{ collapsed: !rootExpanded }"
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
        <svg
          class="ps-doc-folder-icon"
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
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
        </svg>
        <span class="ps-doc-root-title">{{ rootTitle }}</span>
      </button>

      <div class="ps-doc-root-actions">
        <button
          ref="addBtnRef"
          type="button"
          class="ps-doc-add-btn"
          aria-label="新建"
          :aria-expanded="showPopover"
          @click.stop="togglePopover"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>

        <div
          v-if="showPopover"
          ref="popoverRef"
          class="ps-doc-popover"
          role="menu"
          aria-label="新建"
        >
          <button
            type="button"
            class="ps-doc-popover-item"
            role="menuitem"
            @click="handleCreateFolder"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
            </svg>
            <span>文件夹</span>
          </button>
          <button
            type="button"
            class="ps-doc-popover-item"
            role="menuitem"
            @click="handleCreateSheet"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <line x1="3" y1="9" x2="21" y2="9" />
              <line x1="3" y1="15" x2="21" y2="15" />
              <line x1="9" y1="3" x2="9" y2="21" />
              <line x1="15" y1="3" x2="15" y2="21" />
            </svg>
            <span>表格</span>
          </button>
          <button
            type="button"
            class="ps-doc-popover-item"
            role="menuitem"
            @click="handleCreateDocument"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
            </svg>
            <span>文档</span>
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="rootExpanded"
      class="ps-doc-children"
      @contextmenu="handleBlankContextMenu"
    >
      <p v-if="loading" class="ps-doc-empty-hint">加载中…</p>
      <p v-else-if="errorMessage" class="ps-doc-empty-hint ps-doc-error-hint">{{ errorMessage }}</p>
      <template v-else>
        <ProjectDocTreeNode
          v-for="node in nodes"
          :key="node.id"
          :node="node"
          :active-doc-id="activeDocId"
          :expanded-folders="expandedFolders"
          :renaming-node-id="renamingNodeId"
          @select="emit('select', $event)"
          @toggle-folder="emit('toggle-folder', $event)"
          @node-contextmenu="handleNodeContextMenu"
          @commit-rename="(nodeId, title) => emit('commit-rename', nodeId, title)"
          @cancel-rename="(nodeId) => emit('cancel-rename', nodeId)"
        />
        <p v-if="!nodes.length" class="ps-doc-empty-hint">暂无内容，点击 + 新建文件夹、文档或表格</p>
      </template>
    </div>

    <ProjectDocContextMenu
      :open="menuOpen"
      :x="menuX"
      :y="menuY"
      :items="menuItems"
      @select="handleMenuSelect"
      @close="closeMenu"
    />
  </section>
</template>
