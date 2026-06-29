import { request } from '@/api/request'

export const getDictValuesApi = (keyCode) => request(`/api/dict/values/${keyCode}`)

export const getDictValuesBatchApi = (keyCodes) => {
  const codes = Array.isArray(keyCodes) ? keyCodes.join(',') : keyCodes
  return request(`/api/dict/values/batch?keyCodes=${encodeURIComponent(codes)}`)
}
