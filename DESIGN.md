---
name: 云序 CloudSeq
description: 面向软件项目学习的团队协作工作台
defaultTheme: dark
tokenSource: frontend/src/styles/_tokens.scss
colors:
  dark:
    primary: "#8b5cf6"
    primary-hover: "#7c3aed"
    primary-pressed: "#6d28d9"
    primary-soft: "rgba(139, 92, 246, 0.18)"
    primary-border: "#5b4b8a"
    link: "#a78bfa"
    sidebar: "#12101a"
    sidebar-active: "rgba(139, 92, 246, 0.28)"
    sidebar-icon-active: "#c4b5fd"
    page-bg: "#16141f"
    card: "#1f1c2b"
    search-bg: "#2a2640"
    border: "#3d3a52"
    text-primary: "#f4f2fa"
    text-secondary: "#a8a4bc"
    text-muted: "#7a7690"
    btn-dark: "#0f0d16"
    online: "#22c55e"
    tag-pending-bg: "rgba(37, 99, 235, 0.22)"
    tag-pending-text: "#93c5fd"
    tag-success-bg: "rgba(22, 163, 74, 0.22)"
    tag-success-text: "#86efac"
    tag-warning-bg: "rgba(234, 88, 12, 0.2)"
    tag-warning-text: "#fdba74"
    tag-danger-bg: "rgba(220, 38, 38, 0.22)"
    tag-danger-text: "#fca5a5"
    tag-draft-bg: "rgba(180, 83, 9, 0.25)"
    tag-draft-text: "#fcd34d"
    tag-muted-bg: "#2a2640"
    tag-muted-text: "#a8a4bc"
  light:
    primary: "#10b981"
    primary-hover: "#059669"
    primary-pressed: "#047857"
    primary-soft: "#d1fae5"
    primary-border: "#6ee7b7"
    link: "#059669"
    sidebar: "#1e3a2f"
    sidebar-active: "rgba(16, 185, 129, 0.28)"
    sidebar-icon-active: "#6ee7b7"
    page-bg: "#f5faf7"
    card: "#ffffff"
    search-bg: "#e8f2ec"
    border: "#d5e5db"
    text-primary: "#14241c"
    text-secondary: "#5b7266"
    text-muted: "#7a9186"
    btn-dark: "#14241c"
    hover-surface: "#fafafc"
    online: "#16a34a"
    tag-pending-bg: "#dbeafe"
    tag-pending-text: "#2563eb"
    tag-success-bg: "#dcfce7"
    tag-success-text: "#15803d"
    tag-warning-bg: "#fff7ed"
    tag-warning-text: "#ea580c"
    tag-danger-bg: "#fee2e2"
    tag-danger-text: "#dc2626"
    tag-draft-bg: "#fef3c7"
    tag-draft-text: "#b45309"
    tag-muted-bg: "#f3f4f6"
    tag-muted-text: "#6b7280"
typography:
  display:
    fontFamily: "Inter, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"PingFang SC\", \"Microsoft YaHei\", sans-serif"
    fontSize: "28px"
    fontWeight: 700
    lineHeight: 1.25
    letterSpacing: "normal"
  headline:
    fontFamily: "Inter, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"PingFang SC\", \"Microsoft YaHei\", sans-serif"
    fontSize: "20px"
    fontWeight: 700
    lineHeight: 1.35
    letterSpacing: "normal"
  title:
    fontFamily: "Inter, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"PingFang SC\", \"Microsoft YaHei\", sans-serif"
    fontSize: "16px"
    fontWeight: 700
    lineHeight: 1.4
    letterSpacing: "normal"
  body:
    fontFamily: "Inter, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"PingFang SC\", \"Microsoft YaHei\", sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "normal"
  label:
    fontFamily: "Inter, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"PingFang SC\", \"Microsoft YaHei\", sans-serif"
    fontSize: "12px"
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: "0.02em"
rounded:
  xs: "8px"
  sm: "10px"
  md: "12px"
  card: "20px"
  pill: "999px"
spacing:
  xs: "8px"
  sm: "12px"
  md: "16px"
  lg: "20px"
  xl: "24px"
components:
  button-primary:
    backgroundColor: "{colors.dark.primary}"
    textColor: "{colors.dark.card}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "36px"
  button-primary-hover:
    backgroundColor: "{colors.dark.primary-hover}"
    textColor: "{colors.dark.card}"
    rounded: "{rounded.sm}"
    height: "36px"
  button-dark:
    backgroundColor: "{colors.dark.btn-dark}"
    textColor: "{colors.dark.card}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "36px"
  card-surface:
    backgroundColor: "{colors.dark.card}"
    textColor: "{colors.dark.text-primary}"
    rounded: "{rounded.card}"
    padding: "22px"
  input-field:
    backgroundColor: "{colors.dark.card}"
    textColor: "{colors.dark.text-primary}"
    rounded: "{rounded.sm}"
    height: "36px"
  tag-pill:
    backgroundColor: "{colors.dark.primary-soft}"
    textColor: "{colors.dark.primary-pressed}"
    rounded: "{rounded.pill}"
    padding: "2px 10px"
  sidebar-nav-active:
    backgroundColor: "{colors.dark.sidebar-active}"
    textColor: "{colors.dark.sidebar-icon-active}"
    rounded: "{rounded.sm}"
