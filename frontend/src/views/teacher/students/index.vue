<template>
  <div class="console-page">
    <div class="console-filter">
      <div class="console-filter__fields">
        <div class="console-filter__field">
          <span class="console-filter__label">姓名</span>
          <n-input
            v-model:value="nameQuery"
            clearable
            placeholder="输入姓名关键字"
            @keydown.enter.prevent="handleSearch"
          />
        </div>
        <div class="console-filter__field">
          <span class="console-filter__label">学号</span>
          <n-input
            v-model:value="studentNoQuery"
            clearable
            placeholder="输入学号关键字"
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
        :data="students"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.enrollmentId"
        size="medium"
      />
    </n-card>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { NButton } from 'naive-ui'
import { dropCourseEnrollmentApi } from '@/api/course'
import { listTeacherStudentsApi } from '@/api/teaching'
import { useConsoleConfirm } from '@/composables/useConsoleConfirm'

const { confirm } = useConsoleConfirm()

const students = ref([])
const loading = ref(false)
const droppingId = ref(null)
const pageError = ref('')
const nameQuery = ref('')
const studentNoQuery = ref('')
const courseCodeQuery = ref('')

const formatEnrollDate = (value) => {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 19)
}

const currentSearchParams = () => ({
  name: nameQuery.value.trim() || undefined,
  studentNo: studentNoQuery.value.trim() || undefined,
  courseCode: courseCodeQuery.value.trim() || undefined,
})

const loadStudents = async (params = {}) => {
  loading.value = true
  pageError.value = ''
  try {
    const list = await listTeacherStudentsApi(params)
    students.value = Array.isArray(list) ? list : []
  } catch (error) {
    students.value = []
    pageError.value = error?.message || '加载学生列表失败'
  } finally {
    loading.value = false
  }
}

const handleDrop = async (row) => {
  const confirmed = await confirm({
    title: '确认退课',
    message: `确定将「${row.userName || row.studentNo || '该学生'}」从课号 ${row.courseCode || '—'} 退课吗？`,
    danger: true,
    confirmLabel: '退课',
  })
  if (!confirmed) return

  droppingId.value = row.enrollmentId
  pageError.value = ''
  try {
    await dropCourseEnrollmentApi(row.courseId, row.userId)
    await loadStudents(currentSearchParams())
  } catch (error) {
    pageError.value = error?.message || '退课失败'
  } finally {
    droppingId.value = null
  }
}

const columns = computed(() => [
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (_, index) => index + 1,
  },
  {
    title: '姓名',
    key: 'userName',
    minWidth: 120,
    render: (row) => h('strong', null, row.userName || '—'),
  },
  {
    title: '学号',
    key: 'studentNo',
    minWidth: 140,
    render: (row) => row.studentNo || '—',
  },
  {
    title: '课号',
    key: 'courseCode',
    minWidth: 120,
    render: (row) => row.courseCode || '—',
  },
  {
    title: '课程名称',
    key: 'courseName',
    minWidth: 180,
    ellipsis: { tooltip: true },
    render: (row) => row.courseName || '—',
  },
  {
    title: '选课时间',
    key: 'enrollDate',
    minWidth: 160,
    render: (row) => formatEnrollDate(row.enrollDate),
  },
  {
    title: '操作',
    key: 'actions',
    width: 88,
    render: (row) =>
      h(
        NButton,
        {
          text: true,
          type: 'error',
          loading: droppingId.value === row.enrollmentId,
          disabled: droppingId.value != null && droppingId.value !== row.enrollmentId,
          onClick: () => handleDrop(row),
        },
        { default: () => '删除' },
      ),
  },
])

const handleSearch = () => {
  loadStudents(currentSearchParams())
}

const handleReset = () => {
  nameQuery.value = ''
  studentNoQuery.value = ''
  courseCodeQuery.value = ''
  loadStudents()
}

onMounted(() => {
  loadStudents()
})
</script>
