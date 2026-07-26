<template>
  <section class="ps-board" aria-label="数据看板">
    <template v-if="loading">
      <div class="ps-board-head">
        <div class="ps-board-skel ps-board-skel--title" />
        <div class="ps-board-skel ps-board-skel--summary" />
      </div>
      <div class="ps-board-kpis">
        <div v-for="n in 4" :key="n" class="ps-board-kpi ps-board-skel-card">
          <div class="ps-board-skel ps-board-skel--kpi-value" />
          <div class="ps-board-skel ps-board-skel--kpi-label" />
        </div>
      </div>
      <div class="ps-board-main">
        <div class="ps-board-panel ps-board-skel-card ps-board-skel-card--tall" />
        <div class="ps-board-panel ps-board-skel-card ps-board-skel-card--tall" />
      </div>
      <div class="ps-board-panel ps-board-skel-card ps-board-skel-card--trend" />
    </template>

    <div v-else-if="errorMessage" class="ps-board-state ps-board-state--error" role="alert">
      <p>{{ errorMessage }}</p>
      <button type="button" class="ps-board-retry" @click="loadBoardData">重新加载</button>
    </div>

    <template v-else>
      <header class="ps-board-head">
        <div class="ps-board-head__text">
          <h1>数据看板</h1>
          <p>{{ heroSummary }}</p>
        </div>
      </header>

      <div class="ps-board-kpis" role="group" aria-label="关键指标">
        <div v-for="stat in kpiStats" :key="stat.label" class="ps-board-kpi">
          <div class="ps-board-kpi__value" :class="stat.valueClass">{{ stat.value }}</div>
          <div class="ps-board-kpi__label">{{ stat.label }}</div>
        </div>
      </div>

      <div class="ps-board-main">
        <article class="ps-board-panel" aria-labelledby="ps-board-status-title">
          <div class="ps-board-panel__head">
            <h2 id="ps-board-status-title">任务状态</h2>
            <span class="ps-board-panel__meta">共 {{ activeTaskTotal }} 项</span>
          </div>

          <div v-if="!activeTaskTotal" class="ps-board-empty">
            <p>暂无计划任务</p>
            <RouterLink to="/workspace/project/gantt">去甘特图创建任务</RouterLink>
          </div>
          <div v-else class="ps-board-status">
            <div class="ps-board-donut" aria-hidden="true">
              <svg viewBox="0 0 120 120" class="ps-board-donut__svg">
                <circle class="ps-board-donut__track" cx="60" cy="60" r="42" />
                <circle
                  v-for="seg in donutSegments"
                  :key="seg.key"
                  class="ps-board-donut__seg"
                  cx="60"
                  cy="60"
                  r="42"
                  :stroke="seg.color"
                  :stroke-dasharray="seg.dashArray"
                  :stroke-dashoffset="seg.dashOffset"
                />
              </svg>
              <div class="ps-board-donut__center">
                <strong>{{ completionRate }}%</strong>
                <span>完成率</span>
              </div>
            </div>
            <ul class="ps-board-legend">
              <li v-for="item in statusLegend" :key="item.key">
                <span class="ps-board-legend__swatch" :style="{ background: item.color }" />
                <span class="ps-board-legend__label">{{ item.label }}</span>
                <strong class="ps-board-legend__count">{{ item.count }}</strong>
              </li>
            </ul>
          </div>
        </article>

        <article class="ps-board-panel" aria-labelledby="ps-board-due-title">
          <div class="ps-board-panel__head">
            <h2 id="ps-board-due-title">即将到期</h2>
            <span class="ps-board-panel__meta">含逾期 · 7 日内</span>
          </div>

          <div v-if="!dueSoonTasks.length" class="ps-board-empty">
            <p>暂无即将到期任务</p>
          </div>
          <ul v-else class="ps-board-due-list">
            <li v-for="task in dueSoonTasks" :key="task.id">
              <RouterLink class="ps-board-due-item" to="/workspace/project/gantt">
                <div class="ps-board-due-item__main">
                  <span class="ps-board-due-item__title">{{ task.title }}</span>
                  <span class="ps-board-due-item__meta">{{ task.assignee }} · {{ task.dateLabel }}</span>
                </div>
                <span class="ps-board-due-item__tag" :class="`ps-board-due-item__tag--${task.urgency}`">
                  {{ task.urgencyLabel }}
                </span>
              </RouterLink>
            </li>
          </ul>
        </article>
      </div>

      <article class="ps-board-panel ps-board-panel--trend" aria-labelledby="ps-board-trend-title">
        <div class="ps-board-panel__head">
          <h2 id="ps-board-trend-title">近四周周报</h2>
          <div class="ps-board-trend-legend" aria-hidden="true">
            <span><i class="ps-board-trend-legend__dot ps-board-trend-legend__dot--submitted" />已提交</span>
            <span><i class="ps-board-trend-legend__dot ps-board-trend-legend__dot--draft" />草稿</span>
          </div>
        </div>

        <div v-if="weeklyTrend.every((w) => w.kind === 'none')" class="ps-board-empty">
          <p>近四周暂无周报记录</p>
          <RouterLink to="/workspace/project/weekly">去填写本周周报</RouterLink>
        </div>
        <div v-else class="ps-board-trend" role="img" :aria-label="weeklyTrendAria">
          <svg :viewBox="`0 0 ${trendWidth} ${trendHeight}`" class="ps-board-trend__svg">
            <line
              v-for="y in trendGridYs"
              :key="`g-${y}`"
              class="ps-board-trend__grid"
              :x1="trendPad.left"
              :x2="trendWidth - trendPad.right"
              :y1="y"
              :y2="y"
            />
            <polyline class="ps-board-trend__line ps-board-trend__line--submitted" :points="submittedPolyline" />
            <polyline class="ps-board-trend__line ps-board-trend__line--draft" :points="draftPolyline" />
            <g v-for="(pt, index) in weeklyTrendPoints" :key="`p-${index}`">
              <circle
                class="ps-board-trend__point ps-board-trend__point--submitted"
                :cx="pt.x"
                :cy="pt.submittedY"
                r="4.5"
              />
              <circle
                class="ps-board-trend__point ps-board-trend__point--draft"
                :cx="pt.x"
                :cy="pt.draftY"
                r="4.5"
              />
            </g>
            <text
              v-for="(week, index) in weeklyTrend"
              :key="`l-${week.key}`"
              class="ps-board-trend__xlabel"
              :x="weeklyTrendPoints[index].x"
              :y="trendHeight - 10"
              text-anchor="middle"
            >
              {{ week.label }}
            </text>
          </svg>
          <ul class="ps-board-trend-status">
            <li v-for="week in weeklyTrend" :key="`s-${week.key}`">
              <span>{{ week.label }}</span>
              <strong :class="`ps-board-trend-status__tone--${week.kind}`">{{ week.statusLabel }}</strong>
            </li>
          </ul>
        </div>
      </article>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listPlanTasksApi } from '@/api/task'
