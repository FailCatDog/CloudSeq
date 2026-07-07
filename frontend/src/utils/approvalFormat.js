import { dictLabel } from '@/stores/dictStore'
import { CacheCode } from '@/constants/cacheCode'

export const formatApprovalDateTime = (value) => {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

export const formatApprovalSubmitLabel = (value) => formatApprovalDateTime(value)

export const mapTopicApprovalSummary = (item) => ({
  id: item.id,
  teamId: item.teamId,
  courseId: item.courseId,
  courseCode: item.courseCode || '—',
  teamLabel: item.teamLabel || '未命名小组',
  topicTitle: item.topicTitle || '—',
  topicDesc: item.topicDesc || '',
  status: item.approvalStatus,
  rejectReason: item.rejectReason || '',
  leaderName: item.leaderName || '—',
  leaderNo: item.leaderNo || '—',
  memberCount: item.memberCount ?? 0,
  members: item.members || '—',
  submittedAt: formatApprovalSubmitLabel(item.submitDate),
  submittedAtFull: formatApprovalDateTime(item.submitDate),
})

export const approvalStatusLabel = (status) =>
  dictLabel(CacheCode.APPROVAL_STATUS, status, '待审')

export const approvalStatusTagClass = (status) => {
  if (status === CacheCode.APPROVAL_STATUS_APPROVED) return 'tch-tag--success'
  if (status === CacheCode.APPROVAL_STATUS_REJECTED) return 'tch-tag--danger'
  return 'tch-tag--pending'
}

export const isApprovalPending = (status) => status === CacheCode.APPROVAL_STATUS_PENDING
