<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  x: {
    type: Number,
    default: 0,
  },
  y: {
    type: Number,
    default: 0,
  },
  items: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['select', 'close'])

const menuRef = ref(null)
const pos = ref({ x: 0, y: 0 })

const clampPosition = () => {
  const menu = menuRef.value
  if (!menu) {
    pos.value = { x: props.x, y: props.y }
    return
  }

  const rect = menu.getBoundingClientRect()
  const padding = 8
  let x = props.x
  let y = props.y

  if (x + rect.width > window.innerWidth - padding) {
    x = Math.max(padding, window.innerWidth - rect.width - padding)
  }
  if (y + rect.height > window.innerHeight - padding) {
    y = Math.max(padding, window.innerHeight - rect.height - padding)
  }

  pos.value = { x, y }
}

const handleSelect = (item) => {
  if (item.disabled || item.divider) return
  emit('select', item.key)
  emit('close')
}

const handleKeydown = (event) => {
  if (event.key === 'Escape') emit('close')
}

watch(
  () => [props.open, props.x, props.y, props.items],
  async ([open]) => {
    if (!open) return
    pos.value = { x: props.x, y: props.y }
    await nextTick()
    clampPosition()
  },
)

watch(
  () => props.open,
  (open) => {
    if (open) document.addEventListener('keydown', handleKeydown)
    else document.removeEventListener('keydown', handleKeydown)
  },
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="ps-doc-ctx-backdrop"
      @click="emit('close')"
      @contextmenu.prevent="emit('close')"
    >
      <div
        ref="menuRef"
        class="ps-doc-ctx-menu"
        role="menu"
        :style="{ top: `${pos.y}px`, left: `${pos.x}px` }"
        @click.stop
        @contextmenu.prevent
      >
        <template v-for="item in items" :key="item.key">
          <div v-if="item.divider" class="ps-doc-ctx-divider" role="separator" />
          <button
            v-else
            type="button"
            class="ps-doc-ctx-item"
            :class="{ 'is-danger': item.danger, 'is-disabled': item.disabled }"
            role="menuitem"
            :disabled="item.disabled"
            @click="handleSelect(item)"
          >
            <span v-if="item.icon" class="ps-doc-ctx-icon" v-html="item.icon" />
            <span class="ps-doc-ctx-label">{{ item.label }}</span>
          </button>
        </template>
      </div>
    </div>
  </Teleport>
</template>
