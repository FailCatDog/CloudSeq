import { dictLabel } from '@/stores/dictStore'
import { CacheCode } from '@/constants/cacheCode'

export const formatTermLabel = (termYear, termSeason) => {
  const seasonName = dictLabel(CacheCode.TERM_SEASON, termSeason, termSeason || '')
  if (!termYear) return seasonName || '—'
  return seasonName ? `${termYear}年${seasonName}` : `${termYear}年`
}

export const formatCourseSelectLabel = (course) => {
  if (!course) return '选择课号'
  const term = formatTermLabel(course.termYear, course.termSeason)
  const name = course.courseName || course.courseCode || '未命名课号'
  return `${term} · ${name}`
}

/** datetime-local → API `yyyy-MM-dd HH:mm:ss` */
export const toApiDateTime = (datetimeLocal) => {
  if (!datetimeLocal) return null
  const normalized = String(datetimeLocal).trim()
  if (!normalized) return null
  if (normalized.includes(' ')) return normalized
  return `${normalized.replace('T', ' ')}:00`
}

/** API datetime → datetime-local */
export const toDatetimeLocal = (apiDateTime) => {
  if (!apiDateTime) return ''
  return String(apiDateTime).slice(0, 16).replace(' ', 'T')
}

export const courseStatusTagClass = (status) => {
  if (status === CacheCode.COURSE_STATUS_ACTIVE) return 'tch-tag--success'
  if (status === CacheCode.COURSE_STATUS_ARCHIVED) return 'tch-tag--muted'
  return 'tch-tag--pending'
}
