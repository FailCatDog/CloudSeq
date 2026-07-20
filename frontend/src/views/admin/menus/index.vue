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
              placeholder="名称、path 或 perms"
              @keydown.enter.prevent="handleRefresh"
            />
          </div>
        </div>
        <div class="console-filter__actions">
          <n-button type="primary" :disabled="loading" @click="handleRefresh">刷新</n-button>
          <n-button type="primary" secondary @click="openCreate">新增</n-button>
        </div>
      </div>
    </n-card>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <n-card class="console-panel console-panel--table" :bordered="false">
      <n-data-table
        :columns="columns"
        :data="pagedMenuTree"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.id"
        default-expand-all
        children-key="children"
        size="medium"
      />
    </n-card>

    <n-card class="console-panel console-panel--pager" :bordered="false">
      <div class="console-pagination">
        <n-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :item-count="filteredRoots.length"
          :page-sizes="[10, 20, 50]"
          size="small"
          show-size-picker
          show-quick-jumper
        />
      </div>
    </n-card>

    <MenuFormDialog
      v-model:show="formOpen"
      :mode="formMode"
      :saving="saving"
      :menu-tree="menuTree"
      :editing-id="editingId"
      :initial-value="formInitial"
      @submit="submitForm"
    />
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref, watch } from 'vue'
import { NButton, NSpace, NTag } from 'naive-ui'
import {
  createAdminMenuApi,
  deleteAdminMenuApi,
  listAdminMenuTreeApi,
  updateAdminMenuApi,
  updateAdminMenuStatusApi,
} from '@/api/admin'
import { useConsoleConfirm } from '@/composables/useConsoleConfirm'
import { useDict } from '@/composables/useDict'
import { CacheCode } from '@/constants/cacheCode'
import MenuFormDialog from './MenuFormDialog.vue'

const menuTypeDict = useDict(CacheCode.MENU_TYPE)
const sysStatusDict = useDict(CacheCode.SYS_STATUS)
const { confirm } = useConsoleConfirm()

const loading = ref(false)
const saving = ref(false)
const pageError = ref('')
const keyword = ref('')
const menuTree = ref([])
const page = ref(1)
const pageSize = ref(10)

const formOpen = ref(false)
const formMode = ref('create')
const editingId = ref(null)
const formInitial = ref(null)

const nodeMatches = (node, q) => {
  if (!q) return true
  return [node.menuName, node.path, node.perms]
    .some((text) => text?.toLowerCase().includes(q))
}

const filterTree = (nodes, q) => {
  if (!q) return nodes
  return nodes
    .map((node) => {
      const children = filterTree(node.children || [], q)
      if (nodeMatches(node, q) || children.length) {
        return { ...node, children }
      }
      return null
    })
    .filter(Boolean)
}

const filteredRoots = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return filterTree(menuTree.value, q)
})

const pagedMenuTree = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredRoots.value.slice(start, start + pageSize.value)
})

watch([filteredRoots, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(filteredRoots.value.length / pageSize.value) || 1)
  if (page.value > maxPage) page.value = maxPage
})

watch(keyword, () => {
  page.value = 1
})

const loadTree = async () => {
  loading.value = true
  pageError.value = ''
  try {
    menuTree.value = await listAdminMenuTreeApi()
  } catch (error) {
    pageError.value = error?.message || '加载菜单失败'
  } finally {
    loading.value = false
  }
}

const handleRefresh = async () => {
  page.value = 1
  await loadTree()
}

const openCreate = () => {
  formMode.value = 'create'
  editingId.value = null
  formInitial.value = {
    parentId: 0,
    menuType: CacheCode.MENU_TYPE_MENU,
    routeMatch: CacheCode.ROUTE_MATCH_EXACT,
    visible: CacheCode.MENU_VISIBLE_SHOW,
    status: CacheCode.SYS_STATUS_ACTIVE,
    sort: 0,
  }
  formOpen.value = true
}

const openCreateButton = (row) => {
  formMode.value = 'createButton'
  editingId.value = null
  formInitial.value = {
    parentId: row.id,
    menuType: CacheCode.MENU_TYPE_BUTTON,
    visible: CacheCode.MENU_VISIBLE_HIDE,
    status: CacheCode.SYS_STATUS_ACTIVE,
    sort: 0,
  }
  formOpen.value = true
}

const openEdit = (row) => {
  formMode.value = 'edit'
  editingId.value = row.id
  formInitial.value = {
    parentId: row.parentId ?? 0,
    menuName: row.menuName,
    menuType: row.menuType,
    path: row.path || '',
    routeMatch: row.routeMatch || CacheCode.ROUTE_MATCH_EXACT,
    component: row.component || '',
    perms: row.perms || '',
    apiMethod: row.apiMethod || '',
    apiPath: row.apiPath || '',
    icon: row.icon || '',
    sort: row.sort ?? 0,
    visible: row.visible || CacheCode.MENU_VISIBLE_SHOW,
    status: row.status || CacheCode.SYS_STATUS_ACTIVE,
    remark: row.remark || '',
    version: row.version,
  }
  formOpen.value = true
}

const submitForm = async (payload) => {
  saving.value = true
  pageError.value = ''
  try {
    if (formMode.value === 'create' || formMode.value === 'createButton') {
      await createAdminMenuApi(payload)
    } else {
      await updateAdminMenuApi(editingId.value, payload)
    }
    formOpen.value = false
    await loadTree()
  } catch (error) {
    pageError.value = error?.message || '保存失败'
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  const next = row.status === CacheCode.SYS_STATUS_ACTIVE
    ? CacheCode.SYS_STATUS_DISABLED
    : CacheCode.SYS_STATUS_ACTIVE
  try {
    await updateAdminMenuStatusApi(row.id, next)
    await loadTree()
  } catch (error) {
    pageError.value = error?.message || '更新状态失败'
  }
}

const handleDelete = async (row) => {
  const ok = await confirm('确认删除该菜单？存在子菜单时将无法删除。')
  if (!ok) return
  try {
    await deleteAdminMenuApi(row.id)
    await loadTree()
  } catch (error) {
    pageError.value = error?.message || '删除失败'
  }
}

const columns = computed(() => [
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (row) => {
      const rootIndex = pagedMenuTree.value.findIndex((item) => item.id === row.id)
      if (rootIndex < 0) return ''
      return (page.value - 1) * pageSize.value + rootIndex + 1
    },
  },
  { title: '名称', key: 'menuName', minWidth: 160 },
  {
    title: '类型',
    key: 'menuType',
    width: 80,
    render: (row) => menuTypeDict.label(row.menuType),
  },
  { title: 'path', key: 'path', ellipsis: { tooltip: true } },
  { title: 'perms', key: 'perms', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => h(
      NTag,
      { type: row.status === CacheCode.SYS_STATUS_ACTIVE ? 'success' : 'default', round: true, size: 'small' },
      { default: () => sysStatusDict.label(row.status) },
    ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    render: (row) => {
      const actions = [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
      ]
      if (row.menuType === CacheCode.MENU_TYPE_MENU) {
        actions.push(
          h(NButton, { size: 'small', secondary: true, onClick: () => openCreateButton(row) }, {
            default: () => '新增按钮',
          }),
        )
      }
      actions.push(
        h(NButton, { size: 'small', quaternary: true, onClick: () => toggleStatus(row) }, {
          default: () => (row.status === CacheCode.SYS_STATUS_ACTIVE ? '禁用' : '启用'),
        }),
        h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      )
      return h(NSpace, { size: 8 }, { default: () => actions })
    },
  },
])

onMounted(loadTree)
</script>
