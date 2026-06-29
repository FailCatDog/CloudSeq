import { createVNode, render } from 'vue'
import AppPromptProvider from '@/components/AppPromptProvider.vue'

/** @type {HTMLElement | null} */
let container = null

export const appPromptPlugin = {
  install(app) {
    if (container) return

    container = document.createElement('div')
    document.body.appendChild(container)

    const vnode = createVNode(AppPromptProvider)
    vnode.appContext = app._context
    render(vnode, container)
  },
}
