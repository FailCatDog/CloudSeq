import jwt from 'jsonwebtoken'
import { config } from './config.js'

export function verifyCollabToken(token) {
  const payload = jwt.verify(token, config.jwtSecret)
  if (payload.type !== 'collab') throw new Error('invalid token type')
  if (payload.userId == null || payload.nodeId == null) throw new Error('invalid token payload')
  return {
    userId: String(payload.userId),
    nodeId: String(payload.nodeId),
    canWrite: Boolean(payload.canWrite),
    displayName: payload.displayName || '用户',
  }
}

export function parseNodeId(documentName) {
  if (!documentName.startsWith(config.roomPrefix)) {
    throw new Error('invalid document room')
  }
  const nodeId = documentName.slice(config.roomPrefix.length)
  if (!nodeId) throw new Error('invalid node id')
  return nodeId
}

export async function loadDocument(nodeId) {
  const response = await fetch(`${config.springApiUrl}/api/internal/collab/load/${nodeId}`, {
    headers: {
      'X-Collab-Secret': config.internalSecret,
    },
  })
  const body = await response.json().catch(() => ({}))
  if (!response.ok || body.code !== 0) {
    throw new Error(body.message || `load failed (${response.status})`)
  }
  return body.data
}

export async function persistDocument(payload) {
  const response = await fetch(`${config.springApiUrl}/api/internal/collab/persist`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Collab-Secret': config.internalSecret,
    },
    body: JSON.stringify(payload),
  })
  const body = await response.json().catch(() => ({}))
  if (!response.ok || body.code !== 0) {
    throw new Error(body.message || `persist failed (${response.status})`)
  }
  return body.data
}
