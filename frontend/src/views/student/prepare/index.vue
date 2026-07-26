<template>
  <section class="prepare-shell">
    <div v-if="loading" class="prepare-panel wb-card">
      <p class="section-label">加载中</p>
      <h3>正在同步课程准备进度</h3>
      <p class="prepare-hint">正在从服务端拉取课号、小组与审批进度。</p>
    </div>

    <template v-else>
      <header class="prepare-head wb-card">
        <div>
          <p class="section-label">课程准备</p>
          <h3>{{ milestoneLabel }}</h3>
          <p class="prepare-hint">{{ milestoneHint }}</p>
        </div>
        <button type="button" class="prepare-btn prepare-btn--ghost" :disabled="refreshing" @click="refresh">
          {{ refreshing ? '刷新中…' : '刷新' }}
        </button>
      </header>

      <nav class="prepare-stepper wb-card" aria-label="学习准备进度">
        <ol class="prepare-steps">
          <li
            v-for="(step, index) in steps"
            :key="step.id"
            class="prepare-step"
            :class="`prepare-step--${resolveStepState(index)}`"
          >
            <span class="prepare-step__dot" aria-hidden="true">
              <span v-if="resolveStepState(index) === 'done'">✓</span>
              <span v-else>{{ index + 1 }}</span>
            </span>
            <span class="prepare-step__label">{{ step.label }}</span>
            <span v-if="index < steps.length - 1" class="prepare-step__connector" aria-hidden="true" />
          </li>
        </ol>
      </nav>

      <section class="prepare-focus wb-card" :aria-labelledby="`prepare-focus-${currentStepId}`">
        <p class="section-label">当前步骤</p>
        <h4 :id="`prepare-focus-${currentStepId}`">{{ currentStep?.label }}</h4>
        <p v-if="currentStep?.summary(status)" class="prepare-hint">{{ currentStep.summary(status) }}</p>

        <div class="prepare-focus__body">
          <template v-if="currentStepId === 'enroll'">
            <form class="prepare-form" @submit.prevent="handleJoinCourse">
              <label class="prepare-field">
                <span>课号</span>
                <input
                  v-model.trim="enrollForm.courseCode"
                  type="text"
                  placeholder="请输入教师提供的课号，如 PM2026-01"
                  :maxlength="COURSE_CODE_MAX_LENGTH"
                />
              </label>
              <div class="prepare-form-actions">
                <button type="submit" class="prepare-btn prepare-btn--primary" :disabled="joiningCourse">
                  {{ joiningCourse ? '加入中…' : '加入课号' }}
                </button>
              </div>
            </form>
          </template>

          <template v-else-if="currentStepId === 'team'">
            <div class="prepare-team-actions">
              <section class="prepare-team-block">
                <p class="prepare-team-block__title">加入已有小组</p>
                <p v-if="loadingTeams" class="prepare-hint">正在加载同课号小组…</p>
                <ul v-else-if="courseTeams.length" class="prepare-team-list">
                  <li v-for="team in courseTeams" :key="team.id" class="prepare-team-item">
                    <div class="prepare-team-item__meta">
                      <strong>{{ team.teamName }}</strong>
                      <span>组长 {{ team.leaderName || '—' }} · {{ team.memberCount || 0 }}/{{ team.maxTeamSize || '—' }} 人</span>
                    </div>
                    <button
                      type="button"
                      class="prepare-btn prepare-btn--primary"
                      :disabled="joiningTeamId != null || team.full"
                      @click="handleJoinTeam(team)"
                    >
                      {{ joiningTeamId === team.id ? '加入中…' : team.full ? '已满' : '加入' }}
                    </button>
                  </li>
                </ul>
                <p v-else class="prepare-hint">暂无可加入小组，可自行创建。</p>
              </section>

              <section class="prepare-team-block">
                <p class="prepare-team-block__title">创建新小组</p>
                <form class="prepare-form" @submit.prevent="handleCreateTeam">
                  <label class="prepare-field">
                    <span>小组名称</span>
                    <input
                      v-model.trim="teamForm.teamName"
                      type="text"
                      placeholder="请输入小组名称"
                      :maxlength="TEAM_NAME_MAX_LENGTH"
                    />
                  </label>
                  <div class="prepare-form-actions">
                    <button type="submit" class="prepare-btn prepare-btn--primary" :disabled="creatingTeam">
                      {{ creatingTeam ? '创建中…' : '创建小组' }}
                    </button>
                  </div>
                </form>
              </section>
            </div>
          </template>

          <template v-else-if="currentStepId === 'topic'">
            <p v-if="isRejected" class="prepare-alert prepare-alert--warn">
              选题未通过：{{ status.rejectReason || '请根据教师反馈修改后重新提交。' }}
            </p>
            <form class="prepare-form" @submit.prevent="handleSubmitTopic">
              <label class="prepare-field">
                <span>选题标题</span>
                <input
                  v-model.trim="topicForm.topicTitle"
                  type="text"
                  placeholder="输入选题标题"
                  :maxlength="TOPIC_TITLE_MAX_LENGTH"
                />
              </label>
              <label class="prepare-field">
                <span>选题说明</span>
                <textarea
                  v-model.trim="topicForm.topicDesc"
                  rows="4"
                  placeholder="补充选题背景与计划"
                  :maxlength="TOPIC_DESC_MAX_LENGTH"
                />
              </label>
              <div class="prepare-form-actions">
                <button type="submit" class="prepare-btn prepare-btn--primary" :disabled="submittingTopic">
                  {{ submittingTopic ? '提交中…' : isRejected ? '重新提交选题' : '提交选题' }}
                </button>
              </div>
            </form>
          </template>

          <template v-else-if="currentStepId === 'approval'">
            <p class="prepare-hint">选题已提交，正在等待教师审批。通过后即可进入项目空间。</p>
            <dl v-if="status?.topicTitle" class="prepare-facts">
              <div><dt>选题</dt><dd>{{ status.topicTitle }}</dd></div>
              <div><dt>提交时间</dt><dd>{{ formatDate(status.approvalSubmitDate) }}</dd></div>
            </dl>
          </template>

          <template v-else-if="currentStepId === 'workspace'">
            <p class="prepare-hint">审批已通过，可以开始项目协作。</p>
            <RouterLink to="/workspace/project/board" class="prepare-btn prepare-btn--primary prepare-enter-link">
              进入项目空间
            </RouterLink>
          </template>
        </div>
      </section>

      <div class="prepare-context">
        <aside v-if="status?.courseId" class="prepare-panel wb-card prepare-panel--compact">
          <p class="section-label">课号信息</p>
          <dl class="prepare-facts prepare-facts--stack">
            <div><dt>课号</dt><dd>{{ status.courseCode }}</dd></div>
            <div><dt>课程</dt><dd>{{ status.courseName }}</dd></div>
            <div v-if="status.termYear"><dt>学期</dt><dd>{{ status.termYear }} · {{ status.termSeason }}</dd></div>
          </dl>
        </aside>

        <aside v-if="status?.teamId" class="prepare-panel wb-card prepare-panel--compact">
          <p class="section-label">小组概览</p>
          <dl class="prepare-facts prepare-facts--stack">
            <div><dt>组名</dt><dd>{{ status.teamName }}</dd></div>
            <div><dt>成员</dt><dd>{{ status.memberCount || members.length || 0 }} 人</dd></div>
            <div><dt>角色</dt><dd>{{ status.isLeader === 1 ? '组长' : '成员' }}</dd></div>
          </dl>
        </aside>

        <section v-if="members.length" class="prepare-panel wb-card">
          <p class="section-label">小组成员</p>
          <ul class="prepare-members">
            <li v-for="member in members" :key="member.id" class="prepare-member">
              <span class="prepare-member__avatar">{{ member.avatar }}</span>
              <span class="prepare-member__name">{{ member.name }}</span>
              <span class="prepare-member__tag">{{ member.role }}</span>
            </li>
          </ul>
        </section>

        <section v-if="topicApprovals.length" class="prepare-panel wb-card">
          <p class="section-label">审批记录</p>
          <ul class="prepare-approvals">
            <li v-for="approval in topicApprovals" :key="approval.id" class="prepare-approval">
              <div>
                <strong>{{ approval.topicTitle }}</strong>
                <p>{{ approval.topicDesc || '暂无说明' }}</p>
              </div>
              <span class="prepare-approval__tag">{{ approvalStatusText(approval.approvalStatus) }}</span>
            </li>
          </ul>
        </section>
      </div>

      <p v-if="formMessage" class="prepare-form-message" :class="formMessageType">{{ formMessage }}</p>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { getProfileStatusApi } from '@/api/account'
