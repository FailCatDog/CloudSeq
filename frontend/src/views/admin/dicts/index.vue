<template>
  <div class="console-page">
    <n-card class="console-panel" :bordered="false">
      <div class="console-filter">
        <div class="console-filter__fields">
          <div class="console-filter__field console-filter__field--wide console-filter__field--inline">
            <span class="console-filter__label">名称</span>
            <n-input
              v-model:value="keyword"
              clearable
              placeholder="编码或名称"
              @keydown.enter.prevent="handleSearch"
            />
          </div>
        </div>
        <div class="console-filter__actions">
          <n-button type="primary" :disabled="loading" @click="handleSearch">刷新</n-button>
          <n-button type="primary" secondary @click="openCreateKey">新建类型</n-button>
          <n-button quaternary :loading="cacheLoading" @click="reloadCache">重建缓存</n-button>
        </div>
      </div>
    </n-card>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <n-card class="console-panel console-panel--table" :bordered="false">
      <n-data-table
        :columns="keyColumns"
        :data="pagedKeys"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.id"
        :expanded-row-keys="expandedRowKeys"
        size="medium"
        @update:expanded-row-keys="handleExpandedChange"
      />
    </n-card>

    <n-card class="console-panel console-panel--pager" :bordered="false">
      <div class="console-pagination">
        <n-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :item-count="filteredKeys.length"
          :page-sizes="[10, 20, 50]"
          size="small"
          show-size-picker
          show-quick-jumper
        />
      </div>
    </n-card>

    <n-drawer v-model:show="keyFormOpen" :width="480" placement="right" :block-scroll="false">
      <n-drawer-content :title="keyFormTitle" closable>
        <n-form label-placement="top">
          <n-form-item label="类型编码">
            <n-input v-model:value="keyForm.keyCode" :disabled="keyFormMode === 'edit'" placeholder="如 user_role" />
          </n-form-item>
          <n-form-item label="类型名称">
            <n-input v-model:value="keyForm.keyName" placeholder="如 用户角色" />
          </n-form-item>
          <n-form-item label="备注">
            <n-input v-model:value="keyForm.remark" type="textarea" :rows="3" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space>
            <n-button @click="keyFormOpen = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="submitKeyForm">保存</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>

    <n-drawer v-model:show="valueFormOpen" :width="480" placement="right" :block-scroll="false">
      <n-drawer-content :title="valueFormTitle" closable>
        <n-form label-placement="top">
          <n-form-item label="值编码">
            <n-input v-model:value="valueForm.valueCode" :disabled="valueFormMode === 'edit'" placeholder="如 STUDENT" />
          </n-form-item>
          <n-form-item label="显示名称">
            <n-input v-model:value="valueForm.valueName" placeholder="如 学生" />
          </n-form-item>
          <n-form-item label="排序">
            <n-input-number v-model:value="valueForm.sort" :min="0" class="w-full" style="width: 100%" />
          </n-form-item>
          <n-form-item label="备注">
            <n-input v-model:value="valueForm.remark" type="textarea" :rows="3" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space>
            <n-button @click="valueFormOpen = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="submitValueForm">保存</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { NAlert, NButton, NDataTable, NEmpty, NSpace, NSpin } from 'naive-ui'
import {
  createAdminDictKeyApi,
  createAdminDictValueApi,
  deleteAdminDictKeyApi,
  deleteAdminDictValueApi,
  getAdminDictKeyApi,
  listAdminDictKeysApi,
  reloadAdminDictCacheApi,
  updateAdminDictKeyApi,
  updateAdminDictValueApi,
} from '@/api/admin'
import { clearDictStore, initDictStore } from '@/stores/dictStore'

const loading = ref(false)
const cacheLoading = ref(false)
const saving = ref(false)
const pageError = ref('')
const keyword = ref('')
const keys = ref([])
const page = ref(1)
const pageSize = ref(10)
const expandedRowKeys = ref([])
const valueState = ref({})

const keyFormOpen = ref(false)
const keyFormMode = ref('create')
const editingKeyId = ref(null)
const keyForm = reactive({
  keyCode: '',
  keyName: '',
  remark: '',
  version: null,
})

const valueFormOpen = ref(false)
const valueFormMode = ref('create')
const editingValueId = ref(null)
const activeKeyId = ref(null)
const valueForm = reactive({
  valueCode: '',
  valueName: '',
  remark: '',
  sort: 0,
  version: null,
})

