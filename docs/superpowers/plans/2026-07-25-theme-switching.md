# Theme Switching (Dark Violet / Light Mint) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship dark/light theme toggle with default dark violet surfaces, light mint-green accents, anti-flash boot, and Naive UI sync for the workbench shell.

**Architecture:** CSS custom properties on `html[data-theme]` (dark default on `:root`); pure theme helpers + `useTheme` composable sync `localStorage` (`cloudseq-theme`) with the DOM; root `ThemeProvider` (`NConfigProvider`) applies Naive `darkTheme` / light overrides; sidebar footer toggle. Hardcoded page-level hex cleanup beyond the shell is P1 (out of this plan’s acceptance).

**Tech Stack:** Vue 3, SCSS (`sass`), Naive UI 2.x (`darkTheme` + `theme-overrides`), Vite; Node.js built-in test runner (`node --test`) for pure helpers (no Vitest in repo).

**Spec:** `docs/superpowers/specs/2026-07-25-theme-switching-design.md`

## Global Constraints

- Themes: only `dark` | `light`. Default **`dark`** when storage missing or invalid.
- Storage key: exact string `cloudseq-theme`.
- Attribute: `document.documentElement.dataset.theme` → `html[data-theme="dark|light"]`.
- Keep CSS variable **names** `--wb-purple*` etc.; light theme changes **values** to green (no mass rename).
- Do **not** implement `prefers-color-scheme`, multi-brand themes, or server-persisted preference.
- P0 acceptance: shell (sidebar, main bg, cards, primary buttons, scrollbar) + Naive controls readable; no light flash on first paint.
- P1 (not this plan): bulk replace hardcoded `#xxx` in `project-space.scss` / dashboard gradients / gantt / editors.
- Frontend has no Vitest — test pure theme helpers with `node --test`.

---

## File Structure

| Path | Responsibility |
|------|----------------|
| `frontend/src/constants/theme.js` | `THEME_STORAGE_KEY`, `DEFAULT_THEME`, `normalizeTheme`, `readStoredTheme`, `writeStoredTheme`, `applyThemeToDocument` |
| `frontend/src/constants/theme.test.js` | Node tests for normalize / read / write / apply |
| `frontend/src/composables/useTheme.js` | Module-level `ref` + `setTheme` / `toggleTheme` / `isDark` |
| `frontend/src/styles/_tokens.scss` | Dark tokens on `:root` + `[data-theme="dark"]`; light overrides on `[data-theme="light"]` |
| `frontend/index.html` | Inline anti-flash script before app mount |
| `frontend/src/constants/naiveTheme.js` | `darkThemeOverrides` + `lightThemeOverrides` (replace single light-only export) |
| `frontend/src/components/ThemeProvider.vue` | Root `NConfigProvider` bound to `useTheme` |
| `frontend/src/App.vue` | Wrap `RouterView` with `ThemeProvider` |
| `frontend/src/components/console/ConsoleProvider.vue` | Use `useTheme` for `theme` + `theme-overrides` (nested OK) |
| `frontend/src/layout/AppLayout.vue` | Theme toggle in `.wb-sidebar-footer` |
| `frontend/src/styles/workbench.scss` | Toggle styles; ensure `.wb-main` uses page bg; fix shell-critical hardcoded surfaces that break dark |
| `DESIGN.md` / `.impeccable/design.json` | Document dual palettes (fold into final task) |

---

### Task 1: Theme constants + Node tests

**Files:**
- Create: `frontend/src/constants/theme.js`
- Create: `frontend/src/constants/theme.test.js`

**Interfaces:**
- Consumes: none
- Produces:
  - `THEME_STORAGE_KEY` = `'cloudseq-theme'`
  - `DEFAULT_THEME` = `'dark'`
  - `normalizeTheme(value: unknown): 'dark' | 'light'` — only those two; else `DEFAULT_THEME`
  - `readStoredTheme(storage?: Storage | null): 'dark' | 'light'` — try/catch; missing/invalid → default
  - `writeStoredTheme(theme: 'dark' | 'light', storage?: Storage | null): void` — try/catch no-throw
  - `applyThemeToDocument(theme: 'dark' | 'light', doc?: Document): void` — sets `doc.documentElement.dataset.theme`

