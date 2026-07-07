<template>
  <div class="wb-dashboard">
    <!-- 第一层：欢迎横幅 + 快捷入口 -->
    <section class="wb-hero-row">
      <div class="wb-card wb-hero-banner">
        <div class="wb-hero-text">
          <h2>{{ greeting }}，{{ displayName }}</h2>
          <p>{{ heroSummary }}</p>
          <RouterLink
            v-if="canGoWorkspace"
            to="/workspace/project/board"
            class="wb-btn-schedule"
            @click="trackRecent('board')"
          >
            进入项目空间
          </RouterLink>
          <RouterLink
            v-else
            to="/profile?tab=team"
            class="wb-btn-schedule"
          >
            {{ hasTeam ? '查看选题' : '去组队选题' }}
          </RouterLink>
        </div>
        <div class="wb-hero-art" aria-hidden="true">
          <div class="wb-art-sphere wb-art-sphere-1" />
          <div class="wb-art-sphere wb-art-sphere-2" />
          <div class="wb-art-sphere wb-art-sphere-3" />
          <div class="wb-art-sphere wb-art-sphere-4" />
          <div class="wb-art-sphere wb-art-sphere-5" />
        </div>
      </div>

      <div class="wb-quick-grid">
        <component
          :is="app.locked ? 'button' : 'RouterLink'"
          v-for="app in quickApps"
          :key="app.id"
          v-bind="app.locked ? { type: 'button' } : { to: app.to }"
          class="wb-quick-item wb-card"
          :class="{ 'wb-quick-item--locked': app.locked }"
          :title="app.locked ? app.lockHint : undefined"
          @click="handleAppClick(app, $event)"
        >
          <span class="wb-quick-icon" :class="`wb-quick-icon--${app.color}`" v-html="app.icon" />
          <span class="wb-quick-label">{{ app.label }}</span>
          <span v-if="app.locked" class="wb-quick-lock" aria-hidden="true">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </span>
        </component>
      </div>
    </section>

    <!-- 第二层：精选模块 -->
    <section class="wb-featured-section">
      <div class="wb-section-header">
        <h3>精选模块</h3>
        <span class="wb-section-sub">常用工作流，一键直达</span>
      </div>

      <div class="wb-featured-grid">
        <!-- 项目进度 -->
        <article class="wb-featured-card wb-card">
          <div class="wb-featured-preview wb-featured-preview--board">
            <div class="wb-preview-bar" style="width: 72%" />
            <div class="wb-preview-bar" style="width: 48%" />
            <div class="wb-preview-bar" style="width: 86%" />
          </div>
          <div class="wb-featured-body">
            <div class="wb-featured-head">
              <h4>项目进度</h4>
              <span v-if="canGoWorkspace" class="wb-featured-badge">{{ taskProgressPercent }}%</span>
            </div>
            <p v-if="loading" class="wb-featured-desc">正在加载…</p>
            <p v-else-if="canGoWorkspace" class="wb-featured-desc">
              共 {{ totalTaskCount }} 项任务，已完成 {{ completedTaskCount }} 项
            </p>
            <p v-else class="wb-featured-desc">选题通过审批后，可在此查看项目整体进度。</p>
            <RouterLink
              v-if="canGoWorkspace"
              to="/workspace/project/board"
              class="wb-featured-link"
              @click="trackRecent('board')"
            >
              查看数据看板 →
            </RouterLink>
            <RouterLink v-else to="/profile?tab=team" class="wb-featured-link">去选题 →</RouterLink>
          </div>
        </article>

        <!-- 我的待办 -->
        <article class="wb-featured-card wb-card">
          <div class="wb-featured-preview wb-featured-preview--tasks">
            <div v-for="n in 3" :key="n" class="wb-preview-task">
              <span class="wb-preview-check" :class="{ done: n === 2 && focusTasks.length }" />
              <span class="wb-preview-line" />
            </div>
          </div>
          <div class="wb-featured-body">
            <div class="wb-featured-head">
              <h4>我的待办</h4>
              <span class="wb-card-date">{{ todayLabel }}</span>
            </div>

            <ul v-if="focusTasks.length" class="wb-featured-tasks">
              <li v-for="task in focusTasks.slice(0, 3)" :key="task.id" class="wb-featured-task-item">
                <label class="wb-task-checkbox">
                  <input
                    type="checkbox"
                    :checked="task.completed"
                    @change="toggleTask(task)"
                  />
                  <span class="wb-checkmark" />
                </label>
                <span class="wb-featured-task-title" :class="{ done: task.completed }">{{ task.title }}</span>
              </li>
            </ul>
            <p v-else class="wb-featured-desc">
              {{ loading ? '正在加载…' : '暂无待办，去甘特图添加任务。' }}
            </p>

            <button
              v-if="canGoWorkspace"
              type="button"
              class="wb-featured-link wb-featured-link--btn"
              @click="goToGantt"
            >
              查看全部 →
            </button>
          </div>
        </article>

        <!-- 小组选题 -->
        <article class="wb-featured-card wb-card">
          <div class="wb-featured-preview wb-featured-preview--topic">
            <div class="wb-preview-doc">
              <div class="wb-preview-doc-line" />
              <div class="wb-preview-doc-line short" />
              <div class="wb-preview-doc-line" />
            </div>
          </div>
          <div class="wb-featured-body">
            <div class="wb-featured-head">
              <h4>小组选题</h4>
              <span v-if="hasTeam" class="wb-topic-badge" :class="topicStatusClass">{{ topicStatusLabel }}</span>
            </div>

            <p v-if="loading" class="wb-featured-desc">正在加载…</p>
            <template v-else-if="!hasTeam">
              <p class="wb-featured-desc">尚未加入小组，请先创建或加入小组。</p>
              <RouterLink to="/profile?tab=team" class="wb-featured-link">去组队 →</RouterLink>
            </template>
            <template v-else-if="hasTopic">
              <p class="wb-featured-topic-name">{{ team.topicTitle }}</p>
              <p class="wb-featured-desc">{{ team.topicDesc || '暂无选题说明。' }}</p>
              <RouterLink
                :to="canGoWorkspace ? '/workspace/project/board' : '/profile?tab=team'"
                class="wb-featured-link"
                @click="canGoWorkspace && trackRecent('board')"
              >
                {{ canGoWorkspace ? '进入项目空间 →' : isPending ? '查看选题 →' : '去选题 →' }}
              </RouterLink>
            </template>
            <template v-else>
              <p class="wb-featured-desc">填写选题标题与说明，提交后等待教师审批。</p>
              <RouterLink to="/profile?tab=team" class="wb-featured-link">提交选题 →</RouterLink>
            </template>
          </div>
        </article>
      </div>
    </section>

    <!-- 第三层：全部能力 -->
    <section class="wb-catalog-section">
      <div class="wb-section-header">
        <h3>全部能力</h3>
      </div>

      <div class="wb-catalog-tabs" role="tablist" aria-label="能力分类">
        <button
          v-for="tab in catalogTabs"
          :key="tab.id"
          type="button"
          role="tab"
          class="wb-catalog-tab"
          :class="{ active: activeCatalogTab === tab.id }"
          :aria-selected="activeCatalogTab === tab.id"
          @click="activeCatalogTab = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="wb-catalog-grid">
        <component
          :is="app.locked ? 'button' : 'RouterLink'"
          v-for="app in filteredCatalogApps"
          :key="app.id"
          v-bind="app.locked ? { type: 'button' } : { to: app.to }"
          class="wb-catalog-item"
          :class="{ 'wb-catalog-item--locked': app.locked }"
          :title="app.locked ? app.lockHint : undefined"
          @click="handleAppClick(app, $event)"
        >
          <span class="wb-catalog-icon" :class="`wb-quick-icon--${app.color}`" v-html="app.icon" />
          <span class="wb-catalog-name">{{ app.label }}</span>
          <span v-if="app.locked" class="wb-catalog-lock" aria-hidden="true">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </span>
        </component>

        <p v-if="!filteredCatalogApps.length" class="wb-catalog-empty">
          {{ activeCatalogTab === 'recent' ? '暂无最近使用记录，可从下方「全部」浏览能力' : '该分类下暂无能力' }}
        </p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { CacheCode, PLAN_TASK_STATUS } from '@/constants/cacheCode'
