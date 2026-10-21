import { CacheCode } from '@/constants/cacheCode'
import { AUTH_ROUTE_PREFIXES, SHARED_ROUTE_EXACT, SHARED_ROUTE_PREFIXES } from '@/constants/roleRoutes'

/** 页面 path → 组件（唯一前端组件映射，路由结构由 RBAC menus 驱动） */
export const PAGE_LOADERS = {
  '/workspace/dashboard': () => import('@/views/student/dashboard/index.vue'),
  '/prepare': () => import('@/views/student/prepare/index.vue'),
  '/workspace/project/board': () => import('@/views/student/project/board/index.vue'),
  '/workspace/project/gantt': () => import('@/views/student/gantt/index.vue'),
  '/workspace/project/weekly': () => import('@/views/student/weekly/index.vue'),
  '/teaching/dashboard': () => import('@/views/teacher/dashboard/index.vue'),
  '/teaching/courses': () => import('@/views/teacher/courses/index.vue'),
  '/teaching/students': () => import('@/views/teacher/students/index.vue'),
  '/teaching/approvals': () => import('@/views/teacher/approvals/index.vue'),
  '/teaching/teams': () => import('@/views/teacher/teams/index.vue'),
  '/teaching/reports': () => import('@/views/teacher/reports/index.vue'),
  '/admin/dashboard': () => import('@/views/admin/dashboard/index.vue'),
  '/admin/users': () => import('@/views/admin/users/index.vue'),
  '/admin/dicts': () => import('@/views/admin/dicts/index.vue'),
  '/admin/roles': () => import('@/views/admin/roles/index.vue'),
  '/admin/menus': () => import('@/views/admin/menus/index.vue'),
  '/profile': () => import('@/views/common/profile/index.vue'),
  '/messages': () => import('@/views/common/messages/index.vue'),
}

/** 区段 layout */
export const SECTION_LAYOUTS = {
  workspace: () => import('@/views/student/index.vue'),
  teaching: () => import('@/views/teacher/index.vue'),
  admin: () => import('@/views/admin/index.vue'),
}

export const NESTED_LAYOUTS = {
  'workspace/project': () => import('@/views/student/project/ProjectLayout.vue'),
}

/** 不在菜单树中、但随父级 PREFIX 授权的扩展路由 */
export const ROUTE_EXTENSIONS = [
  {
    path: '/workspace/project/doc/:nodeId',
    name: 'project-doc',
    requiresPrefix: '/workspace/project/',
    meta: { title: '项目文档' },
    component: () => import('@/views/student/project/doc/index.vue'),
  },
  {
    path: '/workspace/project/sheet/:nodeId',
    name: 'project-sheet',
    requiresPrefix: '/workspace/project/',
    meta: { title: '项目表格' },
    component: () => import('@/views/student/project/sheet/index.vue'),
  },
  {
    path: '/workspace/gantt',
    redirect: '/workspace/project/gantt',
    requiresPrefix: '/workspace/project/',
  },
  {
    path: '/workspace/weekly',
    redirect: '/workspace/project/weekly',
    requiresPrefix: '/workspace/project/',
  },
  {
    path: '/profile/team',
    redirect: '/prepare',
    requiresExact: '/profile',
  },
  {
    path: '/profile/security',
    redirect: '/profile',
    requiresExact: '/profile',
  },
]

export const SECTION_PREFIXES = ['workspace', 'teaching', 'admin']

export const normalizeRoutePath = (path) => {
  if (!path) return '/'
  const base = path.split('?')[0].split('#')[0]
  if (base.length > 1 && base.endsWith('/')) return base.slice(0, -1)
  return base
}

export const flattenMenuNodes = (menus, acc = []) => {
  for (const menu of menus || []) {
    if (menu?.path) acc.push(menu)
    if (menu?.children?.length) flattenMenuNodes(menu.children, acc)
  }
  return acc
}

export const routeNameFromPath = (path) => `dyn${path.replace(/[/:]/g, '-')}`

export const resolveSectionInfo = (fullPath) => {
  const segments = fullPath.split('/').filter(Boolean)
  if (segments.length === 0) return null
  if (segments[0] === 'workspace' && segments[1] === 'project') {
    return {
      section: 'workspace',
      nested: 'workspace/project',
      relative: segments.slice(2).join('/'),
    }
  }
  if (SECTION_PREFIXES.includes(segments[0])) {
    return {
      section: segments[0],
      nested: null,
      relative: segments.slice(1).join('/'),
    }
  }
  return {
    section: null,
    nested: null,
    relative: segments.join('/'),
  }
}

export const isPrefixAuthorized = (prefixes, requiredPrefix) => {
  const normalized = normalizeRoutePath(requiredPrefix)
  const withSlash = normalized.endsWith('/') ? normalized : `${normalized}/`
  return prefixes.some((prefix) => {
    const p = normalizeRoutePath(prefix)
    const pWithSlash = p.endsWith('/') ? p : `${p}/`
    return pWithSlash === withSlash || pWithSlash.startsWith(withSlash) || withSlash.startsWith(pWithSlash)
  })
}

