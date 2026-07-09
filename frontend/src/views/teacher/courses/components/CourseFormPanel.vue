<template>
  <n-form class="console-drawer-body" @submit.prevent="handleSubmit">
    <n-form-item v-if="mode === 'create'" label="课号" required>
      <n-input v-model:value="form.courseCode" maxlength="32" placeholder="如 PM2026-01" />
    </n-form-item>

    <n-form-item v-if="mode === 'view'" label="课号">
      <n-input :value="course?.courseCode || ''" readonly />
    </n-form-item>

    <n-form-item label="课程名称" :required="!readonly">
      <n-input
        v-model:value="form.courseName"
        maxlength="128"
        placeholder="如：软件项目管理"
        :readonly="readonly"
      />
    </n-form-item>

    <n-grid v-if="mode === 'create'" :cols="2" :x-gap="12">
      <n-form-item-gi label="学年" required>
        <n-input-number v-model:value="form.termYear" :min="2000" :max="2100" style="width: 100%" />
      </n-form-item-gi>
      <n-form-item-gi label="学期" required>
        <n-select
          v-model:value="form.termSeason"
          :options="termSeasonOptions"
          :disabled="!dictLoaded"
          placeholder="请选择"
        />
      </n-form-item-gi>
    </n-grid>

    <n-grid v-if="mode === 'view'" :cols="2" :x-gap="12">
      <n-form-item-gi label="学年">
        <n-input :value="String(course?.termYear ?? '')" readonly />
      </n-form-item-gi>
      <n-form-item-gi label="学期">
        <n-input :value="termSeasonLabel" readonly />
      </n-form-item-gi>
    </n-grid>

    <n-form-item v-if="mode === 'edit' || mode === 'view'" label="状态">
      <n-select
        v-model:value="form.status"
        :options="courseStatusOptions"
        :disabled="readonly || !dictLoaded"
      />
    </n-form-item>

    <n-form-item label="选题截止">
      <n-date-picker
        v-model:value="topicDeadlineTs"
        type="datetime"
        clearable
        style="width: 100%"
        :disabled="readonly"
      />
    </n-form-item>

    <n-grid :cols="2" :x-gap="12">
      <n-form-item-gi label="最小组人数">
        <n-input-number v-model:value="form.minTeamSize" :min="1" :max="20" style="width: 100%" :disabled="readonly" />
      </n-form-item-gi>
      <n-form-item-gi label="最大组人数">
        <n-input-number v-model:value="form.maxTeamSize" :min="1" :max="20" style="width: 100%" :disabled="readonly" />
      </n-form-item-gi>
    </n-grid>

    <n-form-item label=" ">
      <n-checkbox v-model:checked="weeklyRequiredChecked" :disabled="readonly">强制提交周报</n-checkbox>
    </n-form-item>

    <n-form-item label="课号说明">
      <n-input
        v-model:value="form.description"
        type="textarea"
        :rows="4"
        maxlength="2000"
        placeholder="可选，向学生展示的教学说明"
        :readonly="readonly"
      />
    </n-form-item>

    <n-alert v-if="errorMessage" type="error" :bordered="false">{{ errorMessage }}</n-alert>

    <n-space v-if="readonly" justify="end">
      <n-button @click="emit('cancel')">关闭</n-button>
    </n-space>
    <n-space v-else justify="end">
      <n-button @click="emit('cancel')">取消</n-button>
      <n-button type="primary" attr-type="submit" :loading="submitting">
        {{ mode === 'create' ? '创建课号' : '保存修改' }}
      </n-button>
    </n-space>
  </n-form>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { createCourseApi, updateCourseApi } from '@/api/course'
import { CacheCode } from '@/constants/cacheCode'
import { useDict } from '@/composables/useDict'
import { toApiDateTime } from '@/utils/courseFormat'

