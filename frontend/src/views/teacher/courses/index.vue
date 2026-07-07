<template>
  <div class="tch-page">
    <div class="tch-filter-bar">
      <div class="tch-search">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input v-model="searchQuery" type="search" placeholder="搜索课号、课程名…" />
      </div>
      <div class="tch-filter-tabs" role="tablist" aria-label="课号状态">
        <button
          v-for="tab in statusTabs"
          :key="tab.id"
          type="button"
          role="tab"
          class="tch-filter-tab"
          :class="{ active: activeStatus === tab.id }"
          :aria-selected="activeStatus === tab.id"
          @click="activeStatus = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="tch-filter-tabs tch-filter-tabs--end">
        <button type="button" class="wb-btn-schedule wb-btn-schedule--sm" @click="openCreate">
          新建课号
        </button>
      </div>
    </div>

    <p v-if="pageError" class="tch-page-error">{{ pageError }}</p>

    <div class="tch-table-surface">
      <div class="tch-table-wrap tch-table-wrap--flush">
        <table class="tch-table">
          <thead>
            <tr>
              <th>课号</th>
              <th>课程名称</th>
              <th>学期</th>
              <th>状态</th>
              <th>选题截止</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="tch-table-empty">加载中…</td>
            </tr>
            <tr v-for="item in filteredCourses" v-else :key="item.id">
              <td><strong>{{ item.courseCode }}</strong></td>
              <td>{{ item.courseName }}</td>
              <td>{{ formatTermLabel(item.termYear, item.termSeason) }}</td>
              <td>
                <span class="tch-tag" :class="courseStatusTagClass(item.status)">
                  {{ courseStatusLabel(item.status) }}
                </span>
              </td>
              <td>{{ formatDateTime(item.topicDeadline) }}</td>
              <td class="tch-table-actions">
                <button type="button" class="tch-table-link tch-table-link--btn" @click="openEdit(item)">
                  编辑
                </button>
              </td>
            </tr>
            <tr v-if="!loading && !filteredCourses.length">
              <td colspan="6" class="tch-table-empty">
                {{ courses.length ? '没有匹配的课号' : '暂无课号，点击「新建课号」创建' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="formOpen" class="tch-drawer-backdrop" @click.self="closeForm">
        <aside class="tch-drawer tch-drawer--wide" role="dialog" aria-modal="true" :aria-labelledby="formTitleId">
          <header class="tch-drawer-header">
            <h3 :id="formTitleId">{{ formMode === 'create' ? '新建课号' : '编辑课号' }}</h3>
            <button type="button" class="tch-drawer-close" aria-label="关闭" @click="closeForm">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </header>
          <div class="tch-drawer-body">
            <CourseFormPanel
              :mode="formMode"
              :course="editingCourse"
              @saved="handleSaved"
              @cancel="closeForm"
            />
          </div>
        </aside>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getCourseByIdApi } from '@/api/course'
import { useDict } from '@/composables/useDict'
import { CacheCode } from '@/constants/cacheCode'
import { refreshCourseStore, useCourseStore } from '@/stores/courseStore'
import { courseStatusTagClass, formatTermLabel } from '@/utils/courseFormat'
import CourseFormPanel from './components/CourseFormPanel.vue'

const { courses, loading } = useCourseStore()
const { label: courseStatusLabel } = useDict(CacheCode.COURSE_STATUS)

const searchQuery = ref('')
const activeStatus = ref('all')
const pageError = ref('')
const formOpen = ref(false)
const formMode = ref('create')
const editingCourse = ref(null)

const formTitleId = 'course-form-title'

const statusTabs = computed(() => {
  const draft = courses.value.filter((item) => item.status === CacheCode.COURSE_STATUS_DRAFT).length
  const active = courses.value.filter((item) => item.status === CacheCode.COURSE_STATUS_ACTIVE).length
  const archived = courses.value.filter((item) => item.status === CacheCode.COURSE_STATUS_ARCHIVED).length
  return [
    { id: 'all', label: `全部（${courses.value.length}）` },
    { id: CacheCode.COURSE_STATUS_ACTIVE, label: `进行中（${active}）` },
    { id: CacheCode.COURSE_STATUS_DRAFT, label: `草稿（${draft}）` },
    { id: CacheCode.COURSE_STATUS_ARCHIVED, label: `已归档（${archived}）` },
  ]
})

const filteredCourses = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  return courses.value.filter((item) => {
    const statusMatch = activeStatus.value === 'all' || item.status === activeStatus.value
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

const loadPage = async () => {
  pageError.value = ''
  try {
    await refreshCourseStore()
  } catch (error) {
    pageError.value = error?.message || '加载课号列表失败'
  }
}

onMounted(() => {
  loadPage()
})

const openCreate = () => {
  formMode.value = 'create'
  editingCourse.value = null
  formOpen.value = true
}

const openEdit = async (course) => {
  formMode.value = 'edit'
  pageError.value = ''
  try {
    editingCourse.value = await getCourseByIdApi(course.id)
    formOpen.value = true
  } catch (error) {
    pageError.value = error?.message || '加载课号详情失败'
  }
}

const closeForm = () => {
  formOpen.value = false
  editingCourse.value = null
}

const handleSaved = async () => {
  closeForm()
  await loadPage()
}
</script>
