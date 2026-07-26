import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const tokensPath = join(dirname(fileURLToPath(import.meta.url)), '_tokens.scss')
const tokens = readFileSync(tokensPath, 'utf8')

const lightBlock = () => {
  const match = tokens.match(/\[data-theme=['"]light['"]\]\s*\{([\s\S]*?)\n\}/)
  assert.ok(match, 'expected [data-theme=light] block')
  return match[1]
}

describe('light theme mint layering (approach A)', () => {
  it('uses mint page background instead of pure white', () => {
    const body = lightBlock()
    assert.match(body, /--wb-bg-page:\s*#f3faf7/)
    assert.doesNotMatch(body, /--wb-bg-page:\s*#ffffff/)
  })

  it('uses mint-tinted sidebar distinct from white cards', () => {
    const body = lightBlock()
    assert.match(body, /--wb-sidebar-bg:\s*#eef8f3/)
    assert.match(body, /--wb-card-bg:\s*#ffffff/)
  })

  it('tints secondary surfaces and borders toward mint', () => {
    const body = lightBlock()
    assert.match(body, /--wb-search-bg:\s*#e6f5ee/)
    assert.match(body, /--wb-search-border:\s*#d8ebe3/)
  })
})
