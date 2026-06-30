import { request } from './request'

const API_PREFIX = '/api/documents'

export const listDocumentCommentsApi = async (nodeId) => {
  const data = await request(`${API_PREFIX}/${nodeId}/comments`)
  return Array.isArray(data) ? data : []
}

export const createDocumentCommentApi = async (nodeId, body) => {
  const data = await request(`${API_PREFIX}/${nodeId}/comments`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (data == null) throw new Error('发表评论失败：服务端未返回数据')
  return data
}

export const deleteDocumentCommentApi = async (commentId) => {
  await request(`${API_PREFIX}/comments/${commentId}`, {
    method: 'DELETE',
  })
}
