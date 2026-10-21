import Image from '@tiptap/extension-image'
import { mergeAttributes, ResizableNodeView } from '@tiptap/core'
import { isAssetProxyUrl, toAssetDisplayUrl, uploadDocumentAssetApi } from '@/api/asset'
import { resolveTopLevelBlock } from '@/constants/docBlockFormats'

const IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp'

const parseDimension = (value) => {
  if (value == null || value === '') return null
  const match = String(value).match(/^(\d+(?:\.\d+)?)/)
  if (!match) return null
  const num = Math.round(Number(match[1]))
  return Number.isFinite(num) && num > 0 ? num : null
}

const resolveDisplaySrc = (src) => (isAssetProxyUrl(src) ? toAssetDisplayUrl(src) : src)

const escapeHtmlAttr = (value) =>
  String(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;')

export const ProjectDocImage = Image.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: null,
        parseHTML: (element) =>
          parseDimension(element.getAttribute('width'))
          ?? parseDimension(element.style.width),
        renderHTML: (attributes) => (attributes.width ? { width: attributes.width } : {}),
      },
      height: {
        default: null,
        parseHTML: (element) =>
          parseDimension(element.getAttribute('height'))
          ?? parseDimension(element.style.height),
        renderHTML: (attributes) => (attributes.height ? { height: attributes.height } : {}),
      },
    }
  },

  renderHTML({ HTMLAttributes }) {
    const src = HTMLAttributes.src
    const displaySrc = resolveDisplaySrc(src)
    return [
      'img',
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, { src: displaySrc }),
    ]
  },

  parseHTML() {
    return [
      {
        tag: 'img[src]',
        getAttrs: (element) => {
          const rawSrc = element.getAttribute('src') ?? ''
          const pathOnly = rawSrc.split('?')[0]
          return {
            src: pathOnly,
            alt: element.getAttribute('alt') ?? null,
            title: element.getAttribute('title') ?? null,
          }
        },
      },
    ]
  },

  renderMarkdown(node) {
    const src = node.attrs?.src ?? ''
    const alt = node.attrs?.alt ?? ''
    const title = node.attrs?.title ?? ''
    const width = node.attrs?.width
    const height = node.attrs?.height

    if (width || height) {
      const attrs = [`src="${escapeHtmlAttr(src)}"`]
      if (alt) attrs.push(`alt="${escapeHtmlAttr(alt)}"`)
      if (title) attrs.push(`title="${escapeHtmlAttr(title)}"`)
      if (width) attrs.push(`width="${width}"`)
      if (height) attrs.push(`height="${height}"`)
      return `<img class="ps-doc-image" ${attrs.join(' ')} />`
    }

    return title ? `![${alt}](${src} "${title}")` : `![${alt}](${src})`
  },

  addNodeView() {
    if (!this.options.resize?.enabled || typeof document === 'undefined') {
      return null
    }

    const { directions, minWidth, minHeight, alwaysPreserveAspectRatio } = this.options.resize

    return ({ node, getPos, HTMLAttributes, editor }) => {
      const el = document.createElement('img')
      el.draggable = false

      const mergedAttributes = mergeAttributes(this.options.HTMLAttributes, HTMLAttributes)
      Object.entries(mergedAttributes).forEach(([key, value]) => {
        if (value == null || key === 'width' || key === 'height') return
        if (key === 'src') {
          el.setAttribute('src', resolveDisplaySrc(value))
        } else {
          el.setAttribute(key, value)
        }
      })

      const nodeView = new ResizableNodeView({
        element: el,
        editor,
        node,
        getPos,
        onResize: (width, height) => {
          el.style.width = `${width}px`
          el.style.height = `${height}px`
        },
        onCommit: (width, height) => {
          const pos = getPos()
          if (pos === undefined) return
          editor.chain().setNodeSelection(pos).updateAttributes(this.name, { width, height }).run()
        },
        onUpdate: (updatedNode) => {
          if (updatedNode.type !== node.type) return false

          const newSrc = updatedNode.attrs.src
          if (newSrc) {
            const newPath = newSrc.split('?')[0]
            const currentPath = (el.getAttribute('src') ?? '').split('?')[0]
            if (currentPath !== newPath) {
              el.setAttribute('src', resolveDisplaySrc(newSrc))
            }
          }

          const { width, height } = updatedNode.attrs
          if (width) el.style.width = `${width}px`
          if (height) el.style.height = `${height}px`

          return true
        },
        options: {
          directions,
          min: {
            width: minWidth,
            height: minHeight,
          },
          preserveAspectRatio: alwaysPreserveAspectRatio === true,
        },
      })

      const dom = nodeView.dom
      dom.style.visibility = 'hidden'
      dom.style.pointerEvents = 'none'
      el.onload = () => {
        dom.style.visibility = ''
        dom.style.pointerEvents = ''
      }

      return nodeView
    }
  },
}).configure({
  allowBase64: false,
  inline: false,
  HTMLAttributes: {
    class: 'ps-doc-image',
    loading: 'lazy',
  },
  resize: {
    enabled: true,
    directions: ['bottom-right'],
    minWidth: 80,
    minHeight: 80,
    alwaysPreserveAspectRatio: true,
  },
})

