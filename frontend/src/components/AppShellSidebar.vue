<template>
  <aside class="sidebar">
    <div class="brand-block">
      <div class="brand-mark">
        <BrandLogo :size="40" />
      </div>
      <div>
        <p class="eyebrow">{{ BRAND_TAGLINE }}</p>
        <h1>{{ BRAND_FULL_NAME }}</h1>
      </div>
    </div>

    <p class="sidebar-copy">
      面向学生、组长与教师的项目管理工作台，围绕组队、选题、任务与文档协作展开。
    </p>

    <nav class="nav-list">
      <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link">
        <span>{{ item.label }}</span>
        <small>{{ item.desc }}</small>
      </RouterLink>
    </nav>

    <section class="status-panel">
      <p class="eyebrow">当前阶段</p>
      <strong>{{ statusTitle }}</strong>
      <span>{{ statusDesc }}</span>
    </section>
  </aside>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import BrandLogo from '@/components/BrandLogo.vue'
import { BRAND_FULL_NAME, BRAND_NAME, BRAND_TAGLINE } from '@/constants/brand'

defineProps({
  navItems: {
    type: Array,
    required: true,
  },
  statusTitle: {
    type: String,
    required: true,
  },
  statusDesc: {
    type: String,
    required: true,
  },
})
</script>
