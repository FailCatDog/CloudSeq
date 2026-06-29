export const DOC_BLOCK_FORMATS = [
  {
    id: 'paragraph',
    label: '正文',
    short: 'T',
    description: '普通段落文本，用于正文内容',
    iconClass: 'ps-format-icon--text',
  },
  {
    id: 'h1',
    label: '一级标题',
    short: 'H1',
    description: '最大的章节标题，适合文档主标题',
    iconClass: 'ps-format-icon--h1',
  },
  {
    id: 'h2',
    label: '二级标题',
    short: 'H2',
    description: '次级章节标题，适合主要分节',
    iconClass: 'ps-format-icon--h2',
  },
  {
    id: 'h3',
    label: '三级标题',
    short: 'H3',
    description: '小节标题，适合段落分组',
    iconClass: 'ps-format-icon--h3',
  },
  {
    id: 'h4',
    label: '四级标题',
    short: 'H4',
    description: '更细粒度的标题层级',
    iconClass: 'ps-format-icon--h4',
  },
  {
    id: 'h5',
    label: '五级标题',
    short: 'H5',
    description: '最小的标题层级',
    iconClass: 'ps-format-icon--h5',
  },
  {
    id: 'orderedList',
    label: '有序列表',
    short: '1.',
    description: '带编号的列表，适合步骤或排序内容',
    iconClass: 'ps-format-icon--ordered',
  },
  {
    id: 'bulletList',
    label: '无序列表',
    short: '•',
    description: '项目符号列表，适合并列要点',
    iconClass: 'ps-format-icon--bullet',
  },
  {
    id: 'taskList',
    label: '待办列表',
    short: '☑',
    description: '可勾选的待办事项列表',
    iconClass: 'ps-format-icon--task',
  },
  {
    id: 'codeBlock',
    label: '代码块',
    short: '{}',
    description: '保留缩进与格式的代码片段',
    iconClass: 'ps-format-icon--code',
  },
  {
    id: 'blockquote',
    label: '引用',
    short: '“',
    description: '引用说明或摘录内容',
    iconClass: 'ps-format-icon--quote',
  },
  {
    id: 'horizontalRule',
    label: '分割线',
    short: '—',
    description: '将当前行替换为水平分隔线',
    iconClass: 'ps-format-icon--hr',
  },
  {
    id: 'table',
    label: '表格',
    short: '⊞',
    description: '插入表格（默认 1×3），支持粘贴 Markdown 表格',
    iconClass: 'ps-format-icon--table',
  },
]

/** 新建表格默认尺寸：1 行 × 3 列 */
export const TABLE_DEFAULT_ROWS = 1
export const TABLE_DEFAULT_COLS = 3
export const TABLE_DEFAULT_WITH_HEADER_ROW = false

export const TABLE_DEFAULT_SIZE = {
  rows: TABLE_DEFAULT_ROWS,
  cols: TABLE_DEFAULT_COLS,
  withHeaderRow: TABLE_DEFAULT_WITH_HEADER_ROW,
}

export const DOC_TABLE_BLOCK_FORMATS = [
  {
    id: 'deleteTable',
    label: '删除表格',
    short: '✕',
    description: '删除当前整个表格',
    iconClass: 'ps-format-icon--delete-table',
    danger: true,
  },
]

export const resolveTopLevelBlock = (doc, pos) => {
  const safePos = Math.min(Math.max(pos, 0), doc.content.size)
  const $pos = doc.resolve(safePos)

  if ($pos.nodeAfter?.isBlock && $pos.depth === 1) {
    const node = $pos.nodeAfter
    return { from: safePos, to: safePos + node.nodeSize, node }
  }

  for (let depth = $pos.depth; depth > 0; depth -= 1) {
    if ($pos.node(depth - 1).type.name === 'doc') {
      const node = $pos.node(depth)
      return { from: $pos.before(depth), to: $pos.after(depth), node }
    }
  }

  return null
}

const getEditableRange = (node, from, to) => {
  if (node.isTextblock) {
    return { from: from + 1, to: Math.max(from + 1, to - 1) }
  }

  if (node.type.name === 'blockquote' && node.firstChild?.isTextblock) {
    const innerFrom = from + 1
    return { from: innerFrom + 1, to: innerFrom + 1 + node.firstChild.content.size }
  }

  if (['bulletList', 'orderedList'].includes(node.type.name) && node.firstChild) {
    const listItem = node.firstChild
    const paragraph = listItem.firstChild
    if (paragraph?.isTextblock) {
      const innerFrom = from + 2
      return { from: innerFrom + 1, to: innerFrom + 1 + paragraph.content.size }
    }
  }

  return { from: from + 1, to: Math.max(from + 1, to - 1) }
}

export const insertTableAtRow = (editor, row, { rows, cols, withHeaderRow }) => {
  if (!editor?.view) return false

  const block = resolveTopLevelBlock(editor.state.doc, row.pos)
  if (!block) return false

  const { from, to, node } = block
  if (node.type.name === 'table') return false

  const chain = editor.chain().focus()
  if (node.isTextblock && !node.textContent?.trim()) {
    chain.deleteRange({ from, to })
  } else {
    chain.setTextSelection(to)
  }
  return chain.insertTable({ rows, cols, withHeaderRow }).run()
}

export const deleteTableAtRow = (editor, row) => {
  if (!editor?.view) return false

  const block = resolveTopLevelBlock(editor.state.doc, row.pos)
  if (!block || block.node.type.name !== 'table') return false

  const { from } = block
  return editor.chain().focus().setTextSelection(from + 1).deleteTable().run()
}

export const getBlockFormatsForRow = (editor, row) => {
  const block = resolveTopLevelBlock(editor.state.doc, row.pos)
  if (block?.node.type.name === 'table') return DOC_TABLE_BLOCK_FORMATS
  return DOC_BLOCK_FORMATS
}

export const applyBlockFormatToRow = (editor, row, formatId) => {
  if (!editor?.view) return false

  const block = resolveTopLevelBlock(editor.state.doc, row.pos)
  if (!block) return false

  const { from, to, node } = block

  if (formatId === 'horizontalRule') {
    return editor
      .chain()
      .focus()
      .deleteRange({ from, to })
      .insertContentAt(from, { type: 'horizontalRule' })
      .run()
  }

  if (formatId === 'taskList') {
    const text = node.textContent?.trim() || ''
    const markdown = `- [ ] ${text}\n`
    return editor
      .chain()
      .focus()
      .deleteRange({ from, to })
      .insertContentAt(from, markdown, { contentType: 'markdown' })
      .run()
  }

  const { from: innerFrom, to: innerTo } = getEditableRange(node, from, to)
  let chain = editor.chain().focus().setTextSelection({ from: innerFrom, to: innerTo })

  switch (formatId) {
    case 'paragraph':
      return chain.clearNodes().setParagraph().run()
    case 'h1':
    case 'h2':
    case 'h3':
    case 'h4':
    case 'h5':
      return chain.clearNodes().setHeading({ level: Number(formatId.slice(1)) }).run()
    case 'orderedList':
      return chain.clearNodes().toggleOrderedList().run()
    case 'bulletList':
      return chain.clearNodes().toggleBulletList().run()
    case 'codeBlock':
      return chain.clearNodes().setCodeBlock().run()
    case 'blockquote':
      return chain.clearNodes().setBlockquote().run()
    default:
      return false
  }
}
