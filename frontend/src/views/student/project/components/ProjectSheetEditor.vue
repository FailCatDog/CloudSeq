<script setup>
import { HocuspocusProvider } from '@hocuspocus/provider'
import { Sheet, createDefaultDocumentContent } from '@speed-sheet/core'
import * as Y from 'yjs'
import { getCurrentInstance, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { invalidateCollabToken } from '@/utils/collabTokenCache'
import { formatDocUpdateLabel } from '@/utils/formatDocUpdate'
import { NODE_TITLE_MAX_LENGTH } from '@/constants/fieldLimits'
import { ensureSpeedSheetUi, loadSpeedSheetComponent } from '@/plugins/speedSheet'
import { downloadNodeExportApi } from '@/api/export'

const sheetDocNeedsInit = (doc) => {
  const sheets = doc.getMap('sheets')
  if (sheets.size === 0) return true
  for (const sheet of sheets.values()) {
    if (!sheet?.get('rowOrder') || !sheet?.get('colOrder')) return true
  }
  return false
}

const ensureSheetDocInitialized = (doc, { sheetName, snapshotJson }) => {
  if (!sheetDocNeedsInit(doc)) {
    return snapshotJson || null
  }

  if (snapshotJson) {
    try {
      const snapshot = JSON.parse(snapshotJson)
      if (snapshot?.sheets?.length) {
        const tempDoc = new Y.Doc()
        const tempSheet = new Sheet({ ydoc: tempDoc, snapshot })
        Y.applyUpdate(doc, Y.encodeStateAsUpdate(tempDoc))
        tempSheet.destroy()
        return snapshotJson
      }
    } catch {
      // fall through to default workbook
    }
  }

  const { content, nodeJson } = createDefaultDocumentContent({
    sheetName: sheetName || 'Sheet1',
  })
  Y.applyUpdate(doc, content)
  return nodeJson
}

const props = defineProps({
  sheetId: { type: [String, Number], required: true },
  title: { type: String, default: '未命名表格' },
  initialSnapshot: { type: String, default: '' },
  canWrite: { type: Boolean, default: true },
  collabSession: { type: Object, default: null },
  updateDate: { type: [String, Number, Date], default: null },
})

const emit = defineEmits(['update:title', 'snapshot'])

const SpeedSheet = shallowRef(null)
const ydoc = shallowRef(null)
const collabReady = ref(false)
const collabError = ref('')
const sheetUiError = ref('')
const exportMessage = ref('')
const exporting = ref(false)
const lastEditedLabel = ref('')

let provider = null
let contentSeeded = false
let snapshotTimer = null

const SNAPSHOT_DEBOUNCE_MS = 2000

const pushContentSnapshot = (contentJson, { immediate = false } = {}) => {
  if (!props.canWrite || !provider) return

  const payload = JSON.stringify({
    type: 'content-snapshot',
    contentMd: contentJson ?? '',
  })

  const send = () => {
    provider?.sendStateless(payload)
  }

  if (immediate) {
    if (snapshotTimer) {
      clearTimeout(snapshotTimer)
      snapshotTimer = null
    }
    send()
    return
  }

  if (snapshotTimer) clearTimeout(snapshotTimer)
  snapshotTimer = setTimeout(() => {
    snapshotTimer = null
    send()
  }, SNAPSHOT_DEBOUNCE_MS)
}

const teardownCollab = () => {
  if (snapshotTimer) {
    clearTimeout(snapshotTimer)
    snapshotTimer = null
  }
  provider?.destroy()
  provider = null
  ydoc.value = null
  contentSeeded = false
  collabReady.value = false
  collabError.value = ''
}

const setupCollab = () => {
  teardownCollab()
  if (!props.collabSession) return

  try {
    const doc = new Y.Doc()
    provider = new HocuspocusProvider({
      url: props.collabSession.wsUrl,
      name: props.collabSession.room,
      token: () => props.collabSession.getToken(),
      document: doc,
      onAuthenticationFailed: async () => {
        invalidateCollabToken(props.sheetId)
        try {
          await props.collabSession.getToken()
          provider.configuration.websocketProvider.connect()
          collabError.value = ''
        } catch {
          collabError.value = '协同鉴权失败，请刷新页面重试'
        }
      },
      onClose: ({ event }) => {
        if (event?.code && event.code !== 1000) {
          collabError.value = '协同连接已断开，请确认协同服务已启动'
        }
      },
      onSynced: () => {
        if (contentSeeded) return
        contentSeeded = true

        let snapshotJson = ensureSheetDocInitialized(doc, {
          sheetName: props.title,
          snapshotJson: props.initialSnapshot,
        })

        if (!snapshotJson) {
          const reader = new Sheet({ ydoc: doc })
          snapshotJson = JSON.stringify(reader.toSnapshot())
          reader.destroy()
        }

        emit('snapshot', snapshotJson)
        pushContentSnapshot(snapshotJson, { immediate: true })
        collabReady.value = true
      },
    })

    ydoc.value = doc
  } catch (error) {
    collabError.value = error?.message || '协同表格初始化失败'
  }
}

const handleChange = (snapshot) => {
  const contentJson = JSON.stringify(snapshot)
  emit('snapshot', contentJson)
  pushContentSnapshot(contentJson)
  lastEditedLabel.value = formatDocUpdateLabel(new Date())
}

const handleTitleInput = (event) => {
  if (!props.canWrite) return
  emit('update:title', event.target.value)
}

const handleExportExcel = async () => {
  if (exporting.value) return
  exporting.value = true
  exportMessage.value = ''
  try {
    let contentMd
    if (ydoc.value) {
      const reader = new Sheet({ ydoc: ydoc.value })
      try {
        contentMd = JSON.stringify(reader.toSnapshot())
      } finally {
        reader.destroy()
      }
    }
    if (contentMd) {
      pushContentSnapshot(contentMd, { immediate: true })
    }
    await downloadNodeExportApi(props.sheetId, 'xlsx', { contentMd })
  } catch (error) {
    exportMessage.value = error?.message || '导出失败'
  } finally {
    exporting.value = false
  }
}

const initSheetUi = async () => {
  sheetUiError.value = ''
  try {
    const app = getCurrentInstance()?.appContext.app
    await ensureSpeedSheetUi(app)
    SpeedSheet.value = await loadSpeedSheetComponent()
  } catch (error) {
    sheetUiError.value = error?.message || '表格组件加载失败'
  }
}

watch(
  () => [props.collabSession, props.sheetId],
  () => {
    setupCollab()
  },
  { immediate: true },
)

watch(
  () => props.updateDate,
  (value) => {
    if (value) lastEditedLabel.value = formatDocUpdateLabel(value)
  },
  { immediate: true },
)

onMounted(() => {
  void initSheetUi()
})

onBeforeUnmount(() => {
  teardownCollab()
})
</script>

<template>
  <div class="ps-sheet-editor">
    <header class="ps-sheet-editor-head">
      <input
        class="ps-sheet-editor-title"
        :value="title"
        type="text"
        placeholder="未命名表格"
        :readonly="!canWrite"
        :maxlength="NODE_TITLE_MAX_LENGTH"
        @input="handleTitleInput"
      >
      <div class="ps-sheet-editor-meta">
        <span v-if="lastEditedLabel">{{ lastEditedLabel }}</span>
        <button
          type="button"
          class="ps-sheet-export-btn"
          :disabled="exporting"
          @click="handleExportExcel"
        >
          {{ exporting ? '导出中…' : '导出 Excel' }}
        </button>
        <span v-if="exportMessage" class="ps-sheet-editor-notice ps-sheet-editor-notice--error">{{ exportMessage }}</span>
        <span v-else-if="sheetUiError" class="ps-sheet-editor-notice ps-sheet-editor-notice--error">{{ sheetUiError }}</span>
        <span v-else-if="collabError" class="ps-sheet-editor-notice ps-sheet-editor-notice--error">{{ collabError }}</span>
      </div>
    </header>

    <div class="ps-sheet-editor-body">
      <div v-if="!SpeedSheet || !collabReady" class="ps-sheet-editor-loading">
        {{ sheetUiError ? '表格组件不可用' : '加载表格…' }}
      </div>
      <component
        :is="SpeedSheet"
        v-else-if="ydoc"
        class="ps-sheet-editor-canvas"
        :ydoc="ydoc"
        :editable="canWrite"
        lang="zh"
        show-toolbar
        show-formula-bar
        show-sheet-tabs
        @change="handleChange"
      />
    </div>
  </div>
</template>
