# CloudSeq 深浅主题切换设计

> 日期：2026-07-25  
> 状态：已确认  
> 方案：CSS 变量双主题（紫调暗色表面 + 白/浅绿）+ Naive UI 同步；默认深色

---

## 1. 背景与目标

全局样式已迁至 SCSS，设计 token 集中在 `frontend/src/styles/_tokens.scss`（`:root` CSS 自定义属性）。当前产品外观是**浅色页面 + 深紫侧栏 + 紫罗兰强调色**；紫色几乎只出现在侧栏，主内容区是冷灰白（`#f4f6fa`），品牌感割裂。

### 1.1 目标

- 支持 **深色 / 浅色** 一键切换，即时生效并本地持久化
- **首次进入默认深色**
- **深色**：紫罗兰品牌贯穿全局——采用「紫调暗色表面」而非整页饱和紫底板
- **浅色**：白底 + 浅绿强调（替换当前「浅灰 + 紫强调」的浅色外观）
- 工作台壳层（侧栏、主区、卡片、按钮、滚动条）与 Naive UI 控件颜色与主题一致

### 1.2 非目标（本期不做）

- 跟随系统 `prefers-color-scheme`
- 多品牌 / 多套学院色主题
- 服务端存储用户主题偏好
- 第三方编辑器（TipTap / SpeedSheet / 甘特）像素级主题对齐（P1 尽量靠拢，不阻塞 P0）

---

## 2. 决策记录

| 决策 | 选择 | 说明 |
|------|------|------|
| 深色铺色方式 | 方案 A：紫调暗色表面 | 页面/卡片用深紫灰；Lilac 作强调；侧栏与主区同色系 |
| 浅色主色 | 浅绿（约 `#10b981` 一带） | 白卡 + 薄荷白页面底；侧栏同族 |
| 默认主题 | `dark` | 无 localStorage 记录时使用深色 |
| 持久化 | `localStorage` 键 `cloudseq-theme` | 值：`dark` \| `light` |
| 技术载体 | `html[data-theme]` + CSS 变量覆盖 | 与现有 `--wb-*` token 体系一致 |
| Naive UI | 按主题切换 `darkTheme` / 默认 + 各自 overrides | 与 token 对齐，避免控件仍为浅色硬编码 |
| 防闪烁 | `index.html` 内联脚本 | Vue 挂载前根据 storage（或默认 dark）设置 `data-theme` |

---

## 3. 视觉规范

### 3.1 深色（`data-theme="dark"`）— 紫调暗色表面

| Token 角色 | 方向（示意，实现时可微调） | 说明 |
|------------|---------------------------|------|
| `--wb-bg-page` | `#16141f` 一带 | 深紫灰底，非饱和紫 |
| `--wb-card-bg` | 比页面底略亮的紫灰 | 卡片/面板抬升 |
| `--wb-sidebar-bg` | 与页面同族，可略深或同色 | 消除「紫岛」割裂 |
| `--wb-purple` / 主色 | `#8b5cf6`（Ordered Lilac） | 按钮、选中、焦点、链接 |
| 文字 | 近白 / 浅紫灰次级 | 保证对比度 |
| 边框 / 分割线 | 低对比紫灰 | 不抢内容 |
| 滚动条 / 焦点环 | 淡紫 | 与主色呼应 |

原则：紫是**氛围与强调**，不是整页刷成 `#8b5cf6`。

### 3.2 浅色（`data-theme="light"`）— 白 + 浅绿

| Token 角色 | 方向（示意，实现时可微调） | 说明 |
|------------|---------------------------|------|
| `--wb-bg-page` | 极淡薄荷白 | 如 `#f5faf7` 一带 |
| `--wb-card-bg` | `#ffffff` | 纯白卡片 |
| 主色族（变量名仍为 `--wb-purple*` 等） | 柔和绿约 `#10b981` | 浅色下把「品牌主色」槽位的值改为绿，不改名 |
| `--wb-sidebar-bg` | 浅绿灰 / 淡墨绿 | 与主色同族 |
| 文字 | 深墨色 | 办公可读 |

### 3.3 语义色

成功 / 警告 / 危险 / 草稿等 tag 色在两套主题下**保留语义**，仅按明暗微调亮度，不强制跟主色走。

### 3.4 Token 命名策略

