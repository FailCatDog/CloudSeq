import { request } from './request'

const API_PREFIX = '/api/teaching'

export const getTeacherDashboardApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.courseId != null) search.set('courseId', String(params.courseId))
  const query = search.toString()
  return request(query ? `${API_PREFIX}/dashboard?${query}` : `${API_PREFIX}/dashboard`)
}

export const listTeacherStudentsApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.name) search.set('name', params.name)
  if (params.studentNo) search.set('studentNo', params.studentNo)
  if (params.courseCode) search.set('courseCode', params.courseCode)
  const query = search.toString()
  return request(query ? `${API_PREFIX}/students?${query}` : `${API_PREFIX}/students`)
}
