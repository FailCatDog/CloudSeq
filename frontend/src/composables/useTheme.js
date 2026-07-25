import { computed, ref } from 'vue'
import {
  applyThemeToDocument,
  normalizeTheme,
  readStoredTheme,
  writeStoredTheme,
} from '@/constants/theme'

const initial =
  typeof document !== 'undefined' && document.documentElement.dataset.theme
    ? normalizeTheme(document.documentElement.dataset.theme)
    : readStoredTheme()
const theme = ref(initial)

if (typeof document !== 'undefined') {
  applyThemeToDocument(theme.value)
}

export const useTheme = () => {
  const isDark = computed(() => theme.value === 'dark')

  const setTheme = (next) => {
    const resolved = normalizeTheme(next)
    theme.value = resolved
    writeStoredTheme(resolved)
    applyThemeToDocument(resolved)
  }

  const toggleTheme = () => {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, isDark, setTheme, toggleTheme }
}
