<template>
  <div class="console-page">
    <div class="console-filter">
      <div class="console-filter__fields">
        <div class="console-filter__field console-filter__field--wide">
          <span class="console-filter__label">课号</span>
          <n-select
            v-model:value="selectedCourseId"
            :options="courseOptions"
            :loading="coursesLoading"
            clearable
            placeholder="请选择课号"
          />
        </div>
        <div class="console-filter__field">
          <span class="console-filter__label">周次</span>
          <n-select
            v-model:value="selectedWeekValue"
            :options="weekOptions"
            :disabled="!searched || loading || !weekOptions.length"
            clearable
            :placeholder="weekOptions.length ? '请选择周次' : '暂无已提交周报'"
            @update:value="handleWeekChange"
          />
        </div>
      </div>
      <div class="console-filter__actions">
        <n-button type="primary" :loading="loading" :disabled="!selectedCourseId" @click="handleSearch">
          搜索
        </n-button>
        <n-button quaternary :disabled="loading" @click="handleReset">重置</n-button>
      </div>
    </div>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <div class="console-report-layout">
      <n-card class="console-report-tree" :bordered="false">
        <n-spin :show="loading">
          <n-empty v-if="!searched" description="请选择课号并点击搜索" />
          <n-empty v-else-if="!reportTree.length" description="该课号下暂无小组" />
          <n-collapse v-else accordion :expanded-names="expandedGroupId" @update:expanded-names="handleGroupExpand">
            <n-collapse-item
              v-for="group in reportTree"
              :key="group.id"
              :title="`${group.teamLabel} · ${group.topicTitle}`"
              :name="group.id"
            >
              <n-empty v-if="!selectedWeekValue" size="small" description="请先选择周次" />
              <n-empty v-else-if="!groupReports(group).length" size="small" description="该周暂无已提交周报" />
              <n-space v-else vertical :size="4">
                <n-button
                  v-for="report in groupReports(group)"
                  :key="report.id"
                  quaternary
                  block
                  :type="selectedReportId === report.id ? 'primary' : 'default'"
                  style="justify-content: flex-start"
                  @click="selectReport(report.id, group.id)"
                >
                  {{ report.memberName }} · 第 {{ report.reportWeek }} 周
                </n-button>
              </n-space>
            </n-collapse-item>
          </n-collapse>
        </n-spin>
      </n-card>

      <n-card
        v-if="selectedReport && selectedGroup"
        class="console-report-content"
        :bordered="false"
      >
        <header class="console-report-header">
          <div>
            <h4>{{ selectedReport.memberName }} · 第 {{ selectedReport.reportWeek }} 周周报</h4>
            <p class="console-report-meta">
              {{ selectedGroup.teamLabel }} · {{ selectedGroup.topicTitle }} · 提交于 {{ selectedReport.submitDate }}
            </p>
          </div>
          <n-tag type="success" round size="small">已提交</n-tag>
        </header>

        <div class="console-report-body">
          <h5>本周完成</h5>
          <p>{{ selectedReport.weeklyProgress }}</p>

          <h5>下周计划</h5>
          <p>{{ selectedReport.nextPlan }}</p>

          <h5>问题与风险</h5>
          <p>{{ selectedReport.problems }}</p>
        </div>

        <footer class="console-report-footer">
          <n-button disabled title="二期功能">添加评语（二期）</n-button>
          <RouterLink to="/teaching/teams">
            <n-button quaternary>查看该组项目空间</n-button>
          </RouterLink>
        </footer>
      </n-card>

      <n-card v-else class="console-report-content console-report-content--empty" :bordered="false">
        <n-empty
          :description="
            !searched
              ? '请选择课号并搜索后查看周报'
              : !selectedWeekValue
                ? '请选择周次后展开小组查看周报'
                : '从左侧选择成员周报查看详情'
          "
        />
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getTeacherWeeklyReviewApi } from '@/api/weekly'
import { loadCourseStore, useCourseStore } from '@/stores/courseStore'
import { formatCourseSelectLabel } from '@/utils/courseFormat'
import { mapTeacherWeeklyReview, parseWeekOptionValue } from '@/utils/weeklyFormat'

const { courses, loading: coursesLoading } = useCourseStore()

const reportTree = ref([])
const weekOptions = ref([])
const loading = ref(false)
const pageError = ref('')
const searched = ref(false)
const selectedCourseId = ref(null)
const selectedWeekValue = ref(null)
const expandedGroupId = ref(null)
const selectedReportId = ref(null)

const courseOptions = computed(() =>
  courses.value.map((course) => ({
    label: formatCourseSelectLabel(course),
    value: course.id,
  })),
)

const groupReports = (group) => {
  if (!selectedWeekValue.value) return []
  return group.reports
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

const loadReviewData = async ({ keepWeek = false } = {}) => {
  if (!selectedCourseId.value) {
    pageError.value = '请选择课号'
    return
  }

  loading.value = true
  pageError.value = ''

  const weekParams = keepWeek ? parseWeekOptionValue(selectedWeekValue.value) : { reportYear: null, reportWeek: null }
  if (!keepWeek) {
    selectedWeekValue.value = null
    selectedReportId.value = null
    expandedGroupId.value = null
  }

  try {
    const data = await getTeacherWeeklyReviewApi({
      courseId: selectedCourseId.value,
      ...weekParams,
    })
    const mapped = mapTeacherWeeklyReview(data)
    weekOptions.value = mapped.weekOptions.map((item) => ({
      label: item.label,
      value: item.value,
    }))
    reportTree.value = mapped.teams
    searched.value = true

    if (keepWeek && selectedWeekValue.value) {
      const stillValid = weekOptions.value.some((item) => item.value === selectedWeekValue.value)
      if (!stillValid) {
        selectedWeekValue.value = null
        selectedReportId.value = null
      }
    }

    if (expandedGroupId.value && !reportTree.value.some((item) => item.id === expandedGroupId.value)) {
      expandedGroupId.value = reportTree.value[0]?.id ?? null
      selectedReportId.value = null
    }
  } catch (error) {
    reportTree.value = []
    weekOptions.value = []
    pageError.value = error?.message || '加载周报审阅数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadCourseStore()
  } catch {
    // 静默失败
  }
})

const handleSearch = async () => {
  await loadReviewData({ keepWeek: false })
}

const handleWeekChange = async () => {
  if (!searched.value) return
  selectedReportId.value = null
  await loadReviewData({ keepWeek: true })
  const group = reportTree.value.find((item) => item.id === expandedGroupId.value)
  if (group) {
    const reports = groupReports(group)
    selectedReportId.value = reports[0]?.id ?? null
  }
}

const handleReset = () => {
  selectedCourseId.value = null
  selectedWeekValue.value = null
  reportTree.value = []
  weekOptions.value = []
  expandedGroupId.value = null
  selectedReportId.value = null
  searched.value = false
  pageError.value = ''
}

const handleGroupExpand = (name) => {
  expandedGroupId.value = name ?? null
  if (!name) {
    selectedReportId.value = null
    return
  }
  const group = reportTree.value.find((item) => item.id === name)
  const reports = groupReports(group)
  selectedReportId.value = reports[0]?.id ?? null
}

const selectReport = (reportId, groupId) => {
  selectedReportId.value = reportId
  expandedGroupId.value = groupId
}
</script>
