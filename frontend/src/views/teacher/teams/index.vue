<template>
  <div class="console-page">
    <div class="console-filter">
      <div class="console-filter__fields">
        <div class="console-filter__field console-filter__field--wide">
          <span class="console-filter__label">关键字</span>
          <n-input
            v-model:value="searchQuery"
            clearable
            placeholder="输入组名、选题标题关键字"
            @keydown.enter.prevent="handleSearch"
          />
        </div>
        <div class="console-filter__field">
          <span class="console-filter__label">课号</span>
          <n-input
            v-model:value="courseCodeQuery"
            clearable
            placeholder="输入课号关键字"
            @keydown.enter.prevent="handleSearch"
          />
        </div>
        <div class="console-filter__field">
          <span class="console-filter__label">小组状态</span>
          <n-select
            v-model:value="activeStatus"
            :options="statusOptions"
            label-field="label"
            value-field="id"
          />
        </div>
      </div>
      <div class="console-filter__actions">
        <n-button type="primary" :loading="loading" @click="handleSearch">搜索</n-button>
        <n-button quaternary :disabled="loading" @click="handleReset">重置</n-button>
      </div>
    </div>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <n-card class="console-table-card" :bordered="false">
      <n-data-table
        :columns="columns"
        :data="filteredTeams"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.id"
        :expanded-row-keys="expandedRowKeys"
        size="medium"
        @update:expanded-row-keys="handleExpandedChange"
      />
    </n-card>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { NAlert, NButton, NDataTable, NTag } from 'naive-ui'
import { listTeacherTeamOverviewApi, listTeamMembersApi, parseTeamMembersResponse } from '@/api/team'
import { CacheCode } from '@/constants/cacheCode'
import { useDict } from '@/composables/useDict'
import {
  formatMemberJoinDate,
  mapTeamOverviewSummary,
  memberRoleLabel,
  resolveTeamAction,
  teamStatusLabel,
} from '@/utils/teamFormat'
import { teamTagType } from '@/utils/naiveStatus'

const teams = ref([])
const loading = ref(false)
const pageError = ref('')
const expandedRowKeys = ref([])
const memberState = ref({})
const searchQuery = ref('')
const courseCodeQuery = ref('')
const activeStatus = ref('all')
const appliedKeyword = ref('')
const appliedCourseCode = ref('')
const appliedStatus = ref('all')

const { label: teamStatusDictLabel } = useDict(CacheCode.TEAM_STATUS)

const statusOptions = computed(() => [
  { id: 'all', label: '全部状态' },
  { id: CacheCode.TEAM_STATUS_NORMAL, label: teamStatusDictLabel(CacheCode.TEAM_STATUS_NORMAL) },
  { id: CacheCode.TEAM_STATUS_PENDING_TOPIC, label: teamStatusDictLabel(CacheCode.TEAM_STATUS_PENDING_TOPIC) },
  { id: CacheCode.TEAM_STATUS_TOPIC_REJECTED, label: teamStatusDictLabel(CacheCode.TEAM_STATUS_TOPIC_REJECTED) },
  { id: CacheCode.TEAM_STATUS_UNLOCKED, label: teamStatusDictLabel(CacheCode.TEAM_STATUS_UNLOCKED) },
])

const filteredTeams = computed(() => {
  const keyword = appliedKeyword.value.toLowerCase()
  const courseKeyword = appliedCourseCode.value.toLowerCase()
  return teams.value.filter((item) => {
    const statusMatch = appliedStatus.value === 'all' || item.status === appliedStatus.value
    if (!statusMatch) return false
    if (courseKeyword && !String(item.courseCode || '').toLowerCase().includes(courseKeyword)) {
      return false
    }
    if (!keyword) return true
    return (
      item.teamLabel.toLowerCase().includes(keyword) ||
      item.topicTitle.toLowerCase().includes(keyword) ||
      item.leaderName.toLowerCase().includes(keyword)
    )
  })
})