import { listPlanTasksApi } from '@/api/task'
import { getCurrentTeamApi } from '@/api/team'
import { useWorkspace } from '@/composables/useWorkspace'

const RECENT_STORAGE_KEY = 'wb-recent-apps'
const LOCK_HINT = '需先通过选题审批'

const ICONS = {
  board: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>`,
  gantt: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
  weekly: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`,
  doc: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>`,
  sheet: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="9" y1="3" x2="9" y2="21"/></svg>`,
  team: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
  profile: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  security: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
}

const APP_DEFINITIONS = [
  { id: 'board', label: '数据看板', to: '/workspace/project/board', color: 'blue', category: 'project', requiresWorkspace: true, icon: ICONS.board },
  { id: 'gantt', label: '任务甘特图', to: '/workspace/project/gantt', color: 'purple', category: 'project', requiresWorkspace: true, icon: ICONS.gantt },
  { id: 'weekly', label: '周报', to: '/workspace/project/weekly', color: 'green', category: 'project', requiresWorkspace: true, icon: ICONS.weekly },
  { id: 'doc', label: '文档协作', to: '/workspace/project/board', color: 'orange', category: 'collab', requiresWorkspace: true, icon: ICONS.doc },
  { id: 'sheet', label: '表格协作', to: '/workspace/project/board', color: 'cyan', category: 'collab', requiresWorkspace: true, icon: ICONS.sheet },
  { id: 'team', label: '我的小组', to: '/profile?tab=team', color: 'pink', category: 'personal', requiresWorkspace: false, icon: ICONS.team },
  { id: 'profile', label: '个人中心', to: '/profile', color: 'slate', category: 'personal', requiresWorkspace: false, icon: ICONS.profile },
  { id: 'security', label: '安全设置', to: '/profile?tab=security', color: 'slate', category: 'personal', requiresWorkspace: false, icon: ICONS.security },
]

