<template>
  <section class="weekly-page">
    <div class="weekly-canvas">
      <header class="weekly-canvas__header">
        <h2>周报提交表单</h2>
        <p>请于周一 10 点前提交周报。</p>
        <div class="weekly-week-nav">
          <button
            type="button"
            class="weekly-week-nav__btn"
            :disabled="loading || saving || submitting"
            @click="shiftWeek(-1)"
          >
            上一周
          </button>
          <div class="weekly-week-nav__center">
            <strong>{{ weekContext.reportYear }}年 第{{ weekContext.reportWeek }}周</strong>
            <span>{{ reportDateLabel }}</span>
            <span class="weekly-week-nav__status" :class="`weekly-week-nav__status--${weekStatusTone}`">
              {{ weekStatusLabel }}
            </span>
          </div>
          <button
            type="button"
            class="weekly-week-nav__btn"
            :disabled="loading || saving || submitting || !canGoNextWeek"
            @click="shiftWeek(1)"
          >
            下一周
          </button>
          <button
            v-if="!isCurrentWeek"
            type="button"
            class="weekly-week-nav__btn weekly-week-nav__btn--ghost"
            :disabled="loading || saving || submitting"
            @click="goCurrentWeek"
          >
            回到本周
          </button>
        </div>
      </header>

      <div v-if="loading" class="weekly-canvas__loading">正在加载该周周报...</div>

      <form v-else class="weekly-form" @submit.prevent="handleSubmit">
        <div
          v-for="field in formFields"
          :key="field.key"
          class="form-item"
          :class="{ 'form-item--locked': field.locked }"
        >
          <div class="form-item__label-wrap">
            <span v-if="field.required" class="form-item__required">*</span>
            <span v-if="field.locked" class="form-item__lock" aria-hidden="true">
              <svg viewBox="0 0 16 16" width="14" height="14">
                <path
                  fill="currentColor"
                  d="M4.5 7V5a3.5 3.5 0 1 1 7 0v2h.5A1.5 1.5 0 0 1 13.5 8.5v5A1.5 1.5 0 0 1 12 15H4a1.5 1.5 0 0 1-1.5-1.5v-5A1.5 1.5 0 0 1 4 7h.5Zm1.5 0h4V5a2 2 0 1 0-4 0v2Z"
                />
              </svg>
            </span>
            <span class="form-item__label">{{ field.label }}</span>
          </div>

          <div class="form-item__answer">
            <input
              v-if="field.type === 'text' && field.locked"
              :value="field.value"
              type="text"
              class="answer-input answer-input--readonly"
              readonly
              :placeholder="field.placeholder"
              :maxlength="field.maxLength"
            />
            <input
              v-else-if="field.type === 'text'"
              v-model="fieldModels[field.key]"
              type="text"
              class="answer-input"
              :placeholder="field.placeholder"
              :disabled="isReadonly"
              :maxlength="field.maxLength"
            />
            <select
              v-else-if="field.type === 'select'"
              v-model="fieldModels[field.key]"
              class="answer-input answer-input--select"
              :disabled="isReadonly"
            >
              <option value="">{{ field.placeholder }}</option>
              <option v-for="option in field.options" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
            <textarea
              v-else
              v-model="fieldModels[field.key]"
              class="answer-textarea"
              :rows="field.rows || 4"
              :placeholder="field.placeholder"
              :disabled="isReadonly"
              :maxlength="field.maxLength"
            />
          </div>
        </div>

        <p v-if="message" class="form-message" :class="`form-message--${messageType}`">{{ message }}</p>

        <footer v-if="!isReadonly" class="weekly-form__footer">
          <button
            v-if="form.id"
            type="button"
            class="btn btn--ghost"
            :disabled="saving || submitting"
            @click="handleDelete"
          >
            删除草稿
          </button>
          <button type="button" class="btn btn--secondary" :disabled="saving || submitting" @click="handleSaveDraft">
            {{ saving ? '保存中...' : '保存草稿' }}
          </button>
          <button type="submit" class="btn btn--primary" :disabled="saving || submitting">
            {{ submitting ? '提交中...' : '提交周报' }}
          </button>
        </footer>
      </form>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { WEEKLY_REPORT_STATUS } from '@/constants/common.js'
