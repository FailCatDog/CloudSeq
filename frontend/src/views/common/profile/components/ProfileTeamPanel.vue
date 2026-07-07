<template>
  <section class="team-page">
    <div v-if="loading" class="team-panel team-panel--empty wb-card">
      <div>
        <p class="section-label">加载中</p>
        <h4>正在同步小组数据</h4>
        <p class="empty-desc">正在从后端加载当前用户的小组信息、成员列表和选题审批记录。</p>
      </div>
    </div>

    <div v-else-if="!hasTeam" class="team-panel team-panel--empty wb-card">
      <div>
        <p class="section-label">当前状态</p>
        <h4>你还没有小组</h4>
        <p class="empty-desc">先创建一个小组，后续再邀请成员、提交选题。</p>
      </div>
      <button type="button" class="primary-btn" @click="openCreateForm">新建小组</button>
    </div>

    <div v-else class="team-layout">
      <section class="team-panel team-panel--main wb-card">
        <div class="panel-head">
          <div class="team-overview-head">
            <div>
              <p class="section-label">小组概览</p>
              <h4>基础信息</h4>
            </div>
            <button type="button" class="primary-btn primary-btn--ghost" :disabled="!hasTeam" @click="openTopicDialog">
              选题
            </button>
          </div>
          <button type="button" class="secondary-btn" @click="refreshTeam">刷新</button>
        </div>

        <dl class="info-grid">
          <div v-for="item in teamFields" :key="item.label" class="info-item">
            <dt>{{ item.label }}</dt>
            <dd>{{ item.value }}</dd>
          </div>
        </dl>
      </section>

      <aside class="team-panel wb-card">
        <div class="panel-head">
          <div>
            <p class="section-label">快捷入口</p>
            <h4>组队操作</h4>
          </div>
        </div>

        <div class="action-list">
          <button
            v-for="item in actions"
            :key="item.label"
            type="button"
            class="action-item"
            @click="handleAction(item.key)"
          >
            <span>
              <strong>{{ item.label }}</strong>
              <small>{{ item.desc }}</small>
            </span>
            <span aria-hidden="true">→</span>
          </button>
        </div>
      </aside>
    </div>

    <section v-if="hasTeam" class="team-panel wb-card">
      <div class="panel-head">
        <div>
          <p class="section-label">成员列表</p>
          <h4>我的小组成员</h4>
        </div>
      </div>

      <div class="member-list">
        <article v-for="member in members" :key="member.id" class="member-item">
          <div class="member-avatar">{{ member.avatar }}</div>
          <div class="member-meta">
            <strong>{{ member.name }}</strong>
            <p>{{ member.role }}</p>
          </div>
          <span class="member-tag">{{ member.status }}</span>
        </article>
      </div>
    </section>

    <section v-if="hasTeam" class="team-panel wb-card">
      <div class="panel-head">
        <div>
          <p class="section-label">选题审批</p>
          <h4>最近审批记录</h4>
        </div>
      </div>

      <div v-if="topicApprovals.length" class="approval-list">
        <article v-for="approval in topicApprovals" :key="approval.id" class="approval-item">
          <div>
            <strong>{{ approval.topicTitle }}</strong>
            <p>{{ approval.topicDesc || '暂无说明' }}</p>
          </div>
          <span class="member-tag">{{ approvalStatusText(approval.approvalStatus) }}</span>
        </article>
      </div>
      <p v-else class="empty-desc">暂无选题审批记录。</p>
    </section>

    <teleport to="body">
      <div v-if="topicDialogVisible" class="dialog-mask" @click.self="closeTopicDialog">
        <AppScrollArea class="dialog-panel" axis="y" hover-reveal>
          <div class="dialog-head">
            <div>
              <p class="section-label">选题操作</p>
              <h4>填写并提交选题</h4>
            </div>
            <button type="button" class="secondary-btn" @click="closeTopicDialog">关闭</button>
          </div>

          <label class="field">
            <span>选题标题</span>
            <input
              v-model.trim="topicForm.topicTitle"
              type="text"
              placeholder="输入选题标题"
              :maxlength="TOPIC_TITLE_MAX_LENGTH"
            />
          </label>

          <label class="field">
            <span>选题说明</span>
            <textarea
              v-model.trim="topicForm.topicDesc"
              rows="4"
              placeholder="补充选题背景与计划"
              :maxlength="TOPIC_DESC_MAX_LENGTH"
            />
          </label>

          <div class="form-actions form-actions--start">
            <button type="button" class="secondary-btn" @click="resetTopicForm">重置</button>
            <button type="button" class="primary-btn" :disabled="submittingTopic || !hasTeam" @click="handleSubmitTopic">
              {{ submittingTopic ? '提交中...' : '提交选题' }}
            </button>
          </div>

          <p v-if="formMessage" class="form-message" :class="formMessageType">{{ formMessage }}</p>
        </AppScrollArea>
      </div>
    </teleport>

    <section v-if="!hasTeam" class="team-panel wb-card">
      <div class="panel-head">
        <div>
          <p class="section-label">新建小组</p>
          <h4>创建属于你的小组</h4>
        </div>
      </div>

      <form class="create-form" @submit.prevent="handleCreateTeam">
        <label class="field">
          <span>小组名称</span>
          <input
            v-model.trim="form.teamName"
            type="text"
            placeholder="请输入小组名称"
            :maxlength="TEAM_NAME_MAX_LENGTH"
          />
        </label>

        <label class="field">
          <span>组长用户ID</span>
          <input v-model.trim="form.leaderUserId" type="text" placeholder="当前登录用户ID" readonly />
        </label>

        <div class="form-actions">
          <button type="button" class="secondary-btn" @click="resetForm">重置</button>
          <button type="submit" class="primary-btn" :disabled="creating">{{ creating ? '创建中...' : '创建小组' }}</button>
        </div>

        <p v-if="formMessage" class="form-message" :class="formMessageType">{{ formMessage }}</p>
      </form>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppScrollArea from '@/components/AppScrollArea.vue'
