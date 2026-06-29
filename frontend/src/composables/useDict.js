import { computed } from 'vue'
import {
  dictLabel,
  getDictItems,
  getDictLabelMap,
  getDictOptions,
  useDictStore,
} from '@/stores/dictStore'

export { initDictStore, clearDictStore, dictLabel, getDictOptions, getDictLabelMap } from '@/stores/dictStore'

/** 读取全局字典中某一 key 的数据（需 App 启动时已 initDictStore） */
export const useDict = (keyCode) => {
  const store = useDictStore()

  const items = computed(() => getDictItems(keyCode))
  const labelMap = computed(() => getDictLabelMap(keyCode))
  const options = computed(() => getDictOptions(keyCode))
  const label = (valueCode, fallback = '未知') => dictLabel(keyCode, valueCode, fallback)

  return {
    items,
    labelMap,
    options,
    label,
    loaded: store.loaded,
    loading: store.loading,
    error: store.error,
  }
}
