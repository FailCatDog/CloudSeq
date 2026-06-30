import { computed, inject, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { openAppConfirm, openAppPrompt } from '@/composables/appPrompt'
import { getDocumentApi } from '@/api/document'
import { getCollabSession } from '@/utils/collabTokenCache'
import {
  createWorkspaceNodeApi,
  deleteWorkspaceNodeApi,
  getCurrentWorkspaceApi,
  getWorkspaceTreeApi,
  renameWorkspaceNodeApi,
} from '@/api/workspace'
import { WORKSPACE_NODE_TYPE } from '@/constants/workspace'

const TITLE_SAVE_DELAY_MS = 500

const findNode = (nodes, id) => {
  const targetId = String(id)
  for (const node of nodes) {
    if (String(node.id) === targetId) return node
    if (node.children?.length) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

const collectTitles = (nodes, acc = []) => {
  for (const node of nodes) {
    acc.push(node.title)
    if (node.children?.length) collectTitles(node.children, acc)
  }
  return acc
}

const uniqueTitle = (nodes, base) => {
  const titles = new Set(collectTitles(nodes))
  if (!titles.has(base)) return base
  let i = 2
  while (titles.has(`${base} ${i}`)) i += 1
  return `${base} ${i}`
}

const isDocumentNode = (node) => node?.nodeType === WORKSPACE_NODE_TYPE.DOCUMENT

export const PROJECT_DOCS_KEY = Symbol('projectDocs')

export function useProjectDocsContext() {
  const ctx = inject(PROJECT_DOCS_KEY)
  if (!ctx) {
    throw new Error('useProjectDocsContext must be used within ProjectLayout')
  }
  return ctx
}

export function useProjectDocs() {
  const router = useRouter()
  const route = useRoute()
  const workspaceId = ref(null)
  const rootTitle = ref('项目文档')
  const nodes = ref([])
  const activeDocId = ref(null)
  const activeDocDetail = ref(null)
  const expandedFolders = ref(new Set())
  const renamingNodeId = ref(null)
  const renameOriginalTitle = ref('')
  const pendingOpenDocId = ref(null)
  const loading = ref(false)
  const errorMessage = ref('')

  let titleSaveTimer = null
  let pendingTitleNodeId = null
  let pendingTitle = ''
  let latestContentSnapshot = ''

  const activeDoc = computed(() => {
    if (activeDocId.value == null) return null
    const node = findNode(nodes.value, activeDocId.value)
    if (!node || !isDocumentNode(node)) return null

    const detail = activeDocDetail.value
    if (!detail || String(detail.nodeId) !== String(activeDocId.value)) {
      return {
        ...node,
        content: '',
        version: null,
        canWrite: false,
        collab: null,
        loading: true,
        updateDate: null,
      }
    }

    return {
      ...node,
      content: detail.contentMd,
      version: detail.version,
      canWrite: detail.canWrite,
      collab: detail.collab,
      loading: detail.loading,
      updateDate: detail.updateDate ?? null,
    }
  })

  const loadTree = async () => {
    loading.value = true
    errorMessage.value = ''
    try {
      const workspace = await getCurrentWorkspaceApi()
      workspaceId.value = workspace.id

      const tree = await getWorkspaceTreeApi(workspace.id)
      rootTitle.value = '项目文档'
      nodes.value = Array.isArray(tree) ? tree : []
    } catch (error) {
      errorMessage.value = error?.message || '加载文档树失败'
    } finally {
      loading.value = false
    }
  }

  const saveDocTitle = async (nodeId, title) => {
    if (!title?.trim()) return
    await renameWorkspaceNodeApi(nodeId, { title: title.trim() })
  }

  const scheduleTitleSave = (nodeId) => {
    if (titleSaveTimer) clearTimeout(titleSaveTimer)
    titleSaveTimer = setTimeout(async () => {
      titleSaveTimer = null
      const id = pendingTitleNodeId
      const title = pendingTitle
      pendingTitleNodeId = null
      pendingTitle = ''
      if (id == null || !title) return
      try {
        await saveDocTitle(id, title)
      } catch (error) {
        window.alert(error?.message || '标题保存失败')
      }
    }, TITLE_SAVE_DELAY_MS)
  }

  const flushPendingSaves = async () => {
    if (!titleSaveTimer) return

    clearTimeout(titleSaveTimer)
    titleSaveTimer = null
    if (pendingTitleNodeId == null || !pendingTitle) return

    const id = pendingTitleNodeId
    const title = pendingTitle
    pendingTitleNodeId = null
    pendingTitle = ''
    await saveDocTitle(id, title)
  }

  const loadDocument = async (nodeId) => {
    activeDocDetail.value = {
      nodeId,
      contentMd: '',
      version: 0,
      canWrite: false,
      loading: true,
    }
    try {
      const [doc, collab] = await Promise.all([
        getDocumentApi(nodeId),
        getCollabSession(nodeId),
      ])

      activeDocDetail.value = {
        nodeId: doc.nodeId,
        contentMd: doc.contentMd ?? '',
        version: doc.version,
        canWrite: Boolean(doc.canWrite),
        loading: false,
        collab,
        updateDate: doc.updateDate ?? null,
      }
      latestContentSnapshot = doc.contentMd ?? ''

      const node = findNode(nodes.value, nodeId)
      if (node && doc.title) node.title = doc.title
    } catch (error) {
      activeDocDetail.value = null
      activeDocId.value = null
      throw error
    }
  }

  const loadDocByRoute = async (id) => {
    const node = findNode(nodes.value, id)
    if (!isDocumentNode(node)) {
      await router.replace('/workspace/project/board')
      return
    }
    if (String(activeDocId.value) === String(id) && activeDocDetail.value && !activeDocDetail.value.loading) return

    activeDocId.value = id
    errorMessage.value = ''

    await flushPendingSaves()

    try {
      await loadDocument(id)
    } catch (error) {
      errorMessage.value = error?.message || '加载文档失败'
    }
  }

  const selectDoc = async (id) => {
    const node = findNode(nodes.value, id)
    if (!isDocumentNode(node)) return

    const nodeId = String(id)
    if (route.name === 'project-doc' && String(route.params.nodeId) === nodeId) {
      await loadDocByRoute(id)
      return
    }
    await router.push({ name: 'project-doc', params: { nodeId } })
  }

  watch(
    [() => route.name, () => route.params.nodeId, nodes],
    async ([name, nodeId]) => {
      if (name !== 'project-doc' || !nodeId) return
      if (!findNode(nodes.value, nodeId)) return
      await loadDocByRoute(nodeId)
    },
    { immediate: true },
  )

  watch(
    () => route.name,
    (name, prevName) => {
      if (prevName === 'project-doc' && name !== 'project-doc') {
        void flushPendingSaves()
      }
    },
  )

  const clearActiveDoc = () => {
    activeDocId.value = null
    activeDocDetail.value = null
  }

  const toggleFolder = (id) => {
    const next = new Set(expandedFolders.value)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    expandedFolders.value = next
  }

  const createFolder = async (parentId = null) => {
    if (!workspaceId.value) throw new Error('工作区未加载')

    const title = await openAppPrompt({
      title: '新建文件夹',
      label: '文件夹名称',
      defaultValue: '新建文件夹',
    })
    if (title === null || !title.trim()) return null

    const targetParentId = parentId ?? null
    const created = await createWorkspaceNodeApi({
      workspaceId: workspaceId.value,
      parentId: targetParentId,
      nodeType: WORKSPACE_NODE_TYPE.FOLDER,
      title: title.trim(),
    })

    await loadTree()

    if (targetParentId != null) {
      expandedFolders.value = new Set([...expandedFolders.value, targetParentId])
    }
    if (created?.id != null) {
      expandedFolders.value = new Set([...expandedFolders.value, created.id])
    }

    return created
  }

  const createDocument = async (parentId = null) => {
    if (!workspaceId.value) throw new Error('工作区未加载')

    const defaultTitle = uniqueTitle(nodes.value, '未命名文档')
    const targetParentId = parentId ?? null
    const created = await createWorkspaceNodeApi({
      workspaceId: workspaceId.value,
      parentId: targetParentId,
      nodeType: WORKSPACE_NODE_TYPE.DOCUMENT,
      title: defaultTitle,
    })

    await loadTree()

    if (targetParentId != null) {
      expandedFolders.value = new Set([...expandedFolders.value, targetParentId])
    }
    if (created?.id != null) {
      pendingOpenDocId.value = created.id
      startRenameNode(created.id)
    }

    return created
  }

  const startRenameNode = (nodeId) => {
    const node = findNode(nodes.value, nodeId)
    if (!node) return
    renameOriginalTitle.value = node.title || ''
    renamingNodeId.value = nodeId
  }

  const commitNodeRename = async (nodeId, title) => {
    if (renamingNodeId.value == null || String(renamingNodeId.value) !== String(nodeId)) return

    const node = findNode(nodes.value, nodeId)
    const originalTitle = renameOriginalTitle.value
    const shouldOpenDoc = pendingOpenDocId.value != null
      && String(pendingOpenDocId.value) === String(nodeId)

    renamingNodeId.value = null
    renameOriginalTitle.value = ''
    pendingOpenDocId.value = null

    if (!node) return

    const trimmed = title?.trim()
    const fallbackTitle = isDocumentNode(node) ? '未命名文档' : '新建文件夹'
    const finalTitle = trimmed || originalTitle || fallbackTitle

    if (finalTitle === originalTitle) {
      if (shouldOpenDoc) await selectDoc(nodeId)
      return
    }

    node.title = finalTitle
    try {
      await renameWorkspaceNodeApi(nodeId, { title: finalTitle })
      if (shouldOpenDoc) {
        await selectDoc(nodeId)
      } else if (activeDocId.value === nodeId && isDocumentNode(node)) {
        await loadDocument(nodeId)
      }
    } catch (error) {
      node.title = originalTitle
      window.alert(error?.message || '重命名失败')
    }
  }

  const cancelNodeRename = (nodeId) => {
    if (renamingNodeId.value == null || String(renamingNodeId.value) !== String(nodeId)) return

    const node = findNode(nodes.value, nodeId)
    if (node) node.title = renameOriginalTitle.value

    renamingNodeId.value = null
    renameOriginalTitle.value = ''
    pendingOpenDocId.value = null
  }

  const updateDocSnapshot = (content) => {
    latestContentSnapshot = content ?? ''
    const detail = activeDocDetail.value
    if (detail) {
      detail.contentMd = latestContentSnapshot
      detail.updateDate = new Date().toISOString()
    }
  }

  const updateDocTitle = (id, title) => {
    const node = findNode(nodes.value, id)
    if (!node || !title.trim()) return
    node.title = title.trim()

    const detail = activeDocDetail.value
    if (!detail || String(detail.nodeId) !== String(id) || !detail.canWrite || detail.loading) return

    pendingTitleNodeId = id
    pendingTitle = title.trim()
    scheduleTitleSave(id)
  }

  const renameNode = (nodeId) => {
    startRenameNode(nodeId)
  }

  const deleteNode = async (nodeId) => {
    if (nodeId == null) return

    const node = findNode(nodes.value, nodeId)
    if (!node) throw new Error('节点不存在')

    const label = node.nodeType === WORKSPACE_NODE_TYPE.FOLDER ? '文件夹' : '文档'

    const confirmed = await openAppConfirm({
      title: '删除确认',
      message: `确定删除${label}「${node.title}」吗？此操作不可恢复。`,
      confirmLabel: '删除',
      cancelLabel: '取消',
      danger: true,
    })
    if (!confirmed) return

    await flushPendingSaves()
    await deleteWorkspaceNodeApi(nodeId)

    if (activeDocId.value != null && activeDocId.value === nodeId) {
      activeDocId.value = null
      activeDocDetail.value = null
      if (route.name === 'project-doc') {
        await router.push('/workspace/project/board')
      }
    }

    await loadTree()
  }

  return {
    workspaceId,
    rootTitle,
    nodes,
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
    clearActiveDoc,
  }
}
