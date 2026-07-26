import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { toGanttTheme } from './ganttTheme.js'

describe('toGanttTheme', () => {
  it('maps system light to gantt light', () => {
    assert.equal(toGanttTheme('light'), 'light')
  })

  it('maps system dark to gantt dark', () => {
    assert.equal(toGanttTheme('dark'), 'dark')
  })

  it('falls back to dark for unknown (matches system default)', () => {
    assert.equal(toGanttTheme('neon'), 'dark')
    assert.equal(toGanttTheme(null), 'dark')
  })

  it('does not read an independent gantt-theme preference', () => {
    // Contract: gantt theme is derived only from the system theme argument.
    assert.equal(toGanttTheme('light', { storedGanttTheme: 'dark' }), 'light')
  })
})
