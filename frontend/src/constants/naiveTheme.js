/** Naive UI 主题覆盖 — 对齐 workbench.css 设计 token */
export const consoleThemeOverrides = {
  common: {
    primaryColor: '#8b5cf6',
    primaryColorHover: '#7c3aed',
    primaryColorPressed: '#6d28d9',
    primaryColorSuppl: '#a78bfa',
    borderRadius: '10px',
    borderRadiusSmall: '8px',
    fontFamily:
      'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", sans-serif',
    textColor1: '#1a1a2e',
    textColor2: '#4b4b5e',
    textColor3: '#8b8b9e',
    placeholderColor: '#a0a0b0',
    bodyColor: '#ffffff',
    cardColor: '#ffffff',
    modalColor: '#ffffff',
    popoverColor: '#ffffff',
    dividerColor: '#e2e4ea',
    borderColor: '#e2e4ea',
    hoverColor: '#fafafc',
    pressedColor: '#f4f6fa',
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
    thColor: '#fafafc',
    thTextColor: '#8b8b9e',
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
