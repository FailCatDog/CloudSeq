import { CacheCode, PLAN_TASK_STATUS } from '@/constants/cacheCode.js'
import { dictLabel } from '@/stores/dictStore'

export const DRAFT_ROW_ID = 'draft-row'

const TASK_STATUS = PLAN_TASK_STATUS

const TASK_BAR_COLORS = {
  [TASK_STATUS.NOT_STARTED]: '#a0a0b0', // --wb-text-muted
  [TASK_STATUS.IN_PROGRESS]: '#8b5cf6', // --wb-purple
  [TASK_STATUS.COMPLETED]: '#22c55e', // --wb-online-green
  [TASK_STATUS.CANCELLED]: '#a0a0b0',
}

export const GANTT_TASK_STATUS_COLORS = {
  pending: TASK_BAR_COLORS[TASK_STATUS.NOT_STARTED],
  ongoing: TASK_BAR_COLORS[TASK_STATUS.IN_PROGRESS],
  complete: TASK_BAR_COLORS[TASK_STATUS.COMPLETED],
  delay: '#ef4444',
}

function resolveTaskBarColor(taskStatus, endDate, progress) {
  if (progress >= 100 || taskStatus === TASK_STATUS.COMPLETED) {
    return TASK_BAR_COLORS[TASK_STATUS.COMPLETED]
  }

  if (endDate) {
    const end = new Date(endDate)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    end.setHours(0, 0, 0, 0)
    if (!Number.isNaN(end.getTime()) && end < today) {
      return GANTT_TASK_STATUS_COLORS.delay
    }
  }

  return TASK_BAR_COLORS[taskStatus] ?? TASK_BAR_COLORS[TASK_STATUS.NOT_STARTED]
}

export function isDraftTask(task) {
  return task?.id === DRAFT_ROW_ID || task?.isDraft === true
}

export function buildMemberMap(members = []) {
  const map = new Map()
  for (const member of members) {
    if (member?.userId == null) continue
    map.set(member.userId, member.name)
  }
  return map
}

export function sortPlanTasks(rows = []) {
  return [...rows].sort((a, b) => {
    const sortDiff = (a.sortOrder ?? 0) - (b.sortOrder ?? 0)
    if (sortDiff !== 0) return sortDiff
    return String(a.id ?? '').localeCompare(String(b.id ?? ''))
  })
}

function progressFromStatus(taskStatus) {
  if (taskStatus === TASK_STATUS.COMPLETED) return 100
  if (taskStatus === TASK_STATUS.IN_PROGRESS) return 50
  return 0
}

export function inferTaskStatus({ actualStartDate, actualEndDate, taskStatus } = {}) {
  if (taskStatus === TASK_STATUS.CANCELLED) return TASK_STATUS.CANCELLED
  if (actualEndDate) return TASK_STATUS.COMPLETED
  if (actualStartDate) return TASK_STATUS.IN_PROGRESS
  return taskStatus ?? TASK_STATUS.NOT_STARTED
}

export function formatTaskStatusLabel(taskStatus) {
  return dictLabel(
    CacheCode.PLAN_TASK_STATUS,
    taskStatus,
    dictLabel(CacheCode.PLAN_TASK_STATUS, TASK_STATUS.NOT_STARTED, '未开始'),
  )
}

export function patchTaskStatus(task, taskStatus) {
  const progress = progressFromStatus(taskStatus)
  return {
    ...task,
    taskStatus,
    progress,
    barColor: resolveTaskBarColor(taskStatus, task.endDate, progress),
  }
}

function resolveDurationValue(duration, startDate, endDate) {
  if (duration != null && duration !== '') {
    const value = Number(duration)
    if (!Number.isNaN(value)) return value
  }
  return calcTaskDurationDays(startDate, endDate)
}

export function formatDurationCell(task) {
  const days = resolveDurationValue(task?.duration, task?.startDate, task?.endDate)
  return days == null ? '-' : `${days}天`
}

function resolveAssigneeName(assigneeUserId, memberMap) {
  if (!assigneeUserId) return '未分配'
  return memberMap.get(assigneeUserId) ?? '未分配'
}

/** 计算任务总天数（含首尾两天） */
export function calcTaskDurationDays(startDate, endDate) {
  const start = normalizePlanDate(startDate)
  const end = normalizePlanDate(endDate)
  if (!start || !end) return null

  const startTime = new Date(`${start}T00:00:00`).getTime()
  const endTime = new Date(`${end}T00:00:00`).getTime()
  if (Number.isNaN(startTime) || Number.isNaN(endTime)) return null

  const diffDays = Math.floor((endTime - startTime) / (24 * 60 * 60 * 1000))
  return diffDays >= 0 ? diffDays + 1 : null
}

export function formatTaskDurationLabel(task) {
  const days = resolveDurationValue(task?.duration, task?.startDate, task?.endDate)
  return days == null ? '' : `共${days}天`
}

/** jordium-gantt 可能返回 "YYYY-MM-DD HH:mm"，后端 LocalDate 仅接受 yyyy-MM-dd */
function normalizePlanDate(value) {
  if (value == null || value === '') return null
  const str = String(value).trim()
  if (!str) return null
  const datePart = str.slice(0, 10)
  return /^\d{4}-\d{2}-\d{2}$/.test(datePart) ? datePart : null
}

