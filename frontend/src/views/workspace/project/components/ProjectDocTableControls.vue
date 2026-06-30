<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import {
  getEditorViewDom,
  isEditorDestroyed,
  safeEditorOff,
  safeEditorOn,
} from '@/utils/tiptapSafe'
import {
  buildTableContextMenuItems,
  TABLE_MENU_COMMANDS,
} from '../script/projectDocTableMenu.js'
import {
  ensureTableCellSelection,
  focusTableCell,
  getActiveTableWrapper,
  getTableSelectionContext,
  isCursorInTable,
} from '../script/projectDocTableUtils.js'

const props = defineProps({
  editor: { type: Object, default: null },
  canWrite: { type: Boolean, default: true },
})

const tableContext = ref(null)
const barStyle = ref({ top: '0px', left: '0px', width: '0px' })
const barVisible = ref(false)

const menuOpen = ref(false)
const menuPos = ref({ x: 0, y: 0 })
const menuRef = ref(null)

const rowMenuOpen = ref(false)
const colMenuOpen = ref(false)

let scrollEl = null
let activeWrapper = null
/** @type {number | null} */
let savedTablePos = null
let unmounted = false
/** @type {number | null} */
let rafId = null

const handleEditorBlur = () => {
  setTimeout(() => {
    const activeEl = document.activeElement
    if (activeEl instanceof Element && activeEl.closest('.ps-table-bar, .ps-table-menu')) {
      return
    }
    if (!props.editor?.isFocused) {
      barVisible.value = false
      closeMenus()
      clearActiveWrapper()
    }
  }, 120)
}

const canDeleteRow = computed(() => (tableContext.value?.rows ?? 0) > 1)
const canDeleteCol = computed(() => (tableContext.value?.cols ?? 0) > 1)
const currentAlign = computed(() => tableContext.value?.align ?? 'left')
const currentWrap = computed(() => tableContext.value?.wrap ?? 'wrap')

const menuItems = computed(() =>
  buildTableContextMenuItems({
    canDeleteRow: canDeleteRow.value,
    canDeleteCol: canDeleteCol.value,
  }),
)

const clearActiveWrapper = () => {
  activeWrapper?.classList.remove('is-table-active')
  activeWrapper = null
}

const updateControls = () => {
  if (unmounted || isEditorDestroyed(props.editor) || !props.canWrite || !isCursorInTable(props.editor)) {
    if (!rowMenuOpen.value && !colMenuOpen.value && !menuOpen.value) {
      closeMenus()
    }
    if (!isCursorInTable(props.editor)) {
      barVisible.value = false
      tableContext.value = null
      clearActiveWrapper()
    }
    return
  }

  savedTablePos = props.editor.state.selection.from
  tableContext.value = getTableSelectionContext(props.editor)
  const wrapper = getActiveTableWrapper(props.editor)
  if (!wrapper) {
    barVisible.value = false
    clearActiveWrapper()
    return
  }

  if (activeWrapper !== wrapper) {
    clearActiveWrapper()
    activeWrapper = wrapper
    activeWrapper.classList.add('is-table-active')
  }

  const rect = wrapper.getBoundingClientRect()
  const barHeight = 34
  barStyle.value = {
    top: `${Math.max(8, rect.top - barHeight - 2)}px`,
    left: `${rect.left}px`,
    width: `${rect.width}px`,
  }
  barVisible.value = true
}

const scheduleUpdate = () => {
  if (unmounted) return
  if (rafId != null) cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(() => {
    rafId = null
    if (!unmounted) updateControls()
  })
}

const runMenuAction = (key) => {
  const apply = TABLE_MENU_COMMANDS[key]
  if (!apply || !props.editor) return

  ensureTableCellSelection(props.editor, savedTablePos, activeWrapper)

  const ok = apply(props.editor.chain().focus()).run()
  if (ok) {
    savedTablePos = props.editor.state.selection.from
  }

  menuOpen.value = false
  rowMenuOpen.value = false
  colMenuOpen.value = false
  scheduleUpdate()
}

const closeMenus = () => {
  menuOpen.value = false
  rowMenuOpen.value = false
  colMenuOpen.value = false
}

