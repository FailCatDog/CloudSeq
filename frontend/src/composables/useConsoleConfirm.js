import { useDialog } from 'naive-ui'

/**
 * 教师端 / 管理端确认弹窗
 * @returns {{ confirm: (options: { title?: string, message?: string, confirmLabel?: string, cancelLabel?: string, danger?: boolean }) => Promise<boolean> }}
 */
export const useConsoleConfirm = () => {
  const dialog = useDialog()

  const confirm = ({
    title = '确认',
    message = '',
    confirmLabel = '确定',
    cancelLabel = '取消',
    danger = false,
  } = {}) =>
    new Promise((resolve) => {
      const options = {
        title,
        content: message,
        positiveText: confirmLabel,
        negativeText: cancelLabel,
        onPositiveClick: () => {
          resolve(true)
          return true
        },
        onNegativeClick: () => {
          resolve(false)
          return true
        },
        onClose: () => resolve(false),
        onMaskClick: () => resolve(false),
      }

      if (danger) {
        dialog.error(options)
        return
      }
      dialog.warning(options)
    })

  return { confirm }
}
