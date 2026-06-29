export const BRAND_LOGO_URL = '/logo-48.png'
export const BRAND_FAVICON_URL = '/favicon-32.png'

export const BRAND_NAME = '云序'
export const BRAND_NAME_EN = 'CloudSeq'
export const BRAND_FULL_NAME = '云序 CloudSeq'
export const BRAND_SLOGAN = '让项目，有序发生'
export const BRAND_TAGLINE = '面向软件项目学习的团队协作工作台'
export const BRAND_DESCRIPTION =
  '云序 CloudSeq 是面向软件项目学习的团队协作工作台，聚合小组空间、任务进度、文档协作与周报管理。'

export const buildPageTitle = (pageTitle) => {
  return pageTitle ? `${pageTitle} · ${BRAND_NAME}` : BRAND_NAME
}
