<template>
  <section class="profile-page">
    <GuestNotice v-if="!hasProfileData && !loading" />

    <template v-else>
      <!-- 上：基础信息展示 -->
      <header class="profile-top wb-card">
        <div class="profile-top__avatar" aria-hidden="true">
          <img v-if="profileSource?.avatarUrl" :src="profileSource.avatarUrl" alt="" />
          <span v-else>{{ avatarText }}</span>
        </div>
        <div class="profile-top__main">
          <h2>{{ displayName }}</h2>
          <p class="profile-top__bio">{{ titleText }}</p>
          <div class="profile-top__meta">
            <span>{{ roleLabel }}</span>
            <span>{{ profileSource?.username || '-' }}</span>
            <span v-if="profileSource?.studentNo">学号 {{ profileSource.studentNo }}</span>
            <span v-if="profileSource?.lastLoginAt">最近登录 {{ profileSource.lastLoginAt }}</span>
          </div>
        </div>
        <RouterLink v-if="isStudent" to="/prepare" class="profile-top__link">课程准备 →</RouterLink>
      </header>

      <p v-if="errorMessage" class="profile-msg profile-msg--error">{{ errorMessage }}</p>

      <!-- 下：侧栏 + 内容 -->
      <div class="profile-bottom wb-card">
        <nav class="profile-nav" aria-label="个人中心分区">
          <button
            v-for="item in navItems"
            :key="item.id"
            type="button"
            class="profile-nav__item"
            :class="{ active: activeSection === item.id }"
            @click="setSection(item.id)"
          >
            {{ item.label }}
          </button>
        </nav>

        <div class="profile-content">
          <!-- 编辑信息 -->
          <form v-if="activeSection === 'info'" class="profile-form" @submit.prevent="handleSave">
            <label class="profile-field">
              <span>真实姓名</span>
              <input v-model.trim="form.realName" type="text" placeholder="请输入真实姓名" />
            </label>
            <label class="profile-field">
              <span>昵称</span>
              <input v-model.trim="form.nickName" type="text" placeholder="请输入昵称" />
            </label>
            <label class="profile-field">
              <span>个人简介</span>
              <textarea v-model.trim="form.bio" rows="3" placeholder="一句话介绍自己" />
            </label>
            <label class="profile-field">
              <span>头像链接</span>
              <input v-model.trim="form.avatarUrl" type="url" placeholder="https://..." />
            </label>

            <div class="profile-form__actions">
              <button type="submit" class="profile-btn profile-btn--primary" :disabled="saving">
                {{ saving ? '保存中…' : '保存' }}
              </button>
            </div>
            <p v-if="infoMessage" class="profile-msg" :class="infoMessageType">{{ infoMessage }}</p>
          </form>

          <!-- 我的小组 -->
          <div v-else-if="activeSection === 'team'" class="profile-form profile-form--wide">
            <template v-if="teamLoading">
              <p class="profile-hint">正在加载小组信息…</p>
            </template>
            <template v-else-if="!hasTeam">
              <p class="profile-hint">你还没有加入小组。</p>
              <RouterLink to="/prepare" class="profile-btn profile-btn--ghost">去课程准备</RouterLink>
            </template>
            <template v-else>
              <label class="profile-field profile-field--readonly">
                <span>小组名称</span>
                <div class="profile-value">{{ teamInfo.teamName || '-' }}</div>
              </label>
              <label class="profile-field profile-field--readonly">
                <span>小组状态</span>
                <div class="profile-value">{{ teamInfo.status || '-' }}</div>
              </label>
              <label class="profile-field profile-field--readonly">
                <span>当前选题</span>
                <div class="profile-value">{{ teamInfo.topicTitle || '暂无选题' }}</div>
              </label>
              <label class="profile-field profile-field--readonly">
                <span>选题说明</span>
                <div class="profile-value">{{ teamInfo.topicDesc || '-' }}</div>
              </label>
              <label class="profile-field profile-field--readonly">
                <span>成员数量</span>
                <div class="profile-value">{{ members.length }} 人</div>
              </label>
              <label class="profile-field profile-field--readonly">
                <span>创建时间</span>
                <div class="profile-value">{{ teamInfo.createDate || '-' }}</div>
              </label>

              <div v-if="members.length" class="profile-members">
                <p class="profile-members__title">成员列表</p>
                <div class="profile-table-wrap">
                  <table class="profile-table">
                    <thead>
                      <tr>
                        <th>名称</th>
                        <th>学号</th>
                        <th>加入时间</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="member in members" :key="member.id">
                        <td>
                          <span>{{ member.name }}</span>
                          <span v-if="member.isLeader" class="profile-table__tag">组长</span>
                        </td>
                        <td>{{ member.studentNo }}</td>
                        <td>{{ member.joinDate }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import GuestNotice from '@/components/GuestNotice.vue'
import { getProfileApi, updateProfileApi } from '@/api/account'
import { getCurrentTeamApi, listTeamMembersApi, parseTeamMembersResponse } from '@/api/team'
import { CacheCode } from '@/constants/cacheCode'
import { useDict } from '@/composables/useDict'
import { isTeacherRole } from '@/utils/roleHome'

const route = useRoute()
const router = useRouter()
const teamStatusDict = useDict(CacheCode.TEAM_STATUS)

const profile = ref(null)
const loading = ref(false)
const saving = ref(false)
const teamLoading = ref(false)
const errorMessage = ref('')
const infoMessage = ref('')
const infoMessageType = ref('')
const hasTeam = ref(false)

const form = reactive({
  realName: '',
  nickName: '',
  bio: '',
  avatarUrl: '',
})

const teamInfo = ref({
  teamName: '',
  status: '',
  topicTitle: '',
  topicDesc: '',
  createDate: '',
})
const members = ref([])

const getStorageUser = () => {
  const raw = localStorage.getItem('user') || sessionStorage.getItem('user')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

const storageUser = getStorageUser()
const profileSource = computed(() => profile.value || storageUser || null)
const hasProfileData = computed(() => Boolean(profileSource.value))
const userId = computed(() => profileSource.value?.id || null)
const isStudent = computed(() => !isTeacherRole(profileSource.value?.role))

const displayName = computed(() => profileSource.value?.realName || profileSource.value?.nickName || '未获取')
const titleText = computed(() => profileSource.value?.bio || '暂无个人简介')
const avatarText = computed(() => {
  const name = displayName.value
  return name && name !== '未获取' ? name.slice(0, 2) : '我'
})

const roleLabel = computed(() => {
  if (profileSource.value?.role === CacheCode.USER_ROLE_TEACHER) return '教师'
  if (profileSource.value?.role === CacheCode.USER_ROLE_STUDENT) return '学生'
  return '用户'
})

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const navItems = computed(() => {
  const items = [{ id: 'info', label: '编辑信息' }]
  if (isStudent.value) items.push({ id: 'team', label: '我的小组' })
  return items
})

const resolveSection = (tab) => (tab === 'team' && isStudent.value ? 'team' : 'info')
const activeSection = computed(() => resolveSection(route.query.tab))

const setSection = (sectionId) => {
  const nextQuery = sectionId === 'info' ? {} : { tab: sectionId }
  if (activeSection.value === sectionId && JSON.stringify(route.query) === JSON.stringify(nextQuery)) return
  router.replace({ path: '/profile', query: nextQuery })
}

const syncForm = () => {
  form.realName = profileSource.value?.realName || ''
  form.nickName = profileSource.value?.nickName || ''
  form.bio = profileSource.value?.bio || ''
  form.avatarUrl = profileSource.value?.avatarUrl || ''
}

watch(
  () => profileSource.value,
  () => syncForm(),
  { immediate: true },
)

watch(
  () => route.query.tab,
  (tab) => {
    if (tab === 'status' || tab === 'overview') {
      router.replace('/prepare')
      return
    }
    if (tab === 'security') {
      router.replace({ path: '/profile', query: {} })
      return
    }
    if (tab === 'team' && !isStudent.value) {
      router.replace({ path: '/profile', query: {} })
    }
  },
  { immediate: true },
)

const persistLocalUser = (patch) => {
  for (const key of ['localStorage', 'sessionStorage']) {
    const storage = window[key]
    const raw = storage.getItem('user')
    if (!raw) continue
    try {
      const user = JSON.parse(raw)
      storage.setItem('user', JSON.stringify({ ...user, ...patch }))
    } catch {
      // ignore
    }
  }
}

const loadProfile = async () => {
  if (!userId.value) {
    errorMessage.value = '未获取到用户信息，请重新登录后再试。'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    profile.value = await getProfileApi(userId.value)
    syncForm()
  } catch (error) {
    errorMessage.value = error?.message || '个人信息加载失败'
  } finally {
    loading.value = false
  }
}

const loadTeam = async () => {
  if (!isStudent.value) return
  teamLoading.value = true
  try {
    const team = await getCurrentTeamApi()
    if (!team?.id) {
      hasTeam.value = false
      members.value = []
      return
    }
    hasTeam.value = true
    teamInfo.value = {
      teamName: team.teamName || '',
      status: teamStatusDict.label(team.status, team.status || '-'),
      topicTitle: team.topicTitle || '',
      topicDesc: team.topicDesc || '',
      createDate: team.createDate || '-',
    }
    const memberResponse = await listTeamMembersApi(team.id)
    const { memberList } = parseTeamMembersResponse(memberResponse)
    members.value = memberList.map((item) => ({
      id: item.teamMemberId ?? item.userId,
      name: item.name || item.username || `用户 ${item.userId}`,
      studentNo: item.studentNo || '-',
      joinDate: formatDate(item.joinDate),
      isLeader: item.isLeader === 1,
    }))
  } catch {
    hasTeam.value = false
    members.value = []
  } finally {
    teamLoading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  infoMessage.value = ''
  try {
    await updateProfileApi({
      realName: form.realName,
      nickName: form.nickName,
      bio: form.bio,
      avatarUrl: form.avatarUrl,
    })
    persistLocalUser({
      realName: form.realName,
      nickName: form.nickName,
      bio: form.bio,
      avatarUrl: form.avatarUrl,
    })
    await loadProfile()
    infoMessage.value = '资料已保存。'
    infoMessageType.value = 'success'
  } catch (error) {
    infoMessage.value = error?.message || '保存失败，请稍后重试。'
    infoMessageType.value = 'error'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  await loadTeam()
})
</script>

<style lang="scss" scoped>
@use '@/styles/mixins' as *;

.profile-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: 14px;
  width: 100%;
  min-width: 0;
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  flex-shrink: 0;

  &__avatar {
    display: grid;
    place-items: center;
    flex-shrink: 0;
    width: 56px;
    height: 56px;
    border-radius: 16px;
    overflow: hidden;
    color: #fff;
    font-size: 20px;
    font-weight: 700;
    background: linear-gradient(135deg, var(--wb-purple) 0%, var(--wb-purple-light) 100%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__main {
    flex: 1;
    min-width: 0;

    h2 {
      margin: 0;
      color: var(--wb-text-primary);
      font-size: 20px;
      font-weight: 700;
      line-height: 1.2;
    }
  }

  &__bio {
    margin: 4px 0 0;
    color: var(--wb-text-secondary);
    font-size: 13px;
    line-height: 1.5;
  }

  &__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 14px;
    margin-top: 10px;
    color: var(--wb-text-muted);
    font-size: 12px;
  }

  &__link {
    flex-shrink: 0;
    color: var(--wb-purple);
    font-size: 13px;
    font-weight: 600;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.profile-bottom {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.profile-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 12px;
  border-right: 1px solid var(--wb-search-border);

  &__item {
    height: 36px;
    padding: 0 12px;
    border: none;
    border-radius: var(--wb-radius-sm);
    background: transparent;
    color: var(--wb-text-secondary);
    font-size: 13px;
    font-weight: 600;
    text-align: left;
    cursor: pointer;
    font-family: inherit;
    transition: background 0.15s ease, color 0.15s ease;

    &:hover,
    &.active {
      color: var(--wb-purple);
      background: var(--wb-purple-soft);
    }
  }
}

.profile-content {
  padding: 20px 24px;
  overflow-y: auto;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 480px;

  &--wide {
    max-width: 100%;
  }

  &__actions {
    padding-top: 4px;
  }
}

.profile-field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  span {
    color: var(--wb-text-muted);
    font-size: 12px;
    font-weight: 600;
  }

  input,
  textarea {
    width: 100%;
    padding: 0 12px;
    border: 1px solid var(--wb-search-border);
    border-radius: var(--wb-radius-sm);
    background: var(--wb-card-bg);
    color: var(--wb-text-primary);
    font-family: inherit;
    font-size: 14px;
    outline: none;

    &:focus {
      @include focus-ring($alpha: 0.1);
      border-color: var(--wb-purple-border);
    }
  }

  input {
    height: 38px;
  }

  textarea {
    padding-top: 10px;
    padding-bottom: 10px;
    resize: vertical;
  }

  &--readonly .profile-value {
    min-height: 38px;
    padding: 0 12px;
    display: flex;
    align-items: center;
    border-radius: var(--wb-radius-sm);
    background: var(--wb-search-bg);
    color: var(--wb-text-primary);
    font-size: 14px;
    line-height: 1.5;
    word-break: break-word;
  }
}

.profile-hint {
  margin: 0;
  color: var(--wb-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.profile-members {
  margin-top: 8px;
  max-width: 100%;

  &__title {
    margin: 0 0 10px;
    color: var(--wb-text-muted);
    font-size: 12px;
    font-weight: 600;
  }
}

.profile-table-wrap {
  overflow-x: auto;
  border: 1px solid var(--wb-search-border);
  border-radius: var(--wb-radius-sm);
}

.profile-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  th,
  td {
    padding: 10px 14px;
    text-align: left;
    border-bottom: 1px solid var(--wb-search-border);
  }

  th {
    color: var(--wb-text-muted);
    font-size: 12px;
    font-weight: 600;
    background: var(--wb-search-bg);
  }

  td {
    color: var(--wb-text-primary);
  }

  tbody tr:last-child td {
    border-bottom: none;
  }

  &__tag {
    margin-left: 6px;
    padding: 2px 8px;
    border-radius: var(--wb-radius-pill);
    color: var(--wb-purple);
    background: var(--wb-purple-soft);
    font-size: 11px;
    font-weight: 600;
  }
}

.profile-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 16px;
  border-radius: var(--wb-radius-pill);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  text-decoration: none;
  transition: opacity 0.15s ease;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &--primary {
    color: #fff;
    border: none;
    background: var(--wb-btn-dark);
  }

  &--ghost {
    color: var(--wb-purple);
    border: 1.5px solid var(--wb-purple-border);
    background: transparent;
    width: fit-content;
  }
}

.profile-msg {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;

  &--error {
    padding: 10px 14px;
    border-radius: var(--wb-radius-sm);
    color: var(--wb-tag-danger-text);
    background: var(--wb-tag-danger-soft);
  }

  &.success {
    color: var(--wb-tag-success-text);
  }

  &.error {
    color: var(--wb-tag-danger-text);
  }
}

@media (max-width: 640px) {
  .profile-top {
    flex-wrap: wrap;
  }

  .profile-bottom {
    grid-template-columns: 1fr;
  }

  .profile-nav {
    flex-direction: row;
    border-right: none;
    border-bottom: 1px solid var(--wb-search-border);
    padding: 10px 12px;

    &__item {
      flex: 1;
      text-align: center;
    }
  }
}
</style>