const QUICK_APP_IDS = ['board', 'gantt', 'weekly', 'doc', 'sheet', 'team']

const catalogTabs = [
  { id: 'recent', label: '最近使用' },
  { id: 'all', label: '全部' },
  { id: 'project', label: '项目管理' },
  { id: 'collab', label: '协作' },
  { id: 'personal', label: '个人' },
]

const { loadCurrentWorkspace, requireWorkspaceId } = useWorkspace()
const router = useRouter()

const loading = ref(false)
const rawTasks = ref([])
const team = ref(null)
const recentAppIds = ref([])
const activeCatalogTab = ref('all')

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
const displayName = computed(() => storageUser?.realName || storageUser?.nickName || storageUser?.username || '同学')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayLabel = computed(() => {
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  }).format(new Date())
})

const TEAM_STATUS = {
  NORMAL: CacheCode.TEAM_STATUS_NORMAL,
  PENDING_TOPIC: CacheCode.TEAM_STATUS_PENDING_TOPIC,
  TOPIC_REJECTED: CacheCode.TEAM_STATUS_TOPIC_REJECTED,
  UNLOCKED: CacheCode.TEAM_STATUS_UNLOCKED,
}

const hasTeam = computed(() => Boolean(team.value?.id))
const hasTopic = computed(() => Boolean(team.value?.topicTitle?.trim()))
const teamStatus = computed(() => team.value?.status)
const canGoWorkspace = computed(() => teamStatus.value === TEAM_STATUS.UNLOCKED)
const isPending = computed(() => teamStatus.value === TEAM_STATUS.PENDING_TOPIC)
const isRejected = computed(() => teamStatus.value === TEAM_STATUS.TOPIC_REJECTED)

const topicStatusLabel = computed(() => {
  if (!hasTeam.value) return ''
  if (canGoWorkspace.value) return '已通过'
  if (isPending.value) return '审批中'
  if (isRejected.value) return '未通过'
  if (!hasTopic.value) return '未选题'
  return '待审批'
})

const topicStatusClass = computed(() => {
  if (canGoWorkspace.value) return 'wb-topic-badge--success'
  if (isPending.value) return 'wb-topic-badge--pending'
  if (isRejected.value) return 'wb-topic-badge--warning'
  return 'wb-topic-badge--muted'
})

const pendingTaskCount = computed(() => {
  return rawTasks.value.filter((task) => task.taskStatus !== PLAN_TASK_STATUS.COMPLETED).length
})

