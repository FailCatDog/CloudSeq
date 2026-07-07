import { CacheCode } from '@/constants/cacheCode'
import { STUDENT_NAV, TEACHER_NAV } from '@/constants/roleNav'

const unique = (items) => [...new Set(items)]

const navPaths = (nav) => nav.map((item) => item.to)

/** 各角色共享页面 */
export const SHARED_ROUTE_EXACT = ['/profile', '/messages']

export const SHARED_ROUTE_PREFIXES = ['/profile/']

/** 学生端固定路由（含项目空间子页与重定向路径） */
export const STUDENT_ROUTE_EXACT = unique([
  ...navPaths(STUDENT_NAV),
  '/workspace/dashboard',
  '/workspace/project/board',
  '/workspace/project/gantt',
  '/workspace/project/weekly',
  '/workspace/gantt',
  '/workspace/weekly',
  ...SHARED_ROUTE_EXACT,
])

/** 学生端动态路由前缀，如 /workspace/project/doc/:nodeId */
export const STUDENT_ROUTE_PREFIXES = unique([
  '/workspace/project/doc/',
  '/workspace/project/sheet/',
  ...SHARED_ROUTE_PREFIXES,
])

/** 教师端固定路由 */
export const TEACHER_ROUTE_EXACT = unique([...navPaths(TEACHER_NAV), ...SHARED_ROUTE_EXACT])

/** 教师端前缀（预留后续子路由） */
export const TEACHER_ROUTE_PREFIXES = unique(['/teaching/', ...SHARED_ROUTE_PREFIXES])

/**
 * 角色 → 允许访问路径配置（登录后写入访问容器）
 * exact: 完整路径白名单
 * prefixes: 前缀白名单（用于带参数的子路由）
 */
export const ROLE_ROUTE_ACCESS = {
  [CacheCode.USER_ROLE_TEACHER]: {
    exact: TEACHER_ROUTE_EXACT,
    prefixes: TEACHER_ROUTE_PREFIXES,
    home: '/teaching/dashboard',
  },
  [CacheCode.USER_ROLE_STUDENT]: {
    exact: STUDENT_ROUTE_EXACT,
    prefixes: STUDENT_ROUTE_PREFIXES,
    home: '/workspace/dashboard',
  },
}

export const AUTH_ROUTE_PREFIXES = ['/auth', '/login']
