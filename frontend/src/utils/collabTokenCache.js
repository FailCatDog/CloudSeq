import { issueCollabTokenApi } from '@/api/document'

/** 距过期前多久视为需要刷新（毫秒） */
const REFRESH_BUFFER_MS = 60_000
const BROADCAST_CHANNEL = 'collab-token-cache-v1'
const USER_STORAGE_KEY = 'user'

const memoryCache = new Map()
const inflight = new Map()
const refreshTimers = new Map()

let broadcastChannel = null
let storageSyncInitialized = false

const normalizeNodeId = (nodeId) => String(nodeId)

const getCurrentUserId = () => {
  const raw = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return null
  try {
    const user = JSON.parse(raw)
    return user?.id ?? user?.userId ?? null
  } catch {
    return null
  }
}

const storageKey = (scope) => {
  const userId = getCurrentUserId()
  return `collab-token-cache:${scope}:${userId ?? 'guest'}`
}

const parseJwtPayload = (token) => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64))
  } catch {
    return null
  }
}

const parseJwtExpiresAt = (token) => {
  const json = parseJwtPayload(token)
  return typeof json?.exp === 'number' ? json.exp * 1000 : 0
}

const parseJwtUserId = (token) => {
  const json = parseJwtPayload(token)
  const userId = json?.userId
  return userId == null ? null : String(userId)
}

const isFresh = (entry) => {
  if (!entry?.token || !entry.expiresAt) return false
  return entry.expiresAt - Date.now() > REFRESH_BUFFER_MS
}

const belongsToCurrentUser = (entry) => {
  if (!entry) return false
  const currentUserId = getCurrentUserId()
  if (currentUserId == null || entry.userId == null) return true
  return String(entry.userId) === String(currentUserId)
}

const readStorageMap = (storage, key) => {
  try {
    const raw = storage.getItem(key)
    if (!raw) return {}
    const map = JSON.parse(raw)
    return map && typeof map === 'object' ? map : {}
  } catch {
    return {}
  }
}

const pruneStorageMap = (map) => {
  for (const [nodeId, entry] of Object.entries(map)) {
    if (!isFresh(entry) || !belongsToCurrentUser(entry)) {
      delete map[nodeId]
    }
  }
  return map
}

const writeStorageMap = (storage, key, map) => {
  pruneStorageMap(map)
  if (Object.keys(map).length === 0) {
    storage.removeItem(key)
    return
  }
  storage.setItem(key, JSON.stringify(map))
}

const getStoredMaps = () => ({
  session: readStorageMap(sessionStorage, storageKey('session')),
  local: readStorageMap(localStorage, storageKey('local')),
})

const pickBestEntry = (nodeId, ...candidates) => {
  const key = normalizeNodeId(nodeId)
  const valid = candidates
    .filter(Boolean)
    .filter((entry) => normalizeNodeId(entry.nodeId) === key)
    .filter((entry) => belongsToCurrentUser(entry))
    .filter((entry) => isFresh(entry))

  if (valid.length === 0) return null
  return valid.sort((a, b) => b.expiresAt - a.expiresAt)[0]
}

const getMemoryEntry = (nodeId) => {
  const key = normalizeNodeId(nodeId)
  const entry = memoryCache.get(key)
  if (!entry || !isFresh(entry) || !belongsToCurrentUser(entry)) return null
  return entry
}

const hydrateMemoryEntry = (nodeId) => {
  const key = normalizeNodeId(nodeId)
  const memoryEntry = memoryCache.get(key)
  const { session, local } = getStoredMaps()
  const best = pickBestEntry(key, memoryEntry, session[key], local[key])
  if (best) memoryCache.set(key, best)
  else memoryCache.delete(key)
  return best
}

const persistEntry = (entry, { broadcast = true } = {}) => {
  const key = normalizeNodeId(entry.nodeId)
  const normalized = { ...entry, nodeId: key }
  memoryCache.set(key, normalized)

  const sessionMap = readStorageMap(sessionStorage, storageKey('session'))
  const localMap = readStorageMap(localStorage, storageKey('local'))
  sessionMap[key] = normalized
  localMap[key] = normalized
  writeStorageMap(sessionStorage, storageKey('session'), sessionMap)
  writeStorageMap(localStorage, storageKey('local'), localMap)

  scheduleRefresh(key)

  if (broadcast) {
    postBroadcast({ type: 'upsert', entry: normalized })
  }
}

const removeEntry = (nodeId, { broadcast = true } = {}) => {
  const key = normalizeNodeId(nodeId)
  memoryCache.delete(key)
  clearRefreshTimer(key)

  const sessionMap = readStorageMap(sessionStorage, storageKey('session'))
  const localMap = readStorageMap(localStorage, storageKey('local'))
  delete sessionMap[key]
  delete localMap[key]
  writeStorageMap(sessionStorage, storageKey('session'), sessionMap)
  writeStorageMap(localStorage, storageKey('local'), localMap)

  if (broadcast) {
    postBroadcast({ type: 'delete', nodeId: key })
  }
}

const clearRefreshTimer = (nodeId) => {
  const key = normalizeNodeId(nodeId)
  const timer = refreshTimers.get(key)
  if (timer) {
    clearTimeout(timer)
    refreshTimers.delete(key)
  }
}