import {
  WEEKLY_REPORTER_MAX_LENGTH,
  WEEKLY_TEXT_MAX_LENGTH,
  WEEKLY_TITLE_MAX_LENGTH,
} from '@/constants/fieldLimits'
import {
  createWeeklyReportApi,
  deleteWeeklyReportApi,
  getWeeklyReportByWeekApi,
  submitWeeklyReportApi,
  updateWeeklyReportApi,
} from '@/api/weekly'
import { getCurrentTeamApi } from '@/api/team'
import { useWorkspace } from '@/composables/useWorkspace'
import { formatWeekRangeLabel, getIsoWeek, shiftIsoWeek } from '@/utils/isoWeek'

const { loadCurrentWorkspace, requireWorkspaceId } = useWorkspace()
const USER_STORAGE_KEY = 'user'

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const message = ref('')
const messageType = ref('info')
const teamTopicTitle = ref('')

const weekContext = ref(getIsoWeek())

const form = reactive({
  id: null,
  version: null,
  title: '',
  weeklyProgress: '',
  problems: '',
  nextPlan: '',
  projectId: '',
  reportStatus: WEEKLY_REPORT_STATUS.DRAFT,
  submitDate: '',
})

const fieldModels = reactive({
  reporter: '',
  title: '',
  projectId: '',
  weeklyProgress: '',
  problems: '',
  nextPlan: '',
})

const getStoredUser = () => {
  const raw = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return {}
  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
}

const displayName = computed(() => {
  const user = getStoredUser()
  return user.realName || user.nickName || user.username || '当前用户'
})

const reportDateLabel = computed(() =>
  formatWeekRangeLabel(weekContext.value.weekStartDate, weekContext.value.weekEndDate),
)

const currentIsoWeek = computed(() => getIsoWeek())

const isCurrentWeek = computed(() => {
  const now = currentIsoWeek.value
  return weekContext.value.reportYear === now.reportYear
    && weekContext.value.reportWeek === now.reportWeek
})

const canGoNextWeek = computed(() => {
  const next = shiftIsoWeek(
    weekContext.value.reportYear,
    weekContext.value.reportWeek,
    1,
  )
  const now = currentIsoWeek.value
  if (next.reportYear > now.reportYear) return false
  if (next.reportYear === now.reportYear && next.reportWeek > now.reportWeek) return false
  return true
})

const weekStatusLabel = computed(() => {
  if (!form.id) return '未填写'
  if (form.reportStatus === WEEKLY_REPORT_STATUS.SUBMITTED) return '已提交'
  return '草稿'
})

const weekStatusTone = computed(() => {
  if (!form.id) return 'empty'
  if (form.reportStatus === WEEKLY_REPORT_STATUS.SUBMITTED) return 'submitted'
  return 'draft'
})

const shiftWeek = (delta) => {
  if (loading.value || saving.value || submitting.value) return
  weekContext.value = shiftIsoWeek(
    weekContext.value.reportYear,
    weekContext.value.reportWeek,
    delta,
  )
  void loadWeekReport()
}

const goCurrentWeek = () => {
  if (loading.value || saving.value || submitting.value) return
  weekContext.value = getIsoWeek()
  void loadWeekReport()
}

const buildDefaultTitle = () =>
  `${displayName.value}的周报 ${weekContext.value.reportYear}年第${weekContext.value.reportWeek}周`

const projectOptions = computed(() => {
  const options = []
  if (teamTopicTitle.value) {
    options.push({ value: 'team-topic', label: teamTopicTitle.value })
  }
  return options
})