import {
  createTeamApi,
  getCurrentTeamApi,
  listTeamMembersApi,
  listTeamTopicApprovalsApi,
  parseTeamMembersResponse,
  submitTeamTopicApi,
} from '@/api/team'
import {
  CacheCode,
} from '@/constants/cacheCode.js'
import {
  TEAM_NAME_MAX_LENGTH,
  TOPIC_DESC_MAX_LENGTH,
  TOPIC_TITLE_MAX_LENGTH,
} from '@/constants/fieldLimits'
import { useDict } from '@/composables/useDict'

const USER_STORAGE_KEY = 'user'

const creating = ref(false)
const submittingTopic = ref(false)
const loading = ref(false)
const topicDialogVisible = ref(false)
const formMessage = ref('')
const formMessageType = ref('')
const hasTeam = ref(false)
const currentUserId = ref('')
const currentTeamId = ref('')
const topicApprovals = ref([])

const form = reactive({
  teamName: '',
  leaderUserId: '',
})

const topicForm = reactive({
  topicTitle: '',
  topicDesc: '',
})

const teamInfo = ref({
  teamName: '',
  leaderName: '',
  status: '',
  topicTitle: '',
  memberCount: 0,
  createDate: '',
})

const members = ref([])

const teamStatusDict = useDict(CacheCode.TEAM_STATUS)
const memberStatusDict = useDict(CacheCode.MEMBER_STATUS)
const approvalStatusDict = useDict(CacheCode.APPROVAL_STATUS)

const roleTextMap = {
  1: '组长',
  0: '成员',
}

const teamFields = computed(() => [
  { label: '小组名称', value: teamInfo.value.teamName || '-' },
  { label: '组长', value: teamInfo.value.leaderName || '-' },
  { label: '加入状态', value: hasTeam.value ? '已加入小组' : '暂无小组' },
  { label: '小组状态', value: teamInfo.value.status || '-' },
  { label: '当前选题', value: teamInfo.value.topicTitle || '暂无选题' },
  { label: '成员数量', value: `${teamInfo.value.memberCount || 0} 人` },
  { label: '创建时间', value: teamInfo.value.createDate || '-' },
])

const openTopicDialog = () => {
  if (!hasTeam.value) return
  topicDialogVisible.value = true
}

const closeTopicDialog = () => {
  topicDialogVisible.value = false
}

const actions = [
  { key: 'invite', label: '邀请成员', desc: '添加新成员到当前小组' },
  { key: 'topic', label: '提交选题', desc: '打开选题弹窗' },
  { key: 'member', label: '成员管理', desc: '查看和调整小组成员' },
]

