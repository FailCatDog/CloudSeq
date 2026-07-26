<template>
  <div class="console-page admin-dashboard">
    <section class="admin-dashboard__hero wb-card">
      <div>
        <p class="admin-dashboard__eyebrow">系统管理</p>
        <h2>管理控制台</h2>
        <p class="admin-dashboard__summary">
          管理平台用户、字典与 RBAC 权限配置，不涉及教学业务内容。修改权限后用户重新登录或刷新权限即可生效。
        </p>
      </div>
      <n-tag round type="info" size="small">系统管理</n-tag>
    </section>

    <section class="admin-dashboard__grid">
      <RouterLink
        v-for="module in modules"
        :key="module.to"
        :to="module.to"
        class="wb-card admin-module-card"
      >
        <span class="admin-module-card__icon" v-html="module.icon" />
        <div class="admin-module-card__body">
          <h3>{{ module.label }}</h3>
        </div>
        <span class="admin-module-card__arrow" aria-hidden="true">→</span>
      </RouterLink>
    </section>

    <n-card :bordered="false" class="admin-dashboard__note">
      <n-alert type="info" :bordered="false">
        模块入口来自当前账号 RBAC 菜单授权。若缺少页面，请在「菜单管理」中为 ADMIN 角色配置对应
        <code>/admin/*</code> 路由。
      </n-alert>
    </n-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { buildAdminSubNav, usePermissionStore } from '@/stores/permissionStore'

const permissionStore = usePermissionStore()

const modules = computed(() =>
  buildAdminSubNav(permissionStore.menus()).filter((item) => item.to !== '/admin/dashboard'),
)
</script>

<style lang="scss" scoped>
.admin-dashboard {
  gap: 20px;
}

.admin-dashboard__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px;
}

.admin-dashboard__eyebrow {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: var(--wb-text-muted);
}

.admin-dashboard__hero h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--wb-text-primary);
}

.admin-dashboard__summary {
  margin: 10px 0 0;
  max-width: 640px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--wb-text-secondary);
}

.admin-dashboard__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.admin-module-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.admin-module-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--wb-card-shadow-hover, var(--wb-card-shadow));
}

.admin-module-card__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--wb-purple-soft);
  color: var(--wb-purple);
  flex-shrink: 0;
}

.admin-module-card__body {
  flex: 1;
  min-width: 0;
}

.admin-module-card__body h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--wb-text-primary);
}

.admin-module-card__arrow {
  color: var(--wb-text-muted);
  font-size: 18px;
  line-height: 1;
}

.admin-dashboard__note code {
  padding: 2px 6px;
  border-radius: 6px;
  background: var(--wb-search-bg);
  font-size: 12px;
}
</style>