- [ ] **Step 1: Write failing tests**

Create `frontend/src/constants/theme.test.js`:

```js
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
```

- [ ] **Step 2: Run tests — expect FAIL**

Run: `cd frontend && node --test src/constants/theme.test.js`  
Expected: FAIL (Cannot find module `./theme.js` or export errors)

- [ ] **Step 3: Implement `theme.js`**

```js
export const THEME_STORAGE_KEY = 'cloudseq-theme'
export const DEFAULT_THEME = 'dark'

export const normalizeTheme = (value) => {
  if (value === 'dark' || value === 'light') return value
  return DEFAULT_THEME
}

export const readStoredTheme = (storage) => {
  try {
    const store = storage ?? (typeof localStorage !== 'undefined' ? localStorage : null)
    if (!store) return DEFAULT_THEME
    return normalizeTheme(store.getItem(THEME_STORAGE_KEY))
  } catch {
    return DEFAULT_THEME
  }
}

export const writeStoredTheme = (theme, storage) => {
  try {
    const store = storage ?? (typeof localStorage !== 'undefined' ? localStorage : null)
    if (!store) return
    store.setItem(THEME_STORAGE_KEY, normalizeTheme(theme))
  } catch {
    // ignore quota / private mode
  }
}

export const applyThemeToDocument = (theme, doc = typeof document !== 'undefined' ? document : null) => {
  if (!doc?.documentElement) return
  doc.documentElement.dataset.theme = normalizeTheme(theme)
}
```

- [ ] **Step 4: Run tests — expect PASS**

Run: `cd frontend && node --test src/constants/theme.test.js`  
Expected: all tests pass

- [ ] **Step 5: Commit**

```bash
git add frontend/src/constants/theme.js frontend/src/constants/theme.test.js
git commit -m "feat(theme): add theme storage helpers and tests"
```

---

### Task 2: `useTheme` composable

**Files:**
- Create: `frontend/src/composables/useTheme.js`

**Interfaces:**
- Consumes: `readStoredTheme`, `writeStoredTheme`, `applyThemeToDocument`, `normalizeTheme`, `DEFAULT_THEME` from `@/constants/theme`
- Produces: `useTheme()` → `{ theme: Ref<'dark'|'light'>, isDark: ComputedRef<boolean>, setTheme(next), toggleTheme() }`  
  Module-level singleton `ref` so every caller shares state. `setTheme` writes storage + applies DOM + updates ref. On first import in browser, initialize from `readStoredTheme()` and `applyThemeToDocument` (idempotent with anti-flash script).

- [ ] **Step 1: Implement `useTheme.js`**

```js
import { computed, ref } from 'vue'
import {
  applyThemeToDocument,
  normalizeTheme,
  readStoredTheme,
  writeStoredTheme,
} from '@/constants/theme'

const theme = ref(
  typeof document !== 'undefined'
    ? normalizeTheme(document.documentElement.dataset.theme) || readStoredTheme()
    : readStoredTheme(),
)

if (typeof document !== 'undefined') {
  applyThemeToDocument(theme.value)
}

export const useTheme = () => {
  const isDark = computed(() => theme.value === 'dark')

  const setTheme = (next) => {
    const resolved = normalizeTheme(next)
    theme.value = resolved
    writeStoredTheme(resolved)
    applyThemeToDocument(resolved)
  }

  const toggleTheme = () => {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, isDark, setTheme, toggleTheme }
}
```

Note: Prefer reading `dataset.theme` first if anti-flash already set it; otherwise `readStoredTheme()`. Fix init to:

```js
const initial =
  typeof document !== 'undefined' && document.documentElement.dataset.theme
    ? normalizeTheme(document.documentElement.dataset.theme)
    : readStoredTheme()
const theme = ref(initial)
```

- [ ] **Step 2: Smoke-check import**

Run: `cd frontend && node -e "import('./src/constants/theme.js').then(m => console.log(m.DEFAULT_THEME))"`  
Expected: prints `dark`

- [ ] **Step 3: Commit**

