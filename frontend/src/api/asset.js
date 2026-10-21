const TOKEN_STORAGE_KEY = 'authorization'

export const ASSET_PROXY_PREFIX = '/api/assets/'

const getToken = () => {
  return localStorage.getItem(TOKEN_STORAGE_KEY) || sessionStorage.getItem(TOKEN_STORAGE_KEY) || ''
}

export const isAssetProxyUrl = (url) => {
  if (!url) return false
  const path = url.split('?')[0]
  return path.startsWith(ASSET_PROXY_PREFIX)
}

/**
 * 后端代理上传文档资产（图片/附件）
 * @returns {{ assetId, url, fileName, contentType, size, assetType, nodeId, workspaceId }}
 */
export const uploadDocumentAssetApi = async (nodeId, file) => {
  const formData = new FormData()
  formData.append('file', file)

  const token = getToken()
  const headers = {}
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`/api/documents/${nodeId}/assets`, {
    method: 'POST',
    headers,
    body: formData,
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok || data.code !== 0) {
    throw new Error(data.message || '上传失败')
  }
  return data.data
}

/**
 * 将代理 URL 转为可嵌入 img 的地址（附带 access_token，不落库）
 */
export const toAssetDisplayUrl = (proxyUrl) => {
  if (!proxyUrl) return proxyUrl
  const token = getToken()
  if (!token || proxyUrl.includes('access_token=')) {
    return proxyUrl
  }
  const separator = proxyUrl.includes('?') ? '&' : '?'
  return `${proxyUrl}${separator}access_token=${encodeURIComponent(token)}`
}

export { getToken as getAuthToken }