const formFields = computed(() => [
  {
    key: 'reportDate',
    label: '汇报日期',
    type: 'text',
    required: true,
    locked: true,
    value: reportDateLabel.value,
    placeholder: '填写者回答区',
  },
  {
    key: 'title',
    label: '汇报标题',
    type: 'text',
    required: false,
    locked: false,
    placeholder: '填写者回答区',
    maxLength: WEEKLY_TITLE_MAX_LENGTH,
  },
  {
    key: 'reporter',
    label: '汇报人',
    type: 'text',
    required: true,
    locked: false,
    placeholder: '填写者回答区',
    maxLength: WEEKLY_REPORTER_MAX_LENGTH,
  },
  {
    key: 'projectId',
    label: '所属项目',
    type: 'select',
    required: true,
    locked: false,
    placeholder: '填写者回答区',
    options: projectOptions.value,
  },
  {
    key: 'weeklyProgress',
    label: '本周进度内容',
    type: 'textarea',
    required: true,
    locked: false,
    rows: 5,
    placeholder: '填写者回答区',
    maxLength: WEEKLY_TEXT_MAX_LENGTH,
  },
  {
    key: 'problems',
    label: '遇到的问题与风险',
    type: 'textarea',
    required: false,
    locked: false,
    rows: 4,
    placeholder: '填写者回答区',
    maxLength: WEEKLY_TEXT_MAX_LENGTH,
  },
  {
    key: 'nextPlan',
    label: '下周计划',
    type: 'textarea',
    required: false,
    locked: false,
    rows: 4,
    placeholder: '填写者回答区',
    maxLength: WEEKLY_TEXT_MAX_LENGTH,
  },
])

const isReadonly = computed(
  () => (form.reportStatus ?? WEEKLY_REPORT_STATUS.DRAFT) === WEEKLY_REPORT_STATUS.SUBMITTED,
)

watch(
  displayName,
  (name) => {
    fieldModels.reporter = name
  },
  { immediate: true },
)

watch(
  () => [
    fieldModels.title,
    fieldModels.weeklyProgress,
    fieldModels.problems,
    fieldModels.nextPlan,
    fieldModels.projectId,
  ],
  () => {
    form.title = fieldModels.title
    form.weeklyProgress = fieldModels.weeklyProgress
    form.problems = fieldModels.problems
    form.nextPlan = fieldModels.nextPlan
    form.projectId = fieldModels.projectId
  },
)

const resetForm = () => {
  form.id = null
  form.version = null
  form.title = buildDefaultTitle()
  form.weeklyProgress = ''
  form.problems = ''
  form.nextPlan = ''
  form.projectId = ''
  form.reportStatus = WEEKLY_REPORT_STATUS.DRAFT
  form.submitDate = ''
  fieldModels.reporter = displayName.value
  fieldModels.title = buildDefaultTitle()
  fieldModels.projectId = ''
  fieldModels.weeklyProgress = ''
  fieldModels.problems = ''
  fieldModels.nextPlan = ''
}

const applyReport = (report) => {
  if (!report) {
    resetForm()
    return
  }
  form.id = report.id ?? null
  form.version = report.version ?? null
  form.title = report.title || buildDefaultTitle()
  form.weeklyProgress = report.weeklyProgress || ''
  form.problems = report.problems || ''
  form.nextPlan = report.nextPlan || ''
  form.projectId = report.projectId ? String(report.projectId) : ''
  form.reportStatus = report.reportStatus ?? WEEKLY_REPORT_STATUS.DRAFT
  form.submitDate = report.submitDate || ''
  fieldModels.reporter = displayName.value
  fieldModels.title = form.title || buildDefaultTitle()
  fieldModels.projectId = form.projectId || (teamTopicTitle.value ? 'team-topic' : '')
  fieldModels.weeklyProgress = form.weeklyProgress
  fieldModels.problems = form.problems
  fieldModels.nextPlan = form.nextPlan
}

const setMessage = (text, type = 'info') => {
  message.value = text
  messageType.value = type
}

const loadTeamContext = async () => {
  try {
    const team = await getCurrentTeamApi()
    teamTopicTitle.value = team?.topicTitle || ''
    if (!form.id && !fieldModels.projectId && teamTopicTitle.value) {
      fieldModels.projectId = 'team-topic'
    }
  } catch {
    teamTopicTitle.value = ''
  }
}

