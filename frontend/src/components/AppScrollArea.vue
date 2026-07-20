<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, useAttrs, watch } from 'vue'

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
  /**
   * Overlay scrollbar that does not consume layout width.
   * Hides the native gutter and draws a floating thumb.
   */
  overlay: {
    type: Boolean,
    default: true,
  },
})

const attrs = useAttrs()
const viewportRef = ref(null)

const metrics = reactive({
  canY: false,
  canX: false,
  thumbY: { top: 0, height: 0 },
  thumbX: { left: 0, width: 0 },
})

let resizeObserver = null
let mutationObserver = null
let dragging = null

const shellClass = computed(() => [
  'app-scroll-area',
  props.flex && 'app-scroll-area--flex',
  props.overlay && 'app-scroll-area--overlay',
  props.overlay && props.thin && 'app-scroll-area--overlay-thin',
  props.overlay && props.hoverReveal && 'app-scroll-area--overlay-hover',
  attrs.class,
])

const viewportClass = computed(() => [
  'app-scroll-area__viewport',
  `app-scroll-area--${props.axis}`,
  !props.overlay && 'app-scrollbar',
  props.thin && !props.overlay && 'app-scrollbar--thin',
  props.hoverReveal && !props.overlay && 'app-scrollbar--hover-reveal',
  props.overlay && 'app-scroll-area__viewport--overlay',
])

const showRailY = computed(() => props.overlay && (props.axis === 'y' || props.axis === 'both') && metrics.canY)
const showRailX = computed(() => props.overlay && (props.axis === 'x' || props.axis === 'both') && metrics.canX)

const forwardedAttrs = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})

function updateMetrics() {
  const el = viewportRef.value
  if (!el || !props.overlay) return

  const { clientWidth, clientHeight, scrollWidth, scrollHeight, scrollTop, scrollLeft } = el
  const canY = scrollHeight > clientHeight + 1
  const canX = scrollWidth > clientWidth + 1
  metrics.canY = canY
  metrics.canX = canX

  if (canY) {
    const ratio = clientHeight / scrollHeight
    const height = Math.max(24, Math.round(clientHeight * ratio))
    const maxTop = clientHeight - height
    const top = maxTop <= 0 ? 0 : Math.round((scrollTop / (scrollHeight - clientHeight)) * maxTop)
    metrics.thumbY = { top, height }
  }

  if (canX) {
    const ratio = clientWidth / scrollWidth
    const width = Math.max(24, Math.round(clientWidth * ratio))
    const maxLeft = clientWidth - width
    const left = maxLeft <= 0 ? 0 : Math.round((scrollLeft / (scrollWidth - clientWidth)) * maxLeft)
    metrics.thumbX = { left, width }
  }
}

function onScroll() {
  if (dragging) return
  updateMetrics()
}

function startDrag(axis, event) {
  const el = viewportRef.value
  if (!el) return
  event.preventDefault()
  event.stopPropagation()

  const startClient = axis === 'y' ? event.clientY : event.clientX
  const startScroll = axis === 'y' ? el.scrollTop : el.scrollLeft
  dragging = { axis, startClient, startScroll }
  document.addEventListener('pointermove', onDragMove)
  document.addEventListener('pointerup', endDrag)
  document.addEventListener('pointercancel', endDrag)
}

function onDragMove(event) {
  const el = viewportRef.value
  if (!el || !dragging) return

  if (dragging.axis === 'y') {
    const maxScroll = el.scrollHeight - el.clientHeight
    const maxTop = el.clientHeight - metrics.thumbY.height
    if (maxScroll <= 0 || maxTop <= 0) return
    const delta = event.clientY - dragging.startClient
    el.scrollTop = dragging.startScroll + (delta / maxTop) * maxScroll
    updateMetrics()
    return
  }

  const maxScroll = el.scrollWidth - el.clientWidth
  const maxLeft = el.clientWidth - metrics.thumbX.width
  if (maxScroll <= 0 || maxLeft <= 0) return
  const delta = event.clientX - dragging.startClient
  el.scrollLeft = dragging.startScroll + (delta / maxLeft) * maxScroll
  updateMetrics()
}

