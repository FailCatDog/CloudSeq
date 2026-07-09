import { formatTermLabel } from '@/utils/courseFormat'

export const formatRelativeTime = (value) => {
  if (!value) return '—'
  const date = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return String(value)

  const diffMs = Date.now() - date.getTime()
  if (diffMs < 0) return String(value).replace('T', ' ').slice(0, 16)

  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin} 分钟前`

  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour} 小时前`

  const diffDay = Math.floor(diffHour / 24)
  if (diffDay === 1) {
    return `昨天 ${String(value).replace('T', ' ').slice(11, 16)}`
  }
  if (diffDay < 7) return `${diffDay} 天前`

  return String(value).replace('T', ' ').slice(0, 16)
}

export const mapTeachingDashboard = (data) => {
  const course = data?.course
    ? {
        id: data.course.id,
        courseCode: data.course.courseCode || '—',
        courseName: data.course.courseName || '未命名课程',
        termYear: data.course.termYear,
        termSeason: data.course.termSeason,
        termLabel: formatTermLabel(data.course.termYear, data.course.termSeason),
      }
    : null

  const stats = data?.stats || {}
  const pendingApprovals = Array.isArray(data?.pendingApprovals) ? data.pendingApprovals : []
  const atRiskTeams = Array.isArray(data?.atRiskTeams) ? data.atRiskTeams : []

  return {
    course,
    stats: {
      pendingApprovals: stats.pendingApprovalCount ?? 0,
      pendingHint: stats.earliestPendingSubmitDate
        ? `最早提交于 ${formatRelativeTime(stats.earliestPendingSubmitDate)}`
        : '审批队列已清空',
      activeTeams: stats.activeTeamCount ?? 0,
      weeklySubmitted: stats.weeklySubmittedCount ?? 0,
      weeklyTotal: stats.weeklyTotalCount ?? 0,
      overdueTasks: stats.overdueTaskCount ?? 0,
      overdueTeamCount: stats.overdueTeamCount ?? 0,
    },
    pendingItems: pendingApprovals.map((item) => ({
      id: item.id,
      teamLabel: item.teamLabel || '未命名小组',
      topicTitle: item.topicTitle || '—',
      leaderName: item.leaderName || '—',
      memberCount: item.memberCount ?? 0,
      submittedAt: formatRelativeTime(item.submitDate),
    })),
    atRiskTeams: atRiskTeams.map((item) => ({
      id: item.id,
      teamLabel: item.teamLabel || '未命名小组',
      topicTitle: item.topicTitle || '—',
      riskHint: item.riskHint || '—',
      progressPercent: item.progressPercent ?? 0,
      inspectTo: '/teaching/teams',
    })),
  }
}
