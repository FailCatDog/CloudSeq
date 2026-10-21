<script setup>
import AppPromptDialog from './AppPromptDialog.vue'
import { appPromptState, finishAppPrompt } from '@/composables/appPrompt'

const handleConfirm = (value) => {
  finishAppPrompt(value)
}

const handleCancel = () => {
  finishAppPrompt(appPromptState.mode === 'confirm' ? false : null)
}
</script>

<template>
  <AppPromptDialog
    v-model:open="appPromptState.open"
    :mode="appPromptState.mode"
    :title="appPromptState.title"
    :message="appPromptState.message"
    :label="appPromptState.label"
    :default-value="appPromptState.defaultValue"
    :placeholder="appPromptState.placeholder"
    :confirm-label="appPromptState.confirmLabel"
    :cancel-label="appPromptState.cancelLabel"
    :confirm-danger="appPromptState.confirmDanger"
    :table-rows="appPromptState.tableRows"
    :table-cols="appPromptState.tableCols"
    :table-with-header-row="appPromptState.tableWithHeaderRow"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />
</template>
