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
    textColor1: '#1a1a2e',
    textColor2: '#4b5563',
    textColor3: '#6b7280',
    placeholderColor: '#9ca3af',
    bodyColor: '#ffffff',
    cardColor: '#ffffff',
    modalColor: '#ffffff',
    popoverColor: '#ffffff',
    dividerColor: '#e5e7eb',
    borderColor: '#e5e7eb',
    hoverColor: '#f3f4f6',
    pressedColor: '#eceef3',
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
    thColor: '#f9fafb',
    thTextColor: '#6b7280',
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