const filteredKeys = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return keys.value
  return keys.value.filter((item) =>
    item.keyCode?.toLowerCase().includes(q)
    || item.keyName?.toLowerCase().includes(q),
  )
})

const pagedKeys = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredKeys.value.slice(start, start + pageSize.value)
})

watch([filteredKeys, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(filteredKeys.value.length / pageSize.value) || 1)
  if (page.value > maxPage) page.value = maxPage
})

watch(keyword, () => {
  page.value = 1
})

const keyFormTitle = computed(() => (keyFormMode.value === 'create' ? '新建字典类型' : '编辑字典类型'))
const valueFormTitle = computed(() => (valueFormMode.value === 'create' ? '新建字典项' : '编辑字典项'))

const getValueState = (keyId) => {
  if (!valueState.value[keyId]) {
    valueState.value[keyId] = { loading: false, loaded: false, error: '', values: [] }
  }
  return valueState.value[keyId]
}

const loadKeys = async () => {
  loading.value = true
  pageError.value = ''
  try {
    keys.value = await listAdminDictKeysApi()
    const alive = new Set(keys.value.map((item) => item.id))
    expandedRowKeys.value = expandedRowKeys.value.filter((id) => alive.has(id))
    for (const id of expandedRowKeys.value) {
      await loadValues(id, true)
    }
  } catch (error) {
    pageError.value = error?.message || '加载字典类型失败'
  } finally {
    loading.value = false
  }
}

const loadValues = async (keyId, force = false) => {
  const state = getValueState(keyId)
  if (!force && (state.loaded || state.loading)) return
  state.loading = true
  state.error = ''
  try {
    const detail = await getAdminDictKeyApi(keyId)
    state.values = detail?.values || []
    state.loaded = true
  } catch (error) {
    state.error = error?.message || '加载字典项失败'
    state.values = []
    state.loaded = false
  } finally {
    state.loading = false
  }
}

const handleSearch = async () => {
  await loadKeys()
}

const handleExpandedChange = async (nextKeys) => {
  const prev = new Set(expandedRowKeys.value)
  expandedRowKeys.value = nextKeys
  for (const id of nextKeys) {
    if (!prev.has(id)) await loadValues(id)
  }
}

const openCreateKey = () => {
  keyFormMode.value = 'create'
  editingKeyId.value = null
  keyForm.keyCode = ''
  keyForm.keyName = ''
  keyForm.remark = ''
  keyForm.version = null
  keyFormOpen.value = true
}

const openEditKey = (row) => {
  keyFormMode.value = 'edit'
  editingKeyId.value = row.id
  keyForm.keyCode = row.keyCode
  keyForm.keyName = row.keyName
  keyForm.remark = row.remark || ''
  keyForm.version = row.version
  keyFormOpen.value = true
}

const submitKeyForm = async () => {
  saving.value = true
  pageError.value = ''
  try {
    const payload = {
      keyCode: keyForm.keyCode,
      keyName: keyForm.keyName,
      remark: keyForm.remark || null,
      version: keyForm.version,
    }
    let createdId = null
    if (keyFormMode.value === 'create') {
      const created = await createAdminDictKeyApi(payload)
      createdId = created?.id
    } else {
      await updateAdminDictKeyApi(editingKeyId.value, payload)
    }
    keyFormOpen.value = false
    await loadKeys()
    if (createdId) {
      expandedRowKeys.value = [...new Set([...expandedRowKeys.value, createdId])]
      await loadValues(createdId, true)
    }
  } catch (error) {
    pageError.value = error?.message || '保存字典类型失败'
  } finally {
    saving.value = false
  }
}

const removeKey = async (row) => {
  if (!window.confirm(`确定删除字典类型「${row.keyName}」及其全部字典项？`)) return
  pageError.value = ''
  try {
    await deleteAdminDictKeyApi(row.id)
    expandedRowKeys.value = expandedRowKeys.value.filter((id) => id !== row.id)
    delete valueState.value[row.id]
    await loadKeys()
  } catch (error) {
    pageError.value = error?.message || '删除字典类型失败'
  }
}

const openCreateValue = (keyId) => {
  activeKeyId.value = keyId
  valueFormMode.value = 'create'
  editingValueId.value = null
  valueForm.valueCode = ''
  valueForm.valueName = ''
  valueForm.remark = ''
  valueForm.sort = 0
  valueForm.version = null
  valueFormOpen.value = true
}

