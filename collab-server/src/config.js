const PORT = Number(process.env.COLLAB_PORT || 1234)
const JWT_SECRET = process.env.COLLAB_JWT_SECRET || 'soft_manage-collab-jwt-secret-change-me'
const INTERNAL_SECRET = process.env.COLLAB_INTERNAL_SECRET || 'soft_manage-collab-internal-secret-change-me'
const SPRING_API_URL = (process.env.SPRING_API_URL || 'http://localhost:19001').replace(/\/$/, '')
const ROOM_PREFIX = process.env.COLLAB_ROOM_PREFIX || 'doc-'

export const config = {
  port: PORT,
  jwtSecret: JWT_SECRET,
  internalSecret: INTERNAL_SECRET,
  springApiUrl: SPRING_API_URL,
  roomPrefix: ROOM_PREFIX,
}