const props = defineProps({
  mode: {
    type: String,
    default: 'create',
  },
  course: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['saved', 'cancel'])

const readonly = computed(() => props.mode === 'view')

const { options: courseStatusOptions, loaded: dictLoaded } = useDict(CacheCode.COURSE_STATUS)
const { options: termSeasonOptions, label: termSeasonDictLabel } = useDict(CacheCode.TERM_SEASON)

const termSeasonLabel = computed(() => {
  const season = props.course?.termSeason
  if (!season) return '—'
  return termSeasonDictLabel(season) || season
})

const pickDefaultValue = (options, current) => {
  if (!options.length) return current || ''
  if (current && options.some((item) => item.value === current)) return current
  return options[0].value
}

const syncDictDefaults = () => {
  if (props.mode === 'edit' || props.mode === 'view') return
  form.termSeason = pickDefaultValue(termSeasonOptions.value, form.termSeason)
}

const submitting = ref(false)
const errorMessage = ref('')
const topicDeadlineTs = ref(null)

const defaultForm = () => ({
  courseCode: '',
  courseName: '',
  termYear: new Date().getFullYear(),
  termSeason: '',
  description: '',
  status: '',
  minTeamSize: 2,
  maxTeamSize: 5,
  weeklyRequired: 1,
})

const form = reactive(defaultForm())

const weeklyRequiredChecked = computed({
  get: () => form.weeklyRequired === 1,
  set: (value) => {
    form.weeklyRequired = value ? 1 : 0
  },
})

const parseDateTimeToTs = (value) => {
  if (!value) return null
  const normalized = String(value).replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date.getTime()
}

const formatTsToDatetimeLocal = (ts) => {
  const d = new Date(ts)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const resetForm = () => {
  Object.assign(form, defaultForm())
  topicDeadlineTs.value = null
  if ((props.mode === 'edit' || props.mode === 'view') && props.course) {
    form.courseName = props.course.courseName || ''
    form.description = props.course.description || ''
    form.status = props.course.status || pickDefaultValue(courseStatusOptions.value, '')
    topicDeadlineTs.value = parseDateTimeToTs(props.course.topicDeadline)
    form.minTeamSize = props.course.minTeamSize ?? 2
    form.maxTeamSize = props.course.maxTeamSize ?? 5
    form.weeklyRequired = props.course.weeklyRequired ?? 1
  }
  errorMessage.value = ''
}

watch(
  () => [props.mode, props.course],
  () => resetForm(),
  { immediate: true },
)

watch([termSeasonOptions, () => dictLoaded.value], () => {
  if (dictLoaded.value) syncDictDefaults()
})

const handleSubmit = async () => {
  if (readonly.value) return

  errorMessage.value = ''

  if (!form.courseName.trim()) {
    errorMessage.value = '请填写课程名称'
    return
  }
  if (form.minTeamSize > form.maxTeamSize) {
    errorMessage.value = '最小组人数不能大于最大组人数'
    return
  }

  const topicDeadline = topicDeadlineTs.value
    ? toApiDateTime(formatTsToDatetimeLocal(topicDeadlineTs.value))
    : null

  submitting.value = true
  try {
    if (props.mode === 'create') {
      if (!form.courseCode.trim()) {
        errorMessage.value = '请填写课号'
        return
      }
      if (!form.termSeason) {
        errorMessage.value = '请选择学期'
        return
      }

      const created = await createCourseApi({
        courseCode: form.courseCode.trim(),
        courseName: form.courseName.trim(),
        termYear: form.termYear,
        termSeason: form.termSeason,
        description: form.description?.trim() || null,
        topicDeadline,
        minTeamSize: form.minTeamSize,
        maxTeamSize: form.maxTeamSize,
        weeklyRequired: form.weeklyRequired,
      })
      emit('saved', created)
      return
    }

    const updated = await updateCourseApi(props.course.id, {
      courseName: form.courseName.trim(),
      description: form.description?.trim() || null,
      status: form.status,
      topicDeadline,
      minTeamSize: form.minTeamSize,
      maxTeamSize: form.maxTeamSize,
      weeklyRequired: form.weeklyRequired,
    })
    emit('saved', updated)
  } catch (error) {
    errorMessage.value = error?.message || '保存失败'
  } finally {
    submitting.value = false
  }
}
</script>
