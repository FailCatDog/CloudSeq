import { request } from './request'

const API_PREFIX = '/api/workspace'

export const getCurrentWorkspaceApi = () => {
  return request(`${API_PREFIX}/current`)
}

export const getWorkspaceTreeApi = (workspaceId) => {
  const params = new URLSearchParams({ workspaceId: String(workspaceId) })
  return request(`${API_PREFIX}/nodes/tree?${params.toString()}`)
}

export const createWorkspaceNodeApi = (data) => {
  return request(`${API_PREFIX}/nodes`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const renameWorkspaceNodeApi = (nodeId, data) => {
  return request(`${API_PREFIX}/nodes/${nodeId}/rename`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

export const deleteWorkspaceNodeApi = (nodeId) => {
  return request(`${API_PREFIX}/nodes/${nodeId}`, {
    method: 'DELETE',
  })
}