const loadWeekReport = async () => {
  loading.value = true
  message.value = ''
  try {
    await loadCurrentWorkspace()
    await loadTeamContext()
    const report = await getWeeklyReportByWeekApi(
      requireWorkspaceId(),
      weekContext.value.reportYear,
      weekContext.value.reportWeek,
    )
    applyReport(report)
    if (!fieldModels.title.trim()) fieldModels.title = buildDefaultTitle()
    if (!report) {
      setMessage('该周暂无周报，可新建填写', 'info')
    }
  } catch (error) {
    resetForm()
    setMessage(error.message || '加载周报失败', 'error')
  } finally {
    loading.value = false
  }
}

const resolveProjectId = () => {
  if (!fieldModels.projectId || fieldModels.projectId === 'team-topic') return null
  return fieldModels.projectId
}

const buildPayload = () => ({
  workspaceId: requireWorkspaceId(),
  id: form.id ?? undefined,
  version: form.version ?? undefined,
  title: fieldModels.title.trim() || buildDefaultTitle(),
  weeklyProgress: fieldModels.weeklyProgress,
  problems: fieldModels.problems,
  nextPlan: fieldModels.nextPlan,
  projectId: resolveProjectId(),
  reportYear: weekContext.value.reportYear,
  reportWeek: weekContext.value.reportWeek,
  weekStartDate: weekContext.value.weekStartDate,
})