export const pickImageFile = () =>
  new Promise((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = IMAGE_ACCEPT
    input.style.display = 'none'
    document.body.appendChild(input)

    const cleanup = () => {
      input.remove()
    }

    input.addEventListener('change', () => {
      const file = input.files?.[0] ?? null
      cleanup()
      resolve(file)
    })

    input.addEventListener('blur', () => {
      window.setTimeout(() => {
        if (document.body.contains(input) && !input.files?.length) {
          cleanup()
          resolve(null)
        }
      }, 400)
    })

    input.click()
  })

export const insertImageInEditor = (editor, { src, alt, pos } = {}) => {
  if (!editor?.view || !src) return false

  let chain = editor.chain().focus()
  if (pos != null) {
    chain = chain.setTextSelection(pos)
  }
  return chain.setImage({ src, alt: alt ?? '' }).run()
}

export const uploadAndInsertImage = async (editor, nodeId, file, { pos } = {}) => {
  if (!editor?.view || !file || !nodeId) return false

  const asset = await uploadDocumentAssetApi(nodeId, file)
  return insertImageInEditor(editor, {
    src: asset.url,
    alt: asset.fileName,
    pos,
  })
}

export const insertImageAtRow = async (editor, row, nodeId) => {
  if (!editor?.view || !row || !nodeId) return false

  const file = await pickImageFile()
  if (!file) return false

  const asset = await uploadDocumentAssetApi(nodeId, file)
  const block = resolveTopLevelBlock(editor.state.doc, row.pos)
  if (!block) return false

  const { from, to, node } = block
  let chain = editor.chain().focus()

  if (node.isTextblock && !node.textContent?.trim()) {
    chain = chain.deleteRange({ from, to })
  } else {
    chain = chain.setTextSelection(to)
  }

  return chain.setImage({ src: asset.url, alt: asset.fileName }).run()
}

export const tryHandleImagePaste = (view, event, context) => {
  const { editor, nodeId, canWrite, onUploadStart, onUploadEnd, onUploadError } = context
  if (!canWrite || !editor || !nodeId) return false

  const items = Array.from(event.clipboardData?.items ?? [])
  const imageItem = items.find((item) => item.type.startsWith('image/'))
  if (!imageItem) return false

  const file = imageItem.getAsFile()
  if (!file) return false

  event.preventDefault()
  const pos = view.state.selection.from

  void (async () => {
    onUploadStart?.()
    try {
      await uploadAndInsertImage(editor, nodeId, file, { pos })
    } catch (error) {
      onUploadError?.(error?.message || '图片上传失败')
    } finally {
      onUploadEnd?.()
    }
  })()

  return true
}

export const tryHandleImageDrop = (view, event, moved, context) => {
  const { editor, nodeId, canWrite, onUploadStart, onUploadEnd, onUploadError } = context
  if (!canWrite || !editor || !nodeId || moved) return false

  const file = Array.from(event.dataTransfer?.files ?? []).find((item) =>
    item.type.startsWith('image/'),
  )
  if (!file) return false

  event.preventDefault()
  const coords = view.posAtCoords({ left: event.clientX, top: event.clientY })
  const pos = coords?.pos ?? view.state.selection.from

  void (async () => {
    onUploadStart?.()
    try {
      await uploadAndInsertImage(editor, nodeId, file, { pos })
    } catch (error) {
      onUploadError?.(error?.message || '图片上传失败')
    } finally {
      onUploadEnd?.()
    }
  })()

  return true
}

export const triggerImageUpload = async (editor, nodeId, callbacks = {}) => {
  const file = await pickImageFile()
  if (!file) return false

  callbacks.onUploadStart?.()
  try {
    return await uploadAndInsertImage(editor, nodeId, file)
  } catch (error) {
    callbacks.onUploadError?.(error?.message || '图片上传失败')
    return false
  } finally {
    callbacks.onUploadEnd?.()
  }
}
