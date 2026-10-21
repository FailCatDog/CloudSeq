<template>
  <div class="ps-workspace">
    <aside class="ps-sidebar" aria-label="项目空间导航">
      <nav class="ps-nav-list">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="ps-nav-item"
          active-class=""
          exact-active-class=""
          :class="{ active: item.to === activeNavPath }"
          :aria-label="item.label"
        >
          <span class="ps-nav-icon" v-html="item.icon" />
          <span class="ps-nav-label">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="ps-sidebar-divider" role="separator" />

      <ProjectDocTree
        :nodes="nodes"
        :root-title="rootTitle"
        :active-doc-id="activeDocId"
        :expanded-folders="expandedFolders"
        :renaming-node-id="renamingNodeId"
        :loading="loading"
        :error-message="errorMessage"
        @create-document="handleCreateDocument"
        @create-folder="handleCreateFolder"
        @select="selectDoc"
        @toggle-folder="toggleFolder"
        @rename-node="handleRenameNode"
        @commit-rename="handleCommitRename"
        @cancel-rename="cancelNodeRename"
        @delete-node="handleDeleteNode"
      />
    </aside>

    <div class="ps-main">
      <ProjectDocEditor
        v-if="showDocEditor"
        :key="`doc-${activeDoc.id}`"
        :doc-id="activeDoc.id"
        :title="activeDoc.title"
        :initial-content="activeDoc.content"
        :can-write="activeDoc.canWrite"
        :collab-session="activeDoc.collab"
        @snapshot="updateDocSnapshot"
        @update:title="updateDocTitle(activeDoc.id, $event)"
      />
      <RouterView v-else />
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import ProjectDocTree from './components/ProjectDocTree.vue'
import ProjectDocEditor from './components/ProjectDocEditor.vue'
import { useProjectDocs } from '@/composables/useProjectDocs'
import { WORKSPACE_NODE_TYPE } from '@/constants/workspace'

const route = useRoute()

const {
  nodes,
  rootTitle,
  activeDocId,
  activeDoc,
  expandedFolders,
  renamingNodeId,
  loading,
  errorMessage,
  loadTree,
  selectDoc,
  toggleFolder,
  createDocument,
  createFolder,
  renameNode,
  commitNodeRename,
  cancelNodeRename,
  deleteNode,
  updateDocSnapshot,
  updateDocTitle,
  flushPendingSaves,
} = useProjectDocs()

const navItems = [
  {
    to: '/workspace/project/board',
    label: '数据看板',
    icon: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"></rect><rect x="14" y="3" width="7" height="5"></rect><rect x="14" y="12" width="7" height="9"></rect><rect x="3" y="16" width="7" height="5"></rect></svg>`,
  },
  {
    to: '/workspace/project/gantt',
    label: '任务甘特图',
    icon: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>`,
  },
  {
    to: '/workspace/project/weekly',
    label: '周报',
    icon: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line></svg>`,
  },
]

const showDocEditor = computed(() => {
  if (route.name !== 'project-doc') return false

  const doc = activeDoc.value
  if (!doc || doc.nodeType !== WORKSPACE_NODE_TYPE.DOCUMENT) return false
  if (doc.loading) return true

  return Boolean(doc.collab)
})

const activeNavPath = computed(() => {
  if (route.name === 'project-doc') return null
  const match = navItems.find(
    (item) => route.path === item.to || route.path.startsWith(`${item.to}/`),
  )
  return match?.to ?? null
})

const handleCreateDocument = async (parentId = null) => {
  try {
    await createDocument(parentId)
  } catch (error) {
    window.alert(error?.message || '创建文档失败')
  }
}

const handleCreateFolder = async (parentId = null) => {
  try {
    await createFolder(parentId)
  } catch (error) {
    window.alert(error?.message || '创建文件夹失败')
  }
}

const handleRenameNode = (nodeId) => {
  renameNode(nodeId)
}

const handleCommitRename = async (nodeId, title) => {
  try {
    await commitNodeRename(nodeId, title)
  } catch (error) {
    window.alert(error?.message || '重命名失败')
  }
}

const handleDeleteNode = async (nodeId) => {
  try {
    await deleteNode(nodeId)
  } catch (error) {
    window.alert(error?.message || '删除失败')
  }
}

onMounted(() => {
  loadTree()
})

onBeforeUnmount(() => {
  flushPendingSaves()
})
</script>
