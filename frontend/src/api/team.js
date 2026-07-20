import { request } from './request'

const API_PREFIX = '/api/team'

export const createTeamApi = (data) => {
  return request(API_PREFIX, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const addTeamMemberApi = (data) => {
  return request(`${API_PREFIX}/member`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/** 当前学生加入指定小组 */
export const joinTeamApi = (teamId) => {
  return request(`${API_PREFIX}/${teamId}/join`, {
    method: 'POST',
  })
}

/** 当前学生同课号下可加入的小组 */
export const listCourseTeamsApi = () => {
  return request(`${API_PREFIX}/course-teams`)
}

export const removeTeamMemberApi = (teamId, userId) => {
  const params = new URLSearchParams({
    teamId: String(teamId),
    userId: String(userId),
  })

  return request(`${API_PREFIX}/member?${params.toString()}`, {
    method: 'DELETE',
  })
}

export const listTeamMembersApi = (teamId) => {
  return request(`${API_PREFIX}/${teamId}/members`)
}

/** 解析小组成员接口返回，兼容旧版数组结构 */
export const parseTeamMembersResponse = (data) => {
  if (Array.isArray(data)) {
    return { members: data, memberList: [] }
  }
  return {
    members: Array.isArray(data?.members) ? data.members : [],
    memberList: Array.isArray(data?.memberList) ? data.memberList : [],
  }
}

export const submitTeamTopicApi = (data) => {
  return request(`${API_PREFIX}/topic/submit`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const listTeamTopicApprovalsApi = (teamId) => {
  return request(`${API_PREFIX}/${teamId}/topic-approvals`)
}

export const listTeacherTopicApprovalsApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.approvalStatus) search.set('approvalStatus', params.approvalStatus)
  const query = search.toString()
  return request(query ? `${API_PREFIX}/topic-approvals?${query}` : `${API_PREFIX}/topic-approvals`)
}

export const listTeacherTeamOverviewApi = (params = {}) => {
  const search = new URLSearchParams()
  if (params.teamStatus) search.set('teamStatus', params.teamStatus)
  const query = search.toString()
  return request(query ? `${API_PREFIX}/overview?${query}` : `${API_PREFIX}/overview`)
}

export const reviewTopicApprovalApi = (data) => {
  return request(`${API_PREFIX}/topic/review`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export const getCurrentTeamApi = () => {
  return request(`${API_PREFIX}/current`)
}
