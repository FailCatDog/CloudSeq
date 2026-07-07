import { TableCell } from '@tiptap/extension-table-cell'
import { TableHeader } from '@tiptap/extension-table-header'

const wrapAttribute = {
  default: null,
  parseHTML: (element) => {
    if (element.classList.contains('ps-table-cell--nowrap')) return 'nowrap'
    if (element.classList.contains('ps-table-cell--wrap')) return 'wrap'
    return null
  },
  renderHTML: (attributes) => {
    if (attributes.wrap === 'nowrap') {
      return { class: 'ps-table-cell--nowrap' }
    }
    if (attributes.wrap === 'wrap') {
      return { class: 'ps-table-cell--wrap' }
    }
    return {}
  },
}

export const ProjectDocTableCell = TableCell.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      wrap: wrapAttribute,
    }
  },
})

export const ProjectDocTableHeader = TableHeader.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      wrap: wrapAttribute,
    }
  },
})