```bash
git add frontend/src/composables/useTheme.js
git commit -m "feat(theme): add useTheme composable"
```

---

### Task 3: Dual CSS tokens (default dark)

**Files:**
- Modify: `frontend/src/styles/_tokens.scss` (replace entire file content)

**Interfaces:**
- Consumes: none
- Produces: `:root, [data-theme="dark"]` = violet dark surfaces; `[data-theme="light"]` = white + mint green (reuse `--wb-purple*` names for accent)

- [ ] **Step 1: Replace `_tokens.scss` with dual themes**

Use these concrete values (adjust only if contrast fails manual QA):

```scss
// Design tokens — CSS custom properties (runtime theme)
// Default / dark = violet surfaces; light = white + mint green accents.
// Accent slot keeps --wb-purple* names; light theme recolors them to green.

@mixin wb-legacy-aliases {
  --bg: var(--wb-bg-page);
  --surface: var(--wb-card-bg);
  --surface-2: var(--wb-search-bg);
  --line: var(--wb-search-border);
  --text: var(--wb-text-primary);
  --muted: var(--wb-text-secondary);
  --primary: var(--wb-purple);
  --primary-weak: var(--wb-purple-soft);
  --shadow: var(--wb-card-shadow);
  --radius: var(--wb-radius-card);
  --radius-sm: var(--wb-radius-sm);
}

:root,
[data-theme='dark'] {
  color-scheme: dark;

  --wb-bg-page: #16141f;
  --wb-sidebar-width: 72px;
  --wb-sidebar-width-expanded: 220px;
  --wb-sidebar-bg: #12101a;
  --wb-sidebar-active: rgba(139, 92, 246, 0.28);
  --wb-sidebar-icon: rgba(244, 242, 250, 0.55);
  --wb-sidebar-icon-active: #c4b5fd;
  --wb-purple: #8b5cf6;
  --wb-purple-light: #a78bfa;
  --wb-purple-dark: #7c3aed;
  --wb-purple-deeper: #6d28d9;
  --wb-purple-soft: rgba(139, 92, 246, 0.18);
  --wb-purple-border: #5b4b8a;
  --wb-purple-alpha-06: rgba(139, 92, 246, 0.06);
  --wb-purple-alpha-08: rgba(139, 92, 246, 0.08);
  --wb-purple-alpha-10: rgba(139, 92, 246, 0.1);
  --wb-purple-alpha-12: rgba(139, 92, 246, 0.12);
  --wb-purple-alpha-14: rgba(139, 92, 246, 0.14);
  --wb-purple-alpha-16: rgba(139, 92, 246, 0.16);
  --wb-purple-alpha-20: rgba(139, 92, 246, 0.2);
  --wb-purple-alpha-28: rgba(124, 58, 237, 0.28);
  --wb-purple-alpha-35: rgba(167, 139, 250, 0.35);
  --wb-purple-alpha-45: rgba(139, 92, 246, 0.45);
  --wb-text-primary: #f4f2fa;
  --wb-text-secondary: #a8a4bc;
  --wb-text-muted: #7a7690;
  --wb-card-bg: #1f1c2b;
  --wb-search-bg: #2a2640;
  --wb-search-border: #3d3a52;
  --wb-card-shadow: 0 1px 3px rgba(0, 0, 0, 0.35), 0 8px 24px rgba(0, 0, 0, 0.28);
  --wb-radius-card: 20px;
  --wb-radius-sm: 10px;
  --wb-radius-pill: 999px;
  --wb-link-blue: #a78bfa;
  --wb-btn-dark: #0f0d16;
  --wb-progress-track: #2a2640;
  --wb-timeline-line: #3d3a52;
  --wb-code-bg: #2a2640;
  --wb-online-green: #22c55e;
  --wb-icon-purple: #8b5cf6;
  --wb-icon-dark: #c4b5fd;
  --wb-icon-orange: #f97316;

  --wb-tag-pending-bg: rgba(37, 99, 235, 0.22);
  --wb-tag-pending-text: #93c5fd;
  --wb-tag-success-bg: rgba(22, 163, 74, 0.22);
  --wb-tag-success-text: #86efac;
  --wb-tag-success-border: rgba(134, 239, 172, 0.45);
  --wb-tag-warning-bg: rgba(234, 88, 12, 0.2);
  --wb-tag-warning-text: #fdba74;
  --wb-tag-warning-border: rgba(253, 186, 116, 0.45);
  --wb-tag-danger-bg: rgba(220, 38, 38, 0.22);
  --wb-tag-danger-text: #fca5a5;
  --wb-tag-danger-border: rgba(252, 165, 165, 0.45);
  --wb-tag-danger-soft: rgba(220, 38, 38, 0.12);
  --wb-tag-muted-bg: #2a2640;
  --wb-tag-muted-text: #a8a4bc;
  --wb-tag-draft-bg: rgba(180, 83, 9, 0.25);
  --wb-tag-draft-text: #fcd34d;

  --scroll-size: 8px;
  --scroll-size-thin: 6px;
  --scroll-radius: 999px;
  --scroll-track: transparent;
  --scroll-thumb: rgba(196, 181, 253, 0.55);
  --scroll-thumb-hover: var(--wb-purple-light);
  --scroll-thumb-active: var(--wb-purple);
  --scroll-thumb-idle: rgba(196, 181, 253, 0.28);

  @include wb-legacy-aliases;
}

[data-theme='light'] {
  color-scheme: light;

  --wb-bg-page: #f5faf7;
  --wb-sidebar-bg: #1e3a2f;
  --wb-sidebar-active: rgba(16, 185, 129, 0.28);
  --wb-sidebar-icon: rgba(255, 255, 255, 0.55);
  --wb-sidebar-icon-active: #6ee7b7;
  --wb-purple: #10b981;
  --wb-purple-light: #34d399;
  --wb-purple-dark: #059669;
  --wb-purple-deeper: #047857;
  --wb-purple-soft: #d1fae5;
  --wb-purple-border: #6ee7b7;
  --wb-purple-alpha-06: rgba(16, 185, 129, 0.06);
  --wb-purple-alpha-08: rgba(16, 185, 129, 0.08);
  --wb-purple-alpha-10: rgba(16, 185, 129, 0.1);
  --wb-purple-alpha-12: rgba(16, 185, 129, 0.12);
  --wb-purple-alpha-14: rgba(16, 185, 129, 0.14);
  --wb-purple-alpha-16: rgba(16, 185, 129, 0.16);
  --wb-purple-alpha-20: rgba(16, 185, 129, 0.2);
  --wb-purple-alpha-28: rgba(5, 150, 105, 0.28);
  --wb-purple-alpha-35: rgba(52, 211, 153, 0.35);
  --wb-purple-alpha-45: rgba(16, 185, 129, 0.45);
  --wb-text-primary: #14241c;
  --wb-text-secondary: #5b7266;
  --wb-text-muted: #7a9186;
  --wb-card-bg: #ffffff;
  --wb-search-bg: #e8f2ec;
  --wb-search-border: #d5e5db;
  --wb-card-shadow: 0 1px 3px rgba(20, 36, 28, 0.04), 0 4px 16px rgba(20, 36, 28, 0.03);
  --wb-link-blue: #059669;
  --wb-btn-dark: #14241c;
  --wb-progress-track: #e8f2ec;
  --wb-timeline-line: #d5e5db;
  --wb-code-bg: #eef6f1;
  --wb-online-green: #16a34a;
  --wb-icon-purple: #10b981;
  --wb-icon-dark: #14241c;
  --wb-icon-orange: #f97316;

  --wb-tag-pending-bg: #dbeafe;
  --wb-tag-pending-text: #2563eb;
  --wb-tag-success-bg: #dcfce7;
  --wb-tag-success-text: #15803d;
  --wb-tag-success-border: #86efac;
  --wb-tag-warning-bg: #fff7ed;
  --wb-tag-warning-text: #ea580c;
  --wb-tag-warning-border: #fed7aa;
  --wb-tag-danger-bg: #fee2e2;
  --wb-tag-danger-text: #dc2626;
  --wb-tag-danger-border: #fecaca;
  --wb-tag-danger-soft: #fef2f2;
  --wb-tag-muted-bg: #f3f4f6;
  --wb-tag-muted-text: #6b7280;
  --wb-tag-draft-bg: #fef3c7;
  --wb-tag-draft-text: #b45309;

  --scroll-thumb: rgba(16, 185, 129, 0.55);
  --scroll-thumb-hover: var(--wb-purple-light);
  --scroll-thumb-active: var(--wb-purple);
  --scroll-thumb-idle: rgba(16, 185, 129, 0.28);

  @include wb-legacy-aliases;
}
```

