import { request } from './request'

const API_PREFIX = '/api/weekly-report'

export const getWeeklyReportByWeekApi = (workspaceId, reportYear, reportWeek) => {
  const params = new URLSearchParams()
  if (reportYear != null) params.set('reportYear', String(reportYear))
  if (reportWeek != null) params.set('reportWeek', String(reportWeek))
  const query = params.toString()
  return request(`${API_PREFIX}/workspace/${workspaceId}${query ? `?${query}` : ''}`)
}

export const listMyWeeklyReportsApi = (workspaceId) => {
  return request(`${API_PREFIX}/workspace/${workspaceId}/mine`)
}

export const getWeeklyReportApi = (id) => {
  return request(`${API_PREFIX}/${id}`)
}

export const createWeeklyReportApi = (data) => {
  return request(API_PREFIX, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const updateWeeklyReportApi = (data) => {
  return request(API_PREFIX, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

export const submitWeeklyReportApi = (id) => {
  return request(`${API_PREFIX}/${id}/submit`, {
    method: 'POST',
  })
}

export const deleteWeeklyReportApi = (id) => {
  return request(`${API_PREFIX}/${id}`, {
    method: 'DELETE',
  })
}
