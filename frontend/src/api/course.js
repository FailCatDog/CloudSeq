import { request } from './request'

const API_PREFIX = '/api/course'

export const createCourseApi = (data) => {
  return request(API_PREFIX, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const updateCourseApi = (courseId, data) => {
  return request(`${API_PREFIX}/${courseId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

export const getCourseByIdApi = (courseId) => {
  return request(`${API_PREFIX}/${courseId}`)
}

export const listMyCoursesApi = () => {
  return request(`${API_PREFIX}/mine`)
}

export const listCoursesApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.termYear != null) search.set('termYear', String(params.termYear))
  if (params.termSeason) search.set('termSeason', params.termSeason)
  if (params.teacherId != null) search.set('teacherId', String(params.teacherId))
  const query = search.toString()
  return request(query ? `${API_PREFIX}?${query}` : API_PREFIX)
}

export const listCourseEnrollmentsApi = (courseId) => {
  return request(`${API_PREFIX}/${courseId}/enrollments`)
}

export const enrollCourseStudentApi = (courseId, data) => {
  return request(`${API_PREFIX}/${courseId}/enrollments`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const dropCourseEnrollmentApi = (courseId, userId) => {
  return request(`${API_PREFIX}/${courseId}/enrollments/${userId}/drop`, {
    method: 'POST',
  })
}
