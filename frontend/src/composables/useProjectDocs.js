import { computed, inject, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { openAppConfirm, openAppPrompt } from '@/composables/appPrompt'
import { getDocumentApi } from '@/api/document'
import { getSheetApi, issueSheetCollabTokenApi } from '@/api/sheet'
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
const isSheetNode = (node) => node?.nodeType === WORKSPACE_NODE_TYPE.SHEET
const isLeafNode = (node) => isDocumentNode(node) || isSheetNode(node)

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
  const activeLeafId = ref(null)
  const activeDocDetail = ref(null)
  const activeSheetDetail = ref(null)
  const expandedFolders = ref(new Set())
  const renamingNodeId = ref(null)
  const renameOriginalTitle = ref('')
  const pendingOpenLeafId = ref(null)
  const loading = ref(false)
  const errorMessage = ref('')

  let titleSaveTimer = null
  let pendingTitleNodeId = null
  let pendingTitle = ''
  let latestContentSnapshot = ''
  let latestSheetSnapshot = ''

  const activeDocId = computed(() => activeLeafId.value)

  const activeDoc = computed(() => {
    if (activeLeafId.value == null) return null
    const node = findNode(nodes.value, activeLeafId.value)
    if (!node || !isDocumentNode(node)) return null

    const detail = activeDocDetail.value
    if (!detail || String(detail.nodeId) !== String(activeLeafId.value)) {
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

  const activeSheet = computed(() => {
    if (activeLeafId.value == null) return null
    const node = findNode(nodes.value, activeLeafId.value)
    if (!node || !isSheetNode(node)) return null

    const detail = activeSheetDetail.value
    if (!detail || String(detail.nodeId) !== String(activeLeafId.value)) {
      return {
        ...node,
        contentJson: '',
        version: null,
        canWrite: false,
        collab: null,
        loading: true,
        updateDate: null,
      }
    }

    return {
      ...node,
      contentJson: detail.contentJson,
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

  const saveLeafTitle = async (nodeId, title) => {
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
        await saveLeafTitle(id, title)
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
    await saveLeafTitle(id, title)
  }

  const loadDocument = async (nodeId) => {
    activeDocDetail.value = {
      nodeId,
      contentMd: '',
      version: 0,
      canWrite: false,
      loading: true,
    }
    activeSheetDetail.value = null
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
      activeLeafId.value = null
      throw error
    }
  }

  const loadSheet = async (nodeId) => {
    activeSheetDetail.value = {
      nodeId,
      contentJson: '',
      version: 0,
      canWrite: false,
      loading: true,
    }
    activeDocDetail.value = null
    try {
      const [sheet, collab] = await Promise.all([
        getSheetApi(nodeId),
        getCollabSession(nodeId, { issueTokenApi: issueSheetCollabTokenApi }),
      ])

      activeSheetDetail.value = {
        nodeId: sheet.nodeId,
        contentJson: sheet.contentJson ?? '',
        version: sheet.version,
        canWrite: Boolean(sheet.canWrite),
        loading: false,
        collab,
        updateDate: sheet.updateDate ?? null,
      }
      latestSheetSnapshot = sheet.contentJson ?? ''

      const node = findNode(nodes.value, nodeId)
      if (node && sheet.title) node.title = sheet.title
    } catch (error) {
      activeSheetDetail.value = null
      activeLeafId.value = null
      throw error
    }
  }

  const loadLeafByRoute = async (id) => {
    const node = findNode(nodes.value, id)
    if (!isLeafNode(node)) {
      await router.replace('/workspace/project/board')
      return
    }

    if (String(activeLeafId.value) === String(id)) {
      const detail = isDocumentNode(node) ? activeDocDetail.value : activeSheetDetail.value
      if (detail && !detail.loading) return
    }

    activeLeafId.value = id
    errorMessage.value = ''
    await flushPendingSaves()

    try {
      if (isDocumentNode(node)) {
        await loadDocument(id)
      } else {
        await loadSheet(id)
      }
    } catch (error) {
      errorMessage.value = error?.message || (isDocumentNode(node) ? '加载文档失败' : '加载表格失败')
    }
  }

  const selectLeaf = async (id) => {
    const node = findNode(nodes.value, id)
    if (!isLeafNode(node)) return

    const nodeId = String(id)
    const routeName = isSheetNode(node) ? 'project-sheet' : 'project-doc'
    if (route.name === routeName && String(route.params.nodeId) === nodeId) {
      await loadLeafByRoute(id)
      return
    }
    await router.push({ name: routeName, params: { nodeId } })
  }

  watch(
    [() => route.name, () => route.params.nodeId, nodes],
    async ([name, nodeId]) => {
      if ((name !== 'project-doc' && name !== 'project-sheet') || !nodeId) return
      if (!findNode(nodes.value, nodeId)) return
      await loadLeafByRoute(nodeId)
    },
    { immediate: true },
  )

  watch(
    () => route.name,
    (name, prevName) => {
      const leavingLeafRoute = prevName === 'project-doc' || prevName === 'project-sheet'
      const enteringLeafRoute = name === 'project-doc' || name === 'project-sheet'
      if (leavingLeafRoute && !enteringLeafRoute) {
        void flushPendingSaves()
        clearActiveLeaf()
      }
    },
  )

  const clearActiveLeaf = () => {
    activeLeafId.value = null
    activeDocDetail.value = null
    activeSheetDetail.value = null
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
      pendingOpenLeafId.value = created.id
      startRenameNode(created.id)
    }

    return created
  }

  const createSheet = async (parentId = null) => {
    if (!workspaceId.value) throw new Error('工作区未加载')

    const defaultTitle = uniqueTitle(nodes.value, '未命名表格')
    const targetParentId = parentId ?? null
    const created = await createWorkspaceNodeApi({
      workspaceId: workspaceId.value,
      parentId: targetParentId,
      nodeType: WORKSPACE_NODE_TYPE.SHEET,
      title: defaultTitle,
    })

    await loadTree()

    if (targetParentId != null) {
      expandedFolders.value = new Set([...expandedFolders.value, targetParentId])
    }
    if (created?.id != null) {
      pendingOpenLeafId.value = created.id
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

  const defaultLeafTitle = (node) => {
    if (isSheetNode(node)) return '未命名表格'
    if (isDocumentNode(node)) return '未命名文档'
    return '新建文件夹'
  }

  const commitNodeRename = async (nodeId, title) => {
    if (renamingNodeId.value == null || String(renamingNodeId.value) !== String(nodeId)) return

    const node = findNode(nodes.value, nodeId)
    const originalTitle = renameOriginalTitle.value
    const shouldOpenLeaf = pendingOpenLeafId.value != null
      && String(pendingOpenLeafId.value) === String(nodeId)

    renamingNodeId.value = null
    renameOriginalTitle.value = ''
    pendingOpenLeafId.value = null

    if (!node) return

    const trimmed = title?.trim()
    const finalTitle = trimmed || originalTitle || defaultLeafTitle(node)

    if (finalTitle === originalTitle) {
      if (shouldOpenLeaf) await selectLeaf(nodeId)
      return
    }

    node.title = finalTitle
    try {
      await renameWorkspaceNodeApi(nodeId, { title: finalTitle })
      if (shouldOpenLeaf) {
        await selectLeaf(nodeId)
      } else if (activeLeafId.value === nodeId && isLeafNode(node)) {
        if (isDocumentNode(node)) await loadDocument(nodeId)
        else await loadSheet(nodeId)
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
    pendingOpenLeafId.value = null
  }

  const updateDocSnapshot = (content) => {
    latestContentSnapshot = content ?? ''
    const detail = activeDocDetail.value
    if (detail) {
      detail.contentMd = latestContentSnapshot
      detail.updateDate = new Date().toISOString()
    }
  }

  const updateSheetSnapshot = (contentJson) => {
    latestSheetSnapshot = contentJson ?? ''
    const detail = activeSheetDetail.value
    if (detail) {
      detail.contentJson = latestSheetSnapshot
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

  const updateSheetTitle = (id, title) => {
    const node = findNode(nodes.value, id)
    if (!node || !title.trim()) return
    node.title = title.trim()

    const detail = activeSheetDetail.value
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

    const label = node.nodeType === WORKSPACE_NODE_TYPE.FOLDER
      ? '文件夹'
      : isSheetNode(node)
        ? '表格'
        : '文档'

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

    if (activeLeafId.value != null && activeLeafId.value === nodeId) {
      clearActiveLeaf()
      if (route.name === 'project-doc' || route.name === 'project-sheet') {
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
    activeLeafId,
    activeDoc,
    activeSheet,
    expandedFolders,
    renamingNodeId,
    loading,
    errorMessage,
    loadTree,
    selectDoc: selectLeaf,
    selectLeaf,
    toggleFolder,
    createDocument,
    createSheet,
    createFolder,
    renameNode,
    commitNodeRename,
    cancelNodeRename,
    deleteNode,
    updateDocSnapshot,
    updateSheetSnapshot,
    updateDocTitle,
    updateSheetTitle,
    flushPendingSaves,
    clearActiveDoc: clearActiveLeaf,
    clearActiveLeaf,
  }
}
