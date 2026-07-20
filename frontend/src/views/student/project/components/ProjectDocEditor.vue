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
import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch, nextTick } from 'vue'
import { buildCollabUser, hashCollabColor, renderCollabCaret, renderCollabSelection } from '@/utils/collabCaret'
import { formatDocUpdateLabel } from '@/utils/formatDocUpdate'
import { invalidateCollabToken } from '@/utils/collabTokenCache'
import { downloadNodeExportApi } from '@/api/export'
import ProjectDocBlockGutter from './ProjectDocBlockGutter.vue'
import ProjectDocCommentGutter from './ProjectDocCommentGutter.vue'
import ProjectDocOutline from './ProjectDocOutline.vue'
import ProjectDocCommentPanel from './ProjectDocCommentPanel.vue'
import { ProjectDocKeyboardShortcuts } from '../script/projectDocKeyboardShortcuts.js'
import {
  ProjectDocImage,
  tryHandleImageDrop,
  tryHandleImagePaste,
  triggerImageUpload,
} from '../script/projectDocImage.js'
import { focusCommentAnchor, getBlockQuoteText, filterCommentsByAnchor, countCommentsByAnchor } from '../script/projectDocComment.js'
import { openAppConfirm } from '@/composables/appPrompt'
import { NODE_TITLE_MAX_LENGTH } from '@/constants/fieldLimits'
import { useDocumentComments } from '@/composables/useDocumentComments.js'
import AppScrollArea from '@/components/AppScrollArea.vue'

const props = defineProps({
  docId: { type: [String, Number], required: true },
  title: { type: String, default: '未命名文档' },
  initialContent: { type: String, default: '' },
  canWrite: { type: Boolean, default: true },
  collabSession: { type: Object, default: null },
  updateDate: { type: [String, Number, Date], default: null },
})

const emit = defineEmits(['update:title', 'snapshot'])

const editor = shallowRef(null)
const collabReady = ref(false)
const collabError = ref('')
const uploadMessage = ref('')
const uploadingImage = ref(false)
const exportMessage = ref('')
const exporting = ref(false)
const exportMenuOpen = ref(false)
const exportMenuRef = ref(null)
const onlineUsers = ref([])
const lastEditedLabel = ref('')
const activeCommentId = ref(null)
const commentsPanelOpen = ref(false)
const pendingCommentAnchor = ref(null)
const commentPanelRef = ref(null)
let provider = null
let ydoc = null
let contentSeeded = false
let snapshotTimer = null
let awarenessChangeHandler = null

const SNAPSHOT_DEBOUNCE_MS = 2000

const {
  comments,
  loading: commentsLoading,
  errorMessage: commentsErrorMessage,
  submitting: commentSubmitting,
  addComment,
  removeComment,
} = useDocumentComments(
  () => props.docId,
  () => collabReady.value,
)

const commentCountByPos = computed(() => countCommentsByAnchor(comments.value))

const blockComments = computed(() => {
  const anchorPos = pendingCommentAnchor.value?.anchorPos
  return filterCommentsByAnchor(comments.value, anchorPos)
})

const buildSessionUser = () => {
  if (!props.collabSession) return null
  return buildCollabUser({
    name: props.collabSession.displayName,
    avatarUrl: props.collabSession.avatarUrl,
  })
}

const syncOnlineUsers = () => {
  const sessionUser = buildSessionUser()
  const awareness = provider?.awareness
  if (!awareness) {
    onlineUsers.value = sessionUser ? [sessionUser] : []
    return
  }

  const seen = new Set()
  const users = []
  awareness.getStates().forEach((state) => {
    const user = state?.user
    if (!user?.name || seen.has(user.name)) return
    seen.add(user.name)
    users.push({
      name: user.name,
      avatarUrl: user.avatarUrl || '',
      color: user.color || hashCollabColor(user.name),
    })
  })

  onlineUsers.value = users.length > 0 ? users : sessionUser ? [sessionUser] : []
}

const bindAwareness = () => {
  unbindAwareness()
  if (!provider?.awareness) {
    syncOnlineUsers()
    return
  }

  awarenessChangeHandler = () => syncOnlineUsers()
  provider.awareness.on('change', awarenessChangeHandler)
  provider.on('awarenessUpdate', awarenessChangeHandler)
  syncOnlineUsers()
}

const unbindAwareness = () => {
  if (provider?.awareness && awarenessChangeHandler) {
    provider.awareness.off('change', awarenessChangeHandler)
    provider.off('awarenessUpdate', awarenessChangeHandler)
  }
  awarenessChangeHandler = null
  onlineUsers.value = []
}

