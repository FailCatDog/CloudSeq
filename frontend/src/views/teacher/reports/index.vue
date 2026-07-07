<template>
  <div class="tch-page">
    <div class="tch-filter-bar">
      <div class="tch-filter-tabs" role="tablist" aria-label="提交状态">
        <button
          v-for="tab in submitTabs"
          :key="tab.id"
          type="button"
          role="tab"
          class="tch-filter-tab"
          :class="{ active: activeSubmitFilter === tab.id }"
          :aria-selected="activeSubmitFilter === tab.id"
          @click="activeSubmitFilter = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="tch-filter-tabs tch-filter-tabs--end" role="tablist" aria-label="周次">
        <button
          v-for="week in MOCK_REPORT_WEEKS"
          :key="week"
          type="button"
          role="tab"
          class="tch-filter-tab"
          :class="{ active: activeWeek === week }"
          :aria-selected="activeWeek === week"
          @click="selectWeek(week)"
        >
          第 {{ week }} 周
        </button>
      </div>
    </div>

    <div class="tch-report-layout">
      <nav class="wb-card tch-tree" aria-label="周报目录">
        <div v-for="group in reportTree" :key="group.id" class="tch-tree-group">
          <button
            type="button"
            class="tch-tree-group-title"
            :class="{ active: expandedGroupId === group.id }"
            @click="toggleGroup(group.id)"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline :points="expandedGroupId === group.id ? '6 9 12 15 18 9' : '9 18 15 12 9 6'" />
            </svg>
            {{ group.teamLabel }} · {{ group.topicTitle }}
          </button>
          <template v-if="expandedGroupId === group.id">
            <button
              v-for="report in groupReports(group)"
              :key="report.id"
              type="button"
              class="tch-tree-item"
              :class="{ active: selectedReportId === report.id }"
              @click="selectReport(report.id, group.id)"
            >
              {{ report.memberName }} · 第 {{ report.week }} 周
            </button>
            <p v-if="!groupReports(group).length" class="tch-tree-empty">该周暂无已提交周报</p>
          </template>
        </div>
      </nav>

      <article v-if="selectedReport && selectedGroup" class="wb-card tch-report-content">
        <header class="tch-report-header">
          <div>
            <h4>{{ selectedReport.memberName }} · 第 {{ selectedReport.week }} 周周报</h4>
            <p class="tch-report-meta">
              {{ selectedGroup.teamLabel }} · 提交于 {{ selectedReport.submittedAt }}
            </p>
          </div>
          <span class="tch-tag tch-tag--success">已提交</span>
        </header>

        <div class="tch-report-body">
          <h5>本周完成</h5>
          <p>{{ selectedReport.sections.done }}</p>

          <h5>下周计划</h5>
          <p>{{ selectedReport.sections.plan }}</p>

          <h5>问题与风险</h5>
          <p>{{ selectedReport.sections.risk }}</p>
        </div>

        <footer class="tch-report-footer">
          <button type="button" class="wb-btn-schedule wb-btn-schedule--outline wb-btn-schedule--sm" disabled title="二期功能">
            添加评语（二期）
          </button>
          <RouterLink to="/teaching/teams" class="wb-btn-schedule wb-btn-schedule--outline wb-btn-schedule--sm">
            查看该组项目空间
          </RouterLink>
        </footer>
      </article>

      <article v-else class="wb-card tch-report-content tch-report-content--empty">
        <p>从左侧选择小组与成员周报查看详情</p>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { MOCK_REPORT_TREE, MOCK_REPORT_WEEKS } from '../mockTeachingData'

const reportTree = ref(
  MOCK_REPORT_TREE.map((group) => ({ ...group, reports: [...group.reports] })),
)

const activeSubmitFilter = ref('submitted')
const activeWeek = ref(12)
const expandedGroupId = ref('team-1')
const selectedReportId = ref('r-1-12-zl')

const submitTabs = [
  { id: 'submitted', label: '已提交' },
  { id: 'unsubmitted', label: '未提交' },
  { id: 'all', label: '全部' },
]

const groupReports = (group) => {
  return group.reports.filter((report) => {
    if (report.week !== activeWeek.value) return false
    if (activeSubmitFilter.value === 'submitted') return report.status === 'submitted'
    if (activeSubmitFilter.value === 'unsubmitted') return report.status !== 'submitted'
    return true
  })
}

const selectedReport = computed(() => {
  for (const group of reportTree.value) {
    const report = group.reports.find((item) => item.id === selectedReportId.value)
    if (report) return report
  }
  return null
})

const selectedGroup = computed(() => {
  return reportTree.value.find((group) => group.id === expandedGroupId.value) || null
})

const toggleGroup = (groupId) => {
  if (expandedGroupId.value === groupId) {
    expandedGroupId.value = null
    selectedReportId.value = null
    return
  }
  expandedGroupId.value = groupId
  const reports = groupReports(reportTree.value.find((g) => g.id === groupId))
  selectedReportId.value = reports[0]?.id ?? null
}

const selectReport = (reportId, groupId) => {
  selectedReportId.value = reportId
  expandedGroupId.value = groupId
}

const selectWeek = (week) => {
  activeWeek.value = week
}

watch([activeWeek, activeSubmitFilter], () => {
  const group = reportTree.value.find((item) => item.id === expandedGroupId.value)
  if (!group) {
    selectedReportId.value = null
    return
  }
  const reports = groupReports(group)
  if (!reports.some((item) => item.id === selectedReportId.value)) {
    selectedReportId.value = reports[0]?.id ?? null
  }
})
</script>