const scheduleRefresh = (nodeId) => {
  const key = normalizeNodeId(nodeId)
  clearRefreshTimer(key)

  const entry = memoryCache.get(key) || hydrateMemoryEntry(key)
  if (!entry?.expiresAt) return

  const delay = Math.max(entry.expiresAt - REFRESH_BUFFER_MS - Date.now(), 0)
  refreshTimers.set(
    key,
    setTimeout(() => {
      fetchCollabToken(nodeId).catch(() => {})
    }, delay),
  )
}

const applyExternalUpsert = (entry) => {
  if (!entry?.nodeId || !isFresh(entry) || !belongsToCurrentUser(entry)) return
  persistEntry(entry, { broadcast: false })
}

const applyExternalDelete = (nodeId) => {
  removeEntry(nodeId, { broadcast: false })
}

const applyExternalClear = () => {
  memoryCache.clear()
  for (const key of [...refreshTimers.keys()]) clearRefreshTimer(key)
  sessionStorage.removeItem(storageKey('session'))
  localStorage.removeItem(storageKey('local'))
}

const getBroadcastChannel = () => {
  if (typeof BroadcastChannel === 'undefined') return null
  if (!broadcastChannel) {
    broadcastChannel = new BroadcastChannel(BROADCAST_CHANNEL)
    broadcastChannel.onmessage = (event) => {
      handleSyncMessage(event.data)
    }
  }
  return broadcastChannel
}

const postBroadcast = (message) => {
  getBroadcastChannel()?.postMessage(message)
}

const handleSyncMessage = (message) => {
  if (!message?.type) return
  if (message.type === 'upsert') applyExternalUpsert(message.entry)
  else if (message.type === 'delete') applyExternalDelete(message.nodeId)
  else if (message.type === 'clear') applyExternalClear()
}

const syncFromLocalStorageEvent = (event) => {
  const expectedKey = storageKey('local')
  if (event.key !== expectedKey) return

  if (!event.newValue) {
    applyExternalClear()
    return
  }

  try {
    const localMap = JSON.parse(event.newValue)
    const sessionMap = readStorageMap(sessionStorage, storageKey('session'))
    for (const [nodeId, entry] of Object.entries(localMap)) {
      if (isFresh(entry) && belongsToCurrentUser(entry)) {
        memoryCache.set(nodeId, entry)
        sessionMap[nodeId] = entry
        scheduleRefresh(nodeId)
      }
    }
    writeStorageMap(sessionStorage, storageKey('session'), sessionMap)
  } catch {
    // ignore malformed storage payloads
  }
}

const initStorageSync = () => {
  if (storageSyncInitialized || typeof window === 'undefined') return
  storageSyncInitialized = true

  getBroadcastChannel()
  window.addEventListener('storage', syncFromLocalStorageEvent)

  const { session, local } = getStoredMaps()
  for (const entry of Object.values({ ...session, ...local })) {
    if (isFresh(entry) && belongsToCurrentUser(entry)) {
      memoryCache.set(normalizeNodeId(entry.nodeId), entry)
      scheduleRefresh(entry.nodeId)
    }
  }
}

initStorageSync()

const toCollabSession = (entry) => ({
  wsUrl: entry.wsUrl,
  room: entry.room,
  displayName: entry.displayName,
  avatarUrl: entry.avatarUrl || '',
  canWrite: Boolean(entry.canWrite),
  getToken: () => resolveCollabToken(entry.nodeId),
})

const fetchCollabToken = async (nodeId) => {
  const key = normalizeNodeId(nodeId)

  if (inflight.has(key)) return inflight.get(key)

  const promise = issueCollabTokenApi(nodeId)
    .then((collab) => {
      const entry = {
        nodeId: key,
        token: collab.token,
        wsUrl: collab.wsUrl,
        room: collab.room,
        displayName: collab.displayName,
        avatarUrl: collab.avatarUrl || '',
        canWrite: Boolean(collab.canWrite),
        expiresAt: parseJwtExpiresAt(collab.token),
        userId: parseJwtUserId(collab.token),
      }
      persistEntry(entry)
      return entry
    })
    .finally(() => {
      inflight.delete(key)
    })

  inflight.set(key, promise)
  return promise
}

const resolveCachedEntry = (nodeId) => {
  return getMemoryEntry(nodeId) || hydrateMemoryEntry(nodeId)
}

/** 获取协同会话（优先复用未过期的缓存 token） */
export const getCollabSession = async (nodeId) => {
  const cached = resolveCachedEntry(nodeId)
  if (cached) return toCollabSession(cached)
  return toCollabSession(await fetchCollabToken(nodeId))
}

/** 供 Hocuspocus 连接时获取 token，必要时自动刷新 */
export const resolveCollabToken = async (nodeId) => {
  const cached = resolveCachedEntry(nodeId)
  if (cached) return cached.token
  return (await fetchCollabToken(nodeId)).token
}

/** 鉴权失败时清除缓存，下次会重新签发 */
export const invalidateCollabToken = (nodeId) => {
  removeEntry(nodeId)
}

/** 退出登录时清空当前用户的协同 token 缓存 */
export const clearCollabTokenCache = () => {
  applyExternalClear()
  postBroadcast({ type: 'clear' })
}
