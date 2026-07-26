/**
 * Circular theme reveal helpers (View Transitions API).
 */

export const resolveThemeRevealOrigin = (point, viewport = { width: 0, height: 0 }) => {
  const width = Math.max(0, Number(viewport.width) || 0)
  const height = Math.max(0, Number(viewport.height) || 0)
  const x = Number.isFinite(point?.x) ? point.x : 0
  const y = Number.isFinite(point?.y) ? point.y : height
  const r = Math.hypot(Math.max(x, width - x), Math.max(y, height - y))
  return { x, y, r }
}

export const shouldAnimateThemeTransition = ({
  hasViewTransition = false,
  reducedMotion = false,
} = {}) => Boolean(hasViewTransition) && !reducedMotion

/**
 * Apply theme inside a circular View Transition when supported.
 * @param {() => void} apply synchronous DOM/theme mutation
 * @param {{ x?: number, y?: number } | null} origin viewport coordinates (button center)
 * @param {Window & typeof globalThis} [env]
 */
export const runThemeTransition = async (apply, origin = null, env = globalThis) => {
  const doc = env.document
  const reducedMotion = Boolean(env.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches)
  const hasViewTransition = typeof doc?.startViewTransition === 'function'

  if (!shouldAnimateThemeTransition({ hasViewTransition, reducedMotion })) {
    apply()
    return
  }

  const viewport = {
    width: env.innerWidth ?? doc?.documentElement?.clientWidth ?? 0,
    height: env.innerHeight ?? doc?.documentElement?.clientHeight ?? 0,
  }
  const { x, y, r } = resolveThemeRevealOrigin(origin, viewport)
  const root = doc.documentElement
  root.style.setProperty('--theme-x', `${x}px`)
  root.style.setProperty('--theme-y', `${y}px`)
  root.style.setProperty('--theme-r', `${Math.ceil(r)}px`)

  const transition = doc.startViewTransition(() => {
    apply()
  })

  try {
    await transition.finished
  } catch {
    // Transition may be skipped / aborted — theme already applied.
  }
}
