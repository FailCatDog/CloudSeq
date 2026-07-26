<template>
  <n-modal
    :show="show"
    preset="card"
    :title="title"
    :bordered="false"
    :mask-closable="false"
    style="width: min(640px, calc(100vw - 32px))"
    @update:show="emit('update:show', $event)"
  >
    <n-form label-placement="top" class="menu-form-dialog">
      <n-form-item :label="isButtonForm ? '所属菜单' : '上级'">
        <n-select
          v-model:value="form.parentId"
          :options="parentOptions"
          :disabled="parentLocked"
          :placeholder="isButtonForm ? '按钮必须挂在菜单下' : '不选则为顶级'"
        />
      </n-form-item>

      <n-form-item :label="isButtonForm ? '按钮名称' : '菜单名称'">
        <n-input
          v-model:value="form.menuName"
          :placeholder="isButtonForm ? '如 新增角色' : '如 角色管理'"
        />
      </n-form-item>

      <n-form-item v-if="!isButtonForm" label="菜单类型">
        <n-select v-model:value="form.menuType" :options="menuTypeOptions" />
      </n-form-item>

      <template v-if="!isButtonForm">
        <n-form-item label="路由 path">
          <n-input v-model:value="form.path" placeholder="/admin/roles" />
        </n-form-item>
        <n-form-item label="路由匹配">
          <n-select v-model:value="form.routeMatch" :options="routeMatchOptions" clearable />
        </n-form-item>
        <n-form-item label="图标">
          <n-input v-model:value="form.icon" placeholder="roles" />
        </n-form-item>
        <n-form-item label="可见性">
          <n-select v-model:value="form.visible" :options="visibleOptions" />
        </n-form-item>
      </template>

      <n-form-item label="权限标识 perms">
        <n-input
          v-model:value="form.perms"
          :placeholder="isButtonForm ? 'admin:role:create' : 'admin:role:list'"
        />
      </n-form-item>
      <n-form-item label="API 方法">
        <n-select v-model:value="form.apiMethod" :options="httpMethodOptions" clearable />
      </n-form-item>
      <n-form-item label="API 路径">
        <n-input v-model:value="form.apiPath" placeholder="/api/admin/roles" />
      </n-form-item>
      <n-form-item label="排序">
        <n-input-number v-model:value="form.sort" :min="0" class="w-full" />
      </n-form-item>
      <n-form-item label="状态">
        <n-select v-model:value="form.status" :options="statusOptions" />
      </n-form-item>
      <n-form-item label="备注">
        <n-input v-model:value="form.remark" type="textarea" :rows="2" />
      </n-form-item>
    </n-form>

    <template #footer>
      <n-space justify="end">
        <n-button :disabled="saving" @click="emit('update:show', false)">取消</n-button>
        <n-button type="primary" :loading="saving" @click="handleSubmit">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { useDict } from '@/composables/useDict'
import { CacheCode } from '@/constants/cacheCode'
import { flattenMenuTreeOptions } from '@/utils/adminMenuTree'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'createButton', 'edit'].includes(value),
  },
  saving: {
    type: Boolean,
    default: false,
  },
  menuTree: {
    type: Array,
    default: () => [],
  },
  editingId: {
    type: [Number, String],
    default: null,
  },
  /** Snapshot applied each time the dialog opens */
  initialValue: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:show', 'submit'])

const menuTypeDict = useDict(CacheCode.MENU_TYPE)
const sysStatusDict = useDict(CacheCode.SYS_STATUS)
const routeMatchDict = useDict(CacheCode.ROUTE_MATCH)
const visibleDict = useDict(CacheCode.MENU_VISIBLE)
const httpMethodDict = useDict(CacheCode.HTTP_METHOD)

const form = reactive({
  parentId: 0,
  menuName: '',
  menuType: CacheCode.MENU_TYPE_MENU,
  path: '',
  routeMatch: CacheCode.ROUTE_MATCH_EXACT,
  component: '',
  perms: '',
  apiMethod: '',
  apiPath: '',
  icon: '',
  sort: 0,
  visible: CacheCode.MENU_VISIBLE_SHOW,
  status: CacheCode.SYS_STATUS_ACTIVE,
  remark: '',
  version: null,
})

