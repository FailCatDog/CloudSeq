const pad = (value) => String(value).padStart(2, '0')

export const formatDate = (date) => {
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  return `${year}-${month}-${day}`
}

export const parseDate = (value) => {
  const [year, month, day] = String(value).split('-').map(Number)
  return new Date(year, month - 1, day)
}

export const getIsoWeek = (input = new Date()) => {
  const date = input instanceof Date ? new Date(input) : parseDate(input)
  date.setHours(0, 0, 0, 0)

  const day = date.getDay() || 7
  const monday = new Date(date)
  monday.setDate(date.getDate() - day + 1)

  const thursday = new Date(monday)
  thursday.setDate(monday.getDate() + 3)

  const yearStart = new Date(thursday.getFullYear(), 0, 1)
  const reportWeek = Math.ceil((((thursday - yearStart) / 86400000) + 1) / 7)
  const reportYear = thursday.getFullYear()

  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)

  return {
    reportYear,
    reportWeek,
    weekStartDate: formatDate(monday),
    weekEndDate: formatDate(sunday),
    monday,
    sunday,
  }
}

export const getIsoWeekFromYearWeek = (reportYear, reportWeek) => {
  const jan4 = new Date(reportYear, 0, 4)
  const iso = getIsoWeek(jan4)
  const monday = new Date(iso.monday)
  monday.setDate(monday.getDate() + (reportWeek - iso.reportWeek) * 7)
  return getIsoWeek(monday)
}

export const shiftIsoWeek = (reportYear, reportWeek, delta) => {
  const current = getIsoWeekFromYearWeek(reportYear, reportWeek)
  const nextMonday = new Date(current.monday)
  nextMonday.setDate(nextMonday.getDate() + delta * 7)
  return getIsoWeek(nextMonday)
}

export const formatWeekRangeLabel = (weekStartDate, weekEndDate) => {
  const start = parseDate(weekStartDate)
  const end = parseDate(weekEndDate)
  const sameYear = start.getFullYear() === end.getFullYear()
  const startLabel = `${start.getMonth() + 1}月${start.getDate()}日`
  const endLabel = sameYear
    ? `${end.getMonth() + 1}月${end.getDate()}日`
    : `${end.getFullYear()}年${end.getMonth() + 1}月${end.getDate()}日`
  return `${startLabel} - ${endLabel}`
}
