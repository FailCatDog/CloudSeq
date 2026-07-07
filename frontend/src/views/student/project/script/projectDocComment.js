import { isEditorDestroyed } from '@/utils/tiptapSafe'

export const getBlockQuoteText = (editor, pos) => {
  if (isEditorDestroyed(editor) || pos == null || pos < 0) return ''
  try {
    const node = editor.state.doc.nodeAt(pos)
    const text = node?.textContent?.trim() ?? ''
    return text.slice(0, 200)
  } catch {
    return ''
  }
}

export const getBlockAtSelection = (editor) => {
  if (isEditorDestroyed(editor)) return null
  try {
    const { from } = editor.state.selection
    const $pos = editor.state.doc.resolve(from)
    for (let depth = $pos.depth; depth > 0; depth -= 1) {
      const node = $pos.node(depth)
      if (!node.isBlock) continue
      const anchorPos = $pos.before(depth)
      return {
        anchorPos,
        quoteText: node.textContent?.trim().slice(0, 200) ?? '',
      }
    }
    return null
  } catch {
    return null
  }
}

export const focusCommentAnchor = (editor, anchorPos) => {
  if (isEditorDestroyed(editor) || anchorPos == null || anchorPos < 0) return false
  try {
    const docSize = editor.state.doc.content.size
    if (anchorPos > docSize) return false
    editor.chain().focus().setTextSelection(anchorPos).scrollIntoView().run()
    return true
  } catch {
    return false
  }
}

export const filterCommentsByAnchor = (comments, anchorPos) => {
  if (anchorPos == null) return []
  return comments.filter((item) => item.anchorPos === anchorPos)
}

export const countCommentsByAnchor = (comments) => {
  /** @type {Record<number, number>} */
  const counts = {}
  for (const item of comments) {
    if (item.anchorPos == null) continue
    counts[item.anchorPos] = (counts[item.anchorPos] ?? 0) + 1
  }
  return counts
}

export const sortDocumentComments = (comments) => {
  return [...comments].sort((left, right) => {
    const posDiff = (left.anchorPos ?? 0) - (right.anchorPos ?? 0)
    if (posDiff !== 0) return posDiff
    const leftTime = left.createDate ? new Date(left.createDate).getTime() : 0
    const rightTime = right.createDate ? new Date(right.createDate).getTime() : 0
    return leftTime - rightTime
  })
}