Keep `--wb-sidebar-width*` and radii defined in dark block only if light does not redefine them (inherited from cascade when both match on same element — actually light block must **not** drop widths). Light block above omits widths/radii/`--scroll-size*` — those inherit from `:root` only if light doesn’t reset the whole custom property set. **Safer:** duplicate width/radius/scroll-size lines into the light block unchanged, or nest light as overrides of only color tokens.

**Required fix:** In light block, also set:

```scss
  --wb-sidebar-width: 72px;
  --wb-sidebar-width-expanded: 220px;
  --wb-radius-card: 20px;
  --wb-radius-sm: 10px;
  --wb-radius-pill: 999px;
  --scroll-size: 8px;
  --scroll-size-thin: 6px;
  --scroll-radius: 999px;
  --scroll-track: transparent;
```

- [ ] **Step 2: Ensure main shell uses page background**

In `frontend/src/styles/workbench.scss`, add to `.wb-main`:

```scss
.wb-main {
  flex: 1;
  margin-left: var(--wb-sidebar-width);
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  transition: margin-left 0.28s ease;
  min-width: 0;
  background: var(--wb-bg-page);
}
```

- [ ] **Step 3: Manual CSS sanity**

Run: `cd frontend && npm run build`  
Expected: build succeeds (Sass compiles)

