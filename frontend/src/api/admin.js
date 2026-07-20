import { request } from './request'

const MENU_PREFIX = '/api/admin/menus'
const ROLE_PREFIX = '/api/admin/roles'
const USER_PREFIX = '/api/admin/users'
const DICT_PREFIX = '/api/admin/dicts'

export const listAdminMenuTreeApi = () => request(`${MENU_PREFIX}/tree`)

export const getAdminMenuApi = (id) => request(`${MENU_PREFIX}/${id}`)

export const createAdminMenuApi = (data) => request(MENU_PREFIX, {
  method: 'POST',
  body: JSON.stringify(data),
})

export const updateAdminMenuApi = (id, data) => request(`${MENU_PREFIX}/${id}`, {
  method: 'PUT',
  body: JSON.stringify(data),
})

export const deleteAdminMenuApi = (id) => request(`${MENU_PREFIX}/${id}`, {
  method: 'DELETE',
})

export const updateAdminMenuStatusApi = (id, status) => {
  const query = new URLSearchParams({ status })
  return request(`${MENU_PREFIX}/${id}/status?${query}`, { method: 'PATCH' })
}

export const listAdminRolesApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.status) search.set('status', params.status)
  const query = search.toString()
  return request(query ? `${ROLE_PREFIX}?${query}` : ROLE_PREFIX)
}

export const getAdminRoleApi = (id) => request(`${ROLE_PREFIX}/${id}`)

export const createAdminRoleApi = (data) => request(ROLE_PREFIX, {
  method: 'POST',
  body: JSON.stringify(data),
})

export const updateAdminRoleApi = (id, data) => request(`${ROLE_PREFIX}/${id}`, {
  method: 'PUT',
  body: JSON.stringify(data),
})

export const updateAdminRoleStatusApi = (id, status) => {
  const query = new URLSearchParams({ status })
  return request(`${ROLE_PREFIX}/${id}/status?${query}`, { method: 'PATCH' })
}

export const listRoleMenuIdsApi = (roleId) => request(`${ROLE_PREFIX}/${roleId}/menu-ids`)

export const replaceRoleMenuIdsApi = (roleId, menuIds) => request(`${ROLE_PREFIX}/${roleId}/menu-ids`, {
  method: 'PUT',
  body: JSON.stringify({ menuIds }),
})

export const searchAdminUsersApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.keyword) search.set('keyword', params.keyword)
  if (params.roleKey) search.set('roleKey', params.roleKey)
  const query = search.toString()
  return request(query ? `${USER_PREFIX}?${query}` : USER_PREFIX)
}

export const getAdminUserRolesApi = (userId) => request(`${USER_PREFIX}/${userId}/roles`)

export const replaceAdminUserRolesApi = (userId, roleIds) => request(`${USER_PREFIX}/${userId}/roles`, {
  method: 'PUT',
  body: JSON.stringify({ roleIds }),
})

export const updateAdminUserStatusApi = (userId, isActive) => {
  const query = new URLSearchParams({ isActive: String(isActive) })
  return request(`${USER_PREFIX}/${userId}/status?${query}`, { method: 'PATCH' })
}

export const listAdminDictKeysApi = () => request(DICT_PREFIX)

export const getAdminDictKeyApi = (id) => request(`${DICT_PREFIX}/${id}`)

export const createAdminDictKeyApi = (data) => request(DICT_PREFIX, {
  method: 'POST',
  body: JSON.stringify(data),
})

export const updateAdminDictKeyApi = (id, data) => request(`${DICT_PREFIX}/${id}`, {
  method: 'PUT',
  body: JSON.stringify(data),
})

export const deleteAdminDictKeyApi = (id) => request(`${DICT_PREFIX}/${id}`, {
  method: 'DELETE',
})

export const createAdminDictValueApi = (keyId, data) => request(`${DICT_PREFIX}/${keyId}/values`, {
  method: 'POST',
  body: JSON.stringify(data),
})

export const updateAdminDictValueApi = (valueId, data) => request(`${DICT_PREFIX}/values/${valueId}`, {
  method: 'PUT',
  body: JSON.stringify(data),
})

export const deleteAdminDictValueApi = (valueId) => request(`${DICT_PREFIX}/values/${valueId}`, {
  method: 'DELETE',
})

export const reloadAdminDictCacheApi = () => request(`${DICT_PREFIX}/cache/reload`, {
  method: 'POST',
})