const getImageUploadContext = () => ({
  editor: editor.value,
  nodeId: props.docId,
  canWrite: props.canWrite,
  onUploadStart: () => {
    uploadingImage.value = true
    uploadMessage.value = ''
  },
  onUploadEnd: () => {
    uploadingImage.value = false
  },
  onUploadError: (message) => {
    uploadMessage.value = message
  },
})

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
  unbindAwareness()
  editor.value?.destroy()
  editor.value = null
  provider?.destroy()
  provider = null
  ydoc = null
  contentSeeded = false
  collabReady.value = false
  collabError.value = ''
  uploadMessage.value = ''
  uploadingImage.value = false
  exportMessage.value = ''
  exporting.value = false
  exportMenuOpen.value = false
}

const toggleExportMenu = () => {
  exportMenuOpen.value = !exportMenuOpen.value
}

const closeExportMenu = () => {
  exportMenuOpen.value = false
}

const handleExportMenuOutside = (event) => {
  if (!exportMenuOpen.value) return
  const root = exportMenuRef.value
  if (root && !root.contains(event.target)) {
    closeExportMenu()
  }
}

const handleExport = async (format) => {
  if (exporting.value) return
  closeExportMenu()
  exporting.value = true
  exportMessage.value = ''
  try {
    await downloadNodeExportApi(props.docId, format)
  } catch (error) {
    exportMessage.value = error?.message || '导出失败'
  } finally {
    exporting.value = false
  }
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

    bindAwareness()

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
        handleKeyDown: (view, event) => {
          if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
            event.preventDefault()
            if (editor.value) {
              const markdown = editor.value.getMarkdown()
              emit('snapshot', markdown)
              pushContentSnapshot(markdown, { immediate: true })
            }
            return true
          }

          if (
            props.canWrite
            && (event.ctrlKey || event.metaKey)
            && event.shiftKey
            && event.key.toLowerCase() === 'i'
          ) {
            event.preventDefault()
            void triggerImageUpload(editor.value, props.docId, {
              onUploadStart: () => {
                uploadingImage.value = true
                uploadMessage.value = ''
              },
              onUploadEnd: () => {
                uploadingImage.value = false
              },
              onUploadError: (message) => {
                uploadMessage.value = message
              },
            })
            return true
          }

          return false
        },
        handlePaste: (view, event) => tryHandleImagePaste(view, event, getImageUploadContext()),
        handleDrop: (view, event, _slice, moved) =>
          tryHandleImageDrop(view, event, moved, getImageUploadContext()),
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
        ProjectDocImage,
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
        lastEditedLabel.value = formatDocUpdateLabel(new Date())
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

const openCommentsPanel = (anchor = null) => {
  pendingCommentAnchor.value = anchor
  commentsPanelOpen.value = true
  commentPanelRef.value?.focusComposer()
}

const closeCommentsPanel = () => {
  commentsPanelOpen.value = false
  pendingCommentAnchor.value = null
}

const handleCommentBlock = (row) => {
  if (!row || row.pos == null) return
  activeCommentId.value = null
  commentsErrorMessage.value = ''
  openCommentsPanel({
    anchorPos: row.pos,
    quoteText: getBlockQuoteText(editor.value, row.pos),
  })
}

const handlePublishComment = async (content) => {
  const resolved = pendingCommentAnchor.value
  if (!resolved || resolved.anchorPos == null) {
    commentsErrorMessage.value = '请从段落右侧点击评论按钮后再发表'
    return
  }

  try {
    const created = await addComment({
      anchorPos: resolved.anchorPos,
      quoteText: resolved.quoteText || getBlockQuoteText(editor.value, resolved.anchorPos),
      content,
    })
    activeCommentId.value = created?.id ?? null
    commentPanelRef.value?.clearDraft()
    focusCommentAnchor(editor.value, resolved.anchorPos)
  } catch {
    // error handled in composable
  }
}

const handleSelectComment = (comment) => {
  if (!comment) return
  activeCommentId.value = comment.id
  focusCommentAnchor(editor.value, comment.anchorPos)
}

const handleDeleteComment = async (comment) => {
  if (!comment?.id) return

  const confirmed = await openAppConfirm({
    title: '删除评论',
    message: '确定删除这条评论吗？',
    confirmLabel: '删除',
    cancelLabel: '取消',
    danger: true,
  })
  if (!confirmed) return

  try {
    await removeComment(comment.id)
    if (String(activeCommentId.value) === String(comment.id)) {
      activeCommentId.value = null
    }
  } catch {
    // errorMessage handled in composable
  }
}

watch(
  () => props.docId,
  () => {
    closeCommentsPanel()
    activeCommentId.value = null
  },
)

watch(
  () => [props.collabSession, props.docId],
  ([session]) => {
    if (session) {
      setupCollab()
    } else {
      teardownCollab()
    }
  },
  { immediate: true },
)

watch(
  () => props.updateDate,
  (value) => {
    lastEditedLabel.value = formatDocUpdateLabel(value)
  },
  { immediate: true },
)

watch(
  () => props.canWrite,
  (canWrite) => {
    editor.value?.setEditable(canWrite)
  },
)