const handleSaveDraft = async () => {
  saving.value = true
  message.value = ''
  try {
    const payload = buildPayload()
    const saved = form.id
      ? await updateWeeklyReportApi(payload)
      : await createWeeklyReportApi(payload)
    applyReport(saved)
    setMessage('草稿已保存', 'success')
  } catch (error) {
    setMessage(error.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

const handleSubmit = async () => {
  if (!fieldModels.weeklyProgress.trim()) {
    setMessage('请先填写本周进度内容', 'error')
    return
  }

  submitting.value = true
  message.value = ''
  try {
    let reportId = form.id
    if (!reportId) {
      const saved = await createWeeklyReportApi(buildPayload())
      applyReport(saved)
      reportId = saved.id
    } else {
      const saved = await updateWeeklyReportApi(buildPayload())
      applyReport(saved)
      reportId = saved.id
    }
    const submitted = await submitWeeklyReportApi(reportId)
    applyReport(submitted)
    setMessage('周报已提交', 'success')
  } catch (error) {
    setMessage(error.message || '提交失败', 'error')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async () => {
  if (!form.id) return
  if (!window.confirm('确定删除该周草稿吗？')) return

  saving.value = true
  message.value = ''
  try {
    await deleteWeeklyReportApi(form.id)
    resetForm()
    setMessage('草稿已删除', 'success')
  } catch (error) {
    setMessage(error.message || '删除失败', 'error')
  } finally {
    saving.value = false
  }
}

onMounted(loadWeekReport)
</script>

<style lang="scss" scoped>
@use '@/styles/mixins' as *;

.weekly-page {
  position: relative;
  flex: 1;
  align-self: stretch;
  width: 100%;
  min-width: 0;
  height: 100%;
  min-height: 0;
  padding: 48px 20px 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  overflow-x: hidden;
  overflow-y: auto;
  box-sizing: border-box;
  background: var(--wb-bg-page);
}

.weekly-canvas {
  position: relative;
  width: min(100%, 560px);
  min-width: 0;
  max-width: 100%;
  padding: 28px 24px 24px;
  border-radius: var(--wb-radius-card);
  background: var(--wb-card-bg);
  border: 1px solid var(--wb-search-border);
  box-shadow: var(--wb-card-shadow);
  box-sizing: border-box;

  &__header {
    text-align: center;
    margin-bottom: 22px;

    h2 {
      margin: 0 0 8px;
      font-size: 22px;
      font-weight: 700;
      color: var(--wb-text-primary);
    }

    p {
      margin: 0;
      font-size: 13px;
      color: var(--wb-text-secondary);
    }
  }

  &__loading {
    padding: 48px 0;
    text-align: center;
    color: var(--wb-text-secondary);
    font-size: 14px;
  }
}

.weekly-week-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;

  &__center {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    min-width: 200px;
    font-size: 13px;
    color: var(--wb-text-secondary);

    strong {
      font-size: 15px;
      color: var(--wb-text-primary);
    }
  }

  &__status {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px;
    border-radius: var(--wb-radius-pill);
    font-size: 12px;
    font-weight: 600;

    &--empty {
      background: var(--wb-tag-muted-bg);
      color: var(--wb-tag-muted-text);
    }

    &--draft {
      background: var(--wb-tag-draft-bg);
      color: var(--wb-tag-draft-text);
    }

    &--submitted {
      background: var(--wb-tag-success-bg);
      color: var(--wb-tag-success-text);
    }
  }

  &__btn {
    border: 1px solid var(--wb-search-border);
    border-radius: var(--wb-radius-pill);
    background: var(--wb-card-bg);
    color: var(--wb-text-primary);
    font-size: 13px;
    padding: 6px 14px;
    cursor: pointer;
    transition: background 0.15s, border-color 0.15s;

    &:hover:not(:disabled) {
      background: var(--wb-search-bg);
      border-color: var(--wb-purple-border);
    }

    &:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }

    &--ghost {
      color: var(--wb-purple);
    }
  }
}

.weekly-form {
  display: grid;
  gap: 18px;

  &__footer {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
    padding-top: 8px;
  }
}

.form-item {
  display: grid;
  gap: 10px;
  min-width: 0;

  &__label-wrap {
    display: flex;
    align-items: center;
    gap: 4px;
    min-width: 0;
  }

  &__required {
    color: var(--wb-tag-warning-text);
    font-size: 14px;
    line-height: 1;
  }

  &__lock {
    display: inline-flex;
    color: var(--wb-text-muted);
  }

  &__label {
    font-size: 14px;
    font-weight: 600;
    color: var(--wb-text-primary);
  }

  &__answer {
    margin-left: 0;
    min-width: 0;
  }
}

.answer-input,
.answer-textarea {
  width: 100%;
  max-width: 100%;
  border: 1px solid var(--wb-search-border);
  border-radius: var(--wb-radius-sm);
  background: var(--wb-card-bg);
  color: var(--wb-text-primary);
  font: inherit;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;

  &::placeholder {
    color: var(--wb-text-muted);
  }

  &:disabled {
    background: var(--wb-search-bg);
    color: var(--wb-text-secondary);
    cursor: not-allowed;
  }
}

.answer-input {
  height: 40px;
  padding: 0 12px;

  &--select {
    appearance: none;
    padding-right: 32px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%238b8b9e' d='M2.5 4.5 6 8l3.5-3.5'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 12px center;
  }

  &--readonly {
    background: var(--wb-search-bg);
    color: var(--wb-text-secondary);
    cursor: not-allowed;
  }

  &:focus:not(:disabled):not([readonly]) {
    @include focus-ring;
  }
}

.answer-textarea {
  min-height: 96px;
  padding: 10px 12px;
  line-height: 1.6;
  resize: vertical;

  &:focus:not(:disabled) {
    @include focus-ring;
  }
}

.form-message {
  margin: 0;
  font-size: 13px;
  text-align: center;

  &--success {
    color: var(--wb-tag-success-text);
  }

  &--error {
    color: var(--wb-tag-danger-text);
  }

  &--info {
    color: var(--wb-text-secondary);
  }
}

.btn {
  height: 36px;
  padding: 0 18px;
  border-radius: var(--wb-radius-sm);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;

  &:disabled {
    opacity: 0.65;
    cursor: not-allowed;
  }

  &--primary {
    border: 0;
    color: #fff;
    background: var(--wb-purple);

    &:hover:not(:disabled) {
      background: var(--wb-purple-light);
    }
  }

  &--secondary {
    border: 1px solid var(--wb-search-border);
    color: var(--wb-text-primary);
    background: var(--wb-card-bg);

    &:hover:not(:disabled) {
      border-color: var(--wb-purple-border);
      color: var(--wb-purple);
      background: var(--wb-purple-soft);
    }
  }

  &--ghost {
    border: 1px solid var(--wb-tag-danger-border);
    color: var(--wb-tag-danger-text);
    background: var(--wb-tag-danger-soft);

    &:hover:not(:disabled) {
      border-color: #fca5a5;
      background: var(--wb-tag-danger-bg);
    }
  }
}
</style>