export const collectInjectedPaths = (menus, exact, prefixes) => {
  const paths = new Set(SHARED_ROUTE_EXACT)
  for (const menu of flattenMenuNodes(menus)) {
    paths.add(normalizeRoutePath(menu.path))
  }
  for (const extension of ROUTE_EXTENSIONS) {
    if (extension.requiresPrefix && isPrefixAuthorized(prefixes, extension.requiresPrefix)) {
      paths.add(normalizeRoutePath(extension.path))
      continue
    }
    if (extension.requiresExact && exact.includes(normalizeRoutePath(extension.requiresExact))) {
      paths.add(normalizeRoutePath(extension.path))
    }
  }
  return [...paths]
}

export const buildMenuMetaMap = (menus) => {
  const map = new Map()
  for (const menu of flattenMenuNodes(menus)) {
    map.set(normalizeRoutePath(menu.path), menu)
  }
  return map
}

export const buildLeafRoute = (fullPath, menuMeta, extension) => {
  const normalized = normalizeRoutePath(fullPath)
  const info = resolveSectionInfo(normalized)
  const loader = extension?.component || PAGE_LOADERS[normalized]
  if (!loader && !extension?.redirect) return null

  const title = extension?.meta?.title || menuMeta?.menuName || normalized
  return {
    path: info.relative,
    name: extension?.name || routeNameFromPath(normalized),
    component: loader,
    redirect: extension?.redirect,
    meta: {
      title,
      fullPath: normalized,
      dashboard: normalized.endsWith('/dashboard'),
      section: info.section,
    },
  }
}

export const buildDynamicChildRoutes = (menus, exact, prefixes) => {
  const menuMetaMap = buildMenuMetaMap(menus)
  const injectedPaths = collectInjectedPaths(menus, exact, prefixes)
  const topLevel = []
  const sectionChildren = Object.fromEntries(SECTION_PREFIXES.map((key) => [key, []]))
  const nestedChildren = { 'workspace/project': [] }

  for (const fullPath of injectedPaths) {
    const menuMeta = menuMetaMap.get(fullPath)
    const extension = ROUTE_EXTENSIONS.find((item) => normalizeRoutePath(item.path) === fullPath)
    const leaf = buildLeafRoute(fullPath, menuMeta, extension)
    if (!leaf) continue

    const info = resolveSectionInfo(fullPath)
    if (!info.section) {
      topLevel.push(leaf)
      continue
    }
    if (info.nested) {
      nestedChildren[info.nested].push(leaf)
      continue
    }
    sectionChildren[info.section].push(leaf)
  }

  const sectionRoutes = []
  for (const section of SECTION_PREFIXES) {
    const children = [...sectionChildren[section]]
    const nestedKey = section === 'workspace' ? 'workspace/project' : null
    if (nestedKey && nestedChildren[nestedKey].length) {
      children.push({
        path: 'project',
        name: `dyn-layout-${nestedKey.replace('/', '-')}`,
        component: NESTED_LAYOUTS[nestedKey],
        children: nestedChildren[nestedKey],
      })
    }
    if (!children.length) continue
    sectionRoutes.push({
      path: section,
      name: `dyn-layout-${section}`,
      component: SECTION_LAYOUTS[section],
      meta: { section, title: section === 'admin' ? '管理端' : section === 'teaching' ? '教师端' : '工作台' },
      children,
    })
  }

  return [...sectionRoutes, ...topLevel]
}

/** 侧栏父级 → 默认子页面（与 layout redirect 一致） */
export const LAYOUT_ENTRY_TARGETS = {
  '/workspace/project': '/workspace/project/board',
}

export const buildSectionSubNav = (menus, sectionPrefix) => {
  const prefix = normalizeRoutePath(sectionPrefix)
  return flattenMenuNodes(menus)
    .map((menu) => ({
      path: normalizeRoutePath(menu.path),
      menu,
    }))
    .filter(({ path }) => path.startsWith(`${prefix}/`))
    .sort((a, b) => (a.menu.sort ?? 0) - (b.menu.sort ?? 0) || a.path.localeCompare(b.path))
    .map(({ path, menu }) => ({
      to: path,
      label: menu.menuName,
      ariaLabel: menu.menuName,
    }))
}

export const AUTH_PATHS = AUTH_ROUTE_PREFIXES
export const SHARED_EXACT = SHARED_ROUTE_EXACT
export const SHARED_PREFIXES = SHARED_ROUTE_PREFIXES
export const MENU_TYPE_DIR = CacheCode.MENU_TYPE_DIR
export const MENU_TYPE_MENU = CacheCode.MENU_TYPE_MENU
