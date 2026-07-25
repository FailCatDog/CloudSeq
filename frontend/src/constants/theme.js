export const THEME_STORAGE_KEY = 'cloudseq-theme'
export const DEFAULT_THEME = 'dark'

export const normalizeTheme = (value) => {
  if (value === 'dark' || value === 'light') return value
  return DEFAULT_THEME
}

export const readStoredTheme = (storage) => {
  try {
    const store = storage ?? (typeof localStorage !== 'undefined' ? localStorage : null)
    if (!store) return DEFAULT_THEME
    return normalizeTheme(store.getItem(THEME_STORAGE_KEY))
  } catch {
    return DEFAULT_THEME
  }
}

export const writeStoredTheme = (theme, storage) => {
  try {
    const store = storage ?? (typeof localStorage !== 'undefined' ? localStorage : null)
    if (!store) return
    store.setItem(THEME_STORAGE_KEY, normalizeTheme(theme))
  } catch {
    // ignore quota / private mode
  }
}

export const applyThemeToDocument = (theme, doc = typeof document !== 'undefined' ? document : null) => {
  if (!doc?.documentElement) return
  doc.documentElement.dataset.theme = normalizeTheme(theme)
}
