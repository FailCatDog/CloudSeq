<template>
  <div ref="rootRef" class="tch-course-select-wrap">
    <button
      type="button"
      class="tch-course-select"
      :aria-expanded="open"
      aria-haspopup="listbox"
      @click="toggleOpen"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
      </svg>
      <span class="tch-course-select__label">{{ selectLabel }}</span>
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>

    <div v-if="open" class="tch-course-select-menu" role="listbox">
      <p v-if="loading" class="tch-course-select-menu__hint">加载中…</p>
      <p v-else-if="!courses.length" class="tch-course-select-menu__hint">暂无课号</p>
      <button
        v-for="item in courses"
        :key="item.id"
        type="button"
        role="option"
        class="tch-course-select-option"
        :class="{ active: item.id === selectedCourseId }"
        :aria-selected="item.id === selectedCourseId"
        @click="handleSelect(item.id)"
      >
        <span class="tch-course-select-option__title">{{ formatCourseSelectLabel(item) }}</span>
        <span class="tch-course-select-option__code">{{ item.courseCode }}</span>
      </button>
      <RouterLink to="/teaching/courses" class="tch-course-select-manage" @click="open = false">
        管理课号 →
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { loadCourseStore, selectCourse, useCourseStore } from '@/stores/courseStore'
import { formatCourseSelectLabel } from '@/utils/courseFormat'

const rootRef = ref(null)
const open = ref(false)

const { courses, selectedCourseId, selectedCourse, loading } = useCourseStore()

const selectLabel = computed(() => {
  if (loading.value) return '加载课号…'
  return formatCourseSelectLabel(selectedCourse.value)
})

const toggleOpen = () => {
  open.value = !open.value
}

const handleSelect = (courseId) => {
  selectCourse(courseId)
  open.value = false
}

const handleClickOutside = (event) => {
  if (rootRef.value && !rootRef.value.contains(event.target)) {
    open.value = false
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  try {
    await loadCourseStore()
  } catch {
    // 顶部选择器静默失败，管理页会展示错误
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
