import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const scssPath = join(dirname(fileURLToPath(import.meta.url)), 'gantt-theme-overrides.scss')
const scss = readFileSync(scssPath, 'utf8')

describe('gantt theme overrides', () => {
  it('forces system tokens onto dark subtree (beats child reinjection)', () => {
    assert.match(scss, /\[data-theme=['"]dark['"]\]/)
    assert.match(scss, /--gantt-bg-primary:\s*var\(--wb-card-bg\)\s*!important/)
    assert.match(scss, /\.gantt-root\s+\*/)
  })

  it('paints gantt-panel with page token instead of hardcoded white', () => {
    assert.match(scss, /\.gantt-panel[\s\S]*?background:\s*var\(--wb-bg-page\)\s*!important/)
  })
})
