import { readFile, writeFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const target = join(root, 'node_modules/@speed-sheet/vue3-antd/dist/index.js')

const before = 'const y = String(b.key);\n      y === "tabColor-board"'
const after = 'const y = String(b.key).replace(/^item-/, ""); y === "tabColor-board"'

const source = await readFile(target, 'utf8')
if (source.includes('.replace(/^item-/')) {
  process.exit(0)
}
if (!source.includes(before)) {
  console.warn('[patch-speed-sheet-tab-menu] pattern not found — skip')
  process.exit(0)
}

await writeFile(target, source.replace(before, after))
console.log('[patch-speed-sheet-tab-menu] patched @speed-sheet/vue3-antd tab menu click handler')