---

# Design System: 云序 CloudSeq

## 1. Overview

**Creative North Star: "The Ordered Studio"**

云序是一间有序的学院工作室：**默认深色紫罗兰壳层**（`#16141f` 页底 + 淡紫强调），可通过主题切换为**浅色薄荷**（`#f5faf7` 页底 + `#10b981` 强调）。气质对齐 PRODUCT.md 中的「轻快、学院感、工具感，并带一点平台感」；取飞书的「好找入口」与语雀的「好读好写」，不照搬整站视觉。

密度以工作台任务流为准：一屏一事、主路径醒目、状态标签语义清晰。装饰服务于方位感（侧栏、卡片层级），不为热闹而热闹。明确拒绝信息堆满的教务系统、花哨教育 SaaS 模板感，以及用装饰性仪表盘冒充「有序」。

**Key Characteristics:**
- 默认深色紫罗兰壳层；浅色主题为白卡片 + 薄荷强调 + 深绿侧栏
- 大圆角卡片（20px）与中圆角控件（10px）并存
- 轻抬升阴影表达层级，不靠重投影制造「高级感」
- 组件手感清晰利落、平台感偏强（Naive UI 主题与 workbench token 对齐）

## 2. Colors

**Runtime source of truth:** `frontend/src/styles/_tokens.scss` — all hex values below mirror `[data-theme='dark']` and `[data-theme='light']` blocks. Toggle persists in `localStorage` key `cloudseq-theme`; default is **dark**.

云序支持双主题：**默认深色紫罗兰壳层**与**可选浅色薄荷强调**。两主题共用同一 token 命名（如 `--wb-purple*`），浅色主题将 accent 槽位重映射为绿色，避免组件分叉。