const getStoredUser = () => {
  const rawUser = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY)
  if (!rawUser) return {}

  try {
    return JSON.parse(rawUser)
  } catch {
    return {}
  }
}

const resetForm = () => {
  form.teamName = ''
  form.leaderUserId = currentUserId.value
  topicForm.topicTitle = ''
  topicForm.topicDesc = ''
  formMessage.value = ''
  formMessageType.value = ''
}

const resetTopicForm = () => {
  topicForm.topicTitle = ''
  topicForm.topicDesc = ''
  formMessage.value = ''
  formMessageType.value = ''
}

const approvalStatusText = (status) => approvalStatusDict.label(status)

const normalizeTeam = (team) => {
  if (!team) return
  teamInfo.value = {
    teamName: team.teamName || '',
    leaderName: team.leaderUserId ? `用户 ${team.leaderUserId}` : '当前登录用户',
    status: teamStatusDict.label(team.status, team.status || '-'),
    topicTitle: team.topicTitle || '暂无选题',
    memberCount: members.value.length,
    createDate: team.createDate || '刚刚',
  }
}

const syncDetail = async () => {
  if (!currentUserId.value) {
    hasTeam.value = false
    return
  }

  loading.value = true
  try {
    const team = await getCurrentTeamApi()
    if (!team) {
      hasTeam.value = false
      currentTeamId.value = ''
      members.value = []
      topicApprovals.value = []
      return
    }

    currentTeamId.value = String(team.id || '')
    hasTeam.value = Boolean(currentTeamId.value)
    normalizeTeam(team)
    if (hasTeam.value) {
      form.teamName = ''
    }

    const [memberResponse, approvals] = await Promise.all([
      listTeamMembersApi(currentTeamId.value),
      listTeamTopicApprovalsApi(currentTeamId.value),
    ])
    const { memberList } = parseTeamMembersResponse(memberResponse)

    members.value = memberList.map((item) => ({
      id: item.teamMemberId ?? item.userId,
      avatar: (item.name || String(item.userId || '')).slice(0, 1) || '成',
      name: item.isLeader === 1 ? `组长 ${item.name}` : item.name,
      role: `${roleTextMap[item.isLeader] || '成员'} · ${memberStatusDict.label(item.memberStatus)}`,
      status: memberStatusDict.label(item.memberStatus),
    }))

    topicApprovals.value = approvals || []
    teamInfo.value.memberCount = members.value.length
    formMessage.value = ''
  } catch (error) {
    hasTeam.value = false
    formMessage.value = error.message || '加载小组失败。'
    formMessageType.value = 'error'
  } finally {
    loading.value = false
  }
}

const refreshTeam = async () => {
  await syncDetail()
  if (hasTeam.value) {
    formMessage.value = '已刷新当前小组信息。'
    formMessageType.value = 'success'
  }
}

const handleAction = (key) => {
  if (key === 'invite') {
    formMessage.value = '邀请成员接口已预留，后续可在这里补充邀请弹窗。'
    formMessageType.value = 'success'
    return
  }

  if (key === 'topic') {
    openTopicDialog()
    formMessage.value = ''
    formMessageType.value = ''
    return
  }

  if (key === 'member') {
    refreshTeam()
  }
}

const handleCreateTeam = async () => {
  if (!form.teamName) {
    formMessage.value = '请输入小组名称。'
    formMessageType.value = 'error'
    return
  }

  if (!form.leaderUserId) {
    formMessage.value = '未获取到当前登录用户，请重新登录后再试。'
    formMessageType.value = 'error'
    return
  }

  creating.value = true
  formMessage.value = ''

  try {
    const result = await createTeamApi({
      teamName: form.teamName,
      leaderUserId: form.leaderUserId,
    })

    currentTeamId.value = String(result.id || '')
    hasTeam.value = true
    normalizeTeam(result)
    await syncDetail()
    formMessage.value = '小组创建成功。'
    formMessageType.value = 'success'
    resetForm()
  } catch (error) {
    formMessage.value = error.message || '创建失败，请稍后重试。'
    formMessageType.value = 'error'
  } finally {
    creating.value = false
  }
}

const handleSubmitTopic = async () => {
  if (!currentTeamId.value) {
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
      teamId: currentTeamId.value,
      topicTitle: topicForm.topicTitle,
      topicDesc: topicForm.topicDesc,
    })

    topicForm.topicTitle = ''
    topicForm.topicDesc = ''
    await syncDetail()
    formMessage.value = '选题已提交，正在等待审批。'
    formMessageType.value = 'success'
  } catch (error) {
    formMessage.value = error.message || '提交选题失败，请稍后重试。'
    formMessageType.value = 'error'
  } finally {
    submittingTopic.value = false
  }
}

