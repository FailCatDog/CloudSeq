<template>
  <div v-if="!showShell" class="auth-only">
    <RouterView />
  </div>

  <div v-else class="wb-app">
    <aside class="wb-sidebar" :class="{ expanded: sidebarExpanded }" id="sidebar">
      <RouterLink to="/workspace/dashboard" class="wb-sidebar-logo" :aria-label="BRAND_NAME">
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

    <main class="wb-main" :class="{ 'wb-main--project': isProjectRoute }">
      <header v-if="!isProjectRoute" class="wb-topbar">
        <h1 class="wb-topbar-title">{{ pageTitle }}</h1>

        <div class="wb-topbar-actions">
          <button type="button" class="wb-focus-mode-btn" @click="openComingSoon">
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
              <RouterLink to="/profile/team" class="wb-user-menu-item" role="menuitem" @click="showUserMenu = false">
                我的小组
              </RouterLink>
              <RouterLink to="/profile/security" class="wb-user-menu-item" role="menuitem" @click="showUserMenu = false">
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
import { BRAND_NAME } from '@/constants/brand'
import { clearCollabTokenCache } from '@/utils/collabTokenCache'

const route = useRoute()
const router = useRouter()
const userMenuRef = ref(null)
const showUserMenu = ref(false)
const sidebarExpanded = ref(false)

const AUTH_STORAGE_KEY = 'authorization'
const USER_STORAGE_KEY = 'user'

const sidebarNav = [
  {
    to: '/workspace/dashboard',
    label: '工作台',
    ariaLabel: '工作台',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"></polygon><polyline points="2 17 12 22 22 17"></polyline><polyline points="2 12 12 17 22 12"></polyline></svg>`,
  },
  {
    to: '/workspace/project',
    label: '项目空间',
    ariaLabel: '项目空间',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>`,
  },
  {
    to: '/profile',
    label: '个人中心',
    ariaLabel: '个人中心',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>`,
  },
]

const pageTitleMap = {
  '/workspace/dashboard': '工作台',
  '/workspace/project/board': '数据看板',
  '/workspace/project/gantt': '任务甘特图',
  '/workspace/project/weekly': '周报',
  '/profile': '个人中心',
  '/profile/team': '我的小组',
  '/profile/security': '安全设置',
}

const showShell = computed(() => route.path !== '/auth')
const isDashboardRoute = computed(() => route.path === '/workspace/dashboard')
const isProjectRoute = computed(() => route.path.startsWith('/workspace/project'))

const pageContentClass = computed(() => ({
  'wb-page-content--inner': !isDashboardRoute.value && !isProjectRoute.value,
  'wb-page-content--project': isProjectRoute.value,
}))

const pageTitle = computed(() => {
  if (route.name === 'project-doc') {
    return '项目文档'
  }
  if (pageTitleMap[route.path]) {
    return pageTitleMap[route.path]
  }
  if (route.path.startsWith('/workspace/project')) {
    return '项目空间'
  }
  if (route.path.startsWith('/workspace')) {
    return '工作台'
  }
  return '个人中心'
})

const getStorageUser = () => {
  const raw = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

const storageUser = getStorageUser()
const displayName = computed(() => storageUser?.realName || storageUser?.nickName || storageUser?.username || 'User')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const avatarUrl = computed(() => storageUser?.avatarUrl || '')

const isNavActive = (to) => {
  if (to === '/workspace/dashboard') {
    return route.path === '/workspace/dashboard'
  }
  if (to === '/workspace/project') {
    return route.path.startsWith('/workspace/project')
  }
  if (to === '/profile') {
    return route.path.startsWith('/profile')
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