import { getWeeklyReportByWeekApi } from '@/api/weekly'
import { getCurrentTeamApi, listTeamMembersApi, parseTeamMembersResponse } from '@/api/team'
import { useWorkspace } from '@/composables/useWorkspace'
import { PLAN_TASK_STATUS, WEEKLY_REPORT_STATUS } from '@/constants/common'
import { CacheCode } from '@/constants/cacheCode'
import { getIsoWeek, shiftIsoWeek } from '@/utils/isoWeek'
import {
  buildMemberMap,
  formatTaskStatusLabel,
} from '@/views/student/gantt/ganttAdapter'

const DUE_WINDOW_DAYS = 7
const DONUT_RADIUS = 42
const DONUT_CIRCUMFERENCE = 2 * Math.PI * DONUT_RADIUS

const STATUS_COLORS = {
  [PLAN_TASK_STATUS.NOT_STARTED]: '#94a3b8',
  [PLAN_TASK_STATUS.IN_PROGRESS]: '#6366f1',
  [PLAN_TASK_STATUS.COMPLETED]: '#22c55e',
  [PLAN_TASK_STATUS.CANCELLED]: '#cbd5e1',
}

const { loadCurrentWorkspace, requireWorkspaceId } = useWorkspace()

const loading = ref(true)
const errorMessage = ref('')
const tasks = ref([])
const members = ref([])
const weeklyStatus = ref('未填写')
const weeklyTrend = ref([])

const memberMap = computed(() => buildMemberMap(members.value))

const parsePlanDate = (value) => {
  if (!value) return null
  const raw = String(value).slice(0, 10)
  const [year, month, day] = raw.split('-').map(Number)
  if (!year || !month || !day) return null
  return new Date(year, month - 1, day)
}

const startOfToday = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return today
}

const formatShortDate = (date) => `${date.getMonth() + 1}/${date.getDate()}`

const resolveWeeklyStatusLabel = (report) => {
  if (!report) return '未填写'
  if (report.reportStatus === WEEKLY_REPORT_STATUS.SUBMITTED) return '已提交'
  return '草稿'
}

