import { computed, reactive } from 'vue'
import { getDictValuesBatchApi } from '@/api/dict'
import { CacheCode } from '@/constants/cacheCode.js'

/** 应用启动时一次性加载的字典 Key */
export const DICT_KEY_CODES = [
  CacheCode.TEAM_STATUS,
  CacheCode.MEMBER_STATUS,
  CacheCode.APPROVAL_STATUS,
  CacheCode.PLAN_TASK_STATUS,
  CacheCode.WEEKLY_REPORT_STATUS,
  CacheCode.USER_ROLE,
  CacheCode.WORKSPACE_NODE_TYPE,
  CacheCode.COURSE_STATUS,
  CacheCode.TERM_SEASON,
]

const normalizeValueCode = (value) => (value == null ? '' : String(value))

const state = reactive({
  loaded: false,
  loading: false,
  error: null,
  byKey: {},
})

let initPromise = null

const buildLabelMap = (items = []) => {
  const map = {}
  for (const item of items) {
    map[normalizeValueCode(item.valueCode)] = item.valueName
  }
  return map
}

const buildOptions = (items = []) =>
  items.map((item) => ({
    value: normalizeValueCode(item.valueCode),
    label: item.valueName,
    remark: item.remark,
    sort: item.sort,
  }))

export const getDictItems = (keyCode) => state.byKey[keyCode] || []

export const getDictLabelMap = (keyCode) => buildLabelMap(getDictItems(keyCode))

export const getDictOptions = (keyCode) => buildOptions(getDictItems(keyCode))

export const dictLabel = (keyCode, valueCode, fallback = '未知') =>
  getDictLabelMap(keyCode)[normalizeValueCode(valueCode)] || fallback

export const initDictStore = async () => {
  if (state.loaded) return state.byKey
  if (initPromise) return initPromise

  initPromise = (async () => {
    state.loading = true
    state.error = null
    try {
      const batch = await getDictValuesBatchApi(DICT_KEY_CODES)
      for (const keyCode of DICT_KEY_CODES) {
        state.byKey[keyCode] = Array.isArray(batch?.[keyCode]) ? batch[keyCode] : []
      }
      state.loaded = true
      return state.byKey
    } catch (error) {
      state.error = error
      initPromise = null
      throw error
    } finally {
      state.loading = false
    }
  })()

  return initPromise
}

export const clearDictStore = () => {
  state.loaded = false
  state.loading = false
  state.error = null
  state.byKey = {}
  initPromise = null
}

export const useDictStore = () => ({
  loaded: computed(() => state.loaded),
  loading: computed(() => state.loading),
  error: computed(() => state.error),
  byKey: computed(() => state.byKey),
  getDictItems,
  getDictLabelMap,
  getDictOptions,
  dictLabel,
  initDictStore,
  clearDictStore,
})