export function mapPlanTasksToGanttTasks(rows = [], memberMap = new Map()) {
  return sortPlanTasks(rows).map((row, index) => {
    const taskStatus = row.taskStatus ?? TASK_STATUS.NOT_STARTED
    const progress = progressFromStatus(taskStatus)
    const assigneeUserId = row.assigneeUserId
    const startDate = row.startDate || ''
    const endDate = row.endDate || ''
    const actualStartDate = row.actualStartDate || ''
    const actualEndDate = row.actualEndDate || ''
    const duration = resolveDurationValue(row.duration, startDate, endDate)

    return {
      id: row.id,
      name: row.taskName || `任务 ${index + 1}`,
      startDate,
      endDate,
      actualStartDate,
      actualEndDate,
      duration,
      progress,
      barColor: resolveTaskBarColor(taskStatus, endDate, progress),
      assignee: assigneeUserId ? String(assigneeUserId) : '',
      assigneeName: resolveAssigneeName(assigneeUserId, memberMap),
      index: String(index + 1).padStart(2, '0'),
      taskStatus,
      sortOrder: row.sortOrder ?? index,
    }
  })
}

export function mapGanttTaskToPlanTaskPayload(task, options = {}) {
  const { workspaceId, existing, nextSortOrder = 0 } = options
  const startDate = normalizePlanDate(task.startDate)
  const endDate = normalizePlanDate(task.endDate)
  const actualStartDate = normalizePlanDate(task.actualStartDate ?? existing?.actualStartDate)
  const actualEndDate = normalizePlanDate(task.actualEndDate ?? existing?.actualEndDate)
  const duration = resolveDurationValue(
    task.duration ?? existing?.duration,
    startDate,
    endDate,
  )

  return {
    workspaceId,
    id: existing?.id ?? task.id,
    taskName: task.name,
    assigneeUserId: task.assignee || null,
    startDate,
    endDate,
    actualStartDate,
    actualEndDate,
    duration,
    taskStatus: options.preserveTaskStatus
      ? (task.taskStatus ?? existing?.taskStatus ?? TASK_STATUS.NOT_STARTED)
      : inferTaskStatus({
          actualStartDate,
          actualEndDate,
          taskStatus: existing?.taskStatus ?? task.taskStatus,
        }),
    sortOrder: existing?.sortOrder ?? nextSortOrder,
  }
}

export function toPlanTaskMap(rows = []) {
  const map = new Map()
  for (const row of rows) {
    if (row?.id == null) continue
    map.set(String(row.id), row)
  }
  return map
}

export function getPlanTaskFromMap(planTaskMap, taskId) {
  if (taskId == null) return undefined
  return planTaskMap.get(String(taskId))
}

export function createDraftGanttTask() {
  return {
    id: DRAFT_ROW_ID,
    name: '',
    startDate: '',
    endDate: '',
    actualStartDate: '',
    actualEndDate: '',
    duration: null,
    progress: 0,
    assignee: '',
    assigneeName: '',
    index: '',
    taskStatus: TASK_STATUS.NOT_STARTED,
    type: 'milestone',
    isDraft: true,
    isEditable: false,
  }
}

export function ensureDraftRowLast(tasks = []) {
  const draft = tasks.find(isDraftTask) ?? createDraftGanttTask()
  const realTasks = []

  for (const task of tasks) {
    if (isDraftTask(task)) {
      if (Array.isArray(task.children) && task.children.length) {
        realTasks.push(...task.children)
      }
      continue
    }
    realTasks.push(task)
  }

  const normalizedDraft = { ...createDraftGanttTask(), ...draft, children: undefined }
  return [...realTasks, normalizedDraft]
}

/** 按目标行索引重排（支持向上/向下拖拽；库内建逻辑仅支持 after） */
export function reorderTasksByDrag(tasks, draggedId, targetId, orderIds = null) {
  const taskMap = new Map(
    tasks.filter((task) => !isDraftTask(task)).map((task) => [task.id, task]),
  )
  const ids = orderIds ?? [...taskMap.keys()]

  const from = ids.findIndex((id) => id === draggedId)
  const to = ids.findIndex((id) => id === targetId)
  if (from < 0 || to < 0 || from === to) {
    return [...taskMap.values()]
  }

  const nextIds = [...ids]
  const [movedId] = nextIds.splice(from, 1)
  nextIds.splice(to, 0, movedId)

  return nextIds.map((id) => taskMap.get(id)).filter(Boolean)
}

export function buildSortOrderUpdates(orderedTasks, planTaskMap, workspaceId) {
  const updates = []

  orderedTasks.forEach((task, index) => {
    if (isDraftTask(task)) return

    const nextSort = index + 1
    const existing = getPlanTaskFromMap(planTaskMap, task.id)
    if (!existing) return

    task.sortOrder = nextSort
    task.index = String(nextSort).padStart(2, '0')

    if ((existing.sortOrder ?? 0) !== nextSort) {
      updates.push({
        id: existing.id,
        nextSort,
        payload: {
          ...mapGanttTaskToPlanTaskPayload(task, { workspaceId, existing }),
          sortOrder: nextSort,
        },
      })
    }
  })

  return updates
}
