import { CacheCode } from '@/constants/cacheCode'
import {
  clearPermissionContext,
  ensurePermissionContext,
  getHomePath,
  isRouteAllowed,
  setPermissionContext,
} from '@/stores/permissionStore'

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

export const isStaffRole = (role) =>
  role === CacheCode.USER_ROLE_TEACHER || role === CacheCode.USER_ROLE_ADMIN

export const isAuthRoute = (path) => path.startsWith('/auth') || path === '/login'

export const resolveNavigationGuard = async (toPath) => {
  const path = toPath.split('?')[0].split('#')[0] || '/'

  if (isAuthRoute(path)) {
    if (!getAuthToken()) return null
    await ensurePermissionContext()
    return getHomePath()
  }

  if (!getAuthToken()) return '/auth'

  await ensurePermissionContext()

  if (path === '/' || path === '') return getHomePath()
  if (!isRouteAllowed(path)) return getHomePath()

  return null
}

export {
  clearPermissionContext,
  ensurePermissionContext,
  getHomePath,
  isRouteAllowed,
  setPermissionContext,
}
