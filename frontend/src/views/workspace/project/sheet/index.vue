<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ProjectSheetEditor from '../components/ProjectSheetEditor.vue'
import { useProjectDocsContext } from '@/composables/useProjectDocs'

const route = useRoute()
const {
  activeSheet,
  errorMessage,
  updateSheetSnapshot,
  updateSheetTitle,
} = useProjectDocsContext()

const editorReady = computed(() => {
  const sheet = activeSheet.value
  if (!sheet || String(sheet.id) !== String(route.params.nodeId)) return false
  return !sheet.loading && sheet.collab
})
</script>

<template>
  <ProjectSheetEditor
    v-if="editorReady"
    :key="`sheet-${activeSheet.id}`"
    :sheet-id="activeSheet.id"
    :title="activeSheet.title"
    :initial-snapshot="activeSheet.contentJson"
    :can-write="activeSheet.canWrite"
    :collab-session="activeSheet.collab"
    :update-date="activeSheet.updateDate"
    @snapshot="updateSheetSnapshot"
    @update:title="updateSheetTitle(activeSheet.id, $event)"
  />
  <div v-else-if="errorMessage" class="ps-doc-page-state ps-doc-page-state--error">
    {{ errorMessage }}
  </div>
  <div v-else class="ps-doc-page-state">
    加载表格…
  </div>
</template>
