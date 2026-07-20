<template>
  <div class="console-page">
    <n-card class="console-panel" :bordered="false">
      <div class="console-filter">
        <div class="console-filter__fields">
          <div class="console-filter__field console-filter__field--inline">
            <span class="console-filter__label">状态</span>
            <n-select v-model:value="statusFilter" :options="statusOptions" />
          </div>
        </div>
        <div class="console-filter__actions">
          <n-button type="primary" :disabled="loading" @click="handleRefresh">刷新</n-button>
          <n-button type="primary" secondary @click="openCreate">新建角色</n-button>
        </div>
      </div>
    </n-card>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <n-card class="console-panel console-panel--table" :bordered="false">
      <n-data-table
        :columns="columns"
        :data="pagedRoles"
        :loading="loading"
        :bordered="false"
        :single-line="false"
        :row-key="(row) => row.id"
        size="medium"
      />
    </n-card>

    <n-card class="console-panel console-panel--pager" :bordered="false">
      <div class="console-pagination">
        <n-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :item-count="roles.length"
          :page-sizes="[10, 20, 50]"
          size="small"
          show-size-picker
          show-quick-jumper
        />
      </div>
    </n-card>

    <n-drawer v-model:show="formOpen" :width="520" placement="right" :block-scroll="false">
      <n-drawer-content :title="formTitle" closable>
        <n-form label-placement="top">
          <n-form-item label="角色标识">
            <n-input v-model:value="form.roleKey" :disabled="formMode === 'edit'" placeholder="如 STUDENT" />
          </n-form-item>
          <n-form-item label="角色名称">
            <n-input v-model:value="form.roleName" placeholder="如 学生" />
          </n-form-item>
          <n-form-item label="排序">
            <n-input-number v-model:value="form.roleSort" :min="0" class="w-full" />
          </n-form-item>
          <n-form-item label="数据范围">
            <n-select v-model:value="form.dataScope" :options="dataScopeOptions" />
          </n-form-item>
          <n-form-item label="登录首页">
            <n-input v-model:value="form.homePath" placeholder="/workspace/dashboard" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select v-model:value="form.status" :options="sysStatusDict.options.value" />
          </n-form-item>
          <n-form-item label="备注">
            <n-input v-model:value="form.remark" type="textarea" :rows="3" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space>
            <n-button @click="formOpen = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="submitForm">保存</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>

    <n-drawer v-model:show="menuDrawerOpen" :width="480" placement="right" :block-scroll="false">
      <n-drawer-content :title="`菜单授权 · ${assignRole?.roleName || ''}`" closable>
        <n-spin :show="menuLoading">
          <n-tree
            v-if="menuTreeData.length"
            block-line
            cascade
            checkable
            :checked-keys="checkedMenuIds"
            :data="menuTreeData"
            @update:checked-keys="checkedMenuIds = $event"
          />
          <n-empty v-else description="暂无菜单数据" />
        </n-spin>
        <template #footer>
          <n-space>
            <n-button @click="menuDrawerOpen = false">取消</n-button>
            <n-button type="primary" :loading="menuSaving" @click="saveRoleMenus">保存授权</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NSpace, NTag } from 'naive-ui'
import {
  createAdminRoleApi,
  listAdminMenuTreeApi,
  listAdminRolesApi,
  listRoleMenuIdsApi,
  replaceRoleMenuIdsApi,
  updateAdminRoleApi,
  updateAdminRoleStatusApi,
} from '@/api/admin'
import { useDict } from '@/composables/useDict'
import { CacheCode } from '@/constants/cacheCode'
import { toMenuTreeData } from '@/utils/adminMenuTree'

const sysStatusDict = useDict(CacheCode.SYS_STATUS)
const dataScopeDict = useDict(CacheCode.DATA_SCOPE)

const loading = ref(false)
const saving = ref(false)
const pageError = ref('')
const roles = ref([])
const statusFilter = ref('')
const page = ref(1)
const pageSize = ref(10)

const formOpen = ref(false)
const formMode = ref('create')
const editingId = ref(null)
const form = reactive({
  roleKey: '',
  roleName: '',
  roleSort: 0,
  dataScope: CacheCode.DATA_SCOPE_SELF,
  homePath: '',
  status: CacheCode.SYS_STATUS_ACTIVE,
  remark: '',
  version: null,
})

const menuDrawerOpen = ref(false)
const menuLoading = ref(false)
const menuSaving = ref(false)
const assignRole = ref(null)
const menuTreeData = ref([])
const checkedMenuIds = ref([])

const formTitle = computed(() => (formMode.value === 'create' ? '新建角色' : '编辑角色'))

