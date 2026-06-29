import { request } from './request'

const API_PREFIX = '/api/documents'

export const getDocumentApi = async (nodeId) => {
  const data = await request(`${API_PREFIX}/${nodeId}`)
  if (data == null) throw new Error('加载文档失败：服务端未返回数据，请重启后端后重试')
  return data
}

export const issueCollabTokenApi = async (nodeId) => {
  const data = await request(`${API_PREFIX}/${nodeId}/collab-token`, {
    method: 'POST',
  })
  if (data == null) throw new Error('获取协同令牌失败，请重启后端后重试')
  return data
}

export const saveDocumentContentApi = async (nodeId, body) => {
  const data = await request(`${API_PREFIX}/${nodeId}/content`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  if (data == null) throw new Error('保存失败：服务端未返回数据，请重启后端后重试')
  return data
}
