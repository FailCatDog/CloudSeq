import { CacheCode } from '@/constants/cacheCode'

export const approvalTagType = (status) => {
  if (status === CacheCode.APPROVAL_STATUS_APPROVED) return 'success'
  if (status === CacheCode.APPROVAL_STATUS_REJECTED) return 'error'
  return 'info'
}

export const courseTagType = (status) => {
  if (status === CacheCode.COURSE_STATUS_ACTIVE) return 'success'
  if (status === CacheCode.COURSE_STATUS_ARCHIVED) return 'default'
  return 'warning'
}

export const teamTagType = (status) => {
  if (status === CacheCode.TEAM_STATUS_UNLOCKED) return 'success'
  if (status === CacheCode.TEAM_STATUS_PENDING_TOPIC) return 'info'
  if (status === CacheCode.TEAM_STATUS_TOPIC_REJECTED) return 'error'
  return 'default'
}