onMounted(async () => {
  const user = getStoredUser()
  currentUserId.value = user.id ? String(user.id) : ''
  form.leaderUserId = currentUserId.value
  await syncDetail()
})
</script>

<style scoped>
.team-page {
  display: grid;
  gap: 18px;
}

.team-panel {
  padding: 22px 24px;
}

.team-panel--empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.team-panel h4 {
  margin: 0;
  color: #172033;
  letter-spacing: -0.02em;
}

.team-overview-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-meta p,
.action-item small,
.approval-item p,
.empty-desc {
  color: #67758f;
  font-size: 13px;
  line-height: 1.65;
}

.team-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.8fr);
  gap: 18px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.section-label {
  margin: 0 0 4px;
  color: #6a7890;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0;
}

.info-item,
.action-item,
.member-item,
.approval-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: #f7f9fc;
}

.info-item dt {
  color: #71819a;
  font-size: 12px;
}

.info-item dd {
  margin: 6px 0 0;
  color: #1e2a40;
  font-size: 15px;
  font-weight: 600;
}

.action-list,
.member-list,
.approval-list {
  display: grid;
  gap: 10px;
}

.action-item,
.member-item,
.approval-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 0;
}

.action-item {
  cursor: pointer;
  text-align: left;
}

.action-item strong,
.member-meta strong,
.approval-item strong {
  display: block;
  margin-bottom: 4px;
  color: #21324d;
  font-size: 14px;
}

.member-item {
  align-items: center;
}

.member-avatar {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  color: #27407a;
  background: rgba(48, 75, 140, 0.1);
  font-weight: 700;
}

.member-meta {
  flex: 1;
}

.member-tag {
  padding: 6px 10px;
  border-radius: 999px;
  color: #27407a;
  background: rgba(48, 75, 140, 0.1);
  font-size: 12px;
  font-weight: 600;
}

.secondary-btn,
.primary-btn {
  height: 38px;
  border-radius: 12px;
  padding: 0 14px;
  font-size: 14px;
  font-weight: 560;
  cursor: pointer;
}

.secondary-btn {
  color: #33465f;
  border: 1px solid rgba(124, 139, 166, 0.22);
  background: #ffffff;
}

.primary-btn--ghost {
  color: #304b8c;
  border: 1px solid rgba(48, 75, 140, 0.18);
  background: rgba(48, 75, 140, 0.08);
}

.primary-btn {
  color: #ffffff;
  border: 0;
  background: linear-gradient(145deg, #304b8c 0%, #2a447f 100%);
}

.primary-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.create-form {
  display: grid;
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
}

.field span {
  color: #6a7890;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.field input,
.field textarea {
  width: 100%;
  padding: 0 14px;
  border: 1px solid rgba(124, 139, 166, 0.22);
  border-radius: 12px;
  background: #ffffff;
  color: #1e2a40;
  outline: none;
}

.field input {
  height: 42px;
}

.field textarea {
  padding-top: 12px;
  padding-bottom: 12px;
  resize: vertical;
}

.field input:focus,
.field textarea:focus {
  border-color: rgba(48, 75, 140, 0.46);
  box-shadow: 0 0 0 3px rgba(48, 75, 140, 0.12);
}

.field input[readonly] {
  background: #f7f9fc;
  color: #5f6f89;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.form-actions--start {
  justify-content: flex-start;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.48);
  z-index: 50;
}

.dialog-panel {
  width: min(640px, 100%);
  max-height: calc(100vh - 40px);
  border-radius: 24px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 28px 80px rgba(18, 28, 45, 0.22);
}

.dialog-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.form-message {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
}

.form-message.success {
  color: #1f5f3a;
}

.form-message.error {
  color: #b42318;
}

@media (max-width: 960px) {
  .team-layout,
  .info-grid,
  .team-panel--empty {
    grid-template-columns: 1fr;
  }

  .team-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .team-status {
    justify-items: start;
  }

  .team-panel--empty,
  .form-actions,
  .dialog-head {
    flex-direction: column;
    align-items: stretch;
  }

  .team-overview-head {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
