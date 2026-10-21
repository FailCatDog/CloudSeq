<template>
  <RouterView />
</template>

<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { initDictStore } from '@/stores/dictStore'

onMounted(() => {
  initDictStore().catch(() => {})
})
</script>
