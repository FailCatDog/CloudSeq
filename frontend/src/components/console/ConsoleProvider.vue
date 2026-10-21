<template>
  <n-config-provider :theme-overrides="consoleThemeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-dialog-provider>
      <n-message-provider>
        <div class="console-root">
          <slot />
        </div>
      </n-message-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup>
import { dateZhCN, zhCN } from 'naive-ui'
import { consoleThemeOverrides } from '@/constants/naiveTheme'
</script>
