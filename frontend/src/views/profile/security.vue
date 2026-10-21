<template>
  <section class="security-page">
    <div class="security-panel">
      <p class="section-label">安全设置</p>
      <h3>保护账号与登录状态</h3>
      <p class="security-description">在这里管理密码、登录提醒和设备安全，减少账号风险。</p>

      <div class="security-list">
        <article v-for="item in securityItems" :key="item.title" class="security-item">
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.desc }}</p>
          </div>
          <button type="button" class="secondary-btn">管理</button>
        </article>
      </div>
    </div>

    <aside class="security-panel security-panel--compact">
      <p class="section-label">建议</p>
      <h4>保持账号安全</h4>
      <ul class="security-tips">
        <li>定期更换密码。</li>
        <li>优先开启登录提醒。</li>
        <li>定期检查登录设备。</li>
      </ul>
    </aside>
  </section>
</template>

<script setup>
const securityItems = [
  { title: '登录密码', desc: '上次修改时间：3 个月前。' },
  { title: '登录提醒', desc: '新设备登录时发送提醒。' },
  { title: '登录设备', desc: '查看当前已登录设备并可退出。' },
]
</script>

<style scoped>
.security-page {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.6fr);
  gap: 18px;
}

.security-panel {
  padding: 22px;
  border-radius: 24px;
  border: 1px solid rgba(124, 139, 166, 0.16);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 48px rgba(18, 28, 45, 0.06);
}

.security-panel h3,
.security-panel h4 {
  margin: 0;
  color: #172033;
}

.security-panel h3 {
  font-size: 28px;
  line-height: 1.2;
}

.security-description,
.security-item p,
.security-tips {
  color: #67758f;
  font-size: 13px;
  line-height: 1.7;
}

.security-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #f7f9fc;
}

.security-item strong {
  color: #21324d;
  font-size: 14px;
}

.security-tips {
  margin: 14px 0 0;
  padding-left: 18px;
}

.section-label {
  margin: 0 0 4px;
  color: #6a7890;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.secondary-btn {
  height: 38px;
  border-radius: 12px;
  padding: 0 14px;
  color: #33465f;
  border: 1px solid rgba(124, 139, 166, 0.22);
  background: #ffffff;
  font-size: 14px;
  font-weight: 560;
  cursor: pointer;
}

@media (max-width: 960px) {
  .security-page {
    grid-template-columns: 1fr;
  }
}
</style>
