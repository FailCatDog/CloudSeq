<template>
  <div v-if="approval" class="tch-approval-detail">
    <span class="tch-tag" :class="approvalStatusTagClass(approval.status)">
      {{ approvalStatusLabel(approval.status) }}
    </span>
    <h4>{{ approval.topicTitle }}</h4>
    <p class="tch-detail-desc">{{ approval.topicDesc || '暂无说明' }}</p>

    <dl class="tch-detail-meta">
      <div>
        <dt>所属小组</dt>
        <dd>{{ approval.teamLabel }}（{{ approval.memberCount }} 人）</dd>
      </div>
      <div>
        <dt>组长</dt>
        <dd>{{ approval.leaderName }} · {{ approval.leaderNo }}</dd>
      </div>
      <div>
        <dt>提交时间</dt>
        <dd>{{ approval.submittedAtFull }}</dd>
      </div>
      <div>
        <dt>成员名单</dt>
        <dd>{{ approval.members }}</dd>
      </div>
      <div v-if="approval.rejectReason">
        <dt>驳回理由</dt>
        <dd>{{ approval.rejectReason }}</dd>
      </div>
    </dl>

    <template v-if="isApprovalPending(approval.status)">
      <div class="tch-detail-actions">
        <button
          type="button"
          class="wb-btn-schedule tch-detail-action-btn"
          :disabled="submitting"
          @click="emitApprove"
        >
          {{ submitting ? '处理中…' : '通过审批' }}
        </button>
        <button
          type="button"
          class="wb-btn-schedule wb-btn-schedule--outline wb-btn-schedule--danger tch-detail-action-btn"
          :disabled="submitting"
          @click="showRejectReason = !showRejectReason"
        >
          驳回
        </button>
      </div>
      <textarea
        v-if="showRejectReason"
        v-model="rejectReason"
        class="tch-reject-reason"
        :disabled="submitting"
        placeholder="驳回理由（驳回时必填，将展示给学生组）…"
      />
      <button
        v-if="showRejectReason"
        type="button"
        class="wb-btn-schedule wb-btn-schedule--sm tch-reject-submit"
        :disabled="submitting || !rejectReason.trim()"
        @click="emitReject"
      >
        {{ submitting ? '处理中…' : '确认驳回' }}
      </button>
    </template>
    <p v-else class="tch-detail-readonly">该选题已处理，无需再次审批。</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  approvalStatusLabel,
  approvalStatusTagClass,
  isApprovalPending,
} from '@/utils/approvalFormat'

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

<style scoped>
.tch-approval-detail h4 {
  margin: 12px 0 8px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
}

.tch-reject-submit {
  margin-top: 10px;
  width: 100%;
  justify-content: center;
}
</style>