import { joinCourseByCodeApi } from '@/api/course'
import {
  createTeamApi,
  joinTeamApi,
  listCourseTeamsApi,
  listTeamMembersApi,
  listTeamTopicApprovalsApi,
  parseTeamMembersResponse,
  submitTeamTopicApi,
} from '@/api/team'
import { CacheCode } from '@/constants/cacheCode'
import {
  TEAM_NAME_MAX_LENGTH,
  TOPIC_DESC_MAX_LENGTH,
  TOPIC_TITLE_MAX_LENGTH,
} from '@/constants/fieldLimits'
import { useDict } from '@/composables/useDict'

const COURSE_CODE_MAX_LENGTH = 32

const MILESTONE_ORDER = {
  [CacheCode.STUDENT_MILESTONE_NEED_ENROLL]: 0,
  [CacheCode.STUDENT_MILESTONE_NEED_TEAM]: 1,
  [CacheCode.STUDENT_MILESTONE_NEED_TOPIC]: 2,
  [CacheCode.STUDENT_MILESTONE_TOPIC_REJECTED]: 2,
  [CacheCode.STUDENT_MILESTONE_TOPIC_PENDING]: 3,
  [CacheCode.STUDENT_MILESTONE_READY]: 4,
}

const steps = [
  {
    id: 'enroll',
    label: '加入课号',
    summary: (s) => (s?.courseCode ? `${s.courseCode} · ${s.courseName || '课程'}` : '尚未加入课号'),
  },
  {
    id: 'team',
    label: '加入小组',
    summary: (s) => (s?.teamName ? `${s.teamName}（${s.memberCount || 0} 人）` : '尚未加入小组'),
  },
  {
    id: 'topic',
    label: '提交选题',
    summary: (s) => s?.topicTitle || '尚未提交选题',
  },
  {
    id: 'approval',
    label: '教师审批',
    summary: (s) => {
      if (s?.milestoneStatus === CacheCode.STUDENT_MILESTONE_TOPIC_PENDING) return '审批进行中'
      if (s?.milestoneStatus === CacheCode.STUDENT_MILESTONE_READY) return '已通过'
      if (s?.approvalStatus === CacheCode.APPROVAL_STATUS_REJECTED) return '未通过'
      return '等待提交选题'
    },
  },
  {
    id: 'workspace',
    label: '进入项目空间',
    summary: (s) => (s?.workspaceId ? '工作区已就绪' : '审批通过后自动创建'),
  },
]