- **保留**现有 `--wb-*` / legacy `--bg`、`--primary` 等别名，浅色主题通过覆盖变量值实现「绿主色」
- 不在本期做大规模 rename（如 `--wb-purple` → `--wb-accent`）；若后续要 rename，单独开任务

---

## 4. 架构

```
index.html 内联脚本
  → 读 localStorage['cloudseq-theme'] || 'dark'
  → document.documentElement.dataset.theme = ...

Vue 启动
  → useTheme() composable 与 DOM 对齐（模块级单例 ref，非 Pinia）
  → 用户点击切换
       → 写 localStorage
       → 更新 dataset.theme
       → 更新 Naive UI theme + overrides

_tokens.scss
  :root, [data-theme="dark"] { /* 深色变量（默认） */ }
  [data-theme="light"] { /* 浅绿浅色覆盖 */ }
```

### 4.1 模块边界

| 单元 | 职责 | 依赖 |
|------|------|------|
| `_tokens.scss` | 两套 CSS 变量定义 | 无 |
| `composables/useTheme.js` | 读/写主题状态，同步 DOM + storage；导出 `theme`、`isDark`、`setTheme`、`toggleTheme` | `localStorage`、`document.documentElement` |
| `naiveTheme.js` | 导出 `darkThemeOverrides` / `lightThemeOverrides`（及是否挂载 Naive `darkTheme`） | token 色值约定 |
| 挂载 Naive 的 Provider（当前为 `ConsoleProvider` 的 `NConfigProvider`；学生壳若未包在内则补到根布局） | 按当前主题注入 `theme` + `theme-overrides` | `useTheme`、`naiveTheme` |
| 切换控件 | 侧栏底部 UI（文案「深色」「浅色」或图标 + `aria-label`） | `useTheme` |
| `index.html` 内联脚本 | 首屏防闪烁 | 与 storage 键名 `cloudseq-theme` 一致 |

### 4.2 数据流

1. 用户打开应用 → 内联脚本设好 `data-theme` → 首屏 CSS 已是正确主题  
2. Vue 挂载 → `useTheme` 初始化，与 DOM 一致  
3. 用户切换 → storage + DOM + Naive 三者同步  

### 4.3 错误与边界

- `localStorage` 不可用：仅内存 + DOM，默认 dark；不抛错打断启动  
- 非法 storage 值：回退 `dark`  
- 主题切换不触发路由刷新、不丢编辑态  

---

## 5. UI 入口

- 位置：侧栏底部，用户菜单附近  
- 交互：切换 `dark` ↔ `light`（按钮或分段控件）  
- 无障碍：`aria-label` 明确「切换为浅色主题」/「切换为深色主题」  

---

## 6. 实施范围

### P0（本期可交付）

1. `_tokens.scss` 双主题变量；`:root` 与 `[data-theme="dark"]` 对齐默认深色  
2. `index.html` 防闪烁脚本  
3. `useTheme` + 侧栏切换入口  
4. Naive UI 深/浅 overrides + Provider 绑定  
5. 壳层样式依赖 token（侧栏、主区背景、卡片、主按钮、滚动条）在两主题下可读、可用  

### P1（跟进，不阻塞 P0 验收）

1. `workbench.scss` / `project-space.scss` / 页面内硬编码 `#xxx` 逐步改为 `var(--wb-*)`  
2. 甘特条颜色、可主题化的编辑器壳层尽量对齐  

### 验收标准（P0）

- 无 storage 时首屏即为深色紫调表面，无明显浅色闪一下  
- 切换浅色后：白底 + 绿强调，侧栏同族，Naive 控件不「串色」  
- 刷新后主题保持  
- 深色下主内容区与侧栏同属紫调家族，不再是「浅灰内容 + 孤立紫侧栏」  

---

## 7. 测试要点

- 手动：首次无 key → dark；设 light 后刷新仍 light；再清 key → dark  
- 手动：侧栏切换后卡片/按钮/输入框/表格头颜色正确  
- 回归：登录页、工作台、管理端壳层在两主题下布局不错位  

---

## 8. 与现有设计系统关系

- 更新 `DESIGN.md` / `.impeccable/design.json` 的色板说明：深色为默认品牌体验；浅色改为绿主色（实现 P0 时或紧随其后同步文档）  
- 现有「Ordered Lilac」保留为深色主色；浅色主色新增为浅绿族描述  
