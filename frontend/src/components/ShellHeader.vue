<template>
  <header class="topbar">
    <div>
      <p class="eyebrow">{{ BRAND_NAME }}</p>
      <h2>{{ title }}</h2>
      <p class="topbar-copy">{{ description }}</p>
    </div>

    <div class="topbar-actions">
      <button class="ghost-btn" type="button">查看说明</button>
      <button class="primary-btn" type="button">发起审批</button>
    </div>
  </header>
</template>

<script setup>
import { BRAND_NAME } from '@/constants/brand'

defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
})
</script>
