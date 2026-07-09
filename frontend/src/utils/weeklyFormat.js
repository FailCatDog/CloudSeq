export const formatWeekOptionValue = (option) => {
  if (!option?.reportYear || !option?.reportWeek) return ''
  return `${option.reportYear}-${option.reportWeek}`
}

export const parseWeekOptionValue = (value) => {
  if (!value) return { reportYear: null, reportWeek: null }
  const [year, week] = String(value).split('-')
  return {
    reportYear: year ? Number(year) : null,
    reportWeek: week ? Number(week) : null,
  }
}

export const formatSubmitDate = (value) => {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

export const mapTeacherWeeklyReview = (data) => {
  const weekOptions = Array.isArray(data?.weekOptions) ? data.weekOptions : []
  const teams = Array.isArray(data?.teams) ? data.teams : []

  return {
    weekOptions: weekOptions.map((item) => ({
      reportYear: item.reportYear,
      reportWeek: item.reportWeek,
      label: item.label || `${item.reportYear}年 第${item.reportWeek}周`,
      value: formatWeekOptionValue(item),
    })),
    teams: teams.map((team) => ({
      id: team.teamId,
      teamLabel: team.teamLabel || '未命名小组',
      topicTitle: team.topicTitle || '—',
      workspaceId: team.workspaceId ?? null,
      reports: (Array.isArray(team.reports) ? team.reports : []).map((report) => ({
        id: report.id,
        memberName: report.memberName || '—',
        reportYear: report.reportYear,
        reportWeek: report.reportWeek,
        title: report.title || '—',
        weeklyProgress: report.weeklyProgress || '—',
        problems: report.problems || '—',
        nextPlan: report.nextPlan || '—',
        reportStatus: report.reportStatus,
        submitDate: formatSubmitDate(report.submitDate),
      })),
    })),
  }
}