### Default theme — dark violet surfaces
- **Violet Studio Page** (#16141f): 全局页底；卡片浮于 #1f1c2b。
- **Ordered Lilac** (#8b5cf6): 主操作、焦点环、图标强调、侧栏激活底色的色相来源。Hover (#7c3aed)、Pressed (#6d28d9)、Soft (rgba 18%)、Border (#5b4b8a) 组成同族阶梯。
- **Ink Plum Sidebar** (#12101a): 固定侧栏与项目空间深色壳；图标默认半透明白，激活为淡紫 (#c4b5fd)。
- **Light Ink Text** (#f4f2fa): 主文案；次级 (#a8a4bc)、弱化 (#7a7690)。

### Light theme — white + mint green
- **Mint Page Mist** (#f5faf7): 页面背景，让白卡片浮起。
- **Studio Mint** (#10b981): 主操作、焦点环、图标强调（映射 `--wb-purple*`）。Hover (#059669)、Pressed (#047857)、Soft (#d1fae5)、Border (#6ee7b7)。
- **Forest Sidebar** (#1e3a2f): 侧栏深绿壳；激活为 #6ee7b7。
- **Card Porcelain** (#ffffff): 卡片、弹层、表格单元格底。
- **Ink Text** (#14241c): 主文案；次级 (#5b7266)、弱化 (#7a9186)。

### Shared neutral & semantic (theme-keyed in frontmatter `colors.dark` / `colors.light`)
- **Search Fog** (#eceef3 light / #2a2640 dark): 搜索框与进度轨等次级凹面。
- **Hairline Border** (#e2e4ea light / #3d3a52 dark): 分割线与输入描边。
- **Charcoal Action** (#1a1a24 light / #0f0d16 dark): 深色实心按钮（与主 accent CTA 分工）。

### Semantic tags
- Pending / Success / Warning / Danger / Draft / Muted：成对的浅底 + 深字（见 frontmatter `tag-*`），用于审批、周报、课程状态。颜色必须配文字或形态，禁止仅靠色相区分。

### Named Rules
**The One Accent Rule.** Ordered Lilac 在任意一屏的强调占用应克制：主按钮、关键激活、少量高亮即可。大面积铺紫会破坏学院工作室的轻快感。

**The Status Is Not Decoration Rule.** 语义标签色只表达业务状态，禁止当作营销色块或仪表盘装饰。

## 3. Typography

**Display Font:** Inter（系统与 PingFang SC / Microsoft YaHei 回退）
**Body Font:** 同一套栈（单一无衬线，技术清晰 + 中文可读）
**Label/Mono Font:** 无独立 mono；代码块用中性底 (#f3f4f6)，字体继承

**Character:** 单一 Inter 族完成层级，靠字重（400 / 600 / 700）与字号阶梯区分，像工具平台而非杂志排版。

### Hierarchy
- **Display** (700, 28px, 1.25): 工作台问候、空态大标题等少见英雄文案。
- **Headline** (700, 20px, 1.35): 页面主标题、卡片组标题。
- **Title** (700, 16px, 1.4): 卡片标题、表单区块名、Naive Card title。
- **Body** (400, 14px, 1.5): 默认正文与表格内容；行宽控制在可读范围内，长文（语雀式文档）优先舒适阅读而非装饰字号。
- **Label** (600, 12px; eyebrow 可 0.14em uppercase): 辅助标签、表头弱化色、状态旁注。

### Named Rules
**The One Family Rule.** 不引入第二展示字体。学院感来自节奏与留白，不来自衬线装饰。

## 4. Elevation

系统采用**轻抬升**：白卡片浮在 Studio Page Mist 上，阴影柔和、低对比；深度来自「页底 / 卡片 / 弹层」色调分层，而不是多层重投影。

### Shadow Vocabulary
- **Card rest** (`box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.03)`): 默认卡片与 surface-card。
- **Focus ring** (`0 0 0 3px` + primary alpha ~14%): 键盘焦点，不用粗描边冒充强调。
- **Auth / CTA lift**（局部更强紫色投影）: 仅登录主按钮等品牌瞬间，不进入工作台常规卡片。

### Named Rules
**The Soft Lift Rule.** 默认阴影保持极淡。若看起来像 2014 年后台或厚重教务皮肤，说明阴影过深或模糊过小。

## 5. Components

整体手感：**清晰利落、平台感偏强**。圆角分明，字重偏稳（按钮 600、标题 700），反馈短促（约 0.15–0.28s ease）。

### Buttons
- **Shape:** 中圆角 (10px)；高度 Medium 36px / Small 32px。
- **Primary:** Ordered Lilac 底 + 白字；Hover/Pressed 走 primary-hover / primary-pressed。
- **Dark:** Charcoal Action，用于次级强操作或与主紫对比的实心动作。
- **Hover / Focus:** 颜色阶梯变化；焦点用紫色 focus ring，禁止仅靠变透明。

### Chips / Tags
- **Style:** 胶囊圆角 (999px)；语义色浅底深字。
- **State:** 审批待办、通过、警告、危险、草稿、静默；与字典驱动状态一致。

### Cards / Containers
- **Corner Style:** 主卡片 20px；较小 surface 可用 10px。
- **Background:** Card Porcelain；页底 Studio Page Mist。
- **Shadow Strategy:** Soft Lift 默认阴影。
- **Border:** Hairline (#e2e4ea) 可与阴影同用，避免双边框噪音。
- **Internal Padding:** 约 16–22px；网格间距常用 20px。

### Inputs / Fields
- **Style:** 白底、Hairline 边、圆角 10px、高度 36px。
- **Focus:** 边色转主紫系 + 轻环，与 Naive Input 主题一致。
- **Search concave:** Search Fog 底表示「可输入凹槽」，与白卡片区分。

### Navigation
- **Sidebar:** Ink Plum 固定栏（收起 72px / 展开 220px）；激活项淡紫半透明底 + 亮图标。
- **Console / teaching:** 浅色内容区 + Naive 表格与筛选；保持同一主色，不另起一套教务蓝。
- **Project space:** 深壳 + 树/编辑器全高；导航逻辑与工作台一致，避免第三套无关皮肤。

### Signature: Workbench shell
深侧栏 + 浅内容 + 大圆角白卡片是云序的默认构图。新页面优先落入此壳，而不是另起营销落地风布局。

## 6. Do's and Don'ts

### Do:
- **Do** 让「下一步」可见：入课、审批、解锁工作台、交周报等路径用清晰层级与状态标签表达。
- **Do** 保持角色分流：学生工作台、教师教学端、管理端共用 token，不共用信息架构噪音。
- **Do** 用 Ordered Lilac 做稀缺强调，用语义标签做状态，用 Soft Lift 做层级。
- **Do** 文档与表格优先阅读舒适（语雀式），入口与导航优先好找（飞书式）。
- **Do** 满足 WCAG AA：对比度、键盘焦点、色 + 文双通道状态。

### Don't:
- **Don't** 做成信息堆满的教务系统：密密麻麻的表格墙、无主次的筛选项、同时抢注意力的统计与告警。
- **Don't** 用花哨教育 SaaS 模板感，或用装饰性仪表盘冒充「有序」。
- **Don't** 在工作台常规面堆大面积紫渐变、玻璃拟态或霓虹描边。
- **Don't** 用大于 1px 的彩色侧条当卡片/列表强调（禁止 side-stripe 套路）。
- **Don't** 引入第二套无关主色（例如另起「教务蓝」）破坏平台一致性。
- **Don't** 仅靠颜色区分状态；标签必须可读文字或等价形态。
