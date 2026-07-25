<template>
  <n-config-provider
    :theme="naivePreset"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <slot />
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { darkTheme, dateZhCN, zhCN } from 'naive-ui'
import { darkThemeOverrides, lightThemeOverrides } from '@/constants/naiveTheme'
import { useTheme } from '@/composables/useTheme'

const { isDark } = useTheme()
const naivePreset = computed(() => (isDark.value ? darkTheme : null))
const themeOverrides = computed(() =>
  isDark.value ? darkThemeOverrides : lightThemeOverrides,
)
</script>
