<template>
  <section class="gantt-page">
    <div v-if="!ready" class="gantt-loading">加载中...</div>
    <div v-else-if="pageError" class="gantt-loading gantt-loading--error">{{ pageError }}</div>
    <GanttChart
      v-else
      ref="ganttChartRef"
      class="gantt-chart"
      :tasks="visibleTasks"
      :assignee-options="assigneeOptions"
      :toolbar-config="toolbarConfig"
      :task-list-config="taskListConfig"
      :enable-task-list-collapsible="true"
      :task-bar-config="taskBarConfig"
      :pending-task-background-color="GANTT_TASK_STATUS_COLORS.pending"
      :ongoing-task-background-color="GANTT_TASK_STATUS_COLORS.ongoing"
      :complete-task-background-color="GANTT_TASK_STATUS_COLORS.complete"
      :delay-task-background-color="GANTT_TASK_STATUS_COLORS.delay"
      :locale-messages="localeMessages"
      task-list-column-render-mode="declarative"
      :enable-task-row-move="true"
      :enable-link-anchor="false"
      :use-default-drawer="true"
      locale="zh-CN"
      time-scale="week"
      :theme="ganttTheme"
      :on-theme-change="handleGanttThemeChange"
      @task-added="handleTaskAdded"
      @task-updated="handleTaskUpdated"
      @task-deleted="handleTaskDeleted"
      @task-row-moved="handleTaskRowMoved"
    >
      <TaskListColumn prop="index" label="序号" :width="56" align="center">
        <template #default="{ row }">
          <button
            v-if="isDraftTask(row)"
            type="button"
            class="draft-add-btn"
            title="新增任务"
            @click="openAddTaskDrawer"
          >
            +
          </button>
          <span v-else>{{ row.index }}</span>
        </template>
      </TaskListColumn>

      <TaskListColumn prop="name" label="任务概述" :width="200" />
      <TaskListColumn prop="assigneeName" label="指派人" :width="120">
        <template #default="{ row }">
          <select
            v-if="!isDraftTask(row)"
            class="assignee-select"
            :value="resolveAssigneeUserId(row)"
            @mousedown.stop
            @click.stop
            @change="handleAssigneeChange(row, $event)"
          >
            <option value="">未分配</option>
            <option
              v-for="member in memberList"
              :key="member.userId"
              :value="String(member.userId)"
            >
              {{ member.name }}
            </option>
          </select>
        </template>
      </TaskListColumn>
      <TaskListColumn prop="startDate" label="开始时间" :width="110" />
      <TaskListColumn prop="endDate" label="截止时间" :width="110" />
      <TaskListColumn prop="duration" label="工期" :width="72" align="center">
        <template #default="{ row }">
          <span v-if="!isDraftTask(row)">{{ formatDurationCell(row) }}</span>
        </template>
      </TaskListColumn>
      <TaskListColumn prop="taskStatus" label="状态" :width="96" align="center">
        <template #default="{ row }">
          <select
            v-if="!isDraftTask(row)"
            class="status-select"
            :value="String(row.taskStatus ?? PLAN_TASK_STATUS.NOT_STARTED)"
            @mousedown.stop
            @click.stop
            @change="handleTaskStatusChange(row, $event)"
          >
            <option
              v-for="option in planTaskStatusOptions"
              :key="option.value"
              :value="String(option.value)"
            >
              {{ option.label }}
            </option>
          </select>
        </template>
      </TaskListColumn>

      <template #custom-task-content="slotProps">
        <template v-if="slotProps.type === 'task-row'">
          {{ slotProps.task.name }}
        </template>
        <span
          v-else-if="slotProps.type === 'task-bar' && !isDraftTask(slotProps.task)"
          class="task-bar-duration"
        >
          {{ formatTaskDurationLabel(slotProps.task) }}
        </span>
      </template>
    </GanttChart>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { GanttChart, TaskListColumn } from 'jordium-gantt-vue3'
