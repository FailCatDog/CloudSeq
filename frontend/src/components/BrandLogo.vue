<template>
  <img
    class="brand-logo"
    :src="src || BRAND_LOGO_URL"
    :alt="alt || BRAND_NAME"
    :width="size"
    :height="size"
    decoding="async"
  />
</template>

<script setup>
import { BRAND_LOGO_URL, BRAND_NAME } from '@/constants/brand'

defineProps({
  size: {
    type: [Number, String],
    default: 40,
  },
  src: {
    type: String,
    default: '',
  },
  alt: {
    type: String,
    default: '',
  },
})
</script>

<style lang="scss" scoped>
.brand-logo {
  display: block;
  object-fit: contain;
  flex-shrink: 0;
}
</style>
