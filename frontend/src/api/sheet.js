import { request } from './request'

const API_PREFIX = '/api/sheets'

export const getSheetApi = async (nodeId) => {
  const data = await request(`${API_PREFIX}/${nodeId}`)
  if (data == null) throw new Error('加载表格失败：服务端未返回数据，请重启后端后重试')
  return data
}

export const issueSheetCollabTokenApi = async (nodeId) => {
  const data = await request(`${API_PREFIX}/${nodeId}/collab-token`, {
    method: 'POST',
  })
  if (data == null) throw new Error('获取协同令牌失败，请重启后端后重试')
  return data
}