import {
  PLAN_TASK_STATUS,
  CacheCode,
} from '@/constants/cacheCode.js'
import { useDict } from '@/composables/useDict'
import { useWorkspace } from '@/composables/useWorkspace'
import { createPlanTaskApi, deletePlanTaskApi, listPlanTasksApi, updatePlanTaskApi } from '../../../api/task'
import { getCurrentTeamApi, listTeamMembersApi, parseTeamMembersResponse } from '../../../api/team'
import {
  buildMemberMap,
  buildSortOrderUpdates,
  ensureDraftRowLast,
  formatDurationCell,
  formatTaskDurationLabel,
  isDraftTask,
  patchTaskStatus,
  reorderTasksByDrag,
  mapGanttTaskToPlanTaskPayload,
  mapPlanTasksToGanttTasks,
  getPlanTaskFromMap,
  toPlanTaskMap,
  GANTT_TASK_STATUS_COLORS,
} from './ganttAdapter'

const { loadCurrentWorkspace, requireWorkspaceId } = useWorkspace()
const GANTT_THEME_STORAGE_KEY = 'gantt-theme'

const resolveGanttTheme = () => {
  const stored = localStorage.getItem(GANTT_THEME_STORAGE_KEY)
  if (stored === 'dark' || stored === 'light') return stored
  return 'light'
}

const ganttTheme = ref(resolveGanttTheme())

const { options: planTaskStatusOptions } = useDict(CacheCode.PLAN_TASK_STATUS)

const handleGanttThemeChange = (isDark) => {
  ganttTheme.value = isDark ? 'dark' : 'light'
}

const ready = ref(false)
const pageError = ref('')
const ganttChartRef = ref(null)
const tasks = ref([])
const memberList = ref([])
const planTaskMap = ref(new Map())
const rawPlanTasks = ref([])

const toolbarConfig = {
  showAddTask: true,
  showAddMilestone: false,
  showExportCsv: false,
  showExportPdf: false,
  showViewMode: false,
  showFullscreen: false,
  showExpandCollapse: false,
  defaultTimeScale: 'week',
  timeScaleDimensions: ['day', 'week', 'month'],
}

const taskListConfig = ref({
  showAllColumns: false,
  showTaskIcon: false,
  defaultWidth: 748,
  minWidth: 280,
  maxWidth: 960,
})

let cleanupSplitter = null

const getTaskListWidthBounds = (root) => {
  const header = root?.querySelector('.task-list-header')
  const contentWidth = header?.scrollWidth > 0 ? header.scrollWidth : null
  const minWidth = taskListConfig.value.minWidth ?? 280
  const configuredMax = taskListConfig.value.maxWidth ?? 900
  const maxWidth = contentWidth ? Math.min(configuredMax, contentWidth) : configuredMax
  return { minWidth, maxWidth }
}

const bindTaskListSplitter = () => {
  cleanupSplitter?.()
  cleanupSplitter = null

  const root = ganttChartRef.value?.$el
  if (!root) return

  const splitter = root.querySelector('.gantt-splitter')
  const leftPanel = root.querySelector('.gantt-panel-left')
  if (!splitter || !leftPanel) return

  const onMouseDown = (event) => {
    if (event.button !== 0) return
    if (event.target.closest('.task-list-toggle')) return

    event.preventDefault()
    event.stopPropagation()

    const startX = event.clientX
    const startWidth = leftPanel.getBoundingClientRect().width
    const { minWidth, maxWidth } = getTaskListWidthBounds(root)
    let rafId = null
    let pendingWidth = null

    root.classList.add('splitter-dragging')
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
    window.dispatchEvent(new CustomEvent('splitter-drag-start'))

    const applyWidth = (width) => {
      const next = Math.min(maxWidth, Math.max(minWidth, Math.round(width)))
      if (next === taskListConfig.value.defaultWidth) return
      taskListConfig.value = { ...taskListConfig.value, defaultWidth: next }
    }

    const onMove = (moveEvent) => {
      moveEvent.preventDefault()
      pendingWidth = startWidth + (moveEvent.clientX - startX)
      if (rafId !== null) return
      rafId = requestAnimationFrame(() => {
        if (pendingWidth !== null) applyWidth(pendingWidth)
        pendingWidth = null
        rafId = null
      })
    }

    const onUp = () => {
      if (rafId !== null) {
        cancelAnimationFrame(rafId)
        if (pendingWidth !== null) applyWidth(pendingWidth)
      }

      root.classList.remove('splitter-dragging')
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      window.removeEventListener('mousemove', onMove)
      window.removeEventListener('mouseup', onUp)
      window.dispatchEvent(new CustomEvent('splitter-drag-end'))
      window.dispatchEvent(
        new CustomEvent('timeline-container-resized', {
          detail: { source: 'manual-splitter' },
        }),
      )
    }

    window.addEventListener('mousemove', onMove)
    window.addEventListener('mouseup', onUp)
  }

  splitter.addEventListener('mousedown', onMouseDown, true)
  cleanupSplitter = () => splitter.removeEventListener('mousedown', onMouseDown, true)
}

