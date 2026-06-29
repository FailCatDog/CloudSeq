<template>
  <div class="wb-content-grid">
    <!-- Welcome Banner -->
    <section class="wb-card wb-welcome-banner">
      <div class="wb-welcome-text">
        <h2>{{ greeting }}, {{ displayName }}.</h2>
        <p>
          You have <strong>{{ pendingTaskCount }}</strong> critical tasks remaining for the
          <RouterLink to="/workspace/project/gantt">{{ projectName }}</RouterLink> sprint.
        </p>
        <button type="button" class="wb-btn-schedule" @click="goToGantt">View Schedule</button>
      </div>
      <div class="wb-welcome-art" aria-hidden="true">
        <div class="wb-art-sphere wb-art-sphere-1" />
        <div class="wb-art-sphere wb-art-sphere-2" />
        <div class="wb-art-sphere wb-art-sphere-3" />
        <div class="wb-art-sphere wb-art-sphere-4" />
        <div class="wb-art-sphere wb-art-sphere-5" />
      </div>
    </section>

    <!-- My Focus -->
    <section class="wb-card wb-my-focus">
      <div class="wb-card-header">
        <h3>My Focus</h3>
        <span class="wb-card-date">{{ todayLabel }}</span>
      </div>

      <ul v-if="focusTasks.length" class="wb-task-list">
        <li
          v-for="task in focusTasks"
          :key="task.id"
          class="wb-task-item"
          :class="{ completed: task.completed }"
        >
          <label class="wb-task-checkbox">
            <input
              type="checkbox"
              :checked="task.completed"
              @change="toggleTask(task)"
            />
            <span class="wb-checkmark" />
          </label>
          <div class="wb-task-content">
            <span class="wb-task-title">{{ task.title }}</span>
            <div class="wb-task-tags">
              <span
                v-for="tag in task.tags"
                :key="tag.label"
                class="wb-tag"
                :class="tag.className"
              >
                {{ tag.label }}
              </span>
            </div>
          </div>
        </li>
      </ul>
      <p v-else class="wb-empty-hint">{{ loading ? 'Loading tasks...' : 'No tasks yet. Add one from the gantt view.' }}</p>

      <button type="button" class="wb-add-task-btn" @click="goToGantt">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        Add New Task
      </button>
    </section>

    <!-- Team Topic -->
    <section class="wb-team-topic">
      <div class="wb-card-header">
        <h3>小组选题</h3>
        <span v-if="hasTeam" class="wb-topic-badge" :class="topicStatusClass">{{ topicStatusLabel }}</span>
      </div>

      <div class="wb-card wb-topic-card">
        <p v-if="loading" class="wb-topic-loading">正在加载小组选题...</p>

        <template v-else-if="!hasTeam">
          <div class="wb-topic-empty">
            <p class="wb-topic-empty-title">尚未加入小组</p>
            <p class="wb-topic-empty-desc">请先创建或加入小组，再提交课程选题。</p>
          </div>
          <div class="wb-topic-footer">
            <p class="wb-topic-hint">完成组队后，方可进入项目空间开展工作。</p>
            <RouterLink to="/profile/team" class="wb-btn-schedule">去选题</RouterLink>
          </div>
        </template>

        <template v-else>
          <div class="wb-topic-meta">
            <span class="wb-topic-team">{{ team.teamName }}</span>
          </div>

          <template v-if="hasTopic">
            <h4 class="wb-topic-title">{{ team.topicTitle }}</h4>
            <p class="wb-topic-desc">{{ team.topicDesc || '暂无选题说明。' }}</p>
          </template>

          <div v-else class="wb-topic-empty">
            <p class="wb-topic-empty-title">尚未提交选题</p>
            <p class="wb-topic-empty-desc">填写选题标题与说明，提交后等待教师审批。</p>
          </div>

          <div class="wb-topic-footer">
            <p v-if="canGoWorkspace" class="wb-topic-hint wb-topic-hint--success">
              选题已通过审批，可以进入项目空间继续推进任务与文档协作。
            </p>
            <p v-else-if="isPending" class="wb-topic-hint">
              选题已提交，正在等待教师审批。通过后即可进入项目空间。
            </p>
            <p v-else-if="isRejected" class="wb-topic-hint wb-topic-hint--warning">
              选题未通过审批，请根据教师反馈修改后重新提交。
            </p>
            <p v-else class="wb-topic-hint">
              提交选题并通过审批后，即可解锁项目空间。
            </p>

            <RouterLink
              v-if="canGoWorkspace"
              to="/workspace/project/board"
              class="wb-btn-schedule"
            >
              去工作空间
            </RouterLink>
            <RouterLink
              v-else
              to="/profile/team"
              class="wb-btn-schedule"
              :class="{ 'wb-btn-schedule--outline': isPending }"
            >
              {{ isPending ? '查看选题' : '去选题' }}
            </RouterLink>
          </div>
        </template>
      </div>
    </section>

    <!-- Activity Feed -->
    <section class="wb-card wb-activity-feed">
      <div class="wb-card-header">
        <h3>Activity Feed</h3>
        <div class="wb-feed-avatars">
          <img v-for="(avatar, index) in feedAvatars" :key="index" :src="avatar" alt="" />
          <span class="wb-avatar-more">+3</span>
        </div>
      </div>

      <div class="wb-timeline">
        <div v-for="(item, index) in activityFeed" :key="index" class="wb-timeline-item">
          <div class="wb-timeline-dot" :class="{ 'wb-timeline-dot-system': item.system }">
            <svg
              v-if="item.system"
              width="10"
              height="10"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M4.9 19.1C1 15.2 1 8.8 4.9 4.9" />
              <path d="M7.8 16.2c-2.3-2.3-2.3-6.1 0-8.5" />
              <circle cx="12" cy="12" r="2" />
              <path d="M16.2 7.8c2.3 2.3 2.3 6.1 0 8.5" />
              <path d="M19.1 4.9C23 8.8 23 15.1 19.1 19.1" />
            </svg>
          </div>
          <div class="wb-timeline-content">
            <p v-html="item.html" />
            <div v-if="item.code" class="wb-code-block">{{ item.code }}</div>
            <blockquote v-if="item.quote">{{ item.quote }}</blockquote>
          </div>
        </div>
      </div>

      <div class="wb-share-update">
        <input type="text" placeholder="Share an update..." readonly />
        <button type="button" class="wb-send-btn" aria-label="Send" @click="openComingSoon">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { CacheCode, PLAN_TASK_STATUS } from '@/constants/cacheCode'
