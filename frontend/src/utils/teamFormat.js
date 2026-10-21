import { dictLabel } from '@/stores/dictStore'
import { CacheCode } from '@/constants/cacheCode'

export const mapTeamOverviewSummary = (item) => ({
  id: item.id,
  courseId: item.courseId,
  courseCode: item.courseCode || '—',
  teamLabel: item.teamLabel || '未命名小组',
  topicTitle: item.topicTitle || '—',
  status: item.status,
  leaderName: item.leaderName || '—',
  memberCount: item.memberCount ?? 0,
  lastReportYear: item.lastReportYear ?? null,
  lastReportWeek: item.lastReportWeek ?? null,
  lastWeeklyLabel: formatLastWeeklyLabel(item.lastReportWeek),
})

export const formatLastWeeklyLabel = (week) => {
  if (week == null) return '—'
  return `第 ${week} 周`
}

export const formatMemberJoinDate = (value) => {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

export const memberRoleLabel = (member) => {
  if (member?.isLeader === 1) return '组长'
  return '成员'
}

export const teamStatusLabel = (status) =>
  dictLabel(CacheCode.TEAM_STATUS, status, '—')

export const teamStatusTagClass = (status) => {
  if (status === CacheCode.TEAM_STATUS_UNLOCKED) return 'tch-tag--success'
  if (status === CacheCode.TEAM_STATUS_PENDING_TOPIC) return 'tch-tag--pending'
  if (status === CacheCode.TEAM_STATUS_TOPIC_REJECTED) return 'tch-tag--danger'
  return ''
}

export const resolveTeamAction = (status) => {
  if (status === CacheCode.TEAM_STATUS_PENDING_TOPIC) {
    return { label: '去审批', to: '/teaching/approvals' }
  }
  if (status === CacheCode.TEAM_STATUS_UNLOCKED) {
    return { label: '巡查项目', to: '/teaching/reports' }
  }
  return null
}