const taskBarConfig = {
  showAvatar: false,
  showTitle: true,
  showProgress: false,
}

const localeMessages = {
  'zh-CN': {
    resourceAllocation: '指派人',
    selectResource: '请选择指派人',
    actualStartDate: '实际开始',
    actualEndDate: '实际结束',
  },
}

const memberMap = computed(() => buildMemberMap(memberList.value))

const assigneeOptions = computed(() =>
  memberList.value.map((member) => ({
    value: String(member.userId),
    label: member.name,
  })),
)

const resolveAssigneeUserId = (row) => {
  const userId = row?.assignee ?? row?.assigneeUserId
  return userId != null && userId !== '' ? String(userId) : ''
}

const findMemberByUserId = (userId) =>
  memberList.value.find((member) => String(member.userId) === String(userId))

const visibleTasks = ref([])
const preDragOrder = ref([])
/** 列表内联编辑（状态/指派人）时屏蔽库内 task-updated，避免整表重载闪烁 */
const inlineTaskSyncGuard = new Set()

const runWithInlineTaskSyncGuard = async (taskId, runner) => {
  const key = String(taskId)
  inlineTaskSyncGuard.add(key)
  try {
    await runner()
  } finally {
    await nextTick()
    inlineTaskSyncGuard.delete(key)
  }
}

const patchVisibleTaskFromPlan = (planTask) => {
  if (!planTask?.id) return
  const mapped = mapPlanTasksToGanttTasks([planTask], memberMap.value)[0]
  if (!mapped) return
  const row = visibleTasks.value.find(
    (task) => !isDraftTask(task) && String(task.id) === String(planTask.id),
  )
  if (row) Object.assign(row, mapped)
}

const syncVisibleTasks = () => {
  visibleTasks.value = ensureDraftRowLast(
    mapPlanTasksToGanttTasks(rawPlanTasks.value, memberMap.value),
  )
}

const repairDraftRowIfNeeded = (tasks) => {
  if (!tasks.length || !isDraftTask(tasks[tasks.length - 1])) {
    visibleTasks.value = ensureDraftRowLast(tasks)
  }
}

const syncTasksFromPlanRows = (rows) => {
  rawPlanTasks.value = Array.isArray(rows) ? rows : []
  planTaskMap.value = toPlanTaskMap(rawPlanTasks.value)
  tasks.value = mapPlanTasksToGanttTasks(rawPlanTasks.value, memberMap.value)
  syncVisibleTasks()
}

const reloadTasks = async () => {
  const data = await listPlanTasksApi(requireWorkspaceId())
  syncTasksFromPlanRows(data)
}

const initializePage = async () => {
  pageError.value = ''
  try {
    await loadCurrentWorkspace()
    await loadTeamMembers()
    await reloadTasks()
  } catch (error) {
    pageError.value = error?.message || '加载甘特图失败'
    syncTasksFromPlanRows([])
  } finally {
    ready.value = true
    await nextTick()
    bindTaskListSplitter()
  }
}

const loadTeamMembers = async () => {
  const team = await getCurrentTeamApi()
  if (!team?.id) {
    memberList.value = []
    return
  }

  const { memberList: members } = parseTeamMembersResponse(await listTeamMembersApi(team.id))
  memberList.value = members.filter((item) => item.memberStatus === CacheCode.MEMBER_STATUS_ACTIVE)
  if (rawPlanTasks.value.length) {
    tasks.value = mapPlanTasksToGanttTasks(rawPlanTasks.value, memberMap.value)
    syncVisibleTasks()
  }
}

