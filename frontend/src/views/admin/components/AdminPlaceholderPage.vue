<template>
  <div class="console-page admin-placeholder">
    <n-card :bordered="false">
      <div class="admin-placeholder__head">
        <div>
          <p class="admin-placeholder__eyebrow">系统管理</p>
          <h3>{{ title }}</h3>
          <p class="admin-placeholder__desc">{{ description }}</p>
        </div>
        <n-tag round type="warning" size="small">开发中</n-tag>
      </div>

      <ul v-if="features.length" class="admin-placeholder__features">
        <li v-for="feature in features" :key="feature">{{ feature }}</li>
      </ul>

      <n-empty v-else size="large" description="功能即将上线" />
    </n-card>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  features: { type: Array, default: () => [] },
})
</script>

<style lang="scss" scoped>
.admin-placeholder__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.admin-placeholder__eyebrow {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: var(--wb-text-muted);
}

.admin-placeholder h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--wb-text-primary);
}

.admin-placeholder__desc {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--wb-text-secondary);
}

.admin-placeholder__features {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.admin-placeholder__features li {
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--wb-search-bg);
  color: var(--wb-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.admin-placeholder__features li::before {
  content: '·';
  margin-right: 8px;
  color: var(--wb-purple);
  font-weight: 700;
}
</style>
