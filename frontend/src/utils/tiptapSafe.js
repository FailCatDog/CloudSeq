export function isEditorDestroyed(editor) {
  if (!editor) return true
  try {
    return editor.isDestroyed === true
  } catch {
    return true
  }
}

export function getEditorView(editor) {
  if (isEditorDestroyed(editor)) return null
  try {
    return editor.view ?? null
  } catch {
    return null
  }
}

export function getEditorViewDom(editor) {
  const view = getEditorView(editor)
  if (!view) return null
  try {
    return view.dom ?? null
  } catch {
    return null
  }
}

export function safeEditorOff(editor, event, handler) {
  if (isEditorDestroyed(editor)) return
  try {
    editor.off(event, handler)
  } catch {
    // editor already torn down
  }
}

export function safeEditorOn(editor, event, handler) {
  if (isEditorDestroyed(editor)) return false
  try {
    editor.on(event, handler)
    return true
  } catch {
    return false
  }
}
