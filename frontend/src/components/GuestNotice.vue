<template>
  <section class="guest-state" aria-labelledby="guest-state-title">
    <p class="guest-state__eyebrow">个人中心</p>
    <h3 id="guest-state-title">{{ title }}</h3>
    <p class="guest-state__text">{{ description }}</p>
    <RouterLink to="/auth" class="guest-state__action">去登录</RouterLink>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  title: {
    type: String,
    default: '暂无个人信息',
  },
  description: {
    type: String,
    default: '请先登录后再查看你的个人资料。',
  },
})
</script>

<style lang="scss" scoped>
.guest-state {
  display: grid;
  gap: 10px;
  place-items: center;
  text-align: center;
  min-height: 320px;
  padding: 32px 24px;
  border: 1px solid rgba(124, 139, 166, 0.16);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 48px rgba(18, 28, 45, 0.06);
}

.guest-state h3,
.guest-state p {
  margin: 0;
}

.guest-state__eyebrow {
  color: #6a7890;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.guest-state h3 {
  color: #172033;
  font-size: 22px;
  line-height: 1.2;
}

.guest-state__text {
  max-width: 30ch;
  color: #66758f;
  font-size: 14px;
  line-height: 1.6;
}

.guest-state__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  height: 40px;
  margin-top: 8px;
  padding: 0 16px;
  border-radius: 12px;
  color: #ffffff;
  background: linear-gradient(145deg, #304b8c 0%, #2a447f 100%);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}
</style>
