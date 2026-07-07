import { CacheCode } from '@/constants/cacheCode'
import { ROLE_ROUTE_ACCESS } from '@/constants/roleRoutes'
import {
  clearRoleAccess,
  ensureRoleAccess,
  getRoleHomePath as getStoredHomePath,
  isPathAllowed,
  setRoleAccess,
} from '@/stores/roleAccessStore'

const AUTH_STORAGE_KEY = 'authorization'
const USER_STORAGE_KEY = 'user'

export const getAuthToken = () =>
  localStorage.getItem(AUTH_STORAGE_KEY) || sessionStorage.getItem(AUTH_STORAGE_KEY) || ''

export const getUserFromStorage = () => {
  const raw = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export const isTeacherRole = (role) => role === CacheCode.USER_ROLE_TEACHER

export const getRoleHomePath = (role) =>
  ROLE_ROUTE_ACCESS[role]?.home || ROLE_ROUTE_ACCESS[CacheCode.USER_ROLE_STUDENT].home

export const isAuthRoute = (path) => path.startsWith('/auth') || path === '/login'

const isRememberedSession = () => Boolean(localStorage.getItem(AUTH_STORAGE_KEY))

export const resolveNavigationGuard = (toPath) => {
  const path = toPath.split('?')[0].split('#')[0] || '/'

  if (isAuthRoute(path)) {
    if (!getAuthToken()) return null
    const user = getUserFromStorage()
    ensureRoleAccess(user?.role, { remember: isRememberedSession() })
    return getStoredHomePath()
  }

  if (!getAuthToken()) return '/auth'

  const user = getUserFromStorage()
  ensureRoleAccess(user?.role, { remember: isRememberedSession() })

  if (path === '/' || path === '') return getStoredHomePath()
  if (!isPathAllowed(path)) return getStoredHomePath()

  return null
}

export {
  clearRoleAccess,
  ensureRoleAccess,
  isPathAllowed,
  setRoleAccess,
}
