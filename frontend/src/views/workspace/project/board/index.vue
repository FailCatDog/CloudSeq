<template>
  <section class="ps-board">
    <div class="ps-board-stats">
      <div v-for="stat in stats" :key="stat.label" class="ps-stat-card">
        <div class="ps-stat-num">{{ stat.value }}</div>
        <div class="ps-stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <article class="ps-board-card">
      <div class="ps-board-card-head">
        <h2>数据看板</h2>
        <span class="ps-board-badge">概览</span>
      </div>
      <p class="ps-board-desc">这里展示项目整体进度、完成率和风险提示。</p>
    </article>
  </section>
</template>

<script setup>
const stats = [
  { value: '—', label: '计划任务' },
  { value: '—', label: '内容节点' },
  { value: '—', label: '小组成员' },
  { value: '—', label: '已完成任务' },
]
</script>
