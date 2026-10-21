import { ref, shallowRef } from 'vue'
import { getCurrentWorkspaceApi } from '@/api/workspace'

const workspace = shallowRef(null)
const workspaceId = ref(null)
const loading = ref(false)
const errorMessage = ref('')

let loadPromise = null

export function resetWorkspaceState() {
  workspace.value = null
  workspaceId.value = null
  errorMessage.value = ''
  loadPromise = null
}

export async function loadCurrentWorkspace({ force = false } = {}) {
  if (!force && workspaceId.value != null) {
    return workspace.value
  }
  if (!force && loadPromise) {
    return loadPromise
  }

  loading.value = true
  errorMessage.value = ''

  loadPromise = getCurrentWorkspaceApi()
    .then((data) => {
      if (!data?.id) {
        throw new Error('工作区未找到')
      }
      workspace.value = data
      workspaceId.value = data.id
      return data
    })
    .catch((error) => {
      workspace.value = null
      workspaceId.value = null
      errorMessage.value = error?.message || '加载工作区失败'
      throw error
    })
    .finally(() => {
      loading.value = false
      loadPromise = null
    })

  return loadPromise
}

export function useWorkspace() {
  const requireWorkspaceId = () => {
    const id = workspaceId.value
    if (id == null) {
      throw new Error(errorMessage.value || '工作区未加载')
    }
    return id
  }

  return {
    workspace,
    workspaceId,
    loading,
    errorMessage,
    loadCurrentWorkspace,
    requireWorkspaceId,
    resetWorkspaceState,
  }
}
