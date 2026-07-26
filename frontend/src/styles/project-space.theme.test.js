import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const scssPath = join(dirname(fileURLToPath(import.meta.url)), 'project-space.scss')
const scss = readFileSync(scssPath, 'utf8')

/** Extract a top-level rule body for an exact selector (no nested blocks). */
const ruleBody = (selector) => {
  const re = new RegExp(
    `${selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*\\{([^}]*)\\}`,
    'm',
  )
  const match = scss.match(re)
  assert.ok(match, `expected rule for ${selector}`)
  return match[1]
}

describe('project-space sidebar theme tokens', () => {
  it('ps-nav-item default color uses sidebar icon token (readable on light bg)', () => {
    const body = ruleBody('.ps-nav-item')
    assert.match(body, /color:\s*var\(--wb-sidebar-icon\)/)
    assert.doesNotMatch(body, /color:\s*rgba\(\s*255\s*,\s*255\s*,\s*255/)
  })

  it('ps-nav-item:hover uses theme tokens instead of white', () => {
    const body = ruleBody('.ps-nav-item:hover')
    assert.match(body, /color:\s*var\(--wb-sidebar-label\)/)
    assert.doesNotMatch(body, /color:\s*rgba\(\s*255\s*,\s*255\s*,\s*255/)
  })

  it('ps-doc-root-row default color uses sidebar token', () => {
    const body = ruleBody('.ps-doc-root-row')
    assert.match(body, /color:\s*var\(--wb-sidebar-/)
    assert.doesNotMatch(body, /color:\s*rgba\(\s*255\s*,\s*255\s*,\s*255/)
  })
})