const openEditValue = (keyId, row) => {
  activeKeyId.value = keyId
  valueFormMode.value = 'edit'
  editingValueId.value = row.id
  valueForm.valueCode = row.valueCode
  valueForm.valueName = row.valueName
  valueForm.remark = row.remark || ''
  valueForm.sort = row.sort ?? 0
  valueForm.version = row.version
  valueFormOpen.value = true
}

const submitValueForm = async () => {
  if (!activeKeyId.value) return
  saving.value = true
  pageError.value = ''
  try {
    const payload = {
      valueCode: valueForm.valueCode,
      valueName: valueForm.valueName,
      remark: valueForm.remark || null,
      sort: valueForm.sort,
      version: valueForm.version,
    }
    if (valueFormMode.value === 'create') {
      await createAdminDictValueApi(activeKeyId.value, payload)
    } else {
      await updateAdminDictValueApi(editingValueId.value, payload)
    }
    valueFormOpen.value = false
    await loadKeys()
    await loadValues(activeKeyId.value, true)
  } catch (error) {
    pageError.value = error?.message || '保存字典项失败'
  } finally {
    saving.value = false
  }
}

const removeValue = async (keyId, row) => {
  if (!window.confirm(`确定删除字典项「${row.valueName}」？`)) return
  pageError.value = ''
  try {
    await deleteAdminDictValueApi(row.id)
    await loadKeys()
    await loadValues(keyId, true)
  } catch (error) {
    pageError.value = error?.message || '删除字典项失败'
  }
}

const reloadCache = async () => {
  cacheLoading.value = true
  pageError.value = ''
  try {
    await reloadAdminDictCacheApi()
    clearDictStore()
    await initDictStore()
  } catch (error) {
    pageError.value = error?.message || '重建字典缓存失败'
  } finally {
    cacheLoading.value = false
  }
}

const valueColumns = (keyId) => [
  { title: '序号', key: 'index', width: 64, render: (_, index) => index + 1 },
  { title: '值编码', key: 'valueCode', width: 140 },
  { title: '显示名称', key: 'valueName', minWidth: 120 },
  { title: '排序', key: 'sort', width: 70 },
  { title: '备注', key: 'remark', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row) => h(NSpace, { size: 8 }, {
      default: () => [
        h(NButton, { size: 'small', secondary: true, onClick: () => openEditValue(keyId, row) }, { default: () => '编辑' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          secondary: true,
          onClick: () => removeValue(keyId, row),
        }, { default: () => '删除' }),
      ],
    }),
  },
]

const renderExpand = (row) => {
  const state = getValueState(row.id)
  if (state.error) {
    return h(NAlert, { type: 'error', bordered: false }, { default: () => state.error })
  }
  if (state.loading) {
    return h('div', { class: 'dict-expand-loading' }, [h(NSpin, { size: 'small' }), ' 加载字典项…'])
  }
  return h('div', { class: 'dict-expand' }, [
    h('div', { class: 'dict-expand__toolbar' }, [
      h(NButton, {
        size: 'small',
        type: 'primary',
        secondary: true,
        onClick: () => openCreateValue(row.id),
      }, { default: () => '新建字典项' }),
    ]),
    state.values.length
      ? h(NDataTable, {
        columns: valueColumns(row.id),
        data: state.values,
        bordered: false,
        size: 'small',
        rowKey: (item) => item.id,
      })
      : h(NEmpty, { description: '暂无字典项', size: 'small' }),
  ])
}

const keyColumns = computed(() => [
  { type: 'expand', expandable: () => true, renderExpand },
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (_, index) => (page.value - 1) * pageSize.value + index + 1,
  },
  { title: '编码', key: 'keyCode', minWidth: 160 },
  { title: '名称', key: 'keyName', minWidth: 140 },
  { title: '项数', key: 'valueCount', width: 80 },
  { title: '备注', key: 'remark', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row) => h(NSpace, { size: 8 }, {
      default: () => [
        h(NButton, { size: 'small', secondary: true, onClick: () => openEditKey(row) }, { default: () => '编辑' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          secondary: true,
          onClick: () => removeKey(row),
        }, { default: () => '删除' }),
      ],
    }),
  },
])

onMounted(loadKeys)
</script>

<style scoped>
.dict-expand {
  padding: 4px 8px 12px;
}

.dict-expand__toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.dict-expand-loading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  color: var(--wb-text-secondary);
  font-size: 13px;
}
</style>
