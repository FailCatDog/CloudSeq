import { findParentNode } from '@tiptap/core'

const isTableCell = (node) => ['tableCell', 'tableHeader'].includes(node.type.name)

export const getTableSelectionContext = (editor) => {
  if (!editor?.state) return null

  const { selection } = editor.state
  const table = findParentNode((node) => node.type.name === 'table')(selection)
  if (!table) return null

  const cell = findParentNode(isTableCell)(selection)
  const firstRow = table.node.firstChild
  const rows = table.node.childCount
  const cols = firstRow?.childCount ?? 0

  return {
    rows,
    cols,
    align: cell?.node.attrs.align ?? null,
    wrap: cell?.node.attrs.wrap ?? null,
  }
}

export const isCursorInTable = (editor) => Boolean(getTableSelectionContext(editor))

export const getActiveTableWrapper = (editor) => {
  if (!editor?.view) return null

  const { from } = editor.state.selection
  const domAtPos = editor.view.domAtPos(from)
  let node = domAtPos.node

  if (node.nodeType === Node.TEXT_NODE) {
    node = node.parentElement
  }

  if (!(node instanceof Element)) return null
  return node.closest('.tableWrapper')
}

/**
 * 将选区定位到指定单元格，便于工具栏操作后仍能执行表格命令
 * @param {import('@tiptap/core').Editor} editor
 * @param {Element} cell
 */
export const focusTableCell = (editor, cell) => {
  if (!editor?.view || !(cell instanceof Element)) return false

  try {
    const pos = editor.view.posAtDOM(cell, 0)
    return editor.chain().focus().setTextSelection(pos + 1).run()
  } catch {
    return false
  }
}

/**
 * 在选区已离开表格时，恢复到最近一次表格内选区或该表首个单元格
 * @param {import('@tiptap/core').Editor} editor
 * @param {number | null} savedPos
 * @param {Element | null} wrapper
 */
export const ensureTableCellSelection = (editor, savedPos, wrapper = null) => {
  if (!editor?.view) return false
  if (isCursorInTable(editor)) return true

  if (savedPos != null) {
    return editor.chain().focus().setTextSelection(savedPos).run()
  }

  const tableWrapper = wrapper ?? getActiveTableWrapper(editor)
  const cell = tableWrapper?.querySelector('td, th')
  if (!cell) return false
  return focusTableCell(editor, cell)
}