const milestoneDict = useDict(CacheCode.STUDENT_MILESTONE_STATUS)
const approvalStatusDict = useDict(CacheCode.APPROVAL_STATUS)
const memberStatusDict = useDict(CacheCode.MEMBER_STATUS)

const loading = ref(false)
const refreshing = ref(false)
const joiningCourse = ref(false)
const creatingTeam = ref(false)
const joiningTeamId = ref(null)
const loadingTeams = ref(false)
const submittingTopic = ref(false)
const status = ref(null)
const members = ref([])
const topicApprovals = ref([])
const courseTeams = ref([])
const formMessage = ref('')
const formMessageType = ref('')

const enrollForm = reactive({ courseCode: '' })
const teamForm = reactive({ teamName: '' })
const topicForm = reactive({ topicTitle: '', topicDesc: '' })

const currentIndex = computed(() => MILESTONE_ORDER[status.value?.milestoneStatus] ?? 0)
const isReady = computed(() => status.value?.milestoneStatus === CacheCode.STUDENT_MILESTONE_READY)
const isRejected = computed(() => status.value?.milestoneStatus === CacheCode.STUDENT_MILESTONE_TOPIC_REJECTED)
const currentStep = computed(() => steps[currentIndex.value] || steps[0])
const currentStepId = computed(() => currentStep.value?.id || 'enroll')

const milestoneLabel = computed(() => milestoneDict.label(status.value?.milestoneStatus, '学习准备'))

