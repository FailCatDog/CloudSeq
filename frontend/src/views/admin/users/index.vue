<template>
  <div class="console-page">
    <n-card class="console-panel" :bordered="false">
      <div class="console-filter">
        <div class="console-filter__fields">
          <div class="console-filter__field console-filter__field--wide console-filter__field--inline">
            <span class="console-filter__label">关键字</span>
            <n-input
              v-model:value="keyword"
              clearable
              placeholder="用户名、姓名、学号"
              @keydown.enter.prevent="handleSearch"
            />
          </div>
          <div class="console-filter__field console-filter__field--inline">
            <span class="console-filter__label">角色</span>
            <n-select v-model:value="roleKey" :options="roleFilterOptions" clearable />
          </div>
        </div>
        <div class="console-filter__actions">
          <n-button type="primary" :disabled="loading" @click="handleSearch">搜索</n-button>
          <n-button quaternary :disabled="loading" @click="handleReset">重置</n-button>
        </div>
      </div>
    </n-card>

    <n-alert v-if="pageError" type="error" :bordered="false">{{ pageError }}</n-alert>

    <n-card class="console-panel console-panel--table" :bordered="false">
      <n-data-table
        :columns="columns"
        :data="pagedUsers"
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
          :item-count="users.length"
          :page-sizes="[10, 20, 50]"
          size="small"
          show-size-picker
          show-quick-jumper
        />
      </div>
    </n-card>

    <n-drawer v-model:show="drawerOpen" :width="480" placement="right" :block-scroll="false">
      <n-drawer-content :title="`角色分配 · ${selectedUser?.username || ''}`" closable>
        <p v-if="selectedUser" class="user-role-hint">
          {{ displayName(selectedUser) }}
          <span v-if="selectedUser.studentNo"> · {{ selectedUser.studentNo }}</span>
        </p>
        <n-checkbox-group v-model:value="selectedRoleIds">
          <n-space vertical>
            <n-checkbox v-for="role in allRoles" :key="role.id" :value="role.id">
              {{ role.roleName }}（{{ role.roleKey }}）
            </n-checkbox>
          </n-space>
        </n-checkbox-group>
        <template #footer>
          <n-space>
            <n-button @click="drawerOpen = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="saveUserRoles">保存</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref, watch } from 'vue'
import { NButton, NSpace, NTag } from 'naive-ui'
import {
  listAdminRolesApi,
  replaceAdminUserRolesApi,
  searchAdminUsersApi,
  updateAdminUserStatusApi,
} from '@/api/admin'
import { useDict } from '@/composables/useDict'
import { CacheCode } from '@/constants/cacheCode'

const userRoleDict = useDict(CacheCode.USER_ROLE)

const loading = ref(false)
const saving = ref(false)
const pageError = ref('')
const keyword = ref('')
const roleKey = ref(null)
const appliedKeyword = ref('')
const appliedRoleKey = ref(null)
const users = ref([])
const allRoles = ref([])
const page = ref(1)
const pageSize = ref(10)

const drawerOpen = ref(false)
const selectedUser = ref(null)
const selectedRoleIds = ref([])

const roleFilterOptions = computed(() => [
  { label: '全部角色', value: null },
  ...userRoleDict.options.value,
])

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return users.value.slice(start, start + pageSize.value)
})

watch([users, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(users.value.length / pageSize.value) || 1)
  if (page.value > maxPage) page.value = maxPage
})

const displayName = (user) => user.realName || user.nickName || user.username

const loadRoles = async () => {
  allRoles.value = await listAdminRolesApi({ status: CacheCode.SYS_STATUS_ACTIVE })
}

const handleSearch = async () => {
  appliedKeyword.value = keyword.value.trim()
  appliedRoleKey.value = roleKey.value
  page.value = 1
  loading.value = true
  pageError.value = ''
  try {
    users.value = await searchAdminUsersApi({
      keyword: appliedKeyword.value || undefined,
      roleKey: appliedRoleKey.value || undefined,
    })
  } catch (error) {
    pageError.value = error?.message || '搜索用户失败'
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  keyword.value = ''
  roleKey.value = null
  appliedKeyword.value = ''
  appliedRoleKey.value = null
  handleSearch()
}

const openAssign = (row) => {
  selectedUser.value = row
  selectedRoleIds.value = (row.roles || []).map((role) => role.id)
  drawerOpen.value = true
}

const saveUserRoles = async () => {
  if (!selectedUser.value) return
  saving.value = true
  pageError.value = ''
  try {
    await replaceAdminUserRolesApi(selectedUser.value.id, selectedRoleIds.value)
    drawerOpen.value = false
    await handleSearch()
  } catch (error) {
    pageError.value = error?.message || '保存用户角色失败'
  } finally {
    saving.value = false
  }
}

const toggleUserStatus = async (row) => {
  const next = row.isActive === 1 ? 0 : 1
  pageError.value = ''
  try {
    await updateAdminUserStatusApi(row.id, next)
    await handleSearch()
  } catch (error) {
    pageError.value = error?.message || '更新用户状态失败'
  }
}

const columns = computed(() => [
  {
    title: '序号',
    key: 'index',
    width: 64,
    render: (_, index) => (page.value - 1) * pageSize.value + index + 1,
  },
  { title: '用户名', key: 'username', width: 120 },
  {
    title: '姓名',
    key: 'realName',
    width: 120,
    render: (row) => displayName(row),
  },
  { title: '学号', key: 'studentNo', width: 120 },
  {
    title: '档案角色',
    key: 'role',
    width: 90,
    render: (row) => userRoleDict.label(row.role, row.role),
  },
  {
    title: '状态',
    key: 'isActive',
    width: 90,
    render: (row) => h(NTag, {
      size: 'small',
      round: true,
      type: row.isActive === 1 ? 'success' : 'warning',
    }, { default: () => (row.isActive === 1 ? '启用' : '停用') }),
  },
  {
    title: 'RBAC 角色',
    key: 'roles',
    minWidth: 200,
    render: (row) => {
      const roles = row.roles || []
      if (!roles.length) return '—'
      return h('div', { class: 'role-tags' }, roles.map((role) =>
        h(NTag, { size: 'small', round: true, style: { marginRight: '6px' } }, { default: () => role.roleName }),
      ))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => h(NSpace, { size: 8 }, {
      default: () => [
        h(NButton, { size: 'small', type: 'primary', secondary: true, onClick: () => openAssign(row) }, {
          default: () => '角色',
        }),
        h(NButton, {
          size: 'small',
          secondary: true,
          onClick: () => toggleUserStatus(row),
        }, {
          default: () => (row.isActive === 1 ? '停用' : '启用'),
        }),
      ],
    }),
  },
])

onMounted(async () => {
  await loadRoles()
  await handleSearch()
})
</script>

<style scoped>
.user-role-hint {
  margin: 0 0 16px;
  color: var(--wb-text-secondary);
  font-size: 13px;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
