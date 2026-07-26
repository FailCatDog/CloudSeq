import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { resolveActiveProjectNavPath } from './projectNav.js'

const NAV = [
  '/workspace/project/board',
  '/workspace/project/gantt',
  '/workspace/project/weekly',
]

describe('resolveActiveProjectNavPath', () => {
  it('matches board by dyn route name', () => {
    assert.equal(
      resolveActiveProjectNavPath(
        { name: 'dyn-workspace-project-board', path: '/workspace/project/board', meta: { fullPath: '/workspace/project/board' } },
        NAV,
      ),
      '/workspace/project/board',
    )
  })

  it('matches gantt by fullPath when switching submenus', () => {
    assert.equal(
      resolveActiveProjectNavPath(
        { name: 'dyn-workspace-project-gantt', path: '/workspace/project/gantt', meta: { fullPath: '/workspace/project/gantt' } },
        NAV,
      ),
      '/workspace/project/gantt',
    )
  })

  it('matches weekly by path prefix', () => {
    assert.equal(
      resolveActiveProjectNavPath(
        { name: 'dyn-workspace-project-weekly', path: '/workspace/project/weekly', meta: {} },
        NAV,
      ),
      '/workspace/project/weekly',
    )
  })

  it('clears nav highlight on document/sheet routes', () => {
    assert.equal(
      resolveActiveProjectNavPath(
        { name: 'project-doc', path: '/workspace/project/doc/1', meta: { fullPath: '/workspace/project/doc/1' } },
        NAV,
      ),
      null,
    )
    assert.equal(
      resolveActiveProjectNavPath(
        { name: 'project-sheet', path: '/workspace/project/sheet/2', meta: { fullPath: '/workspace/project/sheet/2' } },
        NAV,
      ),
      null,
    )
  })

  it('does not match legacy project-board name alone (dyn names are source of truth)', () => {
    // If only legacy name is checked, this would falsely look like a miss when dyn name is used.
    assert.equal(
      resolveActiveProjectNavPath(
        { name: 'dyn-workspace-project-board', path: '/board', meta: { fullPath: '/workspace/project/board' } },
        NAV,
      ),
      '/workspace/project/board',
    )
  })
})
