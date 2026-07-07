<template>
  <div v-if="!showShell" class="auth-only">
    <RouterView />
  </div>

  <div v-else class="wb-app">
    <aside class="wb-sidebar" :class="{ expanded: sidebarExpanded }" id="sidebar">
      <RouterLink :to="homePath" class="wb-sidebar-logo" :aria-label="BRAND_NAME">
        <BrandLogo :size="40" src="/logo-40.png" />
        <span class="wb-sidebar-logo__label">{{ BRAND_NAME }}</span>
      </RouterLink>

      <nav class="wb-sidebar-nav" aria-label="主导航">
        <RouterLink
          v-for="item in sidebarNav"
          :key="item.to"
          :to="item.to"
          class="wb-nav-item"
          :class="{ active: isNavActive(item.to) }"
          :aria-label="item.ariaLabel"
        >
          <span class="wb-nav-icon" v-html="item.icon" />
          <span class="wb-nav-label">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="wb-sidebar-footer">
        <button
          type="button"
          class="wb-sidebar-toggle"
          :aria-expanded="sidebarExpanded"
          aria-controls="sidebar"
          :aria-label="sidebarExpanded ? 'Collapse sidebar' : 'Expand sidebar'"
          @click="sidebarExpanded = !sidebarExpanded"
        >
          <svg class="toggle-icon toggle-icon-expand" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <line x1="9" y1="3" x2="9" y2="21" />
            <polyline points="14 8 18 12 14 16" />
          </svg>
          <svg class="toggle-icon toggle-icon-collapse" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <line x1="9" y1="3" x2="9" y2="21" />
            <polyline points="18 8 14 12 18 16" />
          </svg>
        </button>
        <button type="button" class="wb-sidebar-add" aria-label="Add" @click="openComingSoon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
      </div>
    </aside>

    <main class="wb-main" :class="{ 'wb-main--project': isProjectRoute, 'wb-main--profile': isProfileRoute }">
      <header v-if="!isProjectRoute" class="wb-topbar">
        <h1 class="wb-topbar-title">{{ pageTitle }}</h1>

        <div class="wb-topbar-actions">
          <CourseSelectDropdown v-if="isTeacherUser" />

          <button v-if="!isTeacherUser" type="button" class="wb-focus-mode-btn" @click="openComingSoon">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            DEEP FOCUS MODE
          </button>

          <div class="wb-user-avatar" ref="userMenuRef">
            <button type="button" class="wb-user-avatar-btn" :aria-expanded="showUserMenu" aria-haspopup="menu" @click="toggleUserMenu">
              <img v-if="avatarUrl" :src="avatarUrl" alt="User avatar" />
              <span v-else>{{ avatarText }}</span>
            </button>
            <span class="wb-online-dot" />
            <div v-if="showUserMenu" class="wb-user-menu" role="menu" aria-label="用户菜单">
              <RouterLink to="/profile" class="wb-user-menu-item" role="menuitem" @click="showUserMenu = false">
                个人中心
              </RouterLink>
              <RouterLink to="/profile?tab=team" class="wb-user-menu-item" role="menuitem" @click="showUserMenu = false">
                我的小组
              </RouterLink>
              <RouterLink to="/profile?tab=security" class="wb-user-menu-item" role="menuitem" @click="showUserMenu = false">
                安全设置
              </RouterLink>
              <button type="button" class="wb-user-menu-item" role="menuitem" @click="logout">
                退出登录
              </button>
            </div>
          </div>
        </div>
      </header>

      <div class="wb-page-content app-scrollbar" :class="pageContentClass">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import BrandLogo from '@/components/BrandLogo.vue'
import CourseSelectDropdown from '@/components/CourseSelectDropdown.vue'
import { BRAND_NAME } from '@/constants/brand'
import { STUDENT_NAV, TEACHER_NAV } from '@/constants/roleNav'
import { clearCollabTokenCache } from '@/utils/collabTokenCache'
import { clearRoleAccess, getRoleHomePath, getUserFromStorage, isTeacherRole } from '@/utils/roleHome'

const route = useRoute()
const router = useRouter()
const userMenuRef = ref(null)
const showUserMenu = ref(false)
const sidebarExpanded = ref(true)

