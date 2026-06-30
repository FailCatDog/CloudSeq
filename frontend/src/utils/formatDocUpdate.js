export const formatDocUpdateLabel = (dateInput) => {
  if (!dateInput) return '尚未保存'

  const date = new Date(dateInput)
  if (Number.isNaN(date.getTime())) return '尚未保存'

  const now = new Date()
  const month = date.getMonth() + 1
  const day = date.getDate()

  if (date.toDateString() === now.toDateString()) {
    const hours = String(date.getHours()).padStart(2, '0')
    const mins = String(date.getMinutes()).padStart(2, '0')
    return `今天 ${hours}:${mins} 修改`
  }

  return `${month}月${day}日修改`
}
