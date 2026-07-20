import { reactive } from 'vue'
import { CacheCode } from '@/constants/cacheCode'
import { ICONS } from '@/constants/roleNav'
import {
  AUTH_PATHS,
  LAYOUT_ENTRY_TARGETS,
  SHARED_EXACT,
  SHARED_PREFIXES,
  buildSectionSubNav,
  flattenMenuNodes,
  normalizeRoutePath,
} from '@/router/routeRegistry'

const STORAGE_KEY = 'permission_context'

/** 后端未返回 home 时的兜底（共享页） */
const FALLBACK_HOME = '/profile'

const state = reactive({
  permissions: [],
  menus: [],
  exact: [],
  prefixes: [],
  home: FALLBACK_HOME,
  dataScope: CacheCode.DATA_SCOPE_SELF,
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

const flattenMenuPaths = (menus, acc = []) => {
  for (const menu of menus || []) {
    if (menu?.path) {
      acc.push({
        path: menu.path,
        routeMatch: menu.routeMatch || CacheCode.ROUTE_MATCH_EXACT,
      })
    }
    if (menu?.children?.length) {
      flattenMenuPaths(menu.children, acc)
    }
  }
  return acc
}

/** 从后端 menus 推导路由白名单 */
export const buildRouteRules = (menus) => {
  const exact = new Set(SHARED_EXACT)
  const prefixes = new Set(SHARED_PREFIXES)

  const entries = flattenMenuPaths(menus)
  const normalizedPaths = []

  for (const { path, routeMatch } of entries) {
    const normalized = normalizeRoutePath(path)
    normalizedPaths.push(normalized)
    if (routeMatch === CacheCode.ROUTE_MATCH_PREFIX) {
      prefixes.add(normalized.endsWith('/') ? normalized : `${normalized}/`)
    } else {
      exact.add(normalized)
    }
  }

  for (const path of normalizedPaths) {
    const hasChildPath = normalizedPaths.some(
      (other) => other !== path && other.startsWith(`${path}/`),
    )
    if (hasChildPath) {
      prefixes.add(path.endsWith('/') ? path : `${path}/`)
    }
  }

  for (const path of exact) {
    if (path === '/workspace/project' || path === '/teaching' || path === '/admin') {
      prefixes.add(`${path}/`)
    }
  }

  return { exact: [...exact], prefixes: [...prefixes] }
}

const resolveIcon = (iconKey) => ICONS[iconKey] || ICONS.dashboard

const collectSidebarItems = (menus, acc = []) => {
  for (const menu of menus || []) {
    if (menu?.path) {
      const path = normalizeRoutePath(menu.path)
      acc.push({
        to: LAYOUT_ENTRY_TARGETS[path] || path,
        label: menu.menuName,
        ariaLabel: menu.menuName,
        icon: resolveIcon(menu.icon),
      })
    } else if (menu?.children?.length) {
      collectSidebarItems(menu.children, acc)
    }
  }
  return acc
}

/** 从后端 menus 生成侧栏导航 */
export const buildSidebarNav = (menus) => collectSidebarItems(menus)

/** 管理端二级导航：从 RBAC menus 筛选 /admin/* */
export const buildAdminSubNav = (menus) => {
  const items = buildSectionSubNav(menus, '/admin')
  return items.map((item) => ({
    ...item,
    icon: resolveIcon(
      flattenMenuNodes(menus).find((menu) => normalizeRoutePath(menu.path) === item.to)?.icon,
    ),
  }))
}

const applyContext = (context) => {
  state.permissions = Array.isArray(context.permissions) ? [...context.permissions] : []
  state.menus = Array.isArray(context.menus) ? context.menus : []
  state.exact = Array.isArray(context.exact) ? context.exact : []
  state.prefixes = Array.isArray(context.prefixes) ? context.prefixes : []
  state.home = context.home || FALLBACK_HOME
  state.dataScope = context.dataScope || CacheCode.DATA_SCOPE_SELF
  state.ready = true
}

const syncRoutesForContext = async (context) => {
  const { syncDynamicRoutesFromContext } = await import('@/router/dynamicRoutes')
  const router = (await import('@/router')).default
  await syncDynamicRoutesFromContext(router, context)
}

/** 登录 / 刷新权限后写入 RBAC 授权上下文 */
export const setPermissionContext = async (payload, { remember = true } = {}) => {
  const menus = payload?.menus || []
  const rules = buildRouteRules(menus)
  const context = {
    permissions: payload?.permissions ? [...payload.permissions] : [],
    menus,
    home: payload?.home || FALLBACK_HOME,
    dataScope: payload?.dataScope || CacheCode.DATA_SCOPE_SELF,
    exact: rules.exact,
    prefixes: rules.prefixes,
  }
  applyContext(context)
  writeStorage(context, remember)
  await syncRoutesForContext(context)
  return context
}

/** 从本地存储恢复（刷新页面） */
export const restorePermissionContext = () => {
  const cached = readStorage()
  if (cached?.exact && Array.isArray(cached.exact)) {
    applyContext(cached)
    return cached
  }
  state.ready = false
  return null
}

const isRememberedSession = () => Boolean(localStorage.getItem('authorization'))

/** 确保授权上下文已就绪；无缓存时从后端拉取 */
export const ensurePermissionContext = async () => {
  if (state.ready) {
    await syncRoutesForContext({
      menus: state.menus,
      home: state.home,
      exact: state.exact,
      prefixes: state.prefixes,
    })
    return state
  }

  const restored = restorePermissionContext()
  if (restored) {
    await syncRoutesForContext(restored)
    return state
  }

  const token = localStorage.getItem('authorization') || sessionStorage.getItem('authorization')
  if (!token) {
    await clearPermissionContext()
    return state
  }

  try {
    const { getPermissionsApi } = await import('@/api/account')
    const data = await getPermissionsApi()
    return setPermissionContext(data, { remember: isRememberedSession() })
  } catch {
    await clearPermissionContext()
    return state
  }
}

export const clearPermissionContext = async () => {
  state.permissions = []
  state.menus = []
  state.exact = []
  state.prefixes = []
  state.home = FALLBACK_HOME
  state.dataScope = CacheCode.DATA_SCOPE_SELF
  state.ready = false
  removeStorage()
  const { resetDynamicRoutes } = await import('@/router/dynamicRoutes')
  const router = (await import('@/router')).default
  resetDynamicRoutes(router)
}

const isAuthPath = (path) => AUTH_PATHS.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))

/** 路径是否在当前用户后端下发的路由规则内 */
export const isRouteAllowed = (path) => {
  const normalized = normalizeRoutePath(path)
  if (isAuthPath(normalized)) return true
  if (!state.ready) return false
  if (state.exact.includes(normalized)) return true
  return state.prefixes.some((prefix) => normalized.startsWith(prefix))
}

export const getHomePath = () => state.home || FALLBACK_HOME

export const hasPermission = (perm) => {
  if (!perm) return false
  return state.permissions.includes(perm)
}

export const usePermissionStore = () => ({
  permissions: () => state.permissions,
  menus: () => state.menus,
  exact: () => state.exact,
  prefixes: () => state.prefixes,
  home: () => state.home,
  dataScope: () => state.dataScope,
  ready: () => state.ready,
  isRouteAllowed,
  getHomePath,
  hasPermission,
  setPermissionContext,
  restorePermissionContext,
  ensurePermissionContext,
  clearPermissionContext,
  buildRouteRules,
  buildSidebarNav,
  buildAdminSubNav,
})
