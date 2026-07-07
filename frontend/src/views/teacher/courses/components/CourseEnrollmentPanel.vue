<template>
  <div class="tch-enrollment-panel">
    <div class="tch-enrollment-summary">
      <h4>{{ course?.courseName }}</h4>
      <p class="tch-enrollment-code">{{ course?.courseCode }}</p>
    </div>

    <form class="tch-enrollment-add" @submit.prevent="handleEnroll">
      <div class="tch-form-field">
        <label for="enroll-user-id">学生用户 ID</label>
        <input
          id="enroll-user-id"
          v-model.trim="enrollForm.userId"
          type="text"
          inputmode="numeric"
          placeholder="输入学生账号 ID"
          required
        >
      </div>
      <div class="tch-form-field">
        <label for="enroll-student-no">学号（可选）</label>
        <input
          id="enroll-student-no"
          v-model.trim="enrollForm.studentNo"
          type="text"
          maxlength="32"
          placeholder="不填则使用学生档案学号"
        >
      </div>
      <button type="submit" class="wb-btn-schedule wb-btn-schedule--sm" :disabled="enrolling">
        {{ enrolling ? '添加中…' : '添加选课' }}
      </button>
      <p v-if="enrollError" class="tch-form-error">{{ enrollError }}</p>
    </form>

    <div class="tch-enrollment-list">
      <div class="tch-enrollment-list__head">
        <span>已选学生（{{ enrollments.length }}）</span>
        <button type="button" class="tch-table-link tch-table-link--btn" :disabled="loading" @click="loadEnrollments">
          刷新
        </button>
      </div>

      <div v-if="loading" class="tch-enrollment-empty">加载中…</div>
      <div v-else-if="!enrollments.length" class="tch-enrollment-empty">暂无选课学生</div>
      <ul v-else class="tch-enrollment-items">
        <li v-for="item in enrollments" :key="item.id" class="tch-enrollment-item">
          <div class="tch-enrollment-item__main">
            <strong>{{ item.userName || `用户 ${item.userId}` }}</strong>
            <span>{{ item.studentNo || '—' }}</span>
          </div>
          <button
            type="button"
            class="tch-table-link tch-table-link--btn tch-table-link--danger"
            @click="handleDrop(item)"
          >
            退课
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { dropCourseEnrollmentApi, enrollCourseStudentApi, listCourseEnrollmentsApi } from '@/api/course'
import { openAppConfirm } from '@/composables/appPrompt'

const props = defineProps({
  course: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['changed'])

const loading = ref(false)
const enrolling = ref(false)
const enrollError = ref('')
const enrollments = ref([])

const enrollForm = reactive({
  userId: '',
  studentNo: '',
})

const loadEnrollments = async () => {
  if (!props.course?.id) return
  loading.value = true
  try {
    const list = await listCourseEnrollmentsApi(props.course.id)
    enrollments.value = Array.isArray(list) ? list : []
  } catch (error) {
    enrollments.value = []
    enrollError.value = error?.message || '加载选课列表失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.course?.id,
  () => {
    enrollForm.userId = ''
    enrollForm.studentNo = ''
    enrollError.value = ''
    loadEnrollments()
  },
  { immediate: true },
)

const handleEnroll = async () => {
  enrollError.value = ''
  const userId = enrollForm.userId.trim()
  if (!userId) {
    enrollError.value = '请输入有效的学生用户 ID'
    return
  }

  enrolling.value = true
  try {
    await enrollCourseStudentApi(props.course.id, {
      userId,
      studentNo: enrollForm.studentNo || undefined,
    })
    enrollForm.userId = ''
    enrollForm.studentNo = ''
    await loadEnrollments()
    emit('changed')
  } catch (error) {
    enrollError.value = error?.message || '添加选课失败'
  } finally {
    enrolling.value = false
  }
}

const handleDrop = async (item) => {
  const confirmed = await openAppConfirm({
    title: '确认退课',
    message: `确定将「${item.userName || item.userId}」从本课号退课吗？`,
    danger: true,
    confirmLabel: '退课',
  })
  if (!confirmed) return

  try {
    await dropCourseEnrollmentApi(props.course.id, item.userId)
    await loadEnrollments()
    emit('changed')
  } catch (error) {
    enrollError.value = error?.message || '退课失败'
  }
}
</script>
