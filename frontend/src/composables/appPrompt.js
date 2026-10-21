import { reactive } from 'vue'
import { TABLE_DEFAULT_SIZE } from '@/constants/docBlockFormats'

/** @typedef {'prompt' | 'confirm' | 'tableSize'} AppPromptMode */

/**
 * @typedef {Object} AppPromptOpenOptions
 * @property {string} [title]
 * @property {string} [message]
 * @property {string} [label]
 * @property {string} [defaultValue]
 * @property {string} [placeholder]
 * @property {string} [confirmLabel]
 * @property {string} [cancelLabel]
 * @property {boolean} [danger]
 */

export const appPromptState = reactive({
  open: false,
  /** @type {AppPromptMode} */
  mode: 'prompt',
  title: '',
  message: '',
  label: '',
  defaultValue: '',
  placeholder: '',
  confirmLabel: '确定',
  cancelLabel: '取消',
  confirmDanger: false,
  tableRows: TABLE_DEFAULT_SIZE.rows,
  tableCols: TABLE_DEFAULT_SIZE.cols,
  tableWithHeaderRow: TABLE_DEFAULT_SIZE.withHeaderRow,
})

/** @type {((value: string | boolean | null) => void) | null} */
let pendingResolve = null

const closePrompt = () => {
  appPromptState.open = false
  pendingResolve = null
}

/**
 * @param {string | boolean | null} value
 */
export const finishAppPrompt = (value) => {
  const resolve = pendingResolve
  closePrompt()
  resolve?.(value)
}

/**
 * 单行输入弹窗，取消返回 null
 * @param {AppPromptOpenOptions} [options]
 * @returns {Promise<string | null>}
 */
export const openAppPrompt = (options = {}) => {
  return new Promise((resolve) => {
    if (pendingResolve) finishAppPrompt(null)

    pendingResolve = resolve
    Object.assign(appPromptState, {
      open: true,
      mode: 'prompt',
      title: options.title ?? '输入',
      message: options.message ?? '',
      label: options.label ?? '',
      defaultValue: options.defaultValue ?? '',
      placeholder: options.placeholder ?? '',
      confirmLabel: options.confirmLabel ?? '确定',
      cancelLabel: options.cancelLabel ?? '取消',
      confirmDanger: false,
    })
  })
}

/**
 * 表格尺寸弹窗，取消返回 null
 * @param {{ title?: string, rows?: number, cols?: number, withHeaderRow?: boolean }} [options]
 * @returns {Promise<{ rows: number, cols: number, withHeaderRow: boolean } | null>}
 */
export const openAppTableSize = (options = {}) => {
  return new Promise((resolve) => {
    if (pendingResolve) finishAppPrompt(null)

    pendingResolve = resolve
    Object.assign(appPromptState, {
      open: true,
      mode: 'tableSize',
      title: options.title ?? '插入表格',
      message: '',
      label: '',
      defaultValue: '',
      placeholder: '',
      tableRows: options.rows ?? TABLE_DEFAULT_SIZE.rows,
      tableCols: options.cols ?? TABLE_DEFAULT_SIZE.cols,
      tableWithHeaderRow: options.withHeaderRow ?? TABLE_DEFAULT_SIZE.withHeaderRow,
      confirmLabel: '插入',
      cancelLabel: '取消',
      confirmDanger: false,
    })
  })
}

/**
 * 确认弹窗，确定 true / 取消 false
 * @param {AppPromptOpenOptions} [options]
 * @returns {Promise<boolean>}
 */
export const openAppConfirm = (options = {}) => {
  return new Promise((resolve) => {
    if (pendingResolve) finishAppPrompt(false)

    pendingResolve = (value) => resolve(Boolean(value))
    Object.assign(appPromptState, {
      open: true,
      mode: 'confirm',
      title: options.title ?? '确认',
      message: options.message ?? '',
      label: '',
      defaultValue: '',
      placeholder: '',
      confirmLabel: options.confirmLabel ?? '确定',
      cancelLabel: options.cancelLabel ?? '取消',
      confirmDanger: Boolean(options.danger),
    })
  })
}
