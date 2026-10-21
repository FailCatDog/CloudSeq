<template>
  <n-spin :show="loading">
    <div class="wb-dashboard">
      <n-alert v-if="pageError" type="error" :bordered="false" style="margin-bottom: 16px">
        {{ pageError }}
      </n-alert>

      <section class="wb-hero-row">
        <div class="wb-card wb-hero-banner">
          <div class="wb-hero-text">
            <span v-if="course" class="wb-hero-course">{{ course.termLabel }} · {{ course.courseCode }}</span>
            <span v-else class="wb-hero-course">暂无课号</span>
            <h2>{{ greeting }}，{{ teacherName }}</h2>
            <p>{{ heroSummary }}</p>
            <RouterLink
              v-if="stats.pendingApprovals > 0"
              to="/teaching/approvals"
            >
              <n-button type="primary">处理待审选题（{{ stats.pendingApprovals }}）</n-button>
            </RouterLink>
            <RouterLink v-else to="/teaching/teams">
              <n-button type="primary">查看小组总览</n-button>
            </RouterLink>
          </div>
          <div class="wb-hero-art" aria-hidden="true">
            <div class="wb-art-sphere wb-art-sphere-1" />
            <div class="wb-art-sphere wb-art-sphere-2" />
            <div class="wb-art-sphere wb-art-sphere-3" />
          </div>
        </div>

        <div class="tch-stat-grid">
          <RouterLink to="/teaching/approvals" class="wb-card tch-stat-card">
            <span class="tch-stat-label">待审选题</span>
            <span class="tch-stat-value tch-stat-value--purple">{{ stats.pendingApprovals }}</span>
            <span class="tch-stat-hint">{{ stats.pendingHint }}</span>
          </RouterLink>
          <RouterLink to="/teaching/teams" class="wb-card tch-stat-card">
            <span class="tch-stat-label">在研小组</span>
            <span class="tch-stat-value">{{ stats.activeTeams }}</span>
            <span class="tch-stat-hint">已解锁项目空间</span>
          </RouterLink>
          <RouterLink to="/teaching/reports" class="wb-card tch-stat-card">
            <span class="tch-stat-label">本周周报</span>
            <span class="tch-stat-value">{{ stats.weeklySubmitted }}</span>
            <span class="tch-stat-hint">已提交 / 共 {{ stats.weeklyTotal }} 份</span>
          </RouterLink>
          <div class="wb-card tch-stat-card tch-stat-card--static">
            <span class="tch-stat-label">逾期任务</span>
            <span class="tch-stat-value tch-stat-value--warning">{{ stats.overdueTasks }}</span>
            <span class="tch-stat-hint">跨 {{ stats.overdueTeamCount }} 个小组</span>
          </div>
        </div>
      </section>

      <section class="tch-two-col">
        <n-card class="tch-panel" :bordered="false">
          <div class="wb-section-header">
            <div>
              <h3>待处理</h3>
              <span class="wb-section-sub">选题审批队列</span>
            </div>
            <RouterLink to="/teaching/approvals" class="wb-view-all">查看全部 →</RouterLink>
          </div>
          <ul v-if="pendingItems.length" class="tch-list">
            <li v-for="item in pendingItems" :key="item.id" class="tch-list-item">
              <div class="tch-list-item__main">
                <div class="tch-list-item__title">
                  <n-tag type="info" round size="small" style="margin-right: 8px">待审</n-tag>
                  {{ item.teamLabel }} · {{ item.topicTitle }}
                </div>
                <div class="tch-list-item__meta">
                  组长 {{ item.leaderName }} · {{ item.memberCount }} 人 · 提交于 {{ item.submittedAt }}
                </div>
              </div>
              <div class="tch-list-item__actions">
                <RouterLink to="/teaching/approvals">
                  <n-button size="small" type="primary">审批</n-button>
                </RouterLink>
              </div>
            </li>
          </ul>
          <n-empty v-else description="暂无待审选题，班级审批队列已清空。" />
        </n-card>

        <n-card class="tch-panel" :bordered="false">
          <div class="wb-section-header">
            <div>
              <h3>需关注小组</h3>
              <span class="wb-section-sub">进度 / 周报风险</span>
            </div>
            <RouterLink to="/teaching/teams" class="wb-view-all">小组总览 →</RouterLink>
          </div>
          <ul v-if="atRiskTeams.length" class="tch-list">
            <li v-for="item in atRiskTeams" :key="item.id" class="tch-list-item">
              <div class="tch-list-item__main">
                <div class="tch-list-item__title">{{ item.teamLabel }} · {{ item.topicTitle }}</div>
                <div class="tch-list-item__meta">{{ item.riskHint }}</div>
                <div class="wb-progress-wrap">
                  <n-progress
                    type="line"
                    :percentage="item.progressPercent"
                    :status="progressStatus(item.progressPercent)"
                    :show-indicator="false"
                    style="flex: 1"
                  />
                  <span class="wb-progress-label">{{ item.progressPercent }}%</span>
                </div>
              </div>
              <RouterLink :to="item.inspectTo">
                <n-button text type="primary">巡查</n-button>
              </RouterLink>
            </li>
          </ul>
          <n-empty v-else description="暂无需要特别关注的小组。" />
        </n-card>
      </section>
    </div>
  </n-spin>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { getTeacherDashboardApi } from '@/api/teaching'