const handleTaskRowDragStart = (event) => {
  const draggedTask = event.detail?.task
  if (!draggedTask || isDraftTask(draggedTask)) {
    preDragOrder.value = []
    return
  }

  preDragOrder.value = visibleTasks.value
    .filter((task) => !isDraftTask(task))
    .map((task) => task.id)
}

const handleTaskRowMoved = async ({ draggedTask, targetTask }) => {
  const dragOrder = [...preDragOrder.value]
  preDragOrder.value = []

  if (isDraftTask(draggedTask) || isDraftTask(targetTask)) {
    syncVisibleTasks()
    return
  }

  const orderedTasks = reorderTasksByDrag(
    visibleTasks.value,
    draggedTask.id,
    targetTask.id,
    dragOrder,
  )
  visibleTasks.value = ensureDraftRowLast(orderedTasks)

  const updates = buildSortOrderUpdates(orderedTasks, planTaskMap.value, requireWorkspaceId())
  if (!updates.length) return

  try {
    await Promise.all(updates.map(({ id, payload }) => updatePlanTaskApi(id, payload)))
    updates.forEach(({ nextSort, id }) => {
      const existing = planTaskMap.value.get(id)
      if (existing) existing.sortOrder = nextSort
    })
    tasks.value = orderedTasks
  } catch {
    await reloadTasks()
  }
}

const openAddTaskDrawer = () => {
  const root = ganttChartRef.value?.$el
  if (!root) return
  const addBtn = root.querySelector('.gantt-toolbar .gantt-btn-group .gantt-btn-group-item')
  addBtn?.click()
}

/** 抽屉内 datetime 选择器挂载在 body，点日期后自动确认 */
const autoConfirmDrawerDatePick = (event) => {
  if (!document.querySelector('.drawer-overlay')) return
  const cell = event.target.closest('.el-picker-panel .el-date-table__cell')
  if (!cell) return
  window.setTimeout(() => {
    document.querySelector('.el-picker-panel .el-date-picker-btn--confirm')?.click()
  }, 0)
}

const buildPayload = (task, existing) =>
  mapGanttTaskToPlanTaskPayload(task, {
    workspaceId: requireWorkspaceId(),
    existing,
    nextSortOrder: rawPlanTasks.value.length + 1,
  })

const handleTaskAdded = async ({ task }) => {
  if (isDraftTask(task)) return

  try {
    const payload = buildPayload(task)
    await createPlanTaskApi(payload)
    await reloadTasks()
  } catch {
    await reloadTasks()
  }
}

const handleTaskUpdated = async ({ task }) => {
  if (isDraftTask(task)) return
  if (inlineTaskSyncGuard.has(String(task.id))) return

  const existing = getPlanTaskFromMap(planTaskMap.value, task.id)
  if (!existing) return

  try {
    const payload = buildPayload(task, existing)
    const saved = await updatePlanTaskApi(existing.id, payload)
    Object.assign(existing, saved ?? payload)
    await runWithInlineTaskSyncGuard(task.id, async () => {
      patchVisibleTaskFromPlan(existing)
    })
  } catch {
    await reloadTasks()
  }
}

const handleTaskStatusChange = async (row, event) => {
  const existing = getPlanTaskFromMap(planTaskMap.value, row.id)
  if (!existing) return

  const taskStatus = event.target.value
  if (taskStatus === existing.taskStatus) return

  const previous = {
    taskStatus: row.taskStatus,
    progress: row.progress,
    barColor: row.barColor,
  }
  const updatedRow = patchTaskStatus(row, taskStatus)

  await runWithInlineTaskSyncGuard(row.id, async () => {
    Object.assign(row, updatedRow)

    try {
      const payload = mapGanttTaskToPlanTaskPayload(updatedRow, {
        workspaceId: requireWorkspaceId(),
        existing,
        preserveTaskStatus: true,
      })
      await updatePlanTaskApi(existing.id, payload)
      existing.taskStatus = taskStatus
    } catch {
      Object.assign(row, previous)
      existing.taskStatus = previous.taskStatus
    }
  })
}