const memberColumns = [
  { title: '序号', key: 'index', width: 64, render: (_, index) => index + 1 },
  {
    title: '姓名',
    key: 'name',
    render: (row) => h('strong', null, row.name || '—'),
  },
  { title: '学号', key: 'studentNo' },
  { title: '账号', key: 'username' },
  {
    title: '角色',
    key: 'role',
    render: (row) => memberRoleLabel(row),
  },
  {
    title: '加入时间',
    key: 'joinDate',
    render: (row) => formatMemberJoinDate(row.joinDate),
  },
]

const renderExpand = (row) => {
  const state = getMemberState(row.id)
  if (state.error) {
    return h(NAlert, { type: 'error', bordered: false }, { default: () => state.error })
  }
  if (state.loading) {
    return h('div', { style: 'padding: 12px 0; color: var(--wb-text-muted);' }, '成员加载中…')
  }
  return h(NDataTable, {
    columns: memberColumns,
    data: state.members,
    bordered: false,
    size: 'small',
    singleLine: false,
  })
}

const columns = computed(() => [
  { type: 'expand', expandable: () => true, renderExpand },
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (_, index) => index + 1,
  },
  {
    title: '课号',
    key: 'courseCode',
    width: 120,
    render: (row) => h('strong', null, row.courseCode),
  },
  {
    title: '组名',
    key: 'teamLabel',
    width: 120,
    render: (row) => h('strong', null, row.teamLabel),
  },
  { title: '选题标题', key: 'topicTitle', ellipsis: { tooltip: true } },
  { title: '组长', key: 'leaderName', width: 100 },
  { title: '成员', key: 'memberCount', width: 72 },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render: (row) =>
      h(
        NTag,
        { type: teamTagType(row.status), round: true, size: 'small' },
        { default: () => teamStatusLabel(row.status) },
      ),
  },
  { title: '最近周报', key: 'lastWeeklyLabel', width: 100 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => {
      const action = resolveTeamAction(row.status)
      if (!action) return '—'
      return h(
        RouterLink,
        { to: action.to, style: 'color: var(--wb-link-blue); text-decoration: none; font-weight: 600;' },
        { default: () => action.label },
      )
    },
  },
])

const resolveApiStatus = (status) => {
  if (status === 'all') return undefined
  return status
}

const loadTeams = async (teamStatus) => {
  loading.value = true
  pageError.value = ''
  expandedRowKeys.value = []
  try {
    const params = teamStatus ? { teamStatus } : {}
    const list = await listTeacherTeamOverviewApi(params)
    teams.value = (Array.isArray(list) ? list : []).map(mapTeamOverviewSummary)
  } catch (error) {
    teams.value = []
    pageError.value = error?.message || '加载小组列表失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  handleSearch()
})

const handleSearch = async () => {
  appliedKeyword.value = searchQuery.value.trim()
  appliedCourseCode.value = courseCodeQuery.value.trim()
  appliedStatus.value = activeStatus.value
  await loadTeams(resolveApiStatus(activeStatus.value))
}

const handleReset = async () => {
  searchQuery.value = ''
  courseCodeQuery.value = ''
  activeStatus.value = 'all'
  appliedKeyword.value = ''
  appliedCourseCode.value = ''
  appliedStatus.value = 'all'
  await loadTeams()
}

const getMemberState = (teamId) => memberState.value[teamId] || { loading: false, error: '', members: [] }

const loadMembers = async (teamId) => {
  memberState.value = {
    ...memberState.value,
    [teamId]: { loading: true, error: '', members: [] },
  }
  try {
    const response = await listTeamMembersApi(teamId)
    const { memberList } = parseTeamMembersResponse(response)
    memberState.value = {
      ...memberState.value,
      [teamId]: { loading: false, error: '', members: memberList },
    }
  } catch (error) {
    memberState.value = {
      ...memberState.value,
      [teamId]: {
        loading: false,
        error: error?.message || '加载成员失败',
        members: [],
      },
    }
  }
}

const handleExpandedChange = async (keys) => {
  expandedRowKeys.value = keys
  const teamId = keys[keys.length - 1]
  if (!teamId) return
  const state = memberState.value[teamId]
  if (!state?.members?.length && !state?.loading) {
    await loadMembers(teamId)
  }
}
</script>
