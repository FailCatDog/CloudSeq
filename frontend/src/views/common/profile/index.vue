<template>
  <section class="profile-shell">
    <GuestNotice v-if="!hasProfileData && !loading" />

    <template v-else>
      <div class="profile-hero wb-card">
        <div class="profile-hero-body">
          <div class="profile-avatar" aria-hidden="true">{{ avatarText }}</div>
          <div class="profile-summary">
            <p class="section-label">个人中心</p>
            <h3>{{ displayName }}</h3>
            <p class="profile-subtitle">{{ titleText }}</p>
            <div class="profile-tags">
              <span class="profile-tag profile-tag--primary">{{ statusText }}</span>
              <span class="profile-tag">{{ profileSource?.username || '未获取' }}</span>
              <span class="profile-tag profile-tag--muted">最近登录 {{ lastLoginText }}</span>
            </div>
          </div>
        </div>

        <div class="profile-hero-actions">
          <button type="button" class="profile-btn profile-btn--ghost" :disabled="loading" @click="loadProfile">
            刷新资料
          </button>
          <button type="button" class="profile-btn profile-btn--primary">编辑资料</button>
        </div>

        <div class="profile-hero-accent" aria-hidden="true">
          <div class="profile-accent-orb profile-accent-orb--1" />
          <div class="profile-accent-orb profile-accent-orb--2" />
          <div class="profile-accent-orb profile-accent-orb--3" />
        </div>
      </div>

      <nav class="profile-tabs wb-card" aria-label="个人中心导航">
        <button
          v-for="tab in profileTabs"
          :key="tab.id"
          type="button"
          class="profile-tab"
          :class="{ active: activeTab === tab.id }"
          :aria-selected="activeTab === tab.id"
          @click="setActiveTab(tab.id)"
        >
          {{ tab.label }}
        </button>
      </nav>

      <AppScrollArea tag="div" class="profile-body" axis="y" flex hover-reveal>
      <div v-if="activeTab === 'overview'" class="profile-tab-panel">
        <div class="profile-layout">
          <section class="profile-panel wb-card">
            <div class="panel-head">
              <div>
                <p class="section-label">资料概览</p>
                <h4>基础信息</h4>
              </div>
              <button type="button" class="profile-btn profile-btn--text">编辑</button>
            </div>

            <dl class="info-grid">
              <div v-for="item in profileFields" :key="item.label" class="info-item">
                <dt>{{ item.label }}</dt>
                <dd>{{ item.value }}</dd>
              </div>
            </dl>
          </section>

          <section class="profile-panel wb-card profile-panel--aside">
            <div class="panel-head">
              <div>
                <p class="section-label">工作区</p>
                <h4>快捷跳转</h4>
              </div>
            </div>

            <RouterLink to="/workspace/project/board" class="shortcut-item shortcut-item--solo">
              <span class="shortcut-icon shortcut-icon--workspace" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                </svg>
              </span>
              <span class="shortcut-copy">
                <strong>返回项目空间</strong>
                <small>继续查看任务和进度</small>
              </span>
              <span class="shortcut-arrow" aria-hidden="true">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6" />
                </svg>
              </span>
            </RouterLink>
          </section>
        </div>
      </div>

      <KeepAlive>
        <ProfileTeamPanel v-if="activeTab === 'team'" class="profile-tab-panel" />
      </KeepAlive>
      <ProfileSecurityPanel v-if="activeTab === 'security'" class="profile-tab-panel" />
      </AppScrollArea>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AppScrollArea from '@/components/AppScrollArea.vue'
import GuestNotice from '@/components/GuestNotice.vue'
import ProfileSecurityPanel from '@/views/common/profile/components/ProfileSecurityPanel.vue'
import ProfileTeamPanel from '@/views/common/profile/components/ProfileTeamPanel.vue'
import { getProfileApi } from '@/api/account'

const route = useRoute()
const router = useRouter()

const profileTabs = [
  { id: 'overview', label: '资料概览' },
  { id: 'team', label: '我的小组' },
  { id: 'security', label: '安全设置' },
]

const profile = ref(null)
const loading = ref(false)
const errorMessage = ref('')

