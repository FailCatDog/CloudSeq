<template>
  <ThemeProvider>
    <RouterView />
  </ThemeProvider>
</template>

<script setup>
import { RouterView } from 'vue-router'
import ThemeProvider from '@/components/ThemeProvider.vue'
</script>
