/** @typedef {{ key: string, label: string, divider?: boolean, danger?: boolean, disabled?: boolean }} TableMenuItem */

/**
 * @param {{ canDeleteRow: boolean, canDeleteCol: boolean }} state
 * @returns {TableMenuItem[]}
 */
export const buildTableContextMenuItems = ({ canDeleteRow, canDeleteCol }) => [
  { key: 'addRowBefore', label: '上方插入行' },
  { key: 'addRowAfter', label: '下方插入行' },
  { key: 'deleteRow', label: '删除行', disabled: !canDeleteRow },
  { key: 'sep-1', label: '', divider: true },
  { key: 'addColumnBefore', label: '左侧插入列' },
  { key: 'addColumnAfter', label: '右侧插入列' },
  { key: 'deleteColumn', label: '删除列', disabled: !canDeleteCol },
  { key: 'sep-2', label: '', divider: true },
  { key: 'alignLeft', label: '左对齐' },
  { key: 'alignCenter', label: '居中对齐' },
  { key: 'alignRight', label: '右对齐' },
  { key: 'sep-3', label: '', divider: true },
  { key: 'wrap', label: '自动换行' },
  { key: 'nowrap', label: '不换行' },
  { key: 'sep-4', label: '', divider: true },
  { key: 'deleteTable', label: '删除表格', danger: true },
]

/** @type {Record<string, (chain: import('@tiptap/core').ChainedCommands) => import('@tiptap/core').ChainedCommands>} */
export const TABLE_MENU_COMMANDS = {
  addRowBefore: (chain) => chain.addRowBefore(),
  addRowAfter: (chain) => chain.addRowAfter(),
  deleteRow: (chain) => chain.deleteRow(),
  addColumnBefore: (chain) => chain.addColumnBefore(),
  addColumnAfter: (chain) => chain.addColumnAfter(),
  deleteColumn: (chain) => chain.deleteColumn(),
  alignLeft: (chain) => chain.setCellAttribute('align', 'left'),
  alignCenter: (chain) => chain.setCellAttribute('align', 'center'),
  alignRight: (chain) => chain.setCellAttribute('align', 'right'),
  wrap: (chain) => chain.setCellAttribute('wrap', 'wrap'),
  nowrap: (chain) => chain.setCellAttribute('wrap', 'nowrap'),
  deleteTable: (chain) => chain.deleteTable(),
}