const statusOptions = computed(() => [
  { label: '全部状态', value: '' },
  ...sysStatusDict.options.value.map((item) => ({ label: item.label, value: item.value })),
])

const dataScopeOptions = computed(() =>
  dataScopeDict.options.value.map((item) => ({ label: item.label, value: item.value })),
)

const pagedRoles = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return roles.value.slice(start, start + pageSize.value)
})

watch([roles, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(roles.value.length / pageSize.value) || 1)
  if (page.value > maxPage) page.value = maxPage
})

const loadRoles = async () => {
  loading.value = true
  pageError.value = ''
  try {
    roles.value = await listAdminRolesApi({
      status: statusFilter.value || undefined,
    })
  } catch (error) {
    pageError.value = error?.message || '加载角色失败'
  } finally {
    loading.value = false
  }
}

const handleRefresh = async () => {
  page.value = 1
  await loadRoles()
}

const resetForm = () => {
  form.roleKey = ''
  form.roleName = ''
  form.roleSort = 0
  form.dataScope = CacheCode.DATA_SCOPE_SELF
  form.homePath = ''
  form.status = CacheCode.SYS_STATUS_ACTIVE
  form.remark = ''
  form.version = null
}

const openCreate = () => {
  formMode.value = 'create'
  editingId.value = null
  resetForm()
  formOpen.value = true
}

const openEdit = (row) => {
  formMode.value = 'edit'
  editingId.value = row.id
  form.roleKey = row.roleKey
  form.roleName = row.roleName
  form.roleSort = row.roleSort ?? 0
  form.dataScope = row.dataScope
  form.homePath = row.homePath || ''
  form.status = row.status || CacheCode.SYS_STATUS_ACTIVE
  form.remark = row.remark || ''
  form.version = row.version
  formOpen.value = true
}

const submitForm = async () => {
  saving.value = true
  pageError.value = ''
  try {
    const payload = {
      roleKey: form.roleKey,
      roleName: form.roleName,
      roleSort: form.roleSort,
      dataScope: form.dataScope,
      homePath: form.homePath || null,
      status: form.status,
      remark: form.remark || null,
      version: form.version,
    }
    if (formMode.value === 'create') {
      await createAdminRoleApi(payload)
    } else {
      await updateAdminRoleApi(editingId.value, payload)
    }
    formOpen.value = false
    await loadRoles()
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
    await updateAdminRoleStatusApi(row.id, next)
    await loadRoles()
  } catch (error) {
    pageError.value = error?.message || '更新状态失败'
  }
}

const openMenuAssign = async (row) => {
  assignRole.value = row
  menuDrawerOpen.value = true
  menuLoading.value = true
  checkedMenuIds.value = []
  try {
    const [tree, menuIds] = await Promise.all([
      listAdminMenuTreeApi(),
      listRoleMenuIdsApi(row.id),
    ])
    menuTreeData.value = toMenuTreeData(tree)
    checkedMenuIds.value = menuIds || []
  } catch (error) {
    pageError.value = error?.message || '加载菜单授权失败'
  } finally {
    menuLoading.value = false
  }
}

const saveRoleMenus = async () => {
  if (!assignRole.value) return
  menuSaving.value = true
  try {
    await replaceRoleMenuIdsApi(assignRole.value.id, checkedMenuIds.value)
    menuDrawerOpen.value = false
  } catch (error) {
    pageError.value = error?.message || '保存菜单授权失败'
  } finally {
    menuSaving.value = false
  }
}

const columns = computed(() => [
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (_, index) => (page.value - 1) * pageSize.value + index + 1,
  },
  { title: '角色标识', key: 'roleKey', width: 120 },
  { title: '角色名称', key: 'roleName', width: 120 },
  {
    title: '数据范围',
    key: 'dataScope',
    width: 100,
    render: (row) => dataScopeDict.label(row.dataScope),
  },
  { title: '首页', key: 'homePath', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) => h(
      NTag,
      { type: row.status === CacheCode.SYS_STATUS_ACTIVE ? 'success' : 'default', round: true, size: 'small' },
      { default: () => sysStatusDict.label(row.status) },
    ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row) => h(NSpace, { size: 8 }, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', secondary: true, onClick: () => openMenuAssign(row) }, { default: () => '菜单授权' }),
        h(NButton, { size: 'small', quaternary: true, onClick: () => toggleStatus(row) }, {
          default: () => (row.status === CacheCode.SYS_STATUS_ACTIVE ? '禁用' : '启用'),
        }),
      ],
    }),
  },
])

onMounted(loadRoles)
</script>

<style scoped>
.w-full {
  width: 100%;
}
</style>
