import { fileNameFromContentDisposition, triggerBlobDownload } from '@/utils/downloadBlob'

const TOKEN_STORAGE_KEY = 'authorization'

const getToken = () => {
  return localStorage.getItem(TOKEN_STORAGE_KEY) || sessionStorage.getItem(TOKEN_STORAGE_KEY) || ''
}

/**
 * Download a node export artifact (docx / pdf / xlsx).
 * Uses raw fetch — do not use request() which always parses JSON.
 *
 * When `contentMd` is provided, POSTs the live editor snapshot so export does not
 * depend on collab debounce persisting contentMd to the database first.
 *
 * @param {string|number} nodeId
 * @param {'docx'|'pdf'|'xlsx'|string} format
 * @param {{ contentMd?: string }} [options]
 */
export const downloadNodeExportApi = async (nodeId, format, options = {}) => {
  const token = getToken()
  const headers = {}
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const contentMd = options?.contentMd
  const useLiveContent = typeof contentMd === 'string' && contentMd.length > 0
  if (useLiveContent) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(
    `/api/nodes/${encodeURIComponent(nodeId)}/export?format=${encodeURIComponent(format)}`,
    {
      method: useLiveContent ? 'POST' : 'GET',
      headers,
      body: useLiveContent ? JSON.stringify({ contentMd }) : undefined,
    },
  )

  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    const data = await response.json().catch(() => ({}))
    throw new Error(data.message || '导出失败')
  }

  if (!response.ok) {
    throw new Error('导出失败')
  }

  const blob = await response.blob()
  const fallback = `export.${format || 'bin'}`
  const fileName = fileNameFromContentDisposition(
    response.headers.get('content-disposition'),
    fallback,
  )
  triggerBlobDownload(blob, fileName)
}
