import { computed, ref } from 'vue'
import {
  applyThemeToDocument,
  normalizeTheme,
  readStoredTheme,
  writeStoredTheme,
} from '@/constants/theme'
import { runThemeTransition } from '@/constants/themeTransition'

const initial =
  typeof document !== 'undefined' && document.documentElement.dataset.theme
    ? normalizeTheme(document.documentElement.dataset.theme)
    : readStoredTheme()
const theme = ref(initial)

if (typeof document !== 'undefined') {
  applyThemeToDocument(theme.value)
}

const applyThemeValue = (next) => {
  const resolved = normalizeTheme(next)
  theme.value = resolved
  writeStoredTheme(resolved)
  applyThemeToDocument(resolved)
  return resolved
}

export const useTheme = () => {
  const isDark = computed(() => theme.value === 'dark')

  const setTheme = (next, options = {}) => {
    const apply = () => {
      applyThemeValue(next)
    }
    return runThemeTransition(apply, options.origin ?? null)
  }

  const toggleTheme = (options = {}) => {
    return setTheme(theme.value === 'dark' ? 'light' : 'dark', options)
  }

  return { theme, isDark, setTheme, toggleTheme }
}
