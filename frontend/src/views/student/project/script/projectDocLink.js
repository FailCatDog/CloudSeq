import { openAppPrompt } from '@/composables/appPrompt'
import { LINK_URL_MAX_LENGTH } from '@/constants/fieldLimits'

/**
 * 通过弹窗为选中文本设置/移除超链接
 * @param {import('@tiptap/core').Editor} editor
 */
export const promptForLink = async (editor) => {
  const previousUrl = editor.getAttributes('link').href ?? ''
  const url = await openAppPrompt({
    title: '插入链接',
    label: '链接地址',
    defaultValue: previousUrl,
    placeholder: 'https://example.com',
    maxLength: LINK_URL_MAX_LENGTH,
    confirmLabel: '确定',
    cancelLabel: '取消',
  })

  if (url === null) return false

  if (url === '') {
    return editor.chain().focus().extendMarkRange('link').unsetLink().run()
  }

  const href = /^https?:\/\//i.test(url) || url.startsWith('/') ? url : `https://${url}`

  return editor.chain().focus().extendMarkRange('link').setLink({ href }).run()
}