function endDrag() {
  dragging = null
  document.removeEventListener('pointermove', onDragMove)
  document.removeEventListener('pointerup', endDrag)
  document.removeEventListener('pointercancel', endDrag)
  updateMetrics()
}

function onRailYPointerDown(event) {
  if (event.target.closest('.app-scroll-area__thumb')) return
  const el = viewportRef.value
  if (!el) return
  const rect = event.currentTarget.getBoundingClientRect()
  const y = event.clientY - rect.top
  const center = metrics.thumbY.height / 2
  const maxTop = el.clientHeight - metrics.thumbY.height
  const top = Math.min(maxTop, Math.max(0, y - center))
  const maxScroll = el.scrollHeight - el.clientHeight
  el.scrollTop = maxTop <= 0 ? 0 : (top / maxTop) * maxScroll
  updateMetrics()
  startDrag('y', event)
}

function onRailXPointerDown(event) {
  if (event.target.closest('.app-scroll-area__thumb')) return
  const el = viewportRef.value
  if (!el) return
  const rect = event.currentTarget.getBoundingClientRect()
  const x = event.clientX - rect.left
  const center = metrics.thumbX.width / 2
  const maxLeft = el.clientWidth - metrics.thumbX.width
  const left = Math.min(maxLeft, Math.max(0, x - center))
  const maxScroll = el.scrollWidth - el.clientWidth
  el.scrollLeft = maxLeft <= 0 ? 0 : (left / maxLeft) * maxScroll
  updateMetrics()
  startDrag('x', event)
}

function bindObservers() {
  const el = viewportRef.value
  if (!el || !props.overlay) return

  resizeObserver = new ResizeObserver(() => updateMetrics())
  resizeObserver.observe(el)
  Array.from(el.children).forEach((child) => {
    resizeObserver.observe(child)
  })

  mutationObserver = new MutationObserver(() => {
    nextTick(() => {
      Array.from(el.children).forEach((child) => {
        try {
          resizeObserver?.observe(child)
        } catch {
          /* ignore */
        }
      })
      updateMetrics()
    })
  })
  mutationObserver.observe(el, { childList: true, subtree: true, characterData: true })
}

function unbindObservers() {
  resizeObserver?.disconnect()
  resizeObserver = null
  mutationObserver?.disconnect()
  mutationObserver = null
  endDrag()
}

onMounted(() => {
  nextTick(() => {
    bindObservers()
    updateMetrics()
  })
})

onBeforeUnmount(() => {
  unbindObservers()
})

watch(
  () => [props.overlay, props.axis],
  () => {
    unbindObservers()
    nextTick(() => {
      bindObservers()
      updateMetrics()
    })
  },
)
</script>

<template>
  <component :is="tag" v-bind="forwardedAttrs" :class="shellClass">
    <div ref="viewportRef" :class="viewportClass" @scroll.passive="onScroll">
      <slot />
    </div>

    <div
      v-if="showRailY"
      class="app-scroll-area__rail app-scroll-area__rail--y"
      aria-hidden="true"
      @pointerdown="onRailYPointerDown"
    >
      <div
        class="app-scroll-area__thumb app-scroll-area__thumb--y"
        :style="{ top: `${metrics.thumbY.top}px`, height: `${metrics.thumbY.height}px` }"
        @pointerdown.stop="startDrag('y', $event)"
      />
    </div>

    <div
      v-if="showRailX"
      class="app-scroll-area__rail app-scroll-area__rail--x"
      aria-hidden="true"
      @pointerdown="onRailXPointerDown"
    >
      <div
        class="app-scroll-area__thumb app-scroll-area__thumb--x"
        :style="{ left: `${metrics.thumbX.left}px`, width: `${metrics.thumbX.width}px` }"
        @pointerdown.stop="startDrag('x', $event)"
      />
    </div>
  </component>
</template>