const openContextMenu = (event) => {
  const dom = getEditorViewDom(props.editor)
  if (!props.canWrite || !dom) return
  const cell = event.target instanceof Element ? event.target.closest('td, th') : null
  if (!cell || !dom.contains(cell)) return

  event.preventDefault()

  focusTableCell(props.editor, cell)
  savedTablePos = props.editor.state.selection.from

  updateControls()
  menuPos.value = { x: event.clientX, y: event.clientY }
  menuOpen.value = true
  nextTick(clampMenuPosition)
}

const clampMenuPosition = () => {
  const menu = menuRef.value
  if (!menu) return

  const rect = menu.getBoundingClientRect()
  const pad = 8
  let x = menuPos.value.x
  let y = menuPos.value.y

  if (x + rect.width > window.innerWidth - pad) {
    x = Math.max(pad, window.innerWidth - rect.width - pad)
  }
  if (y + rect.height > window.innerHeight - pad) {
    y = Math.max(pad, window.innerHeight - rect.height - pad)
  }

  menuPos.value = { x, y }
}

const handleDocumentPointer = (event) => {
  const target = event.target
  if (target instanceof Element && target.closest('.ps-table-bar, .ps-table-menu')) return
  closeMenus()
}

const handleKeydown = (event) => {
  if (event.key === 'Escape') closeMenus()
}

const bindEditor = () => {
  unbindEditor()
  const dom = getEditorViewDom(props.editor)
  if (!dom) return

  dom.addEventListener('contextmenu', openContextMenu)

  safeEditorOn(props.editor, 'selectionUpdate', scheduleUpdate)
  safeEditorOn(props.editor, 'transaction', scheduleUpdate)
  safeEditorOn(props.editor, 'focus', scheduleUpdate)
  safeEditorOn(props.editor, 'blur', handleEditorBlur)

  scrollEl = dom.closest('.ps-doc-editor-scroll')
  scrollEl?.addEventListener('scroll', scheduleUpdate, { passive: true })
  window.addEventListener('resize', scheduleUpdate)
  document.addEventListener('pointerdown', handleDocumentPointer, true)
  document.addEventListener('keydown', handleKeydown)
  scheduleUpdate()
}

const unbindEditor = () => {
  const dom = getEditorViewDom(props.editor)
  if (dom) {
    dom.removeEventListener('contextmenu', openContextMenu)
  }
  safeEditorOff(props.editor, 'selectionUpdate', scheduleUpdate)
  safeEditorOff(props.editor, 'transaction', scheduleUpdate)
  safeEditorOff(props.editor, 'focus', scheduleUpdate)
  safeEditorOff(props.editor, 'blur', handleEditorBlur)
  scrollEl?.removeEventListener('scroll', scheduleUpdate)
  window.removeEventListener('resize', scheduleUpdate)
  document.removeEventListener('pointerdown', handleDocumentPointer, true)
  document.removeEventListener('keydown', handleKeydown)
  scrollEl = null
  clearActiveWrapper()
}