import { loadCourseStore, useCourseStore } from '@/stores/courseStore'
import { mapTeachingDashboard } from '@/utils/teachingFormat'
import { getUserFromStorage } from '@/utils/roleHome'

const { selectedCourseId } = useCourseStore()

const loading = ref(false)
const pageError = ref('')
const course = ref(null)
const stats = ref({
  pendingApprovals: 0,
  pendingHint: '审批队列已清空',
  activeTeams: 0,
  weeklySubmitted: 0,
  weeklyTotal: 0,
  overdueTasks: 0,
  overdueTeamCount: 0,
})
const pendingItems = ref([])
const atRiskTeams = ref([])

const storageUser = getUserFromStorage()

const teacherName = computed(() => {
  const name = storageUser?.realName || storageUser?.nickName || storageUser?.username
  if (!name) return '老师'
  return name.endsWith('老师') ? name : `${name}老师`
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const heroSummary = computed(() => {
  if (loading.value) return '正在同步教学工作台数据…'
  if (!course.value) return '请先创建课号，即可开始管理选题审批与小组进度。'

  const pending = stats.value.pendingApprovals
  const riskCount = atRiskTeams.value.length
  const courseName = course.value.courseName

  if (pending > 0 && riskCount > 0) {
    return `${courseName}课程共有 ${pending} 条选题待审批，${riskCount} 个小组存在进度或周报风险，建议优先处理待审队列。`
  }
  if (pending > 0) {
    return `${courseName}课程共有 ${pending} 条选题待审批，建议优先处理待审队列。`
  }
  if (riskCount > 0) {
    return `${courseName}课程审批队列已清空，仍有 ${riskCount} 个小组需要关注进度或周报。`
  }
  return `${courseName}课程审批队列已清空，可前往小组总览巡查项目进度。`
})

const progressStatus = (percent) => {
  if (percent < 40) return 'error'
  if (percent < 60) return 'warning'
  return 'success'
}

const loadDashboard = async () => {
  loading.value = true
  pageError.value = ''
  try {
    const data = await getTeacherDashboardApi({
      courseId: selectedCourseId.value || undefined,
    })
    const mapped = mapTeachingDashboard(data)
    course.value = mapped.course
    stats.value = mapped.stats
    pendingItems.value = mapped.pendingItems
    atRiskTeams.value = mapped.atRiskTeams
  } catch (error) {
    course.value = null
    stats.value = {
      pendingApprovals: 0,
      pendingHint: '审批队列已清空',
      activeTeams: 0,
      weeklySubmitted: 0,
      weeklyTotal: 0,
      overdueTasks: 0,
      overdueTeamCount: 0,
    }
    pendingItems.value = []
    atRiskTeams.value = []
    pageError.value = error?.message || '加载教学工作台失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadCourseStore()
  } catch {
    // 课号加载失败时仍尝试拉取聚合数据
  }
  await loadDashboard()
})

watch(selectedCourseId, () => {
  loadDashboard()
})
</script>
