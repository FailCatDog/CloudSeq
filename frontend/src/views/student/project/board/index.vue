<template>
  <section class="ps-board">
    <div v-if="loading" class="ps-board-state">加载看板数据…</div>
    <div v-else-if="errorMessage" class="ps-board-state ps-board-state--error">{{ errorMessage }}</div>
    <template v-else>
      <div class="ps-board-stats">
        <div v-for="stat in stats" :key="stat.label" class="ps-stat-card">
          <div class="ps-stat-num">{{ stat.value }}</div>
          <div class="ps-stat-label">{{ stat.label }}</div>
        </div>
      </div>

      <article class="ps-board-card">
        <div class="ps-board-card-head">
          <h2>数据看板</h2>
          <span class="ps-board-badge">概览</span>
        </div>
        <ul class="ps-board-overview">
          <li v-for="line in overviewLines" :key="line">{{ line }}</li>
        </ul>
      </article>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listPlanTasksApi } from '@/api/task'
import { getWorkspaceTreeApi } from '@/api/workspace'
import { getWeeklyReportByWeekApi } from '@/api/weekly'
import { getCurrentTeamApi, listTeamMembersApi, parseTeamMembersResponse } from '@/api/team'
import { useWorkspace } from '@/composables/useWorkspace'
import { PLAN_TASK_STATUS, WEEKLY_REPORT_STATUS } from '@/constants/common'
import { CacheCode } from '@/constants/cacheCode'
import { WORKSPACE_NODE_TYPE } from '@/constants/workspace'
import { getIsoWeek } from '@/utils/isoWeek'

const { loadCurrentWorkspace, requireWorkspaceId } = useWorkspace()

const loading = ref(true)
const errorMessage = ref('')
const tasks = ref([])
const members = ref([])
const nodeCounts = ref({ documents: 0, sheets: 0, total: 0 })
const weeklyStatus = ref('')

const countContentNodes = (nodes) => {
  let documents = 0
  let sheets = 0

  const walk = (list) => {
    for (const node of list ?? []) {
      if (node.nodeType === WORKSPACE_NODE_TYPE.DOCUMENT) {
        documents += 1
      } else if (node.nodeType === WORKSPACE_NODE_TYPE.SHEET) {
        sheets += 1
      }
      if (node.children?.length) {
        walk(node.children)
      }
    }
  }

  walk(nodes)
  return { documents, sheets, total: documents + sheets }
}

const resolveWeeklyStatusLabel = (report) => {
  if (!report) return '未填写'
  if (report.reportStatus === WEEKLY_REPORT_STATUS.SUBMITTED) return '已提交'
  return '草稿'
}

const completedCount = computed(() =>
  tasks.value.filter((task) => task.taskStatus === PLAN_TASK_STATUS.COMPLETED).length,
)

const completionRate = computed(() => {
  if (!tasks.value.length) return 0
  return Math.round((completedCount.value / tasks.value.length) * 100)
})

const stats = computed(() => [
  { value: String(tasks.value.length), label: '计划任务' },
  { value: String(nodeCounts.value.total), label: '内容节点' },
  { value: String(members.value.length), label: '小组成员' },
  { value: String(completedCount.value), label: '已完成任务' },
])

const overviewLines = computed(() => {
  const lines = [
    `任务完成率 ${completionRate.value}%（${completedCount.value}/${tasks.value.length}）`,
  ]

  if (nodeCounts.value.total > 0) {
    lines.push(`文档 ${nodeCounts.value.documents} 篇 · 表格 ${nodeCounts.value.sheets} 个`)
  } else {
    lines.push('暂无文档或表格')
  }

  lines.push(`本周周报：${weeklyStatus.value}`)
  return lines
})

const loadBoardData = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    await loadCurrentWorkspace()
    const workspaceId = requireWorkspaceId()
    const week = getIsoWeek()

    const [taskData, treeData, teamData] = await Promise.all([
      listPlanTasksApi(workspaceId).catch(() => []),
      getWorkspaceTreeApi(workspaceId).catch(() => []),
      getCurrentTeamApi().catch(() => null),
    ])

    tasks.value = Array.isArray(taskData) ? taskData : []
    nodeCounts.value = countContentNodes(Array.isArray(treeData) ? treeData : [])

    if (teamData?.id) {
      const { memberList } = parseTeamMembersResponse(await listTeamMembersApi(teamData.id))
      members.value = memberList.filter(
        (item) => item.memberStatus === CacheCode.MEMBER_STATUS_ACTIVE,
      )
    } else {
      members.value = []
    }

    const report = await getWeeklyReportByWeekApi(
      workspaceId,
      week.reportYear,
      week.reportWeek,
    ).catch(() => null)
    weeklyStatus.value = resolveWeeklyStatusLabel(report)
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
