import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import {
  DEFAULT_THEME,
  THEME_STORAGE_KEY,
  applyThemeToDocument,
  normalizeTheme,
  readStoredTheme,
  writeStoredTheme,
} from './theme.js'

describe('normalizeTheme', () => {
  it('accepts dark and light', () => {
    assert.equal(normalizeTheme('dark'), 'dark')
    assert.equal(normalizeTheme('light'), 'light')
  })

  it('falls back to default for junk', () => {
    assert.equal(normalizeTheme('neon'), DEFAULT_THEME)
    assert.equal(normalizeTheme(null), DEFAULT_THEME)
    assert.equal(normalizeTheme(''), DEFAULT_THEME)
  })
})

describe('readStoredTheme / writeStoredTheme', () => {
  it('defaults when key missing', () => {
    const store = {
      getItem: () => null,
      setItem() {},
      removeItem() {},
    }
    assert.equal(readStoredTheme(store), DEFAULT_THEME)
  })

  it('round-trips light', () => {
    const map = new Map()
    const store = {
      getItem: (k) => (map.has(k) ? map.get(k) : null),
      setItem: (k, v) => map.set(k, String(v)),
      removeItem: (k) => map.delete(k),
    }
    writeStoredTheme('light', store)
    assert.equal(map.get(THEME_STORAGE_KEY), 'light')
    assert.equal(readStoredTheme(store), 'light')
  })

  it('ignores storage that throws', () => {
    const bad = {
      getItem() {
        throw new Error('blocked')
      },
      setItem() {
        throw new Error('blocked')
      },
    }
    assert.equal(readStoredTheme(bad), DEFAULT_THEME)
    assert.doesNotThrow(() => writeStoredTheme('dark', bad))
  })
})

describe('applyThemeToDocument', () => {
  it('sets data-theme on documentElement', () => {
    const el = { dataset: {} }
    const doc = { documentElement: el }
    applyThemeToDocument('light', doc)
    assert.equal(el.dataset.theme, 'light')
  })
})
