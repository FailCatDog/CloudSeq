import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import {
  resolveThemeRevealOrigin,
  runThemeTransition,
  shouldAnimateThemeTransition,
} from './themeTransition.js'

describe('resolveThemeRevealOrigin', () => {
  it('computes radius large enough to cover viewport from bottom-left', () => {
    const { x, y, r } = resolveThemeRevealOrigin({ x: 40, y: 700 }, { width: 1200, height: 800 })
    assert.equal(x, 40)
    assert.equal(y, 700)
    assert.ok(r >= Math.hypot(1200 - 40, 700))
    assert.ok(r >= Math.hypot(40, 800 - 700))
  })

  it('defaults origin to bottom-left when point missing', () => {
    const { x, y, r } = resolveThemeRevealOrigin(null, { width: 1000, height: 600 })
    assert.equal(x, 0)
    assert.equal(y, 600)
    assert.equal(r, Math.hypot(1000, 600))
  })
})

describe('shouldAnimateThemeTransition', () => {
  it('animates only when View Transitions exist and motion is allowed', () => {
    assert.equal(
      shouldAnimateThemeTransition({ hasViewTransition: true, reducedMotion: false }),
      true,
    )
    assert.equal(
      shouldAnimateThemeTransition({ hasViewTransition: false, reducedMotion: false }),
      false,
    )
    assert.equal(
      shouldAnimateThemeTransition({ hasViewTransition: true, reducedMotion: true }),
      false,
    )
  })
})

describe('runThemeTransition', () => {
  it('applies immediately when View Transitions unavailable', async () => {
    let applied = 0
    const env = {
      document: {
        documentElement: { style: { setProperty() {} }, clientWidth: 800, clientHeight: 600 },
      },
      innerWidth: 800,
      innerHeight: 600,
      matchMedia: () => ({ matches: false }),
    }
    await runThemeTransition(() => {
      applied += 1
    }, { x: 10, y: 20 }, env)
    assert.equal(applied, 1)
  })

  it('sets CSS vars and runs startViewTransition when available', async () => {
    const props = {}
    let applied = 0
    let capturedApply = null
    const env = {
      document: {
        documentElement: {
          clientWidth: 800,
          clientHeight: 600,
          style: {
            setProperty(key, value) {
              props[key] = value
            },
          },
        },
        startViewTransition(cb) {
          capturedApply = cb
          cb()
          return { finished: Promise.resolve() }
        },
      },
      innerWidth: 800,
      innerHeight: 600,
      matchMedia: () => ({ matches: false }),
    }
    await runThemeTransition(() => {
      applied += 1
    }, { x: 40, y: 500 }, env)
    assert.equal(applied, 1)
    assert.equal(typeof capturedApply, 'function')
    assert.equal(props['--theme-x'], '40px')
    assert.equal(props['--theme-y'], '500px')
    assert.ok(Number.parseFloat(props['--theme-r']) > 0)
  })
})
