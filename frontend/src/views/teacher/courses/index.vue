<template>
  <div class="console-page">
    <div class="console-filter">
      <div class="console-filter__fields">
        <div class="console-filter__field console-filter__field--wide">
          <span class="console-filter__label">关键字</span>
          <n-input
            v-model:value="searchQuery"
            clearable
            placeholder="搜索课号、课程名…"
            @keydown.enter.prevent="handleSearch"
          />
        </div>
        <div class="console-filter__field">
          <span class="console-filter__label">课号状态</span>
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
        <n-button type="primary" secondary @click="openCreate">新建课号</n-button>
      </div>
    </div>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <n-card class="console-table-card" :bordered="false">
      <n-data-table
        :columns="columns"
        :data="filteredCourses"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.id"
        size="medium"
      />
    </n-card>

    <n-drawer v-model:show="formOpen" :width="560" placement="right">
      <n-drawer-content :title="formTitle" closable>
        <CourseFormPanel
          :mode="formMode"
          :course="editingCourse"
          @saved="handleSaved"
          @cancel="closeForm"
        />
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { NButton, NSpace, NTag } from 'naive-ui'
import { deleteCourseApi, getCourseByIdApi } from '@/api/course'
import { useConsoleConfirm } from '@/composables/useConsoleConfirm'
import { useDict } from '@/composables/useDict'
import { CacheCode } from '@/constants/cacheCode'
import { refreshCourseStore, useCourseStore } from '@/stores/courseStore'
import { formatTermLabel } from '@/utils/courseFormat'
import { courseTagType } from '@/utils/naiveStatus'
import CourseFormPanel from './components/CourseFormPanel.vue'

const { courses, loading } = useCourseStore()
const { label: courseStatusLabel } = useDict(CacheCode.COURSE_STATUS)
const { confirm } = useConsoleConfirm()

const searchQuery = ref('')
const activeStatus = ref('all')
const appliedKeyword = ref('')
const appliedStatus = ref('all')
const pageError = ref('')
const formOpen = ref(false)
const formMode = ref('create')
const editingCourse = ref(null)

const formTitle = computed(() => {
  if (formMode.value === 'create') return '新建课号'
  if (formMode.value === 'view') return '课号详情'
  return '编辑课号'
})

const statusOptions = computed(() => [
  { id: 'all', label: '全部状态' },
  { id: CacheCode.COURSE_STATUS_ACTIVE, label: courseStatusLabel(CacheCode.COURSE_STATUS_ACTIVE) },
  { id: CacheCode.COURSE_STATUS_DRAFT, label: courseStatusLabel(CacheCode.COURSE_STATUS_DRAFT) },
  { id: CacheCode.COURSE_STATUS_ARCHIVED, label: courseStatusLabel(CacheCode.COURSE_STATUS_ARCHIVED) },
])

const filteredCourses = computed(() => {
  const keyword = appliedKeyword.value.toLowerCase()
  return courses.value.filter((item) => {
    const statusMatch = appliedStatus.value === 'all' || item.status === appliedStatus.value
    if (!statusMatch) return false
    if (!keyword) return true
    return (
      String(item.courseCode || '').toLowerCase().includes(keyword) ||
      String(item.courseName || '').toLowerCase().includes(keyword)
    )
  })
})

const formatDateTime = (value) => {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

const columns = computed(() => [
  { title: '序号', key: 'index', width: 64, render: (_, index) => index + 1 },
  {
    title: '课号',
    key: 'courseCode',
    width: 120,
    render: (row) => h('strong', null, row.courseCode),
  },
  { title: '课程名称', key: 'courseName', ellipsis: { tooltip: true } },
  {
    title: '学期',
    key: 'term',
    width: 140,
    render: (row) => formatTermLabel(row.termYear, row.termSeason),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: courseTagType(row.status), round: true, size: 'small' },
        { default: () => courseStatusLabel(row.status) },
      ),
  },
  {
    title: '选题截止',
    key: 'topicDeadline',
    width: 150,
    render: (row) => formatDateTime(row.topicDeadline),
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) =>
      h(
        NSpace,
        { size: 8 },
        {
          default: () => [
            h(NButton, { text: true, type: 'primary', onClick: () => openDetail(row) }, { default: () => '详情' }),
            h(NButton, { text: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
            h(
              NButton,
              { text: true, type: 'error', onClick: () => handleDelete(row) },
              { default: () => '删除' },
            ),
          ],
        },
      ),
  },
])

const loadPage = async () => {
  pageError.value = ''
  try {
    await refreshCourseStore()
  } catch (error) {
    pageError.value = error?.message || '加载课号列表失败'
  }
}

onMounted(() => {
  handleSearch()
})

const handleSearch = async () => {
  appliedKeyword.value = searchQuery.value.trim()
  appliedStatus.value = activeStatus.value
  pageError.value = ''
  closeForm()
  await loadPage()
}

const handleReset = async () => {
  searchQuery.value = ''
  activeStatus.value = 'all'
  appliedKeyword.value = ''
  appliedStatus.value = 'all'
  pageError.value = ''
  closeForm()
  await loadPage()
}

const openCreate = () => {
  formMode.value = 'create'
  editingCourse.value = null
  formOpen.value = true
}

const openCourseForm = async (course, mode) => {
  formMode.value = mode
  pageError.value = ''
  try {
    editingCourse.value = await getCourseByIdApi(course.id)
    formOpen.value = true
  } catch (error) {
    pageError.value = error?.message || '加载课号详情失败'
  }
}

const openDetail = (course) => openCourseForm(course, 'view')
const openEdit = (course) => openCourseForm(course, 'edit')

const closeForm = () => {
  formOpen.value = false
  editingCourse.value = null
}

const handleSaved = async () => {
  closeForm()
  await loadPage()
}

const handleDelete = async (course) => {
  const confirmed = await confirm({
    title: '确认删除',
    message: `确定删除课号「${course.courseCode}」吗？删除后不可恢复。`,
    danger: true,
    confirmLabel: '删除',
  })
  if (!confirmed) return

  pageError.value = ''
  try {
    await deleteCourseApi(course.id)
    await loadPage()
  } catch (error) {
    pageError.value = error?.message || '删除课号失败'
  }
}
</script>