const milestoneHint = computed(() => {
  const code = status.value?.milestoneStatus
  if (code === CacheCode.STUDENT_MILESTONE_NEED_ENROLL) return '输入教师提供的课号加入课程，再在该课号下组队。'
  if (code === CacheCode.STUDENT_MILESTONE_NEED_TEAM) return '可加入同课号已有小组，或自行创建新小组。'
  if (code === CacheCode.STUDENT_MILESTONE_NEED_TOPIC) return '填写选题并提交，等待教师审批。'
  if (code === CacheCode.STUDENT_MILESTONE_TOPIC_PENDING) return '选题审批中，通过后即可进入项目空间。'
  if (code === CacheCode.STUDENT_MILESTONE_TOPIC_REJECTED) return '请根据教师反馈修改选题后重新提交。'
  if (code === CacheCode.STUDENT_MILESTONE_READY) return '全部准备已完成，可以开始协作。'
  return '按步骤完成学习准备。'
})

const resolveStepState = (index) => {
  if (isReady.value) return 'done'
  if (index < currentIndex.value) return 'done'
  if (index === currentIndex.value) return 'current'
  return 'pending'
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const approvalStatusText = (value) => approvalStatusDict.label(value)

const syncForms = () => {
  topicForm.topicTitle = status.value?.topicTitle || ''
  topicForm.topicDesc = status.value?.topicDesc || ''
}

const loadTeamDetails = async () => {
  const teamId = status.value?.teamId
  if (!teamId) {
    members.value = []
    topicApprovals.value = []
    return
  }

  const [memberResponse, approvals] = await Promise.all([
    listTeamMembersApi(teamId),
    listTeamTopicApprovalsApi(teamId),
  ])
  const { memberList } = parseTeamMembersResponse(memberResponse)

  members.value = memberList.map((item) => ({
    id: item.teamMemberId ?? item.userId,
    avatar: (item.name || String(item.userId || '')).slice(0, 1) || '成',
    name: item.name || `用户 ${item.userId}`,
    role: item.isLeader === 1 ? '组长' : memberStatusDict.label(item.memberStatus, '成员'),
  }))
  topicApprovals.value = approvals || []
}

const loadCourseTeams = async () => {
  if (currentStepId.value !== 'team' || !status.value?.courseId) {
    courseTeams.value = []
    return
  }
  loadingTeams.value = true
  try {
    courseTeams.value = (await listCourseTeamsApi()) || []
  } catch (error) {
    courseTeams.value = []
    formMessage.value = error?.message || '加载小组列表失败'
    formMessageType.value = 'error'
  } finally {
    loadingTeams.value = false
  }
}

const loadStatus = async ({ silent = false } = {}) => {
  if (!silent) loading.value = true
  else refreshing.value = true
  formMessage.value = ''
  try {
    status.value = await getProfileStatusApi()
    syncForms()
    await loadTeamDetails()
    await loadCourseTeams()
  } catch (error) {
    formMessage.value = error?.message || '状态加载失败'
    formMessageType.value = 'error'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const refresh = () => loadStatus({ silent: true })

watch(currentStepId, () => {
  loadCourseTeams()
})

const handleJoinCourse = async () => {
  if (!enrollForm.courseCode) {
    formMessage.value = '请输入课号。'
    formMessageType.value = 'error'
    return
  }

  joiningCourse.value = true
  formMessage.value = ''
  try {
    await joinCourseByCodeApi(enrollForm.courseCode)
    enrollForm.courseCode = ''
    await loadStatus({ silent: true })
    formMessage.value = '已成功加入课号。'
    formMessageType.value = 'success'
  } catch (error) {
    formMessage.value = error?.message || '加入课号失败'
    formMessageType.value = 'error'
  } finally {
    joiningCourse.value = false
  }
}

const handleJoinTeam = async (team) => {
  if (!team?.id || team.full) return
  joiningTeamId.value = team.id
  formMessage.value = ''
  try {
    await joinTeamApi(team.id)
    await loadStatus({ silent: true })
    formMessage.value = `已加入「${team.teamName}」。`
    formMessageType.value = 'success'
  } catch (error) {
    formMessage.value = error?.message || '加入小组失败'
    formMessageType.value = 'error'
  } finally {
    joiningTeamId.value = null
  }
}

const handleCreateTeam = async () => {
  if (!teamForm.teamName) {
    formMessage.value = '请输入小组名称。'
    formMessageType.value = 'error'
    return
  }
  const userId = status.value?.userId
  if (!userId) {
    formMessage.value = '未获取到用户信息，请重新登录。'
    formMessageType.value = 'error'
    return
  }

  creatingTeam.value = true
  formMessage.value = ''
  try {
    await createTeamApi({ teamName: teamForm.teamName, leaderUserId: userId })
    teamForm.teamName = ''
    await loadStatus({ silent: true })
    formMessage.value = '小组创建成功。'
    formMessageType.value = 'success'
  } catch (error) {
    formMessage.value = error?.message || '创建小组失败'
    formMessageType.value = 'error'
  } finally {
    creatingTeam.value = false
  }
}

const handleSubmitTopic = async () => {
  if (!status.value?.teamId) {
    formMessage.value = '请先创建或加入小组。'
    formMessageType.value = 'error'
    return
  }
  if (!topicForm.topicTitle) {
    formMessage.value = '请输入选题标题。'
    formMessageType.value = 'error'
    return
  }

  submittingTopic.value = true
  formMessage.value = ''
  try {
    await submitTeamTopicApi({
      teamId: status.value.teamId,
      topicTitle: topicForm.topicTitle,
      topicDesc: topicForm.topicDesc,
    })
    await loadStatus({ silent: true })
    formMessage.value = '选题已提交，等待教师审批。'
    formMessageType.value = 'success'
  } catch (error) {
    formMessage.value = error?.message || '提交选题失败'
    formMessageType.value = 'error'
  } finally {
    submittingTopic.value = false
  }
}

onMounted(() => {
  loadStatus()
})
</script>

<style lang="scss" scoped>
@use '@/styles/mixins' as *;

.prepare-shell {
  display: grid;
  gap: 16px;
  width: 100%;
  min-width: 0;
}

.prepare-panel,
.prepare-head,
.prepare-focus,
.prepare-stepper {
  padding: 22px 24px;
}

.prepare-panel--compact {
  padding: 18px 20px;
}

.section-label {
  margin: 0 0 6px;
  color: var(--wb-text-muted);
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.prepare-hint {
  margin: 8px 0 0;
  color: var(--wb-text-secondary);
  font-size: 13px;
  line-height: 1.65;
}

.prepare-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;

  h3 {
    margin: 0;
    color: var(--wb-text-primary);
    letter-spacing: -0.02em;
    font-size: 22px;
    font-weight: 700;
  }
}

.prepare-focus {
  h4 {
    margin: 0;
    color: var(--wb-text-primary);
    letter-spacing: -0.02em;
    font-size: 18px;
    font-weight: 700;
  }

  &__body {
    margin-top: 18px;
    padding-top: 18px;
    border-top: 1px solid var(--wb-search-border);
  }
}

.prepare-steps {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: flex-start;
  overflow-x: auto;
}

.prepare-step {
  position: relative;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 72px;
  padding: 0 4px;
  text-align: center;

  &__dot {
    display: grid;
    place-items: center;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    font-size: 12px;
    font-weight: 700;
    color: var(--wb-text-muted);
    background: var(--wb-search-bg);
    border: 2px solid var(--wb-search-border);
  }

  &__label {
    color: var(--wb-text-secondary);
    font-size: 12px;
    font-weight: 600;
    line-height: 1.35;
  }

  &__connector {
    position: absolute;
    top: 14px;
    left: calc(50% + 18px);
    width: calc(100% - 36px);
    height: 2px;
    background: var(--wb-search-border);
  }

  &--done {
    .prepare-step__dot {
      color: var(--wb-tag-success-text);
      background: var(--wb-tag-success-bg);
      border-color: var(--wb-tag-success-border);
    }

    .prepare-step__label {
      color: var(--wb-text-primary);
    }
  }

  &--current {
    .prepare-step__dot {
      color: var(--wb-purple);
      background: var(--wb-purple-soft);
      border-color: var(--wb-purple-border);
    }

    .prepare-step__label {
      color: var(--wb-purple);
    }
  }

  &--pending {
    opacity: 0.55;
  }
}

.prepare-team-actions {
  display: grid;
  gap: 20px;
}

.prepare-team-block__title {
  margin: 0 0 10px;
  color: var(--wb-text-primary);
  font-size: 14px;
  font-weight: 700;
}

.prepare-team-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.prepare-team-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--wb-radius-sm);
  background: var(--wb-search-bg);
  border: 1px solid var(--wb-search-border);

  &__meta {
    display: grid;
    gap: 4px;
    min-width: 0;

    strong {
      color: var(--wb-text-primary);
      font-size: 14px;
    }

    span {
      color: var(--wb-text-secondary);
      font-size: 12px;
    }
  }
}

.prepare-context {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.prepare-form {
  display: grid;
  gap: 12px;
}

.prepare-form-actions {
  display: flex;
  gap: 10px;
}

.prepare-field {
  display: grid;
  gap: 6px;

  span {
    color: var(--wb-text-muted);
    font-size: 12px;
    font-weight: 600;
  }

  input,
  textarea {
    width: 100%;
    padding: 0 12px;
    border: 1px solid var(--wb-search-border);
    border-radius: var(--wb-radius-sm);
    background: var(--wb-card-bg);
    color: var(--wb-text-primary);
    font-family: inherit;
    outline: none;

    &:focus {
      @include focus-ring($alpha: 0.12);
      border-color: var(--wb-purple-border);
    }
  }

  input {
    height: 40px;
  }

  textarea {
    padding-top: 10px;
    padding-bottom: 10px;
    resize: vertical;
  }
}

.prepare-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 12px 0 0;

  &--stack {
    grid-template-columns: 1fr;
  }

  div {
    padding: 10px 12px;
    border-radius: var(--wb-radius-sm);
    background: var(--wb-search-bg);
  }

  dt {
    color: var(--wb-text-muted);
    font-size: 11px;
    font-weight: 600;
  }

  dd {
    margin: 4px 0 0;
    color: var(--wb-text-primary);
    font-size: 13px;
    font-weight: 600;
  }
}

.prepare-alert {
  margin: 0 0 12px;
  padding: 10px 12px;
  border-radius: var(--wb-radius-sm);
  font-size: 13px;
  line-height: 1.55;

  &--warn {
    color: var(--wb-tag-warning-text);
    background: var(--wb-tag-warning-bg);
    border: 1px solid var(--wb-tag-warning-border);
  }
}

.prepare-members,
.prepare-approvals {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.prepare-member {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--wb-radius-sm);
  background: var(--wb-search-bg);

  &__avatar {
    display: grid;
    place-items: center;
    width: 32px;
    height: 32px;
    border-radius: var(--wb-radius-sm);
    color: var(--wb-purple);
    background: var(--wb-purple-soft);
    font-size: 13px;
    font-weight: 700;
  }

  &__name {
    flex: 1;
    color: var(--wb-text-primary);
    font-size: 13px;
    font-weight: 600;
  }

  &__tag {
    color: var(--wb-text-secondary);
    font-size: 11px;
    font-weight: 600;
  }
}

.prepare-approval {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: var(--wb-radius-sm);
  background: var(--wb-search-bg);

  strong {
    display: block;
    color: var(--wb-text-primary);
    font-size: 13px;
  }

  p {
    margin: 4px 0 0;
    color: var(--wb-text-secondary);
    font-size: 12px;
    line-height: 1.55;
  }

  &__tag {
    flex-shrink: 0;
    padding: 4px 10px;
    border-radius: var(--wb-radius-pill);
    color: var(--wb-text-secondary);
    background: var(--wb-card-bg);
    font-size: 11px;
    font-weight: 600;
  }
}

.prepare-form-message {
  margin: 0;
  padding: 0 4px;
  font-size: 13px;
  line-height: 1.6;

  &.success {
    color: var(--wb-tag-success-text);
  }

  &.error {
    color: var(--wb-tag-danger-text);
  }
}

.prepare-enter-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  margin-top: 4px;
}

.prepare-btn {
  height: 38px;
  padding: 0 18px;
  border-radius: var(--wb-radius-pill);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.18s ease, background 0.18s ease;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &--primary {
    color: #fff;
    border: none;
    background: var(--wb-btn-dark);
  }

  &--ghost {
    color: var(--wb-purple);
    border: 1.5px solid var(--wb-purple-border);
    background: transparent;

    &:hover:not(:disabled) {
      background: var(--wb-purple-soft);
    }
  }
}

@media (max-width: 720px) {
  .prepare-head {
    flex-direction: column;
  }

  .prepare-facts {
    grid-template-columns: 1fr;
  }

  .prepare-step__label {
    font-size: 11px;
  }
}
</style>
