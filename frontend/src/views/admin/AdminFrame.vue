<template>
  <div class="admin-frame">
    <div class="admin-frame__content">
      <router-view />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.admin-frame {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  min-width: 0;
}

.admin-frame__content {
  min-width: 0;
}
</style>