defineExpose({ getSnapshot })

onMounted(() => {
  document.addEventListener('click', handleExportMenuOutside)
})

onBeforeUnmount(async () => {
  document.removeEventListener('click', handleExportMenuOutside)
  collabReady.value = false
  await nextTick()
  if (editor.value && props.canWrite && provider) {
    try {
      pushContentSnapshot(editor.value.getMarkdown(), { immediate: true })
    } catch {
      // editor may already be torn down
    }
  }
  teardownCollab()
})
</script>

<template>
  <div class="ps-doc-editor">
    <div class="ps-doc-editor-body">
      <div class="ps-doc-editor-workspace">
        <ProjectDocOutline :editor="editor" />
        <AppScrollArea class="ps-doc-editor-scroll" axis="both" flex hover-reveal>
          <ProjectDocTableControls v-if="collabReady" :editor="editor" :can-write="canWrite" />
          <div class="ps-doc-editor-canvas">
            <div class="ps-doc-article-head">
              <input
                class="ps-doc-editor-title"
                type="text"
                :value="title"
                :readonly="!canWrite"
                placeholder="未命名文档"
                :maxlength="NODE_TITLE_MAX_LENGTH"
                @input="handleTitleInput"
              />

              <div class="ps-doc-article-meta">
                <div class="ps-doc-article-presence" aria-label="当前在线编辑者">
                  <div
                    v-for="(user, index) in onlineUsers"
                    :key="`${user.name}-${index}`"
                    class="ps-doc-article-avatar"
                    :style="{ '--ps-collab-color': user.color, zIndex: onlineUsers.length - index }"
                    :title="user.name"
                  >
                    <img v-if="user.avatarUrl" :src="user.avatarUrl" :alt="user.name" />
                    <span v-else>{{ user.name.slice(0, 1).toUpperCase() }}</span>
                  </div>
                </div>
                <div class="ps-doc-article-meta-actions">
                  <span class="ps-doc-article-updated">{{ lastEditedLabel }}</span>
                  <div ref="exportMenuRef" class="ps-doc-export">
                    <button
                      type="button"
                      class="ps-doc-export-btn"
                      :aria-expanded="exportMenuOpen"
                      :disabled="exporting"
                      @click.stop="toggleExportMenu"
                    >
                      {{ exporting ? '导出中…' : '导出' }}
                    </button>
                    <div
                      v-if="exportMenuOpen"
                      class="ps-doc-export-menu"
                      role="menu"
                      aria-label="导出格式"
                    >
                      <button
                        type="button"
                        class="ps-doc-export-menu-item"
                        role="menuitem"
                        :disabled="exporting"
                        @click="handleExport('docx')"
                      >
                        Word
                      </button>
                      <button
                        type="button"
                        class="ps-doc-export-menu-item"
                        role="menuitem"
                        :disabled="exporting"
                        @click="handleExport('pdf')"
                      >
                        PDF
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <p v-if="collabError" class="ps-doc-editor-notice ps-doc-editor-notice--error">{{ collabError }}</p>
              <p v-else-if="exportMessage" class="ps-doc-editor-notice ps-doc-editor-notice--error">{{ exportMessage }}</p>
              <p v-else-if="uploadMessage" class="ps-doc-editor-notice ps-doc-editor-notice--error">{{ uploadMessage }}</p>
              <p v-else-if="uploadingImage" class="ps-doc-editor-notice">图片上传中…</p>
              <p v-else-if="exporting" class="ps-doc-editor-notice">正在导出…</p>
            </div>

            <div class="ps-doc-editor-body-row">
              <ProjectDocBlockGutter
                v-if="collabReady"
                :editor="editor"
                :can-write="canWrite"
              />
              <div v-if="!collabReady && !collabError" class="ps-doc-editor-loading">
                加载编辑器…
              </div>
              <EditorContent v-if="collabReady" :editor="editor" class="ps-doc-editor-content" />
              <ProjectDocCommentGutter
                v-if="collabReady"
                :editor="editor"
                :comment-count-by-pos="commentCountByPos"
                :active-anchor-pos="pendingCommentAnchor?.anchorPos ?? null"
                @comment-block="handleCommentBlock"
              />
            </div>
          </div>
        </AppScrollArea>

        <ProjectDocCommentPanel
          v-if="collabReady"
          ref="commentPanelRef"
          :open="commentsPanelOpen"
          :comments="blockComments"
          :loading="commentsLoading"
          :error-message="commentsErrorMessage"
          :submitting="commentSubmitting"
          :active-comment-id="activeCommentId"
          :pending-quote-text="pendingCommentAnchor?.quoteText ?? ''"
          :has-anchor="pendingCommentAnchor?.anchorPos != null"
          @close="closeCommentsPanel"
          @select="handleSelectComment"
          @delete="handleDeleteComment"
          @publish="handlePublishComment"
        />
      </div>
    </div>
  </div>
</template>
