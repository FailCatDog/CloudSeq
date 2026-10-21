import { Extension } from '@tiptap/core'
import { TABLE_DEFAULT_SIZE } from '@/constants/docBlockFormats'
import { isCursorInTable } from './projectDocTableUtils.js'
import { promptForLink } from './projectDocLink.js'

/** @typedef {{ id: string, keys: string, label: string }} ProjectDocShortcut */

/** @type {ProjectDocShortcut[]} */
export const PROJECT_DOC_SHORTCUTS = [
  { id: 'link', keys: 'Ctrl+Alt+K', label: '超链接' },
  { id: 'table', keys: 'Ctrl+Alt+T', label: '插入表格' },
  { id: 'bold', keys: 'Ctrl+B', label: '加粗' },
  { id: 'italic', keys: 'Ctrl+I', label: '斜体' },
  { id: 'underline', keys: 'Ctrl+U', label: '下划线' },
  { id: 'strike', keys: 'Ctrl+Shift+S', label: '删除线' },
  { id: 'code', keys: 'Ctrl+E', label: '行内代码' },
  { id: 'bulletList', keys: 'Ctrl+Shift+8', label: '无序列表' },
  { id: 'orderedList', keys: 'Ctrl+Shift+7', label: '有序列表' },
  { id: 'blockquote', keys: 'Ctrl+Shift+B', label: '引用' },
  { id: 'codeBlock', keys: 'Ctrl+Alt+C', label: '代码块' },
  { id: 'paragraph', keys: 'Ctrl+Alt+0', label: '正文' },
  { id: 'h1', keys: 'Ctrl+Alt+1', label: '一级标题' },
  { id: 'h2', keys: 'Ctrl+Alt+2', label: '二级标题' },
  { id: 'h3', keys: 'Ctrl+Alt+3', label: '三级标题' },
  { id: 'h4', keys: 'Ctrl+Alt+4', label: '四级标题' },
  { id: 'h5', keys: 'Ctrl+Alt+5', label: '五级标题' },
]

/** 表格内操作快捷键（光标在表格中时生效） */
export const PROJECT_DOC_TABLE_SHORTCUTS = [
  { id: 'tableNextCell', keys: 'Tab', label: '下一单元格' },
  { id: 'tablePrevCell', keys: 'Shift+Tab', label: '上一单元格' },
  { id: 'tableDelete', keys: 'Backspace / Delete', label: '删除表格（选中全部单元格）' },
  { id: 'tableAddRow', keys: 'Ctrl+Enter', label: '下方插入行（表格内）' },
]

/** @type {Record<string, string>} */
export const PROJECT_DOC_SHORTCUT_MAP = Object.fromEntries(
  PROJECT_DOC_SHORTCUTS.map((item) => [item.id, item.keys]),
)

export const getProjectDocShortcutKeys = (formatId) => PROJECT_DOC_SHORTCUT_MAP[formatId] ?? null

export const formatShortcutLabel = (keys) => {
  if (!keys) return null
  const isMac = typeof navigator !== 'undefined' && /Mac|iPhone|iPad|iPod/.test(navigator.platform)
  return isMac ? keys.replace(/Ctrl/g, '⌘') : keys
}

const runIfEditable = (editor, run) => {
  if (!editor.isEditable) return false
  return run()
}

/**
 * 文档编辑器快捷键（Mod = Ctrl / Cmd）
 */
export const ProjectDocKeyboardShortcuts = Extension.create({
  name: 'projectDocKeyboardShortcuts',
  priority: 1000,

  addKeyboardShortcuts() {
    const { editor } = this

    const mark = (apply) => () =>
      runIfEditable(editor, () => apply(editor.chain().focus()).run())

    const block = (apply) => () =>
      runIfEditable(editor, () => apply(editor.chain().focus()).run())

    return {
      'Mod-b': mark((chain) => chain.toggleBold()),
      'Mod-B': mark((chain) => chain.toggleBold()),
      'Mod-i': mark((chain) => chain.toggleItalic()),
      'Mod-I': mark((chain) => chain.toggleItalic()),
      'Mod-u': mark((chain) => chain.toggleUnderline()),
      'Mod-U': mark((chain) => chain.toggleUnderline()),
      'Mod-Shift-s': mark((chain) => chain.toggleStrike()),
      'Mod-Shift-S': mark((chain) => chain.toggleStrike()),
      'Mod-e': mark((chain) => chain.toggleCode()),
      'Mod-E': mark((chain) => chain.toggleCode()),
      'Mod-Shift-8': block((chain) => chain.toggleBulletList()),
      'Mod-Shift-7': block((chain) => chain.toggleOrderedList()),
      'Mod-Shift-b': block((chain) => chain.toggleBlockquote()),
      'Mod-Shift-B': block((chain) => chain.toggleBlockquote()),
      'Mod-Alt-c': block((chain) => chain.toggleCodeBlock()),
      'Mod-Alt-C': block((chain) => chain.toggleCodeBlock()),
      'Mod-Alt-0': block((chain) => chain.setParagraph()),
      'Mod-Alt-1': block((chain) => chain.toggleHeading({ level: 1 })),
      'Mod-Alt-2': block((chain) => chain.toggleHeading({ level: 2 })),
      'Mod-Alt-3': block((chain) => chain.toggleHeading({ level: 3 })),
      'Mod-Alt-4': block((chain) => chain.toggleHeading({ level: 4 })),
      'Mod-Alt-5': block((chain) => chain.toggleHeading({ level: 5 })),
      'Mod-Alt-t': block((chain) => chain.insertTable({
        rows: TABLE_DEFAULT_SIZE.rows,
        cols: TABLE_DEFAULT_SIZE.cols,
        withHeaderRow: TABLE_DEFAULT_SIZE.withHeaderRow,
      })),
      'Mod-Alt-T': block((chain) => chain.insertTable({
        rows: TABLE_DEFAULT_SIZE.rows,
        cols: TABLE_DEFAULT_SIZE.cols,
        withHeaderRow: TABLE_DEFAULT_SIZE.withHeaderRow,
      })),
      'Mod-Enter': () => {
        if (!editor.isEditable || !isCursorInTable(editor)) return false
        return editor.chain().focus().addRowAfter().run()
      },
      'Mod-Alt-k': () => {
        if (!editor.isEditable) return false
        void promptForLink(editor)
        return true
      },
      'Mod-Alt-K': () => {
        if (!editor.isEditable) return false
        void promptForLink(editor)
        return true
      },
    }
  },
})
