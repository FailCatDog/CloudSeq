<template>
  <form class="tch-course-form" @submit.prevent="handleSubmit">
    <div v-if="mode === 'create'" class="tch-form-field">
      <label for="course-code">课号 <span class="tch-form-required">*</span></label>
      <input
        id="course-code"
        v-model.trim="form.courseCode"
        type="text"
        maxlength="32"
        placeholder="如 PM2026-01"
        required
      >
    </div>

    <div class="tch-form-field">
      <label for="course-name">课程名称 <span class="tch-form-required">*</span></label>
      <input
        id="course-name"
        v-model.trim="form.courseName"
        type="text"
        maxlength="128"
        placeholder="如：软件项目管理"
        required
      >
    </div>

    <div v-if="mode === 'create'" class="tch-form-row">
      <div class="tch-form-field">
        <label for="course-year">学年 <span class="tch-form-required">*</span></label>
        <input
          id="course-year"
          v-model.number="form.termYear"
          type="number"
          min="2000"
          max="2100"
          required
        >
      </div>
      <div class="tch-form-field">
        <label for="course-season">学期 <span class="tch-form-required">*</span></label>
        <select id="course-season" v-model="form.termSeason" required :disabled="!dictLoaded">
          <option value="" disabled>{{ dictLoaded ? '请选择' : '加载中…' }}</option>
          <option v-for="item in termSeasonOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="mode === 'edit'" class="tch-form-field">
      <label for="course-status">状态</label>
      <select id="course-status" v-model="form.status" :disabled="!dictLoaded">
        <option v-for="item in courseStatusOptions" :key="item.value" :value="item.value">
          {{ item.label }}
        </option>
      </select>
    </div>

    <div class="tch-form-field">
      <label for="course-deadline">选题截止</label>
      <input id="course-deadline" v-model="form.topicDeadline" type="datetime-local">
    </div>

    <div class="tch-form-row">
      <div class="tch-form-field">
        <label for="course-min-size">最小组人数</label>
        <input id="course-min-size" v-model.number="form.minTeamSize" type="number" min="1" max="20">
      </div>
      <div class="tch-form-field">
        <label for="course-max-size">最大组人数</label>
        <input id="course-max-size" v-model.number="form.maxTeamSize" type="number" min="1" max="20">
      </div>
    </div>

    <label class="tch-form-check">
      <input v-model="weeklyRequiredChecked" type="checkbox">
      <span>强制提交周报</span>
    </label>

    <div class="tch-form-field">
      <label for="course-desc">课号说明</label>
      <textarea
        id="course-desc"
        v-model="form.description"
        rows="4"
        maxlength="2000"
        placeholder="可选，向学生展示的教学说明"
      />
    </div>

    <p v-if="errorMessage" class="tch-form-error">{{ errorMessage }}</p>

    <div class="tch-detail-actions">
      <button type="button" class="wb-btn-schedule wb-btn-schedule--outline tch-detail-action-btn" @click="emit('cancel')">
        取消
      </button>
      <button type="submit" class="wb-btn-schedule tch-detail-action-btn" :disabled="submitting">
        {{ submitting ? '保存中…' : mode === 'create' ? '创建课号' : '保存修改' }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { createCourseApi, updateCourseApi } from '@/api/course'
import { CacheCode } from '@/constants/cacheCode'
import { useDict } from '@/composables/useDict'
import { toApiDateTime, toDatetimeLocal } from '@/utils/courseFormat'

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

const { options: courseStatusOptions, loaded: dictLoaded } = useDict(CacheCode.COURSE_STATUS)
const { options: termSeasonOptions } = useDict(CacheCode.TERM_SEASON)

const pickDefaultValue = (options, current) => {
  if (!options.length) return current || ''
  if (current && options.some((item) => item.value === current)) return current
  return options[0].value
}

const syncDictDefaults = () => {
  if (props.mode === 'edit') return
  form.termSeason = pickDefaultValue(termSeasonOptions.value, form.termSeason)
}

const submitting = ref(false)
const errorMessage = ref('')

const defaultForm = () => ({
  courseCode: '',
  courseName: '',
  termYear: new Date().getFullYear(),
  termSeason: '',
  description: '',
  status: '',
  topicDeadline: '',
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

const resetForm = () => {
  Object.assign(form, defaultForm())
  if (props.mode === 'edit' && props.course) {
    form.courseName = props.course.courseName || ''
    form.description = props.course.description || ''
    form.status = props.course.status || pickDefaultValue(courseStatusOptions.value, '')
    form.topicDeadline = toDatetimeLocal(props.course.topicDeadline)
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
  errorMessage.value = ''

  if (!form.courseName.trim()) {
    errorMessage.value = '请填写课程名称'
    return
  }
  if (form.minTeamSize > form.maxTeamSize) {
    errorMessage.value = '最小组人数不能大于最大组人数'
    return
  }

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
        topicDeadline: toApiDateTime(form.topicDeadline),
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
      topicDeadline: toApiDateTime(form.topicDeadline),
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
