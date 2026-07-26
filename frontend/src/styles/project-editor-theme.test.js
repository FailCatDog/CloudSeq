import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const scssPath = join(dirname(fileURLToPath(import.meta.url)), 'project-editor-theme.scss')
const scss = readFileSync(scssPath, 'utf8')

describe('project editor theme sync', () => {
  it('maps doc editor chrome to system surface tokens', () => {
    assert.match(scss, /\.ps-doc-editor[\s\S]*?background:\s*var\(--wb-bg-page\)/)
    assert.match(scss, /\.ps-doc-outline[\s\S]*?background:\s*var\(--wb-card-bg\)/)
    assert.match(scss, /\.ps-doc-editor-content\s+\.tiptap[\s\S]*?color:\s*var\(--wb-text-primary\)/)
  })

  it('maps sheet editor chrome and forces sheet canvas surfaces', () => {
    assert.match(scss, /\.ps-sheet-editor[\s\S]*?background:\s*var\(--wb-bg-page\)/)
    assert.match(scss, /\.sheet-scroll/)
    assert.match(scss, /\.sheet-viewport/)
    assert.match(scss, /\.sheet-bar/)
    assert.match(scss, /\.sheet-tab\.active/)
    assert.match(scss, /background:\s*var\(--wb-card-bg\)\s*!important/)
  })

  it('approximates dark grid via canvas invert (SpeedSheet has no theme API)', () => {
    assert.match(
      scss,
      /\[data-theme=['"]dark['"]\]\s+\.ps-sheet-editor[\s\S]*?\.sheet-canvas[\s\S]*?filter:\s*invert\(1\)\s+hue-rotate\(180deg\)/,
    )
  })

  it('themes the formula bar under the toolbar', () => {
    assert.match(scss, /\.sheet-formula-bar[\s\S]*?background:\s*var\(--wb-card-bg\)/)
    assert.match(scss, /\.sheet-formula-bar\s+\.formula-input/)
  })
})
