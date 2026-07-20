<template>
  <nav class="admin-subnav" aria-label="系统管理导航">
    <RouterLink
      v-for="item in subNav"
      :key="item.to"
      :to="item.to"
      class="admin-subnav__item"
      :class="{ 'admin-subnav__item--active': isActive(item.to) }"
      :aria-label="item.ariaLabel"
    >
      <span class="admin-subnav__icon" v-html="item.icon" />
      <span class="admin-subnav__label">{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { buildAdminSubNav, usePermissionStore } from '@/stores/permissionStore'

const route = useRoute()
const permissionStore = usePermissionStore()

const subNav = computed(() => buildAdminSubNav(permissionStore.menus()))

const isActive = (to) => route.path === to || route.path.startsWith(`${to}/`)
</script>

<style scoped>
.admin-subnav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px;
  border-radius: 16px;
  background: var(--wb-search-bg);
  border: 1px solid var(--wb-search-border);
}

.admin-subnav__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 12px;
  color: var(--wb-text-secondary);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.18s ease, color 0.18s ease;
}

.admin-subnav__item:hover {
  color: var(--wb-text-primary);
  background: var(--wb-card-bg);
}

.admin-subnav__item--active {
  color: var(--wb-purple);
  background: var(--wb-card-bg);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.admin-subnav__icon {
  display: grid;
  place-items: center;
  width: 18px;
  height: 18px;
}

.admin-subnav__icon :deep(svg) {
  display: block;
}
</style>
