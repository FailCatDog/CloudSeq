const TOKEN_STORAGE_KEY = 'authorization'
const PUBLIC_AUTH_PATHS = ['/login', '/register']

const getToken = () => {
  return localStorage.getItem(TOKEN_STORAGE_KEY) || sessionStorage.getItem(TOKEN_STORAGE_KEY) || ''
}

const shouldAttachToken = (path) => {
  return !PUBLIC_AUTH_PATHS.some((item) => path.includes(item))
}

const clearAuth = () => {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  sessionStorage.removeItem(TOKEN_STORAGE_KEY)
}

const redirectToLogin = () => {
  clearAuth()
  import('@/utils/collabTokenCache').then(({ clearCollabTokenCache }) => {
    clearCollabTokenCache()
  })
  if (window.location.pathname !== '/auth' && window.location.pathname !== '/login') {
    window.location.href = '/auth'
  }
}

export const request = async (path, options) => {
  const headers = {
    'Content-Type': 'application/json',
    ...(options?.headers || {}),
  }

  if (shouldAttachToken(path)) {
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  const response = await fetch(path, {
    ...options,
    headers,
  })

  const data = await response.json().catch(() => ({}))

  if (data?.code === 20001) {
    redirectToLogin()
    throw new Error(data.message || '登录已过期，请重新登录')
  }

  if (!response.ok || data.code !== 0) {
    throw new Error(data.message || '请求失败')
  }

  return data.data
}