watch(
  () => [props.editor, props.canWrite],
  async ([editor]) => {
    if (editor && !isEditorDestroyed(editor)) {
      await nextTick()
      bindEditor()
    } else {
      unbindEditor()
      barVisible.value = false
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unmounted = true
  if (rafId != null) cancelAnimationFrame(rafId)
  unbindEditor()
})
</script>

<template>
  <Teleport to="body">
    <!-- Typora 式：聚焦表格时顶部轻量工具条 -->
    <div
      v-if="barVisible && tableContext"
      class="ps-table-bar"
      :style="barStyle"
      role="toolbar"
      aria-label="表格工具"
      @mousedown.capture.prevent
    >
      <span class="ps-table-bar__badge">{{ tableContext.rows }}×{{ tableContext.cols }}</span>

      <div class="ps-table-bar__segment">
        <div class="ps-table-bar__dropdown">
          <button
            type="button"
            class="ps-table-bar__trigger"
            :class="{ 'is-open': rowMenuOpen }"
            @mousedown.prevent="rowMenuOpen = !rowMenuOpen; colMenuOpen = false"
          >
            行
            <svg width="10" height="10" viewBox="0 0 12 12" aria-hidden="true"><path fill="currentColor" d="M3 4.5 6 7.5 9 4.5z" /></svg>
          </button>
          <div v-if="rowMenuOpen" class="ps-table-bar__menu">
            <button type="button" @mousedown.prevent="runMenuAction('addRowBefore')">上方插入行</button>
            <button type="button" @mousedown.prevent="runMenuAction('addRowAfter')">下方插入行</button>
            <button type="button" :disabled="!canDeleteRow" @mousedown.prevent="runMenuAction('deleteRow')">删除行</button>
          </div>
        </div>

        <div class="ps-table-bar__dropdown">
          <button
            type="button"
            class="ps-table-bar__trigger"
            :class="{ 'is-open': colMenuOpen }"
            @mousedown.prevent="colMenuOpen = !colMenuOpen; rowMenuOpen = false"
          >
            列
            <svg width="10" height="10" viewBox="0 0 12 12" aria-hidden="true"><path fill="currentColor" d="M3 4.5 6 7.5 9 4.5z" /></svg>
          </button>
          <div v-if="colMenuOpen" class="ps-table-bar__menu">
            <button type="button" @mousedown.prevent="runMenuAction('addColumnBefore')">左侧插入列</button>
            <button type="button" @mousedown.prevent="runMenuAction('addColumnAfter')">右侧插入列</button>
            <button type="button" :disabled="!canDeleteCol" @mousedown.prevent="runMenuAction('deleteColumn')">删除列</button>
          </div>
        </div>
      </div>

      <div class="ps-table-bar__segment ps-table-bar__align">
        <button
          type="button"
          class="ps-table-bar__icon"
          :class="{ 'is-active': currentAlign === 'left' || !currentAlign }"
          title="左对齐"
          @mousedown.prevent="runMenuAction('alignLeft')"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <line x1="4" y1="6" x2="20" y2="6" /><line x1="4" y1="12" x2="14" y2="12" /><line x1="4" y1="18" x2="18" y2="18" />
          </svg>
        </button>
        <button
          type="button"
          class="ps-table-bar__icon"
          :class="{ 'is-active': currentAlign === 'center' }"
          title="居中对齐"
          @mousedown.prevent="runMenuAction('alignCenter')"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <line x1="4" y1="6" x2="20" y2="6" /><line x1="7" y1="12" x2="17" y2="12" /><line x1="5" y1="18" x2="19" y2="18" />
          </svg>
        </button>
        <button
          type="button"
          class="ps-table-bar__icon"
          :class="{ 'is-active': currentAlign === 'right' }"
          title="右对齐"
          @mousedown.prevent="runMenuAction('alignRight')"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <line x1="4" y1="6" x2="20" y2="6" /><line x1="10" y1="12" x2="20" y2="12" /><line x1="6" y1="18" x2="20" y2="18" />
          </svg>
        </button>
      </div>

      <div class="ps-table-bar__segment">
        <button
          type="button"
          class="ps-table-bar__pill"
          :class="{ 'is-active': currentWrap !== 'nowrap' }"
          @mousedown.prevent="runMenuAction('wrap')"
        >
          换行
        </button>
        <button
          type="button"
          class="ps-table-bar__pill"
          :class="{ 'is-active': currentWrap === 'nowrap' }"
          @mousedown.prevent="runMenuAction('nowrap')"
        >
          不换行
        </button>
      </div>

      <button
        type="button"
        class="ps-table-bar__icon ps-table-bar__icon--danger"
        title="删除表格"
        @mousedown.prevent="runMenuAction('deleteTable')"
      >
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
        </svg>
      </button>
    </div>

    <!-- Typora 式：右键单元格上下文菜单 -->
    <div v-if="menuOpen" class="ps-table-menu-backdrop ps-table-menu" @mousedown.self="closeMenus" @contextmenu.prevent="closeMenus">
      <div
        ref="menuRef"
        class="ps-table-menu"
        role="menu"
        :style="{ top: `${menuPos.y}px`, left: `${menuPos.x}px` }"
        @click.stop
        @contextmenu.prevent
      >
        <template v-for="item in menuItems" :key="item.key">
          <div v-if="item.divider" class="ps-table-menu__divider" role="separator" />
          <button
            v-else
            type="button"
            class="ps-table-menu__item"
            :class="{ 'is-danger': item.danger, 'is-disabled': item.disabled }"
            role="menuitem"
            :disabled="item.disabled"
            @mousedown.prevent="runMenuAction(item.key)"
          >
            {{ item.label }}
          </button>
        </template>
      </div>
    </div>
  </Teleport>
</template>
