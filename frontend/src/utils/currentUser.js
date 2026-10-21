const USER_STORAGE_KEY = 'user'

export const getCurrentUserId = () => {
  const raw = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return null
  try {
    const user = JSON.parse(raw)
    return user?.id ?? user?.userId ?? null
  } catch {
    return null
  }
}
