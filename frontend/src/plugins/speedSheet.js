let installPromise = null

export async function ensureSpeedSheetUi(app) {
  if (!app) return
  if (app.config.globalProperties.$speedSheetUiInstalled) return
  if (installPromise) return installPromise

  installPromise = (async () => {
    const { installSpeedSheetUi } = await import('@speed-sheet/vue3-antd')
    await import('@speed-sheet/vue3-antd/style.css')
    await import('speed-components-ui/dist/style.css')
    installSpeedSheetUi(app, { locale: 'zh' })
    app.config.globalProperties.$speedSheetUiInstalled = true
  })()

  return installPromise
}

export async function loadSpeedSheetComponent() {
  await import('@speed-sheet/vue3-antd/style.css')
  await import('speed-components-ui/dist/style.css')
  const mod = await import('@speed-sheet/vue3-antd')
  return mod.SpeedSheet
}
