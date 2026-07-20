import { request } from './request'

const API_PREFIX = '/api/account'

export const loginApi = (data) => {
  return request(`${API_PREFIX}/login`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const registerApi = (data) => {
  return request(`${API_PREFIX}/register`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const getProfileApi = (userId) => {
  return request(`${API_PREFIX}/profile/${userId}`)
}

export const getProfileStatusApi = () => {
  return request(`${API_PREFIX}/profile/status`)
}

export const getPermissionsApi = () => {
  return request(`${API_PREFIX}/permissions`)
}

export const updateProfileApi = (data) => {
  return request(`${API_PREFIX}/profile`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  })
}
