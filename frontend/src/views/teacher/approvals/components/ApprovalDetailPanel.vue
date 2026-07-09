<template>
  <div v-if="approval" class="console-drawer-body">
    <n-tag :type="approvalTagType(approval.status)" round size="small">
      {{ approvalStatusLabel(approval.status) }}
    </n-tag>
    <h4 class="console-detail-title">{{ approval.topicTitle }}</h4>
    <p class="console-detail-desc">{{ approval.topicDesc || '暂无说明' }}</p>

    <n-descriptions :column="1" label-placement="left" size="small">
      <n-descriptions-item label="所属小组">
        {{ approval.teamLabel }}（{{ approval.memberCount }} 人）
      </n-descriptions-item>
      <n-descriptions-item label="组长">
        {{ approval.leaderName }} · {{ approval.leaderNo }}
      </n-descriptions-item>
      <n-descriptions-item label="提交时间">
        {{ approval.submittedAtFull }}
      </n-descriptions-item>
      <n-descriptions-item label="成员名单">
        {{ approval.members }}
      </n-descriptions-item>
      <n-descriptions-item v-if="approval.rejectReason" label="驳回理由">
        {{ approval.rejectReason }}
      </n-descriptions-item>
    </n-descriptions>

    <template v-if="isApprovalPending(approval.status)">
      <n-space style="margin-top: 20px" :size="12">
        <n-button type="primary" :loading="submitting" @click="emitApprove">
          {{ submitting ? '处理中…' : '通过审批' }}
        </n-button>
        <n-button type="error" ghost :disabled="submitting" @click="showRejectReason = !showRejectReason">
          驳回
        </n-button>
      </n-space>
      <n-input
        v-if="showRejectReason"
        v-model:value="rejectReason"
        type="textarea"
        :disabled="submitting"
        placeholder="驳回理由（驳回时必填，将展示给学生组）…"
        :rows="4"
        style="margin-top: 12px"
      />
      <n-button
        v-if="showRejectReason"
        type="error"
        block
        style="margin-top: 10px"
        :loading="submitting"
        :disabled="!rejectReason.trim()"
        @click="emitReject"
      >
        {{ submitting ? '处理中…' : '确认驳回' }}
      </n-button>
    </template>
    <n-text v-else depth="3" style="display: block; margin-top: 20px">
      该选题已处理，无需再次审批。
    </n-text>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  approvalStatusLabel,
  isApprovalPending,
} from '@/utils/approvalFormat'
import { approvalTagType } from '@/utils/naiveStatus'

const props = defineProps({
  approval: {
    type: Object,
    default: null,
  },
  submitting: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['approve', 'reject'])

const showRejectReason = ref(false)
const rejectReason = ref('')

watch(
  () => props.approval?.id,
  () => {
    showRejectReason.value = false
    rejectReason.value = ''
  },
)

const emitApprove = () => {
  if (!props.approval || props.submitting) return
  emit('approve', props.approval.id)
  showRejectReason.value = false
  rejectReason.value = ''
}

const emitReject = () => {
  if (!props.approval || props.submitting || !rejectReason.value.trim()) return
  emit('reject', { id: props.approval.id, reason: rejectReason.value.trim() })
  showRejectReason.value = false
  rejectReason.value = ''
}
</script>