- [ ] **Step 4: Commit**

```bash
git add frontend/src/styles/_tokens.scss frontend/src/styles/workbench.scss
git commit -m "feat(theme): add dark violet and light mint CSS tokens"
```

---

### Task 4: Anti-flash script in `index.html`

**Files:**
- Modify: `frontend/index.html`

**Interfaces:**
- Consumes: storage key `cloudseq-theme`, default `dark` (must match `theme.js`)
- Produces: `html[data-theme]` set before CSS/JS paint

- [ ] **Step 1: Insert inline script in `<head>` (first script, before fonts if possible)**

Immediately after `<meta name="viewport" ... />` (or as first child of `<head>` after charset), add:

```html
    <script>
      (function () {
        try {
          var key = 'cloudseq-theme'
          var stored = localStorage.getItem(key)
          var theme = stored === 'light' || stored === 'dark' ? stored : 'dark'
          document.documentElement.setAttribute('data-theme', theme)
        } catch (e) {
          document.documentElement.setAttribute('data-theme', 'dark')
        }
      })()
    </script>
```

Also set a fallback attribute on `<html>`:

```html
<html lang="zh-CN" data-theme="dark">
```

- [ ] **Step 2: Commit**

```bash
git add frontend/index.html
git commit -m "feat(theme): apply data-theme before paint to avoid flash"
```

---

### Task 5: Naive UI dark/light overrides + providers

**Files:**
- Modify: `frontend/src/constants/naiveTheme.js`
- Create: `frontend/src/components/ThemeProvider.vue`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/components/console/ConsoleProvider.vue`

**Interfaces:**
- Consumes: `useTheme()`, Naive `darkTheme`
- Produces:
  - `darkThemeOverrides` / `lightThemeOverrides` objects
  - Optional re-export `consoleThemeOverrides` → alias of `lightThemeOverrides` **or** remove and update imports (prefer update imports; no dead alias)
  - `ThemeProvider` wraps slot with `n-config-provider :theme="naiveTheme" :theme-overrides="overrides"`

- [ ] **Step 1: Rewrite `naiveTheme.js`**

```js
/** Naive UI theme overrides — aligned with _tokens.scss dark violet / light mint */