const getStorageUser = () => {
  const raw = localStorage.getItem('user') || sessionStorage.getItem('user')
  if (!raw) {
    return null
  }

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

const displayName = computed(() => profileSource.value?.realName || profileSource.value?.nickName || '未获取')
const titleText = computed(() => profileSource.value?.bio || '暂无个人简介')
const avatarText = computed(() => {
  const name = displayName.value
  return name && name !== '未获取' ? name.slice(0, 2) : '我'
})
const roleText = computed(() => profileSource.value?.role || '未获取')
const statusText = computed(() => (profileSource.value?.isActive === 1 ? '正常' : profileSource.value?.isActive === 0 ? '停用' : '未获取'))
const lastLoginText = computed(() => profileSource.value?.lastLoginAt || '未获取')

const profileFields = computed(() => [
  { label: '姓名', value: profileSource.value?.realName || profileSource.value?.nickName || '未获取' },
  { label: '账号', value: profileSource.value?.username || '未获取' },
  { label: '岗位', value: roleText.value },
  { label: '学号', value: profileSource.value?.studentNo || '未获取' },
  { label: '简介', value: profileSource.value?.bio || '暂无个人简介' },
  { label: '账号状态', value: statusText.value },
])

const resolveTab = (tab) => (tab === 'team' || tab === 'security' ? tab : 'overview')

const activeTab = computed(() => resolveTab(route.query.tab))

const setActiveTab = (tabId) => {
  const nextQuery = tabId === 'overview' ? {} : { tab: tabId }
  if (activeTab.value === tabId && JSON.stringify(route.query) === JSON.stringify(nextQuery)) return
  router.replace({ path: '/profile', query: nextQuery })
}

watch(
  () => route.query.tab,
  (tab) => {
    if (tab && tab !== 'team' && tab !== 'security') {
      router.replace({ path: '/profile' })
    }
  },
)

const loadProfile = async () => {
  if (!userId.value) {
    errorMessage.value = '未获取到用户信息，请重新登录后再试。'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    profile.value = await getProfileApi(userId.value)
  } catch (error) {
    errorMessage.value = error?.message || '个人信息加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-shell {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  min-width: 0;
  gap: 20px;
}

.profile-body {
  flex: 1;
  min-height: 0;
  background: var(--wb-bg-page);
}

.profile-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 32px 36px;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-hero-body {
  display: flex;
  align-items: center;
  gap: 22px;
  min-width: 0;
  z-index: 1;
}

.profile-avatar {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 22px;
  color: #ffffff;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.04em;
  background: linear-gradient(135deg, var(--wb-purple) 0%, var(--wb-purple-light) 100%);
  box-shadow: 0 12px 28px rgba(139, 92, 246, 0.28);
}

.profile-summary {
  min-width: 0;
}

.profile-summary h3,
.profile-panel h4 {
  margin: 0;
  color: var(--wb-text-primary);
  letter-spacing: -0.3px;
}

.profile-summary h3 {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.profile-subtitle {
  margin: 8px 0 0;
  color: var(--wb-text-secondary);
  font-size: 14px;
  line-height: 1.55;
  max-width: 48ch;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.profile-tag {
  padding: 5px 12px;
  border-radius: var(--wb-radius-pill);
  color: var(--wb-tag-backend-text);
  background: var(--wb-tag-backend-bg);
  font-size: 11.5px;
  font-weight: 600;
}

.profile-tag--primary {
  color: var(--wb-purple);
  background: var(--wb-purple-soft);
}

.profile-tag--muted {
  color: var(--wb-text-secondary);
  background: var(--wb-search-bg);
}

.profile-hero-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  z-index: 1;
}

.profile-hero-accent {
  position: absolute;
  top: 0;
  right: 0;
  width: 220px;
  height: 100%;
  pointer-events: none;
}

.profile-accent-orb {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.profile-accent-orb--1 {
  width: 100px;
  height: 100px;
  top: -20px;
  right: 24px;
  background: radial-gradient(circle at 35% 30%, rgba(255, 255, 255, 0.55), rgba(167, 139, 250, 0.35));
}

.profile-accent-orb--2 {
  width: 64px;
  height: 64px;
  bottom: 16px;
  right: 80px;
  background: radial-gradient(circle at 30% 25%, rgba(255, 255, 255, 0.5), rgba(139, 92, 246, 0.3));
}

.profile-accent-orb--3 {
  width: 36px;
  height: 36px;
  top: 40px;
  right: 120px;
  background: radial-gradient(circle at 35% 30%, rgba(255, 255, 255, 0.6), rgba(196, 181, 253, 0.25));
}

.profile-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  flex-shrink: 0;
}

.profile-tab {
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: var(--wb-radius-pill);
  background: transparent;
  color: var(--wb-text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.profile-tab:hover {
  color: var(--wb-purple);
  background: var(--wb-purple-soft);
}

.profile-tab.active {
  color: var(--wb-purple);
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.12);
}

.profile-tab-panel {
  display: grid;
  gap: 20px;
}

.profile-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(300px, 0.85fr);
  gap: 20px;
  align-items: stretch;
}

.profile-panel {
  padding: 26px 28px;
}

.profile-layout > .profile-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
}

.profile-panel h4 {
  font-size: 16px;
  font-weight: 700;
}

.section-label {
  margin: 0 0 6px;
  color: var(--wb-text-muted);
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 0;
}

.info-item {
  padding: 16px 18px;
  border-radius: 14px;
  border: 1px solid var(--wb-search-border);
  background: #fafafc;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.info-item:hover {
  border-color: var(--wb-purple-border);
  background: var(--wb-purple-soft);
}

.info-item dt {
  color: var(--wb-text-muted);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.4px;
  text-transform: uppercase;
  line-height: 1.4;
}

.info-item dd {
  margin: 8px 0 0;
  color: var(--wb-text-primary);
  font-size: 14.5px;
  font-weight: 600;
  line-height: 1.45;
  word-break: break-word;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid transparent;
  text-decoration: none;
  color: var(--wb-text-primary);
  background: #fafafc;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.shortcut-item--solo {
  width: 100%;
}

.shortcut-item:hover {
  background: var(--wb-purple-soft);
  border-color: rgba(196, 181, 253, 0.5);
  transform: translateX(2px);
}

.shortcut-icon {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: var(--wb-purple);
  background: var(--wb-purple-soft);
}

.shortcut-icon--workspace {
  color: var(--wb-icon-dark);
  background: var(--wb-search-bg);
}

.shortcut-copy {
  flex: 1;
  min-width: 0;
}

.shortcut-copy strong {
  display: block;
  margin-bottom: 3px;
  font-size: 13.5px;
  font-weight: 600;
}

.shortcut-copy small {
  display: block;
  color: var(--wb-text-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.shortcut-arrow {
  flex-shrink: 0;
  color: var(--wb-text-muted);
  transition: color 0.18s ease, transform 0.18s ease;
}

.shortcut-item:hover .shortcut-arrow {
  color: var(--wb-purple);
  transform: translateX(2px);
}

.profile-btn {
  height: 38px;
  padding: 0 18px;
  border-radius: var(--wb-radius-pill);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.18s ease, background 0.18s ease;
}

.profile-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.profile-btn--primary {
  color: #ffffff;
  border: none;
  background: var(--wb-btn-dark);
}

.profile-btn--primary:hover:not(:disabled) {
  opacity: 0.88;
}

.profile-btn--ghost {
  color: var(--wb-purple);
  border: 1.5px solid var(--wb-purple-border);
  background: transparent;
}

.profile-btn--ghost:hover:not(:disabled) {
  background: var(--wb-purple-soft);
}

.profile-btn--text {
  height: auto;
  padding: 6px 12px;
  color: var(--wb-purple);
  border: none;
  background: transparent;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.3px;
}

.profile-btn--text:hover {
  background: var(--wb-purple-soft);
}

@media (max-width: 1100px) {
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .profile-hero {
    flex-direction: column;
    align-items: stretch;
    padding: 24px;
  }

  .profile-hero-body {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-hero-actions {
    width: 100%;
  }

  .profile-hero-actions .profile-btn {
    flex: 1;
  }

  .profile-layout,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-hero-accent {
    width: 140px;
    opacity: 0.7;
  }
}
</style>
