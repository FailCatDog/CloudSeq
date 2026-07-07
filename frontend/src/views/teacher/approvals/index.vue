<template>
  <div class="tch-page">
    <div class="tch-filter-bar">
      <div class="tch-filter-form">
        <div class="tch-filter-row">
          <div class="tch-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              v-model="searchQuery"
              type="search"
              placeholder="搜索组名、选题标题…"
              @keydown.enter.prevent="handleSearch"
            />
          </div>
          <label class="tch-filter-select-wrap">
            <span class="tch-filter-select-label">选题状态</span>
            <select v-model="activeStatus" class="tch-filter-select" aria-label="审批状态">
              <option v-for="option in statusOptions" :key="option.id" :value="option.id">
                {{ option.label }}
              </option>
            </select>
          </label>
        </div>
        <div class="tch-search-actions">
          <button
            type="button"
            class="wb-btn-schedule wb-btn-schedule--sm tch-filter-search"
            :disabled="loading"
            @click="handleSearch"
          >
            搜索
          </button>
          <button
            type="button"
            class="tch-filter-reset"
            :disabled="loading"
            @click="handleReset"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <p v-if="pageError" class="tch-page-error">{{ pageError }}</p>

    <div class="tch-table-surface">
      <div class="tch-table-wrap tch-table-wrap--flush">
        <table class="tch-table">
          <thead>
            <tr>
              <th class="tch-table-col-index">序号</th>
              <th>课号</th>
              <th>组名</th>
              <th>选题标题</th>
              <th>组长</th>
              <th>成员</th>
              <th>提交时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="tch-table-empty">加载中…</td>
            </tr>
            <tr v-for="(item, index) in filteredApprovals" v-else :key="item.id">
              <td class="tch-table-col-index">{{ index + 1 }}</td>
              <td>{{ item.courseCode }}</td>
              <td><strong>{{ item.teamLabel }}</strong></td>
              <td>{{ item.topicTitle }}</td>
              <td>{{ item.leaderName }}</td>
              <td>{{ item.memberCount }}</td>
              <td>{{ item.submittedAt }}</td>
              <td>
                <span class="tch-tag" :class="approvalStatusTagClass(item.status)">
                  {{ approvalStatusLabel(item.status) }}
                </span>
              </td>
              <td>
                <button type="button" class="tch-table-link tch-table-link--btn" @click="openDetail(item.id)">
                  详情
                </button>
              </td>
            </tr>
            <tr v-if="!loading && !filteredApprovals.length">
              <td colspan="9" class="tch-table-empty">没有匹配的审批记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="detailOpen"
        class="tch-drawer-backdrop"
        @click.self="closeDetail"
      >
        <aside class="tch-drawer" role="dialog" aria-modal="true" aria-labelledby="approval-detail-title">
          <header class="tch-drawer-header">
            <h3 id="approval-detail-title">选题详情</h3>
            <button type="button" class="tch-drawer-close" aria-label="关闭" @click="closeDetail">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </header>
          <div class="tch-drawer-body">
            <p v-if="actionError" class="tch-page-error">{{ actionError }}</p>
            <ApprovalDetailPanel
              :approval="selectedApproval"
              :submitting="submitting"
              @approve="handleApprove"
              @reject="handleReject"
            />
          </div>
        </aside>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { listTeacherTopicApprovalsApi, reviewTopicApprovalApi } from '@/api/team'
import { CacheCode } from '@/constants/cacheCode'
import {
  approvalStatusLabel,
  approvalStatusTagClass,
  isApprovalPending,
  mapTopicApprovalSummary,
} from '@/utils/approvalFormat'
import ApprovalDetailPanel from './components/ApprovalDetailPanel.vue'

const approvals = ref([])
const loading = ref(false)
const submitting = ref(false)
const pageError = ref('')
const actionError = ref('')
const searchQuery = ref('')
const activeStatus = ref('all')
const appliedKeyword = ref('')
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
  return approvals.value.filter((item) => {
    const statusMatch =
      appliedStatus.value === 'all' ||
      (appliedStatus.value === 'pending' && isApprovalPending(item.status)) ||
      item.status === appliedStatus.value
    if (!statusMatch) return false
    if (!keyword) return true
    return (
      item.teamLabel.toLowerCase().includes(keyword) ||
      item.topicTitle.toLowerCase().includes(keyword) ||
      item.leaderName.toLowerCase().includes(keyword) ||
      String(item.courseCode || '').toLowerCase().includes(keyword)
    )
  })
})

const selectedApproval = computed(() => {
  return approvals.value.find((item) => item.id === selectedId.value) || null
})

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
  appliedStatus.value = activeStatus.value
  actionError.value = ''
  closeDetail()
  await loadApprovals(resolveApiStatus(activeStatus.value))
}

const handleReset = async () => {
  searchQuery.value = ''
  activeStatus.value = 'all'
  appliedKeyword.value = ''
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

const onKeydown = (event) => {
  if (event.key === 'Escape') {
    closeDetail()
  }
}

watch(detailOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
  if (open) {
    window.addEventListener('keydown', onKeydown)
  } else {
    window.removeEventListener('keydown', onKeydown)
    actionError.value = ''
  }
})

watch(filteredApprovals, (list) => {
  if (detailOpen.value && selectedId.value && !list.some((item) => item.id === selectedId.value)) {
    closeDetail()
  }
})

onBeforeUnmount(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeydown)
})
</script>
