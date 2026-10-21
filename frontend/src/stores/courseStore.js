import { computed, reactive } from 'vue'
import { listMyCoursesApi } from '@/api/course'

const STORAGE_KEY = 'teaching_selected_course_id'

const state = reactive({
  loaded: false,
  loading: false,
  error: null,
  courses: [],
  selectedCourseId: null,
})

let loadPromise = null

const persistSelection = (courseId) => {
  if (courseId == null || courseId === '') {
    localStorage.removeItem(STORAGE_KEY)
    return
  }
  localStorage.setItem(STORAGE_KEY, courseId)
}

const resolveSelection = () => {
  const savedId = localStorage.getItem(STORAGE_KEY)
  if (savedId && state.courses.some((item) => item.id === savedId)) {
    state.selectedCourseId = savedId
    return
  }
  state.selectedCourseId = state.courses[0]?.id ?? null
  persistSelection(state.selectedCourseId)
}

export const loadCourseStore = async ({ force = false } = {}) => {
  if (!force && state.loaded) return state.courses
  if (!force && loadPromise) return loadPromise

  loadPromise = (async () => {
    state.loading = true
    state.error = null
    try {
      const list = await listMyCoursesApi()
      state.courses = Array.isArray(list) ? list : []
      resolveSelection()
      state.loaded = true
      return state.courses
    } catch (error) {
      state.error = error
      loadPromise = null
      throw error
    } finally {
      state.loading = false
    }
  })()

  return loadPromise
}

export const refreshCourseStore = () => loadCourseStore({ force: true })

export const selectCourse = (courseId) => {
  state.selectedCourseId = courseId ?? null
  persistSelection(state.selectedCourseId)
}

export const clearCourseStore = () => {
  state.loaded = false
  state.loading = false
  state.error = null
  state.courses = []
  state.selectedCourseId = null
  loadPromise = null
  localStorage.removeItem(STORAGE_KEY)
}

export const useCourseStore = () => {
  const selectedCourse = computed(
    () => state.courses.find((item) => item.id === state.selectedCourseId) || null,
  )

  return {
    loaded: computed(() => state.loaded),
    loading: computed(() => state.loading),
    error: computed(() => state.error),
    courses: computed(() => state.courses),
    selectedCourseId: computed(() => state.selectedCourseId),
    selectedCourse,
    loadCourseStore,
    refreshCourseStore,
    selectCourse,
    clearCourseStore,
  }
}
