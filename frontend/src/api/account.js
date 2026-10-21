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
