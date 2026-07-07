import { reactive } from 'vue'
import { CacheCode } from '@/constants/cacheCode'
import { AUTH_ROUTE_PREFIXES, ROLE_ROUTE_ACCESS } from '@/constants/roleRoutes'

const STORAGE_KEY = 'role_allowed_access'

const normalizePath = (path) => {
  if (!path) return '/'
  const base = path.split('?')[0].split('#')[0]
  if (base.length > 1 && base.endsWith('/')) return base.slice(0, -1)
  return base
}

const defaultStudentAccess = () => ROLE_ROUTE_ACCESS[CacheCode.USER_ROLE_STUDENT]

const state = reactive({
  role: null,
  exact: [],
  prefixes: [],
  home: defaultStudentAccess().home,
  ready: false,
})

const readStorage = () => {
  const raw = localStorage.getItem(STORAGE_KEY) || sessionStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

const writeStorage = (payload, remember) => {
  const storage = remember ? localStorage : sessionStorage
  const other = remember ? sessionStorage : localStorage
  storage.setItem(STORAGE_KEY, JSON.stringify(payload))
  other.removeItem(STORAGE_KEY)
}

const removeStorage = () => {
  localStorage.removeItem(STORAGE_KEY)
  sessionStorage.removeItem(STORAGE_KEY)
}

const applyAccess = (access) => {
  state.role = access.role
  state.exact = Array.isArray(access.exact) ? access.exact : []
  state.prefixes = Array.isArray(access.prefixes) ? access.prefixes : []
  state.home = access.home || defaultStudentAccess().home
  state.ready = true
}

/** 根据角色生成允许访问路径容器 */
export const buildRoleAccess = (role) => {
  const config = ROLE_ROUTE_ACCESS[role] || defaultStudentAccess()
  return {
    role: role || CacheCode.USER_ROLE_STUDENT,
    exact: [...config.exact],
    prefixes: [...config.prefixes],
    home: config.home,
  }
}

/** 登录成功后写入路径白名单容器 */
export const setRoleAccess = (role, { remember = true } = {}) => {
  const access = buildRoleAccess(role)
  applyAccess(access)
  writeStorage(access, remember)
  return access
}

/** 从本地存储恢复（刷新页面） */
export const restoreRoleAccess = () => {
  const cached = readStorage()
  if (cached?.role && Array.isArray(cached.exact)) {
    applyAccess(cached)
    return cached
  }
  state.ready = false
  return null
}

/** 确保访问容器已就绪（有 user 但无缓存时按角色重建） */
export const ensureRoleAccess = (role, { remember = true } = {}) => {
  if (state.ready && state.role === role) return state
  const restored = restoreRoleAccess()
  if (restored?.role === role) return state
  if (role) return setRoleAccess(role, { remember })
  clearRoleAccess()
  return state
}

export const clearRoleAccess = () => {
  state.role = null
  state.exact = []
  state.prefixes = []
  state.home = defaultStudentAccess().home
  state.ready = false
  removeStorage()
}

const isAuthPath = (path) => AUTH_ROUTE_PREFIXES.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))

/** 路径是否在当前角色的白名单容器内 */
export const isPathAllowed = (path) => {
  const normalized = normalizePath(path)
  if (isAuthPath(normalized)) return true
  if (!state.ready) return false
  if (state.exact.includes(normalized)) return true
  return state.prefixes.some((prefix) => normalized.startsWith(prefix))
}

export const getRoleHomePath = () => state.home || defaultStudentAccess().home

export const useRoleAccessStore = () => ({
  role: () => state.role,
  exact: () => state.exact,
  prefixes: () => state.prefixes,
  home: () => state.home,
  ready: () => state.ready,
  isPathAllowed,
  getRoleHomePath,
  setRoleAccess,
  restoreRoleAccess,
  ensureRoleAccess,
  clearRoleAccess,
  buildRoleAccess,
})
