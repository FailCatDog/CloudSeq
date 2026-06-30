<script setup>
import { computed, useAttrs } from 'vue'

defineOptions({ inheritAttrs: false })

const props = defineProps({
  /** Root element tag */
  tag: {
    type: String,
    default: 'div',
  },
  /** Scroll direction */
  axis: {
    type: String,
    default: 'y',
    validator: (value) => ['x', 'y', 'both'].includes(value),
  },
  /** Narrower scrollbar for dense panels */
  thin: {
    type: Boolean,
    default: false,
  },
  /** Subtle thumb until hover */
  hoverReveal: {
    type: Boolean,
    default: false,
  },
  /** Fill flex parent and allow internal scroll */
  flex: {
    type: Boolean,
    default: false,
  },
})

const attrs = useAttrs()

const rootClass = computed(() => [
  'app-scroll-area',
  'app-scrollbar',
  `app-scroll-area--${props.axis}`,
  props.thin && 'app-scrollbar--thin',
  props.hoverReveal && 'app-scrollbar--hover-reveal',
  props.flex && 'app-scroll-area--flex',
  attrs.class,
])
</script>

<template>
  <component :is="tag" v-bind="{ ...attrs, class: undefined }" :class="rootClass">
    <slot />
  </component>
</template>
