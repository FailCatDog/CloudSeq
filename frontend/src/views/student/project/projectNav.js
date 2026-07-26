const DOC_ROUTE_NAMES = new Set(['project-doc', 'project-sheet'])

const routeNameFromPath = (path) => `dyn${String(path).replace(/[/:]/g, '-')}`

/**
 * Resolve which project-space submenu path should show the active style.
 * Dynamic routes use names like `dyn-workspace-project-board`, not `project-board`.
 */
export const resolveActiveProjectNavPath = (route, navPaths) => {
  if (!route || DOC_ROUTE_NAMES.has(route.name)) return null

  const fullPath = route.meta?.fullPath || route.path || ''
  const paths = navPaths || []

  const byPath = paths.find(
    (to) => fullPath === to || fullPath.startsWith(`${to}/`),
  )
  if (byPath) return byPath

  const byName = paths.find((to) => route.name === routeNameFromPath(to))
  return byName ?? null
}