const handleAssigneeChange = async (row, event) => {
  const existing = getPlanTaskFromMap(planTaskMap.value, row.id)
  if (!existing) return

  const assigneeUserId = event.target.value
  const nextAssigneeUserId = assigneeUserId || null
  if (String(existing.assigneeUserId ?? '') === String(nextAssigneeUserId ?? '')) return

  const assigneeName = assigneeUserId
    ? findMemberByUserId(assigneeUserId)?.name ?? '未分配'
    : '未分配'

  const previous = {
    assignee: row.assignee,
    assigneeName: row.assigneeName,
  }
  const updatedRow = {
    ...row,
    assignee: assigneeUserId,
    assigneeName,
  }

  await runWithInlineTaskSyncGuard(row.id, async () => {
    row.assignee = assigneeUserId
    row.assigneeName = assigneeName

    try {
      const payload = mapGanttTaskToPlanTaskPayload(updatedRow, {
        workspaceId: requireWorkspaceId(),
        existing,
      })
      await updatePlanTaskApi(existing.id, payload)
      existing.assigneeUserId = nextAssigneeUserId
    } catch {
      row.assignee = previous.assignee
      row.assigneeName = previous.assigneeName
    }
  })
}

/** 抽屉新建任务时 resources 为空，需自动添加一行才能显示指派人下拉 */
const ensureDrawerAssigneeRow = () => {
  const overlay = document.querySelector('.drawer-overlay')
  if (!overlay) return

  const resourceList = overlay.querySelector('.resource-list')
  if (!resourceList || resourceList.querySelector('.resource-item')) return

  overlay.querySelector('.btn-add-resource')?.click()
}

let drawerObserver = null

const bindDrawerAssigneeInit = () => {
  drawerObserver?.disconnect()
  drawerObserver = new MutationObserver(() => {
    if (!document.querySelector('.drawer-overlay')) return
    window.setTimeout(ensureDrawerAssigneeRow, 0)
  })
  drawerObserver.observe(document.body, { childList: true, subtree: true })
}

const handleTaskDeleted = async ({ task }) => {
  if (isDraftTask(task)) return

  const existing = getPlanTaskFromMap(planTaskMap.value, task.id)
  if (!existing) return

  try {
    await deletePlanTaskApi(existing.id)
    await reloadTasks()
  } catch {
    await reloadTasks()
  }
}

watch(
  () => {
    const list = visibleTasks.value
    const last = list[list.length - 1]
    return `${list.length}:${last?.id ?? ''}`
  },
  () => {
    repairDraftRowIfNeeded(visibleTasks.value)
  },
)

onMounted(async () => {
  document.addEventListener('click', autoConfirmDrawerDatePick, true)
  window.addEventListener('task-row-drag-start', handleTaskRowDragStart)
  bindDrawerAssigneeInit()
  await initializePage()
})

onBeforeUnmount(() => {
  cleanupSplitter?.()
  drawerObserver?.disconnect()
  document.removeEventListener('click', autoConfirmDrawerDatePick, true)
  window.removeEventListener('task-row-drag-start', handleTaskRowDragStart)
})
</script>

<style scoped>
.gantt-page {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 640px;
  background: var(--wb-bg-page);
}

.gantt-loading {
  display: grid;
  place-items: center;
  flex: 1;
  color: var(--wb-text-secondary);
  font-size: 14px;
}

.gantt-loading--error {
  color: #dc2626;
  padding: 0 24px;
  text-align: center;
}

.gantt-chart {
  flex: 1;
  min-height: 0;
}

.gantt-chart :deep(.gantt-root) {
  --gantt-primary: var(--wb-purple);
  --gantt-primary-color: var(--wb-purple);
  --gantt-primary-dark: #7c3aed;
  --gantt-primary-hover: var(--wb-purple-light);
  --gantt-primary-light: rgba(139, 92, 246, 0.2);
  --gantt-primary-lightest: var(--wb-purple-soft);
  --gantt-bg-primary: var(--wb-card-bg);
  --gantt-bg-secondary: var(--wb-bg-page);
  --gantt-bg-toolbar: var(--wb-bg-page);
  --gantt-bg-hover: var(--wb-purple-soft);
  --gantt-border-color: var(--wb-search-border);
  --gantt-border-light: var(--wb-search-border);
  --gantt-border-medium: var(--wb-search-border);
  --gantt-text-primary: var(--wb-text-primary);
  --gantt-text-secondary: var(--wb-text-secondary);
  --gantt-text-muted: var(--wb-text-muted);
  --gantt-text-header: var(--wb-text-primary);
  --gantt-success: #22c55e;
  --gantt-warning: var(--wb-icon-orange);
  --gantt-danger: #ef4444;
  --gantt-scrollbar-thumb: var(--scroll-thumb);
  --gantt-scrollbar-thumb-hover: var(--scroll-thumb-hover);
}