export const darkThemeOverrides = {
  common: {
    primaryColor: '#8b5cf6',
    primaryColorHover: '#7c3aed',
    primaryColorPressed: '#6d28d9',
    primaryColorSuppl: '#a78bfa',
    borderRadius: '10px',
    borderRadiusSmall: '8px',
    fontFamily:
      'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", sans-serif',
    textColor1: '#f4f2fa',
    textColor2: '#c4c2d0',
    textColor3: '#a8a4bc',
    placeholderColor: '#7a7690',
    bodyColor: '#1f1c2b',
    cardColor: '#1f1c2b',
    modalColor: '#1f1c2b',
    popoverColor: '#252232',
    dividerColor: '#3d3a52',
    borderColor: '#3d3a52',
    hoverColor: '#2a2640',
    pressedColor: '#16141f',
  },
  Button: {
    borderRadiusMedium: '10px',
    borderRadiusSmall: '8px',
    heightMedium: '36px',
    heightSmall: '32px',
    fontWeight: '600',
  },
  Card: {
    borderRadius: '20px',
    color: '#1f1c2b',
    titleFontWeight: '700',
  },
  DataTable: {
    borderRadius: '20px',
    thColor: '#252232',
    thTextColor: '#a8a4bc',
    tdColor: '#1f1c2b',
    thFontWeight: '600',
  },
  Input: {
    borderRadius: '10px',
    heightMedium: '36px',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '10px',
        heightMedium: '36px',
      },
    },
  },
  Drawer: {
    borderRadius: '0px',
  },
  Tag: {
    borderRadius: '999px',
  },
  Alert: {
    borderRadius: '12px',
  },
  Form: {
    labelFontWeight: '600',
  },
  Tree: {
    nodeBorderRadius: '8px',
  },
  Collapse: {
    titleFontWeight: '700',
  },
}

export const lightThemeOverrides = {
  common: {
    primaryColor: '#10b981',
    primaryColorHover: '#059669',
    primaryColorPressed: '#047857',
    primaryColorSuppl: '#34d399',
    borderRadius: '10px',
    borderRadiusSmall: '8px',
    fontFamily:
      'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", sans-serif',
    textColor1: '#14241c',
    textColor2: '#3d5248',
    textColor3: '#5b7266',
    placeholderColor: '#7a9186',
    bodyColor: '#ffffff',
    cardColor: '#ffffff',
    modalColor: '#ffffff',
    popoverColor: '#ffffff',
    dividerColor: '#d5e5db',
    borderColor: '#d5e5db',
    hoverColor: '#f5faf7',
    pressedColor: '#e8f2ec',
  },
  Button: {
    borderRadiusMedium: '10px',
    borderRadiusSmall: '8px',
    heightMedium: '36px',
    heightSmall: '32px',
    fontWeight: '600',
  },
  Card: {
    borderRadius: '20px',
    color: '#ffffff',
    titleFontWeight: '700',
  },
  DataTable: {
    borderRadius: '20px',
    thColor: '#f5faf7',
    thTextColor: '#5b7266',
    tdColor: '#ffffff',
    thFontWeight: '600',
  },
  Input: {
    borderRadius: '10px',
    heightMedium: '36px',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '10px',
        heightMedium: '36px',
      },
    },
  },
  Drawer: {
    borderRadius: '0px',
  },
  Tag: {
    borderRadius: '999px',
  },
  Alert: {
    borderRadius: '12px',
  },
  Form: {
    labelFontWeight: '600',
  },
  Tree: {
    nodeBorderRadius: '8px',
  },
  Collapse: {
    titleFontWeight: '700',
  },
}
```

Search/replace any `consoleThemeOverrides` import to use theme-aware provider instead (Task 5 Step 3).

- [ ] **Step 2: Create `ThemeProvider.vue`**

```vue
<template>
  <n-config-provider
    :theme="naivePreset"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <slot />
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { darkTheme, dateZhCN, zhCN } from 'naive-ui'
import { darkThemeOverrides, lightThemeOverrides } from '@/constants/naiveTheme'
import { useTheme } from '@/composables/useTheme'

