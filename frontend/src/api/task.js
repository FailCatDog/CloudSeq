import { request } from './request'

const API_PREFIX = '/api/plan-task'

export const listPlanTasksApi = (workspaceId) => {
  return request(`${API_PREFIX}/workspace/${workspaceId}`)
}

export const getPlanTaskApi = (id) => {
  return request(`${API_PREFIX}/${id}`)
}

export const createPlanTaskApi = (data) => {
  return request(API_PREFIX, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const updatePlanTaskApi = (id, data) => {
  return request(API_PREFIX, {
    method: 'PUT',
    body: JSON.stringify({ ...data, id: data.id ?? id }),
  })
}

export const deletePlanTaskApi = (id) => {
  return request(`${API_PREFIX}/${id}`, {
    method: 'DELETE',
  })
}