/* 时间轴头部与网格：今日列、指示线 */
.gantt-chart :deep(.timeline-day.today),
.gantt-chart :deep(.timeline-week.today),
.gantt-chart :deep(.timeline-hour.today) {
  background-color: var(--wb-purple);
  color: #fff;
}

.gantt-chart :deep(.sub-day-column.today),
.gantt-chart :deep(.day-column.today) {
  border-left-color: var(--wb-purple);
  background-color: rgba(139, 92, 246, 0.12);
}

.gantt-chart :deep(.day-column.today::before) {
  background: linear-gradient(
    to bottom,
    rgba(139, 92, 246, 0.1),
    rgba(139, 92, 246, 0.05),
    rgba(139, 92, 246, 0.1)
  );
}

.gantt-chart :deep(.day-column.today-highlight) {
  background-color: rgba(139, 92, 246, 0.28) !important;
  border-left-color: var(--wb-purple) !important;
  box-shadow: 0 0 8px rgba(139, 92, 246, 0.35);
  animation: gantt-today-pulse 2s ease-in-out;
}

.gantt-chart :deep(.day-column.today-highlight::before) {
  background: linear-gradient(
    to bottom,
    rgba(139, 92, 246, 0.3),
    rgba(139, 92, 246, 0.2),
    rgba(139, 92, 246, 0.3)
  ) !important;
}

.gantt-chart :deep(.today-line-year-view) {
  background-color: var(--wb-purple);
  box-shadow: 0 0 4px rgba(139, 92, 246, 0.35);
}

.gantt-chart :deep(.segmented-control:hover) {
  border-color: var(--wb-purple-border);
}

.gantt-chart :deep(.segmented-thumb) {
  background: var(--wb-purple);
}

.gantt-chart :deep(.segmented-item:hover:not(.active)) {
  color: var(--wb-purple);
  background: var(--wb-purple-soft);
}

@keyframes gantt-today-pulse {
  0% {
    opacity: 0.8;
    transform: scale(1);
    box-shadow: 0 0 8px rgba(139, 92, 246, 0.45);
  }

  50% {
    opacity: 0.9;
    transform: scale(1.02);
    box-shadow: 0 0 12px rgba(139, 92, 246, 0.55);
  }

  100% {
    opacity: 0.5;
    transform: scale(1);
    box-shadow: 0 0 8px rgba(139, 92, 246, 0.3);
  }
}

/* 数据行后的空白新增行 */
.gantt-chart :deep(.task-list .task-row[data-task-id='draft-row']) {
  background: var(--wb-purple-soft);
  cursor: default;
}

/* 任务表格与时间轴分割条：加宽拖拽热区，避免 6px 难以命中 */
.gantt-chart :deep(.gantt-splitter) {
  position: relative;
  flex-shrink: 0;
  width: 8px;
  z-index: 50;
  cursor: col-resize;
  touch-action: none;
}

.gantt-chart :deep(.gantt-splitter::before) {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: -8px;
  width: 24px;
  cursor: col-resize;
}

.gantt-chart :deep(.gantt-panel-left) {
  min-width: 280px !important;
}

.gantt-chart :deep(.gantt-root.splitter-dragging *) {
  cursor: col-resize !important;
}

/* 任务表格：去掉左侧类型色条 */
.gantt-chart :deep(.task-list .task-row),
.gantt-chart :deep(.task-list-header) {
  border-left: none !important;
}

/* 任务行可上下拖拽排序 */
.gantt-chart :deep(.task-list .task-row:not([data-task-id='draft-row'])) {
  cursor: grab;
}