const resolveWeeklyKind = (report) => {
  if (!report) return 'none'
  if (report.reportStatus === WEEKLY_REPORT_STATUS.SUBMITTED) return 'submitted'
  return 'draft'
}

const activeTasks = computed(() =>
  tasks.value.filter((task) => task.taskStatus !== PLAN_TASK_STATUS.CANCELLED),
)

const activeTaskTotal = computed(() => activeTasks.value.length)

const completedCount = computed(
  () => activeTasks.value.filter((task) => task.taskStatus === PLAN_TASK_STATUS.COMPLETED).length,
)

const completionRate = computed(() => {
  if (!activeTaskTotal.value) return 0
  return Math.round((completedCount.value / activeTaskTotal.value) * 100)
})

const statusCounts = computed(() => {
  const counts = {
    [PLAN_TASK_STATUS.NOT_STARTED]: 0,
    [PLAN_TASK_STATUS.IN_PROGRESS]: 0,
    [PLAN_TASK_STATUS.COMPLETED]: 0,
    [PLAN_TASK_STATUS.CANCELLED]: 0,
  }
  for (const task of tasks.value) {
    const key = task.taskStatus || PLAN_TASK_STATUS.NOT_STARTED
    if (counts[key] == null) counts[PLAN_TASK_STATUS.NOT_STARTED] += 1
    else counts[key] += 1
  }
  return counts
})

const statusLegend = computed(() =>
  [
    PLAN_TASK_STATUS.NOT_STARTED,
    PLAN_TASK_STATUS.IN_PROGRESS,
    PLAN_TASK_STATUS.COMPLETED,
    PLAN_TASK_STATUS.CANCELLED,
  ]
    .map((key) => ({
      key,
      label: formatTaskStatusLabel(key),
      count: statusCounts.value[key],
      color: STATUS_COLORS[key],
    }))
    .filter((item) => item.count > 0 || activeTaskTotal.value === 0),
)

const donutSegments = computed(() => {
  const total = tasks.value.length
  if (!total) return []

  let offset = DONUT_CIRCUMFERENCE * 0.25
  const order = [
    PLAN_TASK_STATUS.COMPLETED,
    PLAN_TASK_STATUS.IN_PROGRESS,
    PLAN_TASK_STATUS.NOT_STARTED,
    PLAN_TASK_STATUS.CANCELLED,
  ]

  return order
    .map((key) => {
      const count = statusCounts.value[key]
      if (!count) return null
      const length = (count / total) * DONUT_CIRCUMFERENCE
      const seg = {
        key,
        color: STATUS_COLORS[key],
        dashArray: `${length} ${DONUT_CIRCUMFERENCE - length}`,
        dashOffset: offset,
      }
      offset -= length
      return seg
    })
    .filter(Boolean)
})

const dueSoonTasks = computed(() => {
  const today = startOfToday()
  const horizon = new Date(today)
  horizon.setDate(horizon.getDate() + DUE_WINDOW_DAYS)

  return activeTasks.value
    .filter((task) => task.taskStatus !== PLAN_TASK_STATUS.COMPLETED)
    .map((task) => {
      const end = parsePlanDate(task.endDate)
      if (!end || end > horizon) return null

      const diffDays = Math.round((end - today) / 86400000)
      let urgency = 'soon'
      let urgencyLabel = `${diffDays} 天后`

      if (diffDays < 0) {
        urgency = 'overdue'
        urgencyLabel = `逾期 ${Math.abs(diffDays)} 天`
      } else if (diffDays === 0) {
        urgency = 'today'
        urgencyLabel = '今天到期'
      } else if (diffDays === 1) {
        urgency = 'tomorrow'
        urgencyLabel = '明天到期'
      }

      const assigneeId = task.assigneeUserId
      const assignee =
        assigneeId == null
          ? '未分配'
          : memberMap.value.get(assigneeId) ||
            memberMap.value.get(Number(assigneeId)) ||
            memberMap.value.get(String(assigneeId)) ||
            '未分配'

      return {
        id: task.id,
        title: task.taskName || '未命名任务',
        assignee,
        dateLabel: formatShortDate(end),
        end,
        urgency,
        urgencyLabel,
        statusLabel: formatTaskStatusLabel(task.taskStatus),
      }
    })
    .filter(Boolean)
    .sort((a, b) => a.end - b.end)
    .slice(0, 8)
})