const AUTH_STORAGE_KEY = 'authorization'
const USER_STORAGE_KEY = 'user'

const storageUser = getUserFromStorage()
const isTeacherUser = computed(() => isTeacherRole(storageUser?.role))
const isTeachingRoute = computed(() => route.path.startsWith('/teaching'))

const sidebarNav = computed(() => (isTeacherUser.value ? TEACHER_NAV : STUDENT_NAV))

const homePath = computed(() => getRoleHomePath(storageUser?.role))

const pageTitleMap = {
  '/workspace/dashboard': '工作台',
  '/workspace/project/board': '数据看板',
  '/workspace/project/gantt': '任务甘特图',
  '/workspace/project/weekly': '周报',
  '/teaching/dashboard': '教学工作台',
  '/teaching/courses': '课号管理',
  '/teaching/approvals': '选题审批',
  '/teaching/teams': '小组总览',
  '/teaching/reports': '周报审阅',
  '/profile': '个人中心',
}

const profileTabTitleMap = {
  team: '我的小组',
  security: '安全设置',
}

const showShell = computed(() => route.path !== '/auth')
const isDashboardRoute = computed(() => {
  return route.path === '/workspace/dashboard' || route.path === '/teaching/dashboard'
})
const isProjectRoute = computed(() => route.path.startsWith('/workspace/project'))

const isProfileRoute = computed(() => route.path === '/profile' || route.path.startsWith('/profile/'))

const pageContentClass = computed(() => ({
  'wb-page-content--inner': !isDashboardRoute.value && !isProjectRoute.value && !isProfileRoute.value,
  'wb-page-content--project': isProjectRoute.value,
  'wb-page-content--profile-host': isProfileRoute.value,
}))

const pageTitle = computed(() => {
  if (route.name === 'project-doc') {
    return '项目文档'
  }
  if (route.name === 'project-sheet') {
    return '项目表格'
  }
  if (route.path === '/profile' || route.path.startsWith('/profile/')) {
    const tab = route.query.tab
    if (typeof tab === 'string' && profileTabTitleMap[tab]) {
      return profileTabTitleMap[tab]
    }
    return pageTitleMap['/profile']
  }
  if (pageTitleMap[route.path]) {
    return pageTitleMap[route.path]
  }
  if (route.path.startsWith('/teaching')) {
    return route.meta.title || '教师端'
  }
  if (route.path.startsWith('/workspace/project')) {
    return '项目空间'
  }
  if (route.path.startsWith('/workspace')) {
    return '工作台'
  }
  return '个人中心'
})

const getStorageUser = () => getUserFromStorage()

const storageUserRef = computed(() => getStorageUser())
const displayName = computed(() => storageUserRef.value?.realName || storageUserRef.value?.nickName || storageUserRef.value?.username || 'User')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const avatarUrl = computed(() => storageUserRef.value?.avatarUrl || '')

const isNavActive = (to) => {
  if (to === '/workspace/dashboard' || to === '/teaching/dashboard') {
    return route.path === to
  }
  if (to === '/workspace/project') {
    return route.path.startsWith('/workspace/project')
  }
  if (to === '/profile') {
    return route.path === '/profile' || route.path.startsWith('/profile/')
  }
  return route.path === to || route.path.startsWith(`${to}/`)
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const closeUserMenu = (event) => {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target)) {
    showUserMenu.value = false
  }
}

const logout = async () => {
  showUserMenu.value = false
  clearCollabTokenCache()
  clearRoleAccess()
  localStorage.removeItem(AUTH_STORAGE_KEY)
  localStorage.removeItem(USER_STORAGE_KEY)
  sessionStorage.removeItem(AUTH_STORAGE_KEY)
  sessionStorage.removeItem(USER_STORAGE_KEY)
  await router.push('/auth')
}

const openComingSoon = () => {
  window.alert('开发中')
}

onMounted(() => {
  document.addEventListener('click', closeUserMenu)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeUserMenu)
})
</script>

<style scoped>
.auth-only {
  min-height: 100vh;
  min-height: 100dvh;
}
</style>
