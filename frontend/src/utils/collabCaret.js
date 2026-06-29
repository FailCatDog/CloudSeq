const COLLAB_COLORS = [
  '#7c5cfc',
  '#2563eb',
  '#0891b2',
  '#059669',
  '#d97706',
  '#dc2626',
  '#db2777',
  '#4f46e5',
]

export const hashCollabColor = (input = '') => {
  let hash = 0
  for (let i = 0; i < input.length; i += 1) {
    hash = input.charCodeAt(i) + ((hash << 5) - hash)
  }
  return COLLAB_COLORS[Math.abs(hash) % COLLAB_COLORS.length]
}

export const buildCollabUser = ({ name, avatarUrl, color }) => {
  const safeName = name?.trim() || '用户'
  return {
    name: safeName,
    avatarUrl: avatarUrl || '',
    color: color || hashCollabColor(safeName),
  }
}

const createAvatarElement = (user) => {
  const avatar = document.createElement('span')
  avatar.className = 'ps-collab-bubble__avatar'
  avatar.style.setProperty('--ps-collab-color', user.color)

  if (user.avatarUrl) {
    const img = document.createElement('img')
    img.src = user.avatarUrl
    img.alt = user.name
    img.loading = 'lazy'
    img.referrerPolicy = 'no-referrer'
    img.addEventListener('error', () => {
      img.remove()
      avatar.textContent = user.name.slice(0, 1).toUpperCase()
    })
    avatar.appendChild(img)
  } else {
    avatar.textContent = user.name.slice(0, 1).toUpperCase()
  }

  return avatar
}

export const renderCollabCaret = (user) => {
  const caret = document.createElement('span')
  caret.className = 'ps-collab-caret'
  caret.style.setProperty('--ps-collab-color', user.color)

  const line = document.createElement('span')
  line.className = 'ps-collab-caret__line'
  line.setAttribute('aria-hidden', 'true')

  const bubble = document.createElement('span')
  bubble.className = 'ps-collab-bubble'
  bubble.title = user.name

  bubble.appendChild(createAvatarElement(user))

  const name = document.createElement('span')
  name.className = 'ps-collab-bubble__name'
  name.textContent = user.name
  bubble.appendChild(name)

  caret.appendChild(line)
  caret.appendChild(bubble)

  return caret
}

export const renderCollabSelection = () => ({
  class: 'ps-collab-selection',
})
