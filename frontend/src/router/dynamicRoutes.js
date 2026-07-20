import { buildDynamicChildRoutes, routeNameFromPath } from '@/router/routeRegistry'

const APP_LAYOUT_NAME = 'app-layout'
const ROOT_REDIRECT_NAME = 'dyn-root-redirect'

let injectedRouteNames = []
let lastSyncKey = ''

const buildSyncKey = (context) => JSON.stringify({
  menus: context.menus,
  home: context.home,
  exact: context.exact,
  prefixes: context.prefixes,
})

export const resetDynamicRoutes = (router) => {
  for (const name of injectedRouteNames) {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  }
  injectedRouteNames = []
  lastSyncKey = ''
}

export const syncDynamicRoutesFromContext = async (router, context) => {
  const syncKey = buildSyncKey(context)
  if (syncKey === lastSyncKey) return

  resetDynamicRoutes(router)

  const home = context.home || '/profile'
  router.addRoute(APP_LAYOUT_NAME, {
    path: '',
    name: ROOT_REDIRECT_NAME,
    redirect: home,
  })
  injectedRouteNames.push(ROOT_REDIRECT_NAME)

  const childRoutes = buildDynamicChildRoutes(context.menus, context.exact, context.prefixes)
  for (const route of childRoutes) {
    router.addRoute(APP_LAYOUT_NAME, route)
    injectedRouteNames.push(route.name)
    if (route.children?.length) {
      for (const child of route.children) {
        if (child.name) injectedRouteNames.push(child.name)
        if (child.children?.length) {
          for (const nested of child.children) {
            if (nested.name) injectedRouteNames.push(nested.name)
          }
        }
      }
    }
  }

  lastSyncKey = syncKey
}

export const ensureDynamicRoutesSynced = async (context) => {
  const router = (await import('@/router')).default
  await syncDynamicRoutesFromContext(router, context)
}

export const getInjectedRouteNames = () => [...injectedRouteNames]

export const routeNameForPath = routeNameFromPath