const { isDark } = useTheme()
const naivePreset = computed(() => (isDark.value ? darkTheme : null))
const themeOverrides = computed(() =>
  isDark.value ? darkThemeOverrides : lightThemeOverrides,
)
</script>
```

- [ ] **Step 3: Wrap App + update ConsoleProvider**

`App.vue`:

```vue
<template>
  <ThemeProvider>
    <RouterView />
  </ThemeProvider>
</template>

<script setup>
import { RouterView } from 'vue-router'
import ThemeProvider from '@/components/ThemeProvider.vue'
</script>
```

`ConsoleProvider.vue` — keep dialog/message providers; sync theme (nested provider OK):

```vue
<template>
  <n-config-provider
    :theme="naivePreset"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <n-dialog-provider>
      <n-message-provider>
        <div class="console-root">
          <slot />
        </div>
      </n-message-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { darkTheme, dateZhCN, zhCN } from 'naive-ui'
import { darkThemeOverrides, lightThemeOverrides } from '@/constants/naiveTheme'
import { useTheme } from '@/composables/useTheme'

const { isDark } = useTheme()
const naivePreset = computed(() => (isDark.value ? darkTheme : null))
const themeOverrides = computed(() =>
  isDark.value ? darkThemeOverrides : lightThemeOverrides,
)
</script>
```

Grep for `consoleThemeOverrides` and remove leftover imports.

- [ ] **Step 4: Build**

Run: `cd frontend && npm run build`  
Expected: success

- [ ] **Step 5: Commit**

```bash
git add frontend/src/constants/naiveTheme.js frontend/src/components/ThemeProvider.vue frontend/src/App.vue frontend/src/components/console/ConsoleProvider.vue
git commit -m "feat(theme): wire Naive UI dark/light providers"
```

---

### Task 6: Sidebar theme toggle UI

**Files:**
- Modify: `frontend/src/layout/AppLayout.vue`
- Modify: `frontend/src/styles/workbench.scss` (toggle button styles near `.wb-sidebar-footer`)

**Interfaces:**
- Consumes: `useTheme().theme`, `toggleTheme`, `isDark`
- Produces: accessible toggle in sidebar footer (above or beside expand control)

- [ ] **Step 1: Add toggle button in footer**

In `.wb-sidebar-footer`, before the expand toggle button, add:

```vue
        <button
          type="button"
          class="wb-theme-toggle"
          :aria-label="isDark ? '切换为浅色主题' : '切换为深色主题'"
          @click="toggleTheme"
        >
          <span class="wb-theme-toggle__icon" aria-hidden="true">{{ isDark ? '浅' : '深' }}</span>
          <span class="wb-theme-toggle__label">{{ isDark ? '浅色' : '深色' }}</span>
        </button>
```

In script:

```js
import { useTheme } from '@/composables/useTheme'
const { isDark, toggleTheme } = useTheme()
```

- [ ] **Step 2: Style `.wb-theme-toggle`**

Add to `workbench.scss` near footer styles:

```scss
.wb-theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 40px;
  height: 40px;
  margin: 0 auto 10px;
  padding: 0;
  border-radius: var(--wb-radius-sm);
  background: transparent;
  color: var(--wb-sidebar-icon);
  transition: background 0.18s ease, color 0.18s ease;
}

.wb-theme-toggle:hover {
  background: var(--wb-sidebar-active);
  color: var(--wb-sidebar-icon-active);
}