const kpiStats = computed(() => [
  { label: '计划任务', value: String(activeTaskTotal.value) },
  { label: '完成率', value: `${completionRate.value}%`, valueClass: 'ps-board-kpi__value--accent' },
  { label: '小组成员', value: String(members.value.length) },
  {
    label: '本周周报',
    value: weeklyStatus.value,
    valueClass:
      weeklyStatus.value === '已提交'
        ? 'ps-board-kpi__value--success'
        : weeklyStatus.value === '草稿'
          ? 'ps-board-kpi__value--warning'
          : 'ps-board-kpi__value--muted',
  },
])

const heroSummary = computed(() => {
  const dueCount = dueSoonTasks.value.length
  const overdueCount = dueSoonTasks.value.filter((item) => item.urgency === 'overdue').length
  const parts = [`完成率 ${completionRate.value}%`]

  if (overdueCount > 0) parts.push(`${overdueCount} 项已逾期`)
  else if (dueCount > 0) parts.push(`${dueCount} 项即将到期`)
  else parts.push('暂无即将到期任务')

  parts.push(`本周周报${weeklyStatus.value}`)
  return parts.join(' · ')
})

const trendWidth = 640
const trendHeight = 168
const trendPad = { top: 18, right: 16, bottom: 36, left: 16 }

const trendGridYs = computed(() => {
  const chartH = trendHeight - trendPad.top - trendPad.bottom
  return [0, 0.5, 1].map((t) => trendPad.top + chartH * (1 - t))
})

const weeklyTrendPoints = computed(() => {
  const n = weeklyTrend.value.length || 1
  const chartW = trendWidth - trendPad.left - trendPad.right
  const chartH = trendHeight - trendPad.top - trendPad.bottom
  const step = n === 1 ? 0 : chartW / (n - 1)

  return weeklyTrend.value.map((week, index) => {
    const x = trendPad.left + step * index
    const submittedY = trendPad.top + chartH * (1 - week.submitted)
    const draftY = trendPad.top + chartH * (1 - week.draft)
    return { x, submittedY, draftY }
  })
})

const submittedPolyline = computed(() =>
  weeklyTrendPoints.value.map((pt) => `${pt.x},${pt.submittedY}`).join(' '),
)

const draftPolyline = computed(() =>
  weeklyTrendPoints.value.map((pt) => `${pt.x},${pt.draftY}`).join(' '),
)

const weeklyTrendAria = computed(() =>
  weeklyTrend.value.map((week) => `${week.label} ${week.statusLabel}`).join('；'),
)

const loadWeeklyTrend = async (workspaceId) => {
  const current = getIsoWeek()
  const weeks = [3, 2, 1, 0].map((offset) => {
    const week = offset === 0 ? current : shiftIsoWeek(current.reportYear, current.reportWeek, -offset)
    return {
      key: `${week.reportYear}-W${week.reportWeek}`,
      label: offset === 0 ? '本周' : `W${week.reportWeek}`,
      reportYear: week.reportYear,
      reportWeek: week.reportWeek,
      isCurrent: offset === 0,
    }
  })

  const reports = await Promise.all(
    weeks.map((week) =>
      getWeeklyReportByWeekApi(workspaceId, week.reportYear, week.reportWeek).catch(() => null),
    ),
  )

  weeklyTrend.value = weeks.map((week, index) => {
    const report = reports[index]
    const kind = resolveWeeklyKind(report)
    return {
      ...week,
      kind,
      statusLabel: resolveWeeklyStatusLabel(report),
      submitted: kind === 'submitted' ? 1 : 0,
      draft: kind === 'draft' ? 1 : 0,
    }
  })

  const currentReport = reports[reports.length - 1]
  weeklyStatus.value = resolveWeeklyStatusLabel(currentReport)
}

const loadBoardData = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    await loadCurrentWorkspace()
    const workspaceId = requireWorkspaceId()

    const [taskData, teamData] = await Promise.all([
      listPlanTasksApi(workspaceId).catch(() => []),
      getCurrentTeamApi().catch(() => null),
    ])

    tasks.value = Array.isArray(taskData) ? taskData : []

    if (teamData?.id) {
      const { memberList } = parseTeamMembersResponse(await listTeamMembersApi(teamData.id))
      members.value = memberList.filter(
        (item) => item.memberStatus === CacheCode.MEMBER_STATUS_ACTIVE,
      )
    } else {
      members.value = []
    }

    await loadWeeklyTrend(workspaceId)
  } catch (error) {
    errorMessage.value = error?.message || '加载看板数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadBoardData()
})
</script>
