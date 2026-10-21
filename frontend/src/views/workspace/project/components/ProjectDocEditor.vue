<script setup>
import { Editor } from '@tiptap/core'
import { EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Collaboration from '@tiptap/extension-collaboration'
import CollaborationCaret from '@tiptap/extension-collaboration-caret'
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { Markdown } from '@tiptap/markdown'
import { ProjectDocTableCell, ProjectDocTableHeader } from '../script/projectDocTableExtensions.js'
import ProjectDocTableControls from './ProjectDocTableControls.vue'
import { HocuspocusProvider } from '@hocuspocus/provider'
import * as Y from 'yjs'
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { buildCollabUser, renderCollabCaret, renderCollabSelection } from '@/utils/collabCaret'
import { invalidateCollabToken } from '@/utils/collabTokenCache'
import ProjectDocBlockGutter from './ProjectDocBlockGutter.vue'
import ProjectDocOutline from './ProjectDocOutline.vue'
import { ProjectDocKeyboardShortcuts } from '../script/projectDocKeyboardShortcuts.js'

const props = defineProps({
  docId: { type: [String, Number], required: true },
  title: { type: String, default: '未命名文档' },
  initialContent: { type: String, default: '' },
  canWrite: { type: Boolean, default: true },
  collabSession: { type: Object, required: true },
})

const emit = defineEmits(['update:title', 'snapshot'])

const editor = shallowRef(null)
const collabReady = ref(false)
const collabError = ref('')
let provider = null
let ydoc = null
let contentSeeded = false
let snapshotTimer = null

const SNAPSHOT_DEBOUNCE_MS = 2000

const pushContentSnapshot = (markdown, { immediate = false } = {}) => {
  if (!props.canWrite || !provider) return

  const payload = JSON.stringify({
    type: 'content-snapshot',
    contentMd: markdown ?? '',
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
  editor.value?.destroy()
  editor.value = null
  provider?.destroy()
  provider = null
  ydoc = null
  contentSeeded = false
  collabReady.value = false
  collabError.value = ''
}

const setupCollab = () => {
  teardownCollab()
  if (!props.collabSession) return

  try {
    ydoc = new Y.Doc()
    provider = new HocuspocusProvider({
      url: props.collabSession.wsUrl,
      name: props.collabSession.room,
      token: () => props.collabSession.getToken(),
      document: ydoc,
      onAuthenticationFailed: async () => {
        invalidateCollabToken(props.docId)
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
        if (contentSeeded || !editor.value) return
        contentSeeded = true
        if (props.initialContent && editor.value.isEmpty) {
          editor.value.commands.setContent(props.initialContent, { contentType: 'markdown' })
        }
        if (editor.value) {
          const markdown = editor.value.getMarkdown()
          emit('snapshot', markdown)
          pushContentSnapshot(markdown, { immediate: true })
        }
      },
    })

    editor.value = new Editor({
      editable: props.canWrite,
      editorProps: {
        handleClick: (view, _pos, event) => {
          if (event.button !== 0) return false

          const anchor = event.target instanceof Element ? event.target.closest('a[href]') : null
          if (!(anchor instanceof HTMLAnchorElement) || !anchor.href) return false

          const modClick = event.ctrlKey || event.metaKey
          if (modClick || !view.editable) {
            window.open(anchor.href, '_blank', 'noopener,noreferrer')
            return true
          }
          return false
        },
        handleKeyDown: (_view, event) => {
          if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
            event.preventDefault()
            if (editor.value) {
              const markdown = editor.value.getMarkdown()
              emit('snapshot', markdown)
              pushContentSnapshot(markdown, { immediate: true })
            }
            return true
          }
          return false
        },
      },
      extensions: [
        StarterKit.configure({
          undoRedo: false,
          link: {
            openOnClick: false,
            autolink: true,
            linkOnPaste: true,
            HTMLAttributes: {
              class: 'ps-doc-link',
              rel: 'noopener noreferrer',
              target: '_blank',
            },
          },
        }),
        Table.configure({ resizable: true }),
        TableRow,
        ProjectDocTableHeader,
        ProjectDocTableCell,
        Markdown.configure({
          markedOptions: { gfm: true },
        }),
        Collaboration.configure({ document: ydoc }),
        CollaborationCaret.configure({
          provider,
          user: buildCollabUser({
            name: props.collabSession.displayName,
            avatarUrl: props.collabSession.avatarUrl,
          }),
          render: renderCollabCaret,
          selectionRender: renderCollabSelection,
        }),
        Placeholder.configure({
          placeholder: '开始编写文档…',
        }),
        ProjectDocKeyboardShortcuts,
      ],
      onUpdate: ({ editor: currentEditor }) => {
        const markdown = currentEditor.getMarkdown()
        emit('snapshot', markdown)
        pushContentSnapshot(markdown)
      },
    })

    collabReady.value = true
  } catch (error) {
    collabError.value = error?.message || '协同编辑器初始化失败'
  }
}

const handleTitleInput = (event) => {
  if (!props.canWrite) return
  emit('update:title', event.target.value)
}

const getSnapshot = () => editor.value?.getMarkdown() ?? ''

onMounted(() => {
  setupCollab()
})

onBeforeUnmount(() => {
  if (editor.value && props.canWrite && provider) {
    pushContentSnapshot(editor.value.getMarkdown(), { immediate: true })
  }
  teardownCollab()
})

watch(
  () => props.canWrite,
  (canWrite) => {
    editor.value?.setEditable(canWrite)
  },
)

defineExpose({ getSnapshot })
</script>

<template>
  <div class="ps-doc-editor">
    <header class="ps-doc-editor-header">
      <input
        class="ps-doc-editor-title"
        type="text"
        :value="title"
        :readonly="!canWrite"
        placeholder="未命名文档"
        @input="handleTitleInput"
      />
    </header>

    <div class="ps-doc-editor-body">
      <div class="ps-doc-editor-workspace">
        <ProjectDocOutline :editor="editor" />
        <div class="ps-doc-editor-scroll">
          <ProjectDocTableControls v-if="collabReady" :editor="editor" :can-write="canWrite" />
          <div class="ps-doc-editor-canvas">
            <ProjectDocBlockGutter v-if="collabReady" :editor="editor" :can-write="canWrite" />
            <EditorContent v-if="collabReady" :editor="editor" class="ps-doc-editor-content" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