.wb-theme-toggle__label {
  display: none;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.wb-sidebar.expanded .wb-theme-toggle {
  width: 100%;
  justify-content: flex-start;
  padding: 0 12px;
  margin-left: 0;
  margin-right: 0;
}

.wb-sidebar.expanded .wb-theme-toggle__label {
  display: inline;
}
```

- [ ] **Step 3: Manual check**

Run: `cd frontend && npm run dev`  
Verify: footer shows toggle; click flips `html[data-theme]`; expanded sidebar shows「浅色」/「深色」 label.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/layout/AppLayout.vue frontend/src/styles/workbench.scss
git commit -m "feat(theme): add sidebar dark/light toggle"
```

---

### Task 7: Shell-critical hardcoded color fixes (P0 only)

**Files:**
- Modify: `frontend/src/styles/workbench.scss` (targeted replacements only)

**Interfaces:**
- Consumes: CSS variables from `_tokens.scss`
- Produces: shell remains readable in dark (no `#fafafc` / pure white panels stuck on dark page)

- [ ] **Step 1: Replace shell-critical hardcoded backgrounds**

In `workbench.scss`, replace these patterns where they style shell chrome / generic cards (not decorative dashboard illustration gradients — those can wait for P1):

| Find | Replace with |
|------|----------------|
| `background: #fafafc;` (table/header-like shell) | `background: var(--wb-search-bg);` or `var(--wb-card-bg)` as appropriate |
| `color: #ffffff` on sidebar logo label | keep white **or** use `var(--wb-text-primary)` only if sidebar text stays light on both themes (sidebar is dark in both → `#ffffff` OK) |
| `border: 2px solid var(--wb-bg-page)` online dot | already tokenized — leave |

Do **not** rewrite large dashboard gradient blocks in this task (P1).

Also fix user menu / dropdown if they use hardcoded white:

Search: `rg -n "#[0-9a-fA-F]{3,8}" frontend/src/styles/workbench.scss` and fix only selectors under `.wb-sidebar*`, `.wb-user-menu*`, `.wb-main`, `.wb-page-content`, `.wb-card` (if a shared card class exists).

Example for user menu (adjust to match actual selectors):

```scss
.wb-user-menu {
  background: var(--wb-card-bg);
  border: 1px solid var(--wb-search-border);
  color: var(--wb-text-primary);
}
```

- [ ] **Step 2: Build + visual pass**

Run: `cd frontend && npm run build`  
Manual: dark default shell OK; toggle light → mint accents on buttons/active nav.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/styles/workbench.scss
git commit -m "fix(theme): tokenize shell-critical workbench surfaces"
```

---

### Task 8: Design docs sync + acceptance checklist

**Files:**
- Modify: `DESIGN.md` (colors section — note dual themes)
- Modify: `.impeccable/design.json` (`colorMeta` — document dark default + light mint primary)

**Interfaces:**
- Consumes: finalized token hex from Task 3
- Produces: docs match runtime tokens

- [ ] **Step 1: Update `DESIGN.md` colors header**

Document:

- Default theme: dark violet surfaces
- Light theme: white + `#10b981` accent
- Point to `_tokens.scss` as source of truth

- [ ] **Step 2: Update `.impeccable/design.json`**

Add/adjust `colorMeta` entries for dark page bg `#16141f`, light primary `#10b981`, keep Ordered Lilac for dark primary.

- [ ] **Step 3: Acceptance checklist (manual)**

- [ ] Clear `localStorage` key `cloudseq-theme` → reload → dark, no light flash
- [ ] Toggle to light → green accents, white cards, mint page; refresh stays light
- [ ] Clear key again → dark
- [ ] Teacher/admin console (Naive table/button) follows theme
- [ ] Auth route body background follows theme
- [ ] `node --test src/constants/theme.test.js` still passes

- [ ] **Step 4: Commit**

```bash
git add DESIGN.md .impeccable/design.json
git commit -m "docs: sync design tokens for dual theme"
```

---

## Spec coverage (self-review)

| Spec requirement | Task |
|------------------|------|
| Dark/light toggle + localStorage | 1, 2, 6 |
| Default dark | 1, 3, 4 |
| Violet dark surfaces globally (shell) | 3, 7 |
| Light white + mint green | 3, 5 |
| `data-theme` + CSS variables | 3, 4 |
| Anti-flash | 4 |
| Naive sync | 5 |
| Sidebar entry | 6 |
| P1 hardcoded bulk / editors | Explicitly out of plan |
| DESIGN.md / design.json | 8 |
| No system preference / server prefs | Honored (not implemented) |

## Placeholder / consistency check

- Storage key `cloudseq-theme` consistent in Tasks 1 and 4.
- API names: `normalizeTheme`, `useTheme`, `darkThemeOverrides`, `lightThemeOverrides`, `ThemeProvider`.
- No TBD steps remaining.