const isButtonForm = computed(() =>
  props.mode === 'createButton'
  || form.menuType === CacheCode.MENU_TYPE_BUTTON,
)

const parentLocked = computed(() =>
  props.mode === 'createButton'
  || (props.mode === 'edit' && form.menuType === CacheCode.MENU_TYPE_BUTTON),
)

const title = computed(() => {
  if (props.mode === 'createButton') return '新增按钮'
  if (props.mode === 'create') return '新增菜单'
  return isButtonForm.value ? '编辑按钮' : '编辑菜单'
})

/** 顶部新增只允许目录 / 菜单，按钮仅从菜单行创建 */
const menuTypeOptions = computed(() =>
  menuTypeDict.options.value.filter((item) => item.value !== CacheCode.MENU_TYPE_BUTTON),
)

const statusOptions = computed(() => sysStatusDict.options.value)
const routeMatchOptions = computed(() => routeMatchDict.options.value)
const visibleOptions = computed(() => visibleDict.options.value)
const httpMethodOptions = computed(() => httpMethodDict.options.value)

const parentOptions = computed(() => {
  const flat = flattenMenuTreeOptions(props.menuTree)
    .filter((item) => item.key !== props.editingId)
    .filter((item) => {
      const type = item.raw?.menuType
      if (isButtonForm.value) {
        return type === CacheCode.MENU_TYPE_MENU
      }
      return type !== CacheCode.MENU_TYPE_BUTTON
    })
    .map((item) => ({ label: item.label, value: item.key }))

  if (isButtonForm.value) {
    return flat
  }
  return [{ label: '顶级', value: 0 }, ...flat]
})

const applyDefaults = () => {
  form.parentId = 0
  form.menuName = ''
  form.menuType = CacheCode.MENU_TYPE_MENU
  form.path = ''
  form.routeMatch = CacheCode.ROUTE_MATCH_EXACT
  form.component = ''
  form.perms = ''
  form.apiMethod = ''
  form.apiPath = ''
  form.icon = ''
  form.sort = 0
  form.visible = CacheCode.MENU_VISIBLE_SHOW
  form.status = CacheCode.SYS_STATUS_ACTIVE
  form.remark = ''
  form.version = null
}

const applyInitial = (value) => {
  applyDefaults()
  if (!value) return
  form.parentId = value.parentId ?? 0
  form.menuName = value.menuName || ''
  form.menuType = value.menuType || CacheCode.MENU_TYPE_MENU
  form.path = value.path || ''
  form.routeMatch = value.routeMatch || CacheCode.ROUTE_MATCH_EXACT
  form.component = value.component || ''
  form.perms = value.perms || ''
  form.apiMethod = value.apiMethod || ''
  form.apiPath = value.apiPath || ''
  form.icon = value.icon || ''
  form.sort = value.sort ?? 0
  form.visible = value.visible || CacheCode.MENU_VISIBLE_SHOW
  form.status = value.status || CacheCode.SYS_STATUS_ACTIVE
  form.remark = value.remark || ''
  form.version = value.version ?? null
}

watch(
  () => props.show,
  (open) => {
    if (open) applyInitial(props.initialValue)
  },
)

const handleSubmit = () => {
  const button = isButtonForm.value
  emit('submit', {
    parentId: form.parentId === 0 ? null : form.parentId,
    menuName: form.menuName,
    menuType: button ? CacheCode.MENU_TYPE_BUTTON : form.menuType,
    path: button ? null : (form.path || null),
    routeMatch: button ? null : (form.routeMatch || null),
    component: button ? null : (form.component || null),
    perms: form.perms || null,
    apiMethod: form.apiMethod || null,
    apiPath: form.apiPath || null,
    icon: button ? null : (form.icon || null),
    sort: form.sort,
    visible: button ? CacheCode.MENU_VISIBLE_HIDE : form.visible,
    status: form.status,
    remark: form.remark || null,
    version: form.version,
  })
}
</script>

<style lang="scss" scoped>
.menu-form-dialog {
  max-height: min(70vh, 640px);
  overflow: auto;
  padding-right: 4px;
}

.w-full {
  width: 100%;
}
</style>
