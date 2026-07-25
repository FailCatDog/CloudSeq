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
          class="wb-theme-toggle"
          :aria-label="isDark ? '切换为浅色主题' : '切换为深色主题'"
          @click="toggleTheme"
        >
          <span class="wb-theme-toggle__icon" aria-hidden="true">{{ isDark ? '浅' : '深' }}</span>
          <span class="wb-theme-toggle__label">{{ isDark ? '浅色' : '深色' }}</span>
        </button>

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

        <div class="wb-sidebar-user" ref="userMenuRef">
          <div class="wb-sidebar-user-info">
            <button
              type="button"
              class="wb-sidebar-user-avatar"
              :aria-expanded="showUserMenu"
              aria-haspopup="menu"
              :aria-label="`${nickName} 账户菜单`"
              @click="toggleUserMenu"
            >
              <img v-if="avatarUrl" :src="avatarUrl" alt="" />
              <span v-else>{{ avatarText }}</span>
            </button>
            <span class="wb-sidebar-user-name">{{ nickName }}</span>
          </div>
          <div v-if="showUserMenu" class="wb-user-menu wb-user-menu--sidebar" role="menu" aria-label="用户菜单">
            <RouterLink to="/profile" class="wb-user-menu-item" role="menuitem" @click="showUserMenu = false">
              个人中心
            </RouterLink>
            <RouterLink
              v-if="!isStaffUser"
              to="/prepare"
              class="wb-user-menu-item"
              role="menuitem"
              @click="showUserMenu = false"
            >
              课程准备
            </RouterLink>
            <button type="button" class="wb-user-menu-item" role="menuitem" @click="logout">
              退出登录
            </button>
          </div>
        </div>
      </div>
    </aside>

    <main class="wb-main" :class="{ 'wb-main--project': isProjectRoute, 'wb-main--profile': isProfileRoute }">
      <AppScrollArea class="wb-page-content" :class="pageContentClass" axis="y" flex>
        <RouterView />
      </AppScrollArea>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import BrandLogo from '@/components/BrandLogo.vue'
import AppScrollArea from '@/components/AppScrollArea.vue'
import { useTheme } from '@/composables/useTheme'
import { BRAND_NAME } from '@/constants/brand'
import { buildSidebarNav, usePermissionStore } from '@/stores/permissionStore'
import { clearCollabTokenCache } from '@/utils/collabTokenCache'
import {
  clearPermissionContext,
  getHomePath,
  getUserFromStorage,
  isStaffRole,
} from '@/utils/roleHome'

const { isDark, toggleTheme } = useTheme()

const route = useRoute()
const router = useRouter()
const userMenuRef = ref(null)
const showUserMenu = ref(false)
const sidebarExpanded = ref(true)

const AUTH_STORAGE_KEY = 'authorization'
const USER_STORAGE_KEY = 'user'

const permissionStore = usePermissionStore()

const getStorageUser = () => getUserFromStorage()
const storageUserRef = computed(() => getStorageUser())

const isStaffUser = computed(() => isStaffRole(storageUserRef.value?.role))

const sidebarNav = computed(() => buildSidebarNav(permissionStore.menus()))

const homePath = computed(() => getHomePath())

const showShell = computed(() => route.path !== '/auth')
const isDashboardRoute = computed(() => route.meta?.dashboard === true)
const isProjectRoute = computed(() => route.path.startsWith('/workspace/project'))

const isProfileRoute = computed(() =>
  route.path === '/profile'
  || route.path.startsWith('/profile/')
  || route.path === '/prepare',
)

const pageContentClass = computed(() => ({
  'wb-page-content--inner': !isDashboardRoute.value && !isProjectRoute.value && !isProfileRoute.value,
  'wb-page-content--project': isProjectRoute.value,
  'wb-page-content--profile-host': isProfileRoute.value,
}))

const isNavActive = (to) => {
  if (to.endsWith('/dashboard')) {
    return route.path === to
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

const nickName = computed(() =>
  storageUserRef.value?.nickName
  || storageUserRef.value?.realName
  || storageUserRef.value?.username
  || '用户',
)
const avatarText = computed(() => nickName.value.slice(0, 1).toUpperCase())
const avatarUrl = computed(() => storageUserRef.value?.avatarUrl || '')

const logout = async () => {
  showUserMenu.value = false
  clearCollabTokenCache()
  await clearPermissionContext()
  localStorage.removeItem(AUTH_STORAGE_KEY)
  localStorage.removeItem(USER_STORAGE_KEY)
  sessionStorage.removeItem(AUTH_STORAGE_KEY)
  sessionStorage.removeItem(USER_STORAGE_KEY)
  await router.push('/auth')
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
