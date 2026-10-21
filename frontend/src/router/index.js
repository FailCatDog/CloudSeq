import { createRouter, createWebHistory } from 'vue-router'
import { buildPageTitle } from '@/constants/brand'
import { resolveNavigationGuard } from '@/utils/roleHome'
import AppLayout from '../layout/AppLayout.vue'
import AuthView from '../views/common/auth/index.vue'

/** 静态壳路由：登录页 + AppLayout，业务路由由 RBAC menus 动态注入 */
const routes = [
  {
    path: '/',
    name: 'app-layout',
    component: AppLayout,
    children: [],
  },
  { path: '/auth', name: 'auth', component: AuthView, meta: { title: '登录' } },
  { path: '/auth/register', redirect: '/auth' },
  { path: '/auth/reset', redirect: '/auth' },
  { path: '/login', redirect: '/auth' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const redirect = await resolveNavigationGuard(to.path)
  if (redirect && redirect !== to.path) {
    return redirect
  }
  if (to.matched.length === 0 && to.path !== '/auth') {
    return { ...to, replace: true }
  }
})

router.afterEach((to) => {
  let title = to.meta?.title
  if (to.path === '/profile') {
    if (to.query.tab === 'team') title = '我的小组'
    else title = title || '个人中心'
  }
  document.title = buildPageTitle(title || 'CloudSeq')
})

export default router
