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
          <span class="console-filter__label">选题状态</span>
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
        :data="filteredApprovals"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.id"
        size="medium"
      />
    </n-card>

    <n-drawer v-model:show="detailOpen" :width="480" placement="right">
      <n-drawer-content title="选题详情" closable>
        <n-alert v-if="actionError" type="error" :bordered="false" style="margin-bottom: 12px">
          {{ actionError }}
        </n-alert>
        <ApprovalDetailPanel
          :approval="selectedApproval"
          :submitting="submitting"
          @approve="handleApprove"
          @reject="handleReject"
        />
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { computed, h, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { NButton, NTag } from 'naive-ui'
import { listTeacherTopicApprovalsApi, reviewTopicApprovalApi } from '@/api/team'
import ApprovalDetailPanel from './components/ApprovalDetailPanel.vue'
import { CacheCode } from '@/constants/cacheCode'
import {
  approvalStatusLabel,
  isApprovalPending,
  mapTopicApprovalSummary,
} from '@/utils/approvalFormat'
import { approvalTagType } from '@/utils/naiveStatus'

const approvals = ref([])
const loading = ref(false)
const submitting = ref(false)
const pageError = ref('')
const actionError = ref('')
const searchQuery = ref('')
const courseCodeQuery = ref('')
const activeStatus = ref('all')
const appliedKeyword = ref('')
const appliedCourseCode = ref('')
const appliedStatus = ref('all')
const detailOpen = ref(false)
const selectedId = ref(null)

const resolveApiStatus = (status) => {
  if (status === 'all') return undefined
  if (status === 'pending') return CacheCode.APPROVAL_STATUS_PENDING
  return status
}

const statusOptions = [
  { id: 'all', label: '全部状态' },
  { id: 'pending', label: '待审' },
  { id: CacheCode.APPROVAL_STATUS_APPROVED, label: '已通过' },
  { id: CacheCode.APPROVAL_STATUS_REJECTED, label: '已驳回' },
]

const filteredApprovals = computed(() => {
  const keyword = appliedKeyword.value.toLowerCase()
  const courseKeyword = appliedCourseCode.value.toLowerCase()
  return approvals.value.filter((item) => {
    const statusMatch =
      appliedStatus.value === 'all' ||
      (appliedStatus.value === 'pending' && isApprovalPending(item.status)) ||
      item.status === appliedStatus.value
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

const selectedApproval = computed(() => {
  return approvals.value.find((item) => item.id === selectedId.value) || null
})

const columns = computed(() => [
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (_, index) => index + 1,
  },
  { title: '课号', key: 'courseCode', width: 120 },
  {
    title: '组名',
    key: 'teamLabel',
    width: 120,
    render: (row) => h('strong', null, row.teamLabel),
  },
  { title: '选题标题', key: 'topicTitle', ellipsis: { tooltip: true } },
  { title: '组长', key: 'leaderName', width: 100 },
  { title: '成员', key: 'memberCount', width: 72 },
  { title: '提交时间', key: 'submittedAt', width: 140 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: approvalTagType(row.status), round: true, size: 'small' },
        { default: () => approvalStatusLabel(row.status) },
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 88,
    render: (row) =>
      h(
        NButton,
        { text: true, type: 'primary', onClick: () => openDetail(row.id) },
        { default: () => '详情' },
      ),
  },
])

const loadApprovals = async (approvalStatus) => {
  loading.value = true
  pageError.value = ''
  try {
    const params = approvalStatus ? { approvalStatus } : {}
    const list = await listTeacherTopicApprovalsApi(params)
    approvals.value = (Array.isArray(list) ? list : []).map(mapTopicApprovalSummary)
  } catch (error) {
    approvals.value = []
    pageError.value = error?.message || '加载选题审批列表失败'
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
  actionError.value = ''
  closeDetail()
  await loadApprovals(resolveApiStatus(activeStatus.value))
}

const handleReset = async () => {
  searchQuery.value = ''
  courseCodeQuery.value = ''
  activeStatus.value = 'all'
  appliedKeyword.value = ''
  appliedCourseCode.value = ''
  appliedStatus.value = 'all'
  actionError.value = ''
  closeDetail()
  await loadApprovals()
}

const openDetail = (id) => {
  actionError.value = ''
  selectedId.value = id
  detailOpen.value = true
}

const closeDetail = () => {
  detailOpen.value = false
}

const handleApprove = async (id) => {
  const target = approvals.value.find((item) => item.id === id)
  if (!target || !isApprovalPending(target.status) || submitting.value) return

  submitting.value = true
  actionError.value = ''
  try {
    await reviewTopicApprovalApi({
      approvalId: id,
      approvalStatus: CacheCode.APPROVAL_STATUS_APPROVED,
    })
    await loadApprovals(resolveApiStatus(appliedStatus.value))
    closeDetail()
  } catch (error) {
    actionError.value = error?.message || '审批通过失败'
  } finally {
    submitting.value = false
  }
}

const handleReject = async ({ id, reason }) => {
  const target = approvals.value.find((item) => item.id === id)
  if (!target || !isApprovalPending(target.status) || submitting.value) return

  submitting.value = true
  actionError.value = ''
  try {
    await reviewTopicApprovalApi({
      approvalId: id,
      approvalStatus: CacheCode.APPROVAL_STATUS_REJECTED,
      rejectReason: reason,
    })
    await loadApprovals(resolveApiStatus(appliedStatus.value))
    closeDetail()
  } catch (error) {
    actionError.value = error?.message || '驳回失败'
  } finally {
    submitting.value = false
  }
}

watch(detailOpen, (open) => {
  if (!open) {
    actionError.value = ''
  }
})

watch(filteredApprovals, (list) => {
  if (detailOpen.value && selectedId.value && !list.some((item) => item.id === selectedId.value)) {
    closeDetail()
  }
})

onBeforeUnmount(() => {
  detailOpen.value = false
})
</script>