import { dictLabel } from '@/stores/dictStore'
import { listPlanTasksApi } from '@/api/task'
import { getCurrentTeamApi } from '@/api/team'

const WORKSPACE_ID = 1

const router = useRouter()
const loading = ref(false)
const rawTasks = ref([])
const team = ref(null)

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
const displayName = computed(() => storageUser?.realName || storageUser?.nickName || storageUser?.username || 'Alex')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 18) return 'Good afternoon'
  return 'Good evening'
})

const todayLabel = computed(() => {
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  })
    .format(new Date())
    .toUpperCase()
})

const TEAM_STATUS = {
  NORMAL: CacheCode.TEAM_STATUS_NORMAL,
  PENDING_TOPIC: CacheCode.TEAM_STATUS_PENDING_TOPIC,
  TOPIC_REJECTED: CacheCode.TEAM_STATUS_TOPIC_REJECTED,
  UNLOCKED: CacheCode.TEAM_STATUS_UNLOCKED,
}

const projectName = computed(() => team.value?.topicTitle || team.value?.teamName || 'Nexus Redesign')
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
  return rawTasks.value.filter((task) => task.taskStatus !== PLAN_TASK_STATUS.COMPLETED).length || 4
})

const tagPalette = [
  { className: 'wb-tag-backend', label: 'Backend' },
  { className: 'wb-tag-priority', label: 'High Priority' },
  { className: 'wb-tag-design', label: 'Design' },
  { className: 'wb-tag-marketing', label: 'Marketing' },
  { className: 'wb-tag-upcoming', label: 'Upcoming' },
  { className: 'wb-tag-devops', label: 'DevOps' },
]

const resolveTaskTags = (task, index) => {
  const tags = []
  const statusLabel = dictLabel(CacheCode.PLAN_TASK_STATUS, task.taskStatus)
  if (statusLabel) {
    tags.push({
      className: task.taskStatus === PLAN_TASK_STATUS.IN_PROGRESS ? 'wb-tag-priority' : 'wb-tag-upcoming',
      label: statusLabel,
    })
  }
  tags.push(tagPalette[index % tagPalette.length])
  return tags.slice(0, 2)
}

const focusTasks = computed(() => {
  if (!rawTasks.value.length) {
    return fallbackTasks
  }

  return rawTasks.value.slice(0, 4).map((task, index) => ({
    id: task.id,
    title: task.name || task.taskName || 'Untitled task',
    completed: task.taskStatus === PLAN_TASK_STATUS.COMPLETED,
    raw: task,
    tags: resolveTaskTags(task, index),
  }))
})

const feedAvatars = [
  'https://i.pravatar.cc/40?img=1',
  'https://i.pravatar.cc/40?img=2',
  'https://i.pravatar.cc/40?img=3',
]

const activityFeed = [
  {
    html: '<strong>Sarah Chen</strong> pushed to <code class="wb-inline-code">feature/login-v2</code> <span class="time">12 minutes ago</span>',
    code: 'fix(auth): resolve token expiration edge case',
  },
  {
    html: '<strong>Marco Ross</strong> completed <strong>Dashboard Redesign</strong> <span class="time">3 hours ago</span>',
  },
  {
    html: '<strong>David Kim</strong> commented on <strong>Nexus API</strong> <span class="time">5 hours ago</span>',
    quote: 'Should we move the rate limiter to the edge? Might be faster.',
  },
  {
    system: true,
    html: 'System Alert: <strong>Apollo SDK v1.2 Deployed</strong> <span class="time">Yesterday at 11:05 PM</span>',
  },
  {
    html: '<strong>Leon Vance</strong> joined the <strong>Zenith App</strong> team <span class="time">Yesterday</span>',
  },
]

const fallbackTasks = [
  {
    id: 'f1',
    title: 'Refactor authentication flow',
    completed: false,
    tags: [
      { className: 'wb-tag-backend', label: 'Backend' },
      { className: 'wb-tag-priority', label: 'High Priority' },
    ],
  },
  {
    id: 'f2',
    title: 'Update design system tokens',
    completed: true,
    tags: [{ className: 'wb-tag-design', label: 'Design' }],
  },
  {
    id: 'f3',
    title: 'Prepare client presentation',
    completed: false,
    tags: [
      { className: 'wb-tag-marketing', label: 'Marketing' },
      { className: 'wb-tag-upcoming', label: 'Upcoming' },
    ],
  },
  {
    id: 'f4',
    title: 'Review pull request #452',
    completed: false,
    tags: [{ className: 'wb-tag-devops', label: 'DevOps' }],
  },
]

const loadData = async () => {
  loading.value = true
  try {
    const [taskData, teamData] = await Promise.all([
      listPlanTasksApi(WORKSPACE_ID).catch(() => []),
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
  await router.push('/workspace/project/gantt')
}

const openComingSoon = () => {
  window.alert('开发中')
}

onMounted(() => {
  loadData()
})
</script>
