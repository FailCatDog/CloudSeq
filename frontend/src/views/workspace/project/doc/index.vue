<script setup>
import { computed, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import ProjectDocEditor from '../components/ProjectDocEditor.vue'
import { useProjectDocsContext } from '@/composables/useProjectDocs'

const route = useRoute()
const { activeDoc, errorMessage, updateDocSnapshot, updateDocTitle, clearActiveDoc } =
  useProjectDocsContext()

const editorReady = computed(() => {
  const doc = activeDoc.value
  if (!doc || String(doc.id) !== String(route.params.nodeId)) return false
  return !doc.loading && doc.collab
})

onBeforeUnmount(() => {
  clearActiveDoc()
})
</script>

<template>
  <ProjectDocEditor
    v-if="editorReady"
    :key="`doc-${activeDoc.id}`"
    :doc-id="activeDoc.id"
    :title="activeDoc.title"
    :initial-content="activeDoc.content"
    :can-write="activeDoc.canWrite"
    :collab-session="activeDoc.collab"
    :update-date="activeDoc.updateDate"
    @snapshot="updateDocSnapshot"
    @update:title="updateDocTitle(activeDoc.id, $event)"
  />
  <div v-else-if="errorMessage" class="ps-doc-page-state ps-doc-page-state--error">
    {{ errorMessage }}
  </div>
  <div v-else class="ps-doc-page-state">
    加载文档…
  </div>
</template>
