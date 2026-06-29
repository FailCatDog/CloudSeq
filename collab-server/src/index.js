import { Server } from '@hocuspocus/server'
import * as Y from 'yjs'
import { config } from './config.js'
import { loadDocument, parseNodeId, persistDocument, verifyCollabToken } from './spring-api.js'

const documentMeta = new Map()

const server = new Server({
  port: config.port,
  debounce: 3000,
  maxDebounce: 15000,

  async onAuthenticate({ token, documentName, connectionConfig }) {
    const claims = verifyCollabToken(token)
    const nodeId = parseNodeId(documentName)
    if (claims.nodeId !== nodeId) throw new Error('token room mismatch')

    connectionConfig.readOnly = !claims.canWrite

    return {
      userId: claims.userId,
      nodeId: claims.nodeId,
      displayName: claims.displayName,
      canWrite: claims.canWrite,
    }
  },

  async onLoadDocument({ document, documentName }) {
    const nodeId = parseNodeId(documentName)
    const data = await loadDocument(nodeId)

    documentMeta.set(documentName, {
      nodeId,
      version: data.version ?? 0,
      contentMd: data.contentMd ?? '',
      updateUser: null,
    })

    if (data.yjsStateBase64) {
      const update = Uint8Array.from(Buffer.from(data.yjsStateBase64, 'base64'))
      Y.applyUpdate(document, update)
    }
  },

  async onStateless({ payload, documentName }) {
    try {
      const message = JSON.parse(payload)
      if (message?.type !== 'content-snapshot') return

      const meta = documentMeta.get(documentName)
      if (!meta) return

      documentMeta.set(documentName, {
        ...meta,
        contentMd: message.contentMd ?? meta.contentMd,
        updateUser: message.userId ?? meta.updateUser,
      })
    } catch {
      // ignore malformed payloads
    }
  },

  async onStoreDocument({ document, documentName, lastContext }) {
    const meta = documentMeta.get(documentName)
    if (!meta) return

    const state = Y.encodeStateAsUpdate(document)
    const result = await persistDocument({
      nodeId: meta.nodeId,
      version: meta.version,
      yjsStateBase64: Buffer.from(state).toString('base64'),
      contentMd: meta.contentMd || lastContext?.contentMd || null,
      updateUser: meta.updateUser ?? lastContext?.userId ?? null,
    })

    if (result?.version != null) {
      documentMeta.set(documentName, {
        ...meta,
        version: result.version,
      })
    }
  },
})

server.listen().catch((error) => {
  console.error(`Collab server failed to start: ${error.message}`)
  process.exit(1)
})
console.log(`Collab server listening on ws://localhost:${config.port}`)