.gantt-chart :deep(.task-list .task-row:not([data-task-id='draft-row']):active) {
  cursor: grabbing;
}

/* 任务条：用 barColor 写入的 --parent-color，覆盖库内联 backgroundColor */
.gantt-chart :deep(.task-bar:not(.parent-task):not(.resource-view)) {
  background-color: var(--parent-color) !important;
  border-color: var(--parent-color) !important;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.22) !important;
}

.gantt-chart :deep(.task-bar:not(.parent-task):not(.resource-view) .progress-bar),
.gantt-chart :deep(.task-bar:not(.parent-task):not(.resource-view) .task-avatars-container) {
  display: none !important;
}

.gantt-chart :deep(.task-bar:not(.parent-task):not(.resource-view) .task-bar-content) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 0 6px;
  pointer-events: none;
}

.gantt-chart :deep(.task-bar-duration) {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.28);
}

/* 工具栏保留新增能力，但隐藏按钮；由列表末尾 + 行触发 */
.gantt-chart :deep(.gantt-toolbar .gantt-btn-group > .gantt-btn-group-item:first-child) {
  display: none !important;
}

.draft-add-btn {
  width: 24px;
  height: 24px;
  border: 1px dashed rgba(139, 92, 246, 0.45);
  border-radius: 8px;
  background: rgba(139, 92, 246, 0.08);
  color: var(--wb-purple);
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
}

.draft-add-btn:hover {
  background: rgba(139, 92, 246, 0.16);
}

.assignee-select {
  width: 100%;
  max-width: 108px;
  height: 28px;
  padding: 0 6px;
  border: 1px solid var(--wb-search-border);
  border-radius: 6px;
  background: var(--wb-card-bg);
  color: var(--wb-text-primary);
  font-size: 12px;
  cursor: pointer;
}

.assignee-select:focus {
  outline: none;
  border-color: var(--wb-purple);
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.14);
}

.status-select {
  width: 100%;
  max-width: 84px;
  height: 28px;
  padding: 0 4px;
  border: 1px solid var(--wb-search-border);
  border-radius: 6px;
  background: var(--wb-card-bg);
  color: var(--wb-text-primary);
  font-size: 12px;
  cursor: pointer;
}

.status-select:focus {
  outline: none;
  border-color: var(--wb-purple);
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.14);
}
</style>

<style>
/* TaskDrawer 挂载在 body，需全局样式 */
.drawer-overlay {
  --gantt-primary: var(--wb-purple);
  --gantt-primary-color: var(--wb-purple);
  --gantt-primary-dark: #7c3aed;
  --gantt-primary-hover: var(--wb-purple-light);
  --gantt-primary-light: var(--wb-purple-border);
  --gantt-primary-lightest: var(--wb-purple-soft);
}

.drawer-overlay .gantt-btn-primary,
.drawer-overlay .btn-primary {
  background: var(--wb-purple) !important;
  border-color: var(--wb-purple) !important;
}

.drawer-overlay .gantt-btn-primary:hover,
.drawer-overlay .btn-primary:hover {
  background: var(--wb-purple-light) !important;
  border-color: var(--wb-purple-light) !important;
}

.drawer-overlay .form-group:has(#task-type) {
  display: none !important;
}

.drawer-overlay .task-form > div:has(#predecessor-select) {
  display: none !important;
}

.drawer-overlay .form-row:has(#task-estimated-hours) {
  display: none !important;
}

.drawer-overlay .form-group:has(#task-progress) {
  display: none !important;
}

/* 资源分配改为单人指派：隐藏占比与多资源操作 */
.drawer-overlay .resource-header,
.drawer-overlay .capacity-select,
.drawer-overlay .btn-add-resource,
.drawer-overlay .btn-remove-resource {
  display: none !important;
}

.drawer-overlay .resource-list .resource-item ~ .resource-item {
  display: none !important;
}

.drawer-overlay .resource-list .resource-select {
  width: 100%;
}

/* datetime 选择器 Teleport 到 body，抽屉打开时隐藏时间与确认按钮 */
body:has(.drawer-overlay) .el-picker-panel .el-time-picker-input,
body:has(.drawer-overlay) .el-picker-panel .el-date-picker-footer {
  display: none !important;
}
</style>
