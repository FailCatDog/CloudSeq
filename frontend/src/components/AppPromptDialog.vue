<script setup>
import { nextTick, ref, watch } from 'vue'
import {
  PROMPT_TEXT_MAX_LENGTH,
  TABLE_MAX_COLS,
  TABLE_MAX_ROWS,
  TABLE_MIN_SIZE,
} from '@/constants/fieldLimits'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: {
    type: String,
    default: 'prompt',
    validator: (value) => ['prompt', 'confirm', 'tableSize'].includes(value),
  },
  title: { type: String, default: '' },
  message: { type: String, default: '' },
  label: { type: String, default: '' },
  defaultValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  maxLength: { type: Number, default: PROMPT_TEXT_MAX_LENGTH },
  confirmLabel: { type: String, default: '确定' },
  cancelLabel: { type: String, default: '取消' },
  confirmDanger: { type: Boolean, default: false },
  tableRows: { type: Number, default: 1 },
  tableCols: { type: Number, default: 3 },
  tableWithHeaderRow: { type: Boolean, default: false },
})

const emit = defineEmits(['update:open', 'confirm', 'cancel'])

const inputRef = ref(null)
const inputValue = ref('')
const rowsValue = ref(3)
const colsValue = ref(3)
const withHeaderRowValue = ref(true)

const TABLE_MIN = TABLE_MIN_SIZE

const clampTableSize = (value, max) => {
  const num = Number.parseInt(String(value), 10)
  if (Number.isNaN(num)) return TABLE_MIN
  return Math.min(max, Math.max(TABLE_MIN, num))
}

watch(
  () => props.open,
  async (open) => {
    if (!open) return
    inputValue.value = props.defaultValue
    rowsValue.value = props.tableRows
    colsValue.value = props.tableCols
    withHeaderRowValue.value = props.tableWithHeaderRow
    await nextTick()
    if (props.mode === 'prompt') {
      const input = inputRef.value
      input?.focus({ preventScroll: true })
      input?.select()
    } else if (props.mode === 'tableSize') {
      inputRef.value?.focus({ preventScroll: true })
    }
  },
)

const close = () => {
  emit('update:open', false)
}

const handleCancel = () => {
  emit('cancel')
  close()
}

const handleConfirm = () => {
  if (props.mode === 'confirm') {
    emit('confirm', true)
  } else if (props.mode === 'tableSize') {
    emit('confirm', {
      rows: clampTableSize(rowsValue.value, TABLE_MAX_ROWS),
      cols: clampTableSize(colsValue.value, TABLE_MAX_COLS),
      withHeaderRow: withHeaderRowValue.value,
    })
  } else {
    emit('confirm', inputValue.value.trim())
  }
  close()
}

const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    event.preventDefault()
    handleCancel()
    return
  }
  if (event.key === 'Enter') {
    event.preventDefault()
    handleConfirm()
  }
}
</script>

<template>
  <Teleport to="body">
    <div
      v-show="open"
      class="app-prompt-backdrop"
      @click="handleCancel"
    >
      <div
        class="app-prompt-dialog"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="title ? 'app-prompt-title' : undefined"
        @click.stop
        @keydown="handleKeydown"
      >
        <h2 v-if="title" id="app-prompt-title" class="app-prompt-title">
          {{ title }}
        </h2>

        <p v-if="message" class="app-prompt-message">
          {{ message }}
        </p>

        <label v-if="mode === 'prompt' && label" class="app-prompt-field">
          <span class="app-prompt-label">{{ label }}</span>
          <input
            ref="inputRef"
            v-model="inputValue"
            type="text"
            class="app-prompt-input"
            :placeholder="placeholder"
            :maxlength="maxLength"
            autocomplete="off"
            @keydown.enter.prevent="handleConfirm"
          >
        </label>

        <div v-else-if="mode === 'tableSize'" class="app-prompt-table-size">
          <div class="app-prompt-table-size__grid">
            <label class="app-prompt-field app-prompt-field--inline">
              <span class="app-prompt-label">行数</span>
              <input
                ref="inputRef"
                v-model.number="rowsValue"
                type="number"
                class="app-prompt-input"
                :min="TABLE_MIN"
                :max="TABLE_MAX_ROWS"
                autocomplete="off"
              >
            </label>
            <label class="app-prompt-field app-prompt-field--inline">
              <span class="app-prompt-label">列数</span>
              <input
                v-model.number="colsValue"
                type="number"
                class="app-prompt-input"
                :min="TABLE_MIN"
                :max="TABLE_MAX_COLS"
                autocomplete="off"
              >
            </label>
          </div>
          <label class="app-prompt-check">
            <input v-model="withHeaderRowValue" type="checkbox">
            <span>首行作为表头</span>
          </label>
        </div>

        <div class="app-prompt-actions">
          <button type="button" class="app-prompt-btn app-prompt-btn--ghost" @click="handleCancel">
            {{ cancelLabel }}
          </button>
          <button
            type="button"
            class="app-prompt-btn app-prompt-btn--primary"
            :class="{ 'app-prompt-btn--danger': confirmDanger }"
            @click="handleConfirm"
          >
            {{ confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
