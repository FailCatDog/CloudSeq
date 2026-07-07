<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { DOC_BLOCK_FORMATS } from '@/constants/docBlockFormats'

const props = defineProps({
  anchor: {
    type: Object,
    required: true,
  },
  formats: {
    type: Array,
    default: () => DOC_BLOCK_FORMATS,
  },
})

const emit = defineEmits(['select', 'close'])

const hoveredId = ref(null)
const panelRef = ref(null)

const hoveredFormat = computed(() =>
  props.formats.find((format) => format.id === hoveredId.value) ?? null,
)

const panelStyle = computed(() => {
  const width = hoveredFormat.value ? 460 : 248
  const height = 120
  let top = props.anchor.top
  let left = props.anchor.left

  if (left + width > window.innerWidth - 12) {
    left = Math.max(12, props.anchor.left - width - 40)
  }
  if (top + height > window.innerHeight - 12) {
    top = Math.max(12, window.innerHeight - height - 12)
  }

  return { top: `${top}px`, left: `${left}px` }
})

const handleSelect = (formatId) => {
  emit('select', formatId)
  emit('close')
}

const handleDocumentPointer = (event) => {
  if (panelRef.value?.contains(event.target)) return
  emit('close')
}

const handleKeydown = (event) => {
  if (event.key === 'Escape') emit('close')
}

onMounted(() => {
  document.addEventListener('pointerdown', handleDocumentPointer, true)
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointer, true)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <div
      ref="panelRef"
      class="ps-format-picker"
      :style="panelStyle"
      role="dialog"
      aria-label="选择文本格式"
      @pointerdown.stop
      @mouseleave="hoveredId = null"
    >
      <div class="ps-format-picker__grid">
        <button
          v-for="format in formats"
          :key="format.id"
          type="button"
          class="ps-format-picker__item"
          :class="{ 'is-hovered': hoveredId === format.id, 'is-danger': format.danger }"
          :aria-label="format.label"
          @mouseenter="hoveredId = format.id"
          @focus="hoveredId = format.id"
          @click="handleSelect(format.id)"
        >
          <span class="ps-format-picker__icon" :class="format.iconClass">{{ format.short }}</span>
        </button>
      </div>

      <div
        v-if="hoveredFormat"
        class="ps-format-picker__bubble"
        :class="{ 'is-danger': hoveredFormat.danger }"
        aria-live="polite"
      >
        <p class="ps-format-picker__bubble-title">{{ hoveredFormat.label }}</p>
        <p v-if="hoveredFormat.description" class="ps-format-picker__bubble-desc">
          {{ hoveredFormat.description }}
        </p>
      </div>
    </div>
  </Teleport>
</template>
