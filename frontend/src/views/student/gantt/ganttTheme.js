import { normalizeTheme } from '../../../constants/theme.js'

/**
 * Map CloudSeq system theme → jordium-gantt-vue3 theme prop.
 * Gantt must follow system theme only (never independent gantt-theme storage).
 */
export const toGanttTheme = (systemTheme) => normalizeTheme(systemTheme)