const totalTaskCount = computed(() => rawTasks.value.length)
const completedTaskCount = computed(() => {
  return rawTasks.value.filter((task) => task.taskStatus === PLAN_TASK_STATUS.COMPLETED).length
})

const taskProgressPercent = computed(() => {
  if (!totalTaskCount.value) return 0
  return Math.round((completedTaskCount.value / totalTaskCount.value) * 100)
})

const projectLabel = computed(() => team.value?.topicTitle || team.value?.teamName || '项目')

const heroSummary = computed(() => {
  if (loading.value) return '正在同步工作台数据…'
  if (canGoWorkspace.value) {
    const pending = pendingTaskCount.value
    if (pending > 0) {
      return `「${projectLabel.value}」还有 ${pending} 项任务待完成，继续推进吧。`
    }
    return `「${projectLabel.value}」任务已全部完成，去看看本周周报吧。`
  }
  if (isPending.value) return '选题已提交，正在等待教师审批。通过后即可进入项目空间。'
  if (isRejected.value) return '选题未通过审批，请根据教师反馈修改后重新提交。'
  if (hasTeam.value && !hasTopic.value) return '小组已创建，请尽快提交课程选题。'
  if (hasTeam.value) return '完成选题审批后，即可解锁项目空间与协作能力。'
  return '创建或加入小组，提交选题后即可开始协作。'
})

const resolveApp = (def) => ({
  ...def,
  locked: def.requiresWorkspace && !canGoWorkspace.value,
  lockHint: LOCK_HINT,
})

const quickApps = computed(() => {
  return QUICK_APP_IDS.map((id) => resolveApp(APP_DEFINITIONS.find((app) => app.id === id))).filter(Boolean)
})

const filteredCatalogApps = computed(() => {
  if (activeCatalogTab.value === 'recent') {
    const recent = recentAppIds.value
      .map((id) => APP_DEFINITIONS.find((app) => app.id === id))
      .filter(Boolean)
    return recent.map(resolveApp)
  }
  if (activeCatalogTab.value === 'all') {
    return APP_DEFINITIONS.map(resolveApp)
  }
  return APP_DEFINITIONS.filter((app) => app.category === activeCatalogTab.value).map(resolveApp)
})

const focusTasks = computed(() => {
  return rawTasks.value
    .filter((task) => task.taskStatus !== PLAN_TASK_STATUS.COMPLETED)
    .slice(0, 4)
    .map((task) => ({
      id: task.id,
      title: task.name || task.taskName || '未命名任务',
      completed: task.taskStatus === PLAN_TASK_STATUS.COMPLETED,
      raw: task,
    }))
})

const loadRecentApps = () => {
  try {
    const stored = JSON.parse(localStorage.getItem(RECENT_STORAGE_KEY) || '[]')
    recentAppIds.value = Array.isArray(stored) ? stored : []
    if (recentAppIds.value.length) {
      activeCatalogTab.value = 'recent'
    }
  } catch {
    recentAppIds.value = []
  }
}

const trackRecent = (appId) => {
  const next = [appId, ...recentAppIds.value.filter((id) => id !== appId)].slice(0, 6)
  recentAppIds.value = next
  localStorage.setItem(RECENT_STORAGE_KEY, JSON.stringify(next))
}

const handleAppClick = (app, event) => {
  if (app.locked) {
    event.preventDefault()
    router.push('/profile?tab=team')
    return
  }
  trackRecent(app.id)
}

const loadData = async () => {
  loading.value = true
  try {
    await loadCurrentWorkspace()
    const workspaceId = requireWorkspaceId()
    const [taskData, teamData] = await Promise.all([
      listPlanTasksApi(workspaceId).catch(() => []),
      getCurrentTeamApi().catch(() => null),
    ])
    rawTasks.value = Array.isArray(taskData) ? taskData : []
    team.value = teamData
  } finally {
    loading.value = false
  }
}

const toggleTask = async (task) => {
  if (!task.raw) return
  const nextStatus = task.completed ? PLAN_TASK_STATUS.NOT_STARTED : PLAN_TASK_STATUS.COMPLETED
  task.raw.taskStatus = nextStatus
  rawTasks.value = [...rawTasks.value]
}

const goToGantt = async () => {
  trackRecent('gantt')
  await router.push('/workspace/project/gantt')
}

onMounted(() => {
  loadRecentApps()
  loadData()
})
</script>
