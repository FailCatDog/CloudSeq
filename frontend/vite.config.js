import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const dayjsPluginAliases = Object.fromEntries(
  [
    'advancedFormat',
    'customParseFormat',
    'localeData',
    'quarterOfYear',
    'weekOfYear',
    'weekYear',
    'weekday',
  ].map((plugin) => [
    `dayjs/plugin/${plugin}`,
    fileURLToPath(new URL(`./node_modules/dayjs/esm/plugin/${plugin}/index.js`, import.meta.url)),
  ]),
)

/** @speed-sheet/vue3-antd 页签菜单 MenuItem key 带 item- 前缀，导致 delete/rename 等动作无法匹配 */
function fixSpeedSheetTabMenu() {
  const patch = (code) => {
    if (!code.includes('tabColor-board') || code.includes('.replace(/^item-/')) return code
    return code.replace(
      /const y = String\(b\.key\);\s*\n?\s*y === "tabColor-board"/,
      'const y = String(b.key).replace(/^item-/, ""); y === "tabColor-board"',
    )
  }

  return {
    name: 'fix-speed-sheet-tab-menu',
    enforce: 'pre',
    transform(code, id) {
      if (!id.includes('@speed-sheet/vue3-antd') || !id.endsWith('.js')) return null
      const next = patch(code)
      return next === code ? null : { code: next, map: null }
    },
    config() {
      return {
        optimizeDeps: {
          esbuildOptions: {
            plugins: [{
              name: 'fix-speed-sheet-tab-menu-esbuild',
              setup(build) {
                build.onLoad({ filter: /vue3-antd.*\/dist\/index\.js$/ }, async (args) => {
                  const contents = await import('node:fs/promises').then((fs) => fs.readFile(args.path, 'utf8'))
                  const next = patch(contents)
                  return next === contents ? null : { contents: next, loader: 'js' }
                })
              },
            }],
          },
        },
      }
    },
  }
}

export default defineConfig({
  plugins: [vue(), fixSpeedSheetTabMenu()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      vuedraggable: fileURLToPath(new URL('./node_modules/vuedraggable/src/vuedraggable.js', import.meta.url)),
      sortablejs: fileURLToPath(new URL('./node_modules/sortablejs/modular/sortable.esm.js', import.meta.url)),
      ...dayjsPluginAliases,
    },
    dedupe: ['yjs', 'y-protocols', 'lib0', 'dayjs', 'vue'],
  },
  optimizeDeps: {
    include: [
      'dayjs',
      'dayjs/plugin/advancedFormat',
      'dayjs/plugin/customParseFormat',
      'dayjs/plugin/localeData',
      'dayjs/plugin/quarterOfYear',
      'dayjs/plugin/weekOfYear',
      'dayjs/plugin/weekYear',
      'dayjs/plugin/weekday',
      'vuedraggable',
      'sortablejs',
      'ant-design-vue/es/vc-picker/generate/dayjs',
      '@speed-sheet/vue3-antd',
      '@speed-sheet/vue3',
      '@speed-sheet/core',
      '@speed-sheet/shared',
      'speed-components-ui',
    ],
    exclude: [
      'yjs',
      '@hocuspocus/provider',
    ],
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:19001',
        changeOrigin: true,
      },
      '/collab': {
        target: 'ws://localhost:1234',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/collab/, ''),
      },
    },
  },
})
