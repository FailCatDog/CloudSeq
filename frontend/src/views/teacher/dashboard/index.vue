<template>
  <div class="wb-dashboard">
    <section class="wb-hero-row">
      <div class="wb-card wb-hero-banner">
        <div class="wb-hero-text">
          <span class="wb-hero-course">{{ course.termLabel }} · {{ course.courseCode }}</span>
          <h2>{{ greeting }}，{{ teacherName }}</h2>
          <p>{{ heroSummary }}</p>
          <RouterLink
            v-if="stats.pendingApprovals > 0"
            to="/teaching/approvals"
            class="wb-btn-schedule"
          >
            处理待审选题（{{ stats.pendingApprovals }}）
          </RouterLink>
          <RouterLink v-else to="/teaching/teams" class="wb-btn-schedule">
            查看小组总览
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
      <article class="wb-card tch-panel">
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
                <span class="tch-tag tch-tag--pending">待审</span>
                {{ item.teamLabel }} · {{ item.topicTitle }}
              </div>
              <div class="tch-list-item__meta">
                组长 {{ item.leaderName }} · {{ item.memberCount }} 人 · 提交于 {{ item.submittedAt }}
              </div>
            </div>
            <div class="tch-list-item__actions">
              <RouterLink to="/teaching/approvals" class="wb-btn-schedule wb-btn-schedule--sm">
                审批
              </RouterLink>
            </div>
          </li>
        </ul>
        <p v-else class="tch-empty-hint">暂无待审选题，班级审批队列已清空。</p>
      </article>

      <article class="wb-card tch-panel">
        <div class="wb-section-header">
          <div>
            <h3>需关注小组</h3>
            <span class="wb-section-sub">进度 / 周报风险</span>
          </div>
          <RouterLink to="/teaching/teams" class="wb-view-all">小组总览 →</RouterLink>
        </div>
        <ul class="tch-list">
          <li v-for="item in atRiskTeams" :key="item.id" class="tch-list-item">
            <div class="tch-list-item__main">
              <div class="tch-list-item__title">{{ item.teamLabel }} · {{ item.topicTitle }}</div>
              <div class="tch-list-item__meta">{{ item.riskHint }}</div>
              <div class="wb-progress-wrap">
                <div class="wb-progress-bar">
                  <div
                    class="wb-progress-fill"
                    :class="progressClass(item.progressPercent)"
                    :style="{ width: `${item.progressPercent}%` }"
                  />
                </div>
                <span class="wb-progress-label">{{ item.progressPercent }}%</span>
              </div>
            </div>
            <RouterLink :to="item.inspectTo" class="tch-table-link">巡查</RouterLink>
          </li>
        </ul>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

/** 静态演示数据，后续接入 GET /api/teaching/dashboard */
const MOCK_COURSE = {
  termLabel: '2026年春季学期',
  courseCode: 'SE2026-01',
  courseName: '软件工程',
}

const MOCK_STATS = {
  pendingApprovals: 3,
  pendingHint: '最早提交于 2 小时前',
  activeTeams: 12,
  weeklySubmitted: 28,
  weeklyTotal: 48,
  overdueTasks: 5,
  overdueTeamCount: 4,
}

const MOCK_PENDING = [
  {
    id: 1,
    teamLabel: '第 3 组',
    topicTitle: '校园社团协作系统',
    leaderName: '李明',
    memberCount: 4,
    submittedAt: '2 小时前',
  },
  {
    id: 2,
    teamLabel: '第 7 组',
    topicTitle: '在线考试防作弊方案',
    leaderName: '王芳',
    memberCount: 5,
    submittedAt: '昨天 18:32',
  },
  {
    id: 3,
    teamLabel: '第 11 组',
    topicTitle: '实验室设备预约平台',
    leaderName: '陈浩',
    memberCount: 4,
    submittedAt: '昨天 09:15',
  },
]

const MOCK_AT_RISK = [
  {
    id: 1,
    teamLabel: '第 5 组',
    topicTitle: '智慧校园导航',
    riskHint: '连续 2 周未交周报 · 任务完成率 35%',
    progressPercent: 35,
    inspectTo: '/teaching/teams',
  },
  {
    id: 2,
    teamLabel: '第 8 组',
    topicTitle: '二手教材交易平台',
    riskHint: '3 项任务逾期 · 最近周报 第 10 周',
    progressPercent: 52,
    inspectTo: '/teaching/teams',
  },
  {
    id: 3,
    teamLabel: '第 2 组',
    topicTitle: '课程问答社区',
    riskHint: '本周周报提交率 50%（2/4 人）',
    progressPercent: 68,
    inspectTo: '/teaching/teams',
  },
]

const getStorageUser = () => {
  const raw = localStorage.getItem('user') || sessionStorage.getItem('user')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

const storageUser = getStorageUser()
const course = MOCK_COURSE
const stats = MOCK_STATS
const pendingItems = MOCK_PENDING
const atRiskTeams = MOCK_AT_RISK

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
  const pending = stats.pendingApprovals
  const riskCount = atRiskTeams.length
  if (pending > 0 && riskCount > 0) {
    return `${course.courseName}课程共有 ${pending} 条选题待审批，${riskCount} 个小组存在进度或周报风险，建议优先处理待审队列。`
  }
  if (pending > 0) {
    return `${course.courseName}课程共有 ${pending} 条选题待审批，建议优先处理待审队列。`
  }
  return `${course.courseName}课程审批队列已清空，可前往小组总览巡查项目进度。`
})

const progressClass = (percent) => {
  if (percent < 40) return 'wb-progress-fill--danger'
  if (percent < 60) return 'wb-progress-fill--warning'
  return ''
}
</script>

<style scoped>
.tch-empty-hint {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--wb-text-secondary);
  line-height: 1.6;
}
</style>
