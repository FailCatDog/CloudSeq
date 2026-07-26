<template>
  <!-- Auth is always violet-dark brand surface, independent of global light/dark toggle -->
  <section class="auth-page" data-theme="dark">
    <div class="auth-bg" aria-hidden="true">
      <div class="auth-bg__base"></div>
      <div
        v-for="tile in floatTiles"
        :key="tile.id"
        class="auth-bg__tile"
        :style="tile.style"
      />
    </div>

    <div class="auth-shell">
      <div class="auth-visual" aria-hidden="true">
        <div class="visual-orbit visual-orbit--one"></div>
        <div class="visual-orbit visual-orbit--two"></div>
        <div class="visual-orbit visual-orbit--three"></div>
        <div class="visual-mark">
          <BrandLogo :size="80" src="/logo.png" />
        </div>
        <div class="auth-brand-copy">
          <p class="auth-brand-name">{{ BRAND_FULL_NAME }}</p>
          <p class="auth-brand-slogan">{{ BRAND_SLOGAN }}</p>
          <p class="auth-brand-tagline">{{ BRAND_TAGLINE }}</p>
        </div>
      </div>

      <div class="auth-panel-wrap" :class="{ flipped: currentTab !== 'login' }">
        <div class="auth-panel auth-panel--front">
          <div class="auth-tabs" role="tablist" aria-label="认证方式">
            <button
              type="button"
              class="auth-tab"
              :class="{ active: currentTab === 'login' }"
              role="tab"
              :aria-selected="currentTab === 'login'"
              @click="goLogin"
            >
              登录
            </button>
            <button
              type="button"
              class="auth-tab"
              :class="{ active: currentTab === 'register' }"
              role="tab"
              :aria-selected="currentTab === 'register'"
              @click="goRegister"
            >
              注册
            </button>
          </div>

          <form class="auth-form" @submit.prevent="submitLogin">
            <div class="auth-form__body">
              <div class="field">
                <label for="login-username">用户名</label>
                <input
                  id="login-username"
                  v-model.trim="loginForm.username"
                  type="text"
                  autocomplete="username"
                  placeholder="请输入用户名"
                  :maxlength="USERNAME_MAX_LENGTH"
                >
              </div>
              <div class="field">
                <label for="login-password">密码</label>
                <input
                  id="login-password"
                  v-model="loginForm.password"
                  type="password"
                  autocomplete="current-password"
                  placeholder="请输入密码"
                  :maxlength="PASSWORD_MAX_LENGTH"
                >
              </div>
              <div class="form-row">
                <label class="check">
                  <input v-model="loginForm.remember" type="checkbox">
                  <span>记住我</span>
                </label>
                <button type="button" class="link-btn" @click="goReset">忘记密码？</button>
              </div>
              <p v-if="loginError" class="error-text">{{ loginError }}</p>
            </div>

            <div class="auth-form__actions">
              <button class="submit-btn" type="submit" :disabled="loading.login">
                <span class="submit-btn__label">{{ loading.login ? '登录中…' : '登录' }}</span>
              </button>
              <p class="auth-switch-hint">
                还没有账号？
                <button type="button" class="inline-link" @click="goRegister">立即注册</button>
              </p>
            </div>
          </form>
        </div>

        <div class="auth-panel auth-panel--back">
          <div class="auth-tabs" role="tablist" aria-label="认证方式">
            <button
              type="button"
              class="auth-tab"
              :class="{ active: currentTab === 'login' }"
              role="tab"
              :aria-selected="currentTab === 'login'"
              @click="goLogin"
            >
              登录
            </button>
            <button
              type="button"
              class="auth-tab"
              :class="{ active: currentTab === 'register' }"
              role="tab"
              :aria-selected="currentTab === 'register'"
              @click="goRegister"
            >
              注册
            </button>
          </div>

          <form class="auth-form auth-form--register" @submit.prevent="submitRegister">
            <div class="auth-form__body auth-form__sections">
              <section class="auth-form__section">
                <p class="auth-form__section-title">账号信息</p>
                <div class="field-grid field-grid--register">
                  <div class="field field--full">
                    <label for="register-username">用户名 <span class="field-required">*</span></label>
                    <input
                      id="register-username"
                      v-model.trim="registerForm.username"
                      type="text"
                      autocomplete="username"
                      placeholder="用于登录，建议学号或英文名"
                      :maxlength="USERNAME_MAX_LENGTH"
                    >
                  </div>
                  <div class="field field--full">
                    <label for="register-password">密码 <span class="field-required">*</span></label>
                    <input
                      id="register-password"
                      v-model="registerForm.password"
                      type="password"
                      autocomplete="new-password"
                      placeholder="至少 6 位，建议字母与数字组合"
                      :maxlength="PASSWORD_MAX_LENGTH"
                    >
                  </div>
                </div>
              </section>

              <section class="auth-form__section">
                <p class="auth-form__section-title">个人信息</p>
                <div class="field-grid field-grid--register">
                  <div class="field">
                    <label for="register-real-name">真实姓名</label>
                    <input
                      id="register-real-name"
                      v-model.trim="registerForm.realName"
                      type="text"
                      autocomplete="name"
                      placeholder="与学籍一致"
                      :maxlength="REAL_NAME_MAX_LENGTH"
                    >
                  </div>
                  <div class="field">
                    <label for="register-nick-name">昵称</label>
                    <input
                      id="register-nick-name"
                      v-model.trim="registerForm.nickName"
                      type="text"
                      placeholder="协作中显示的名称"
                      :maxlength="NICK_NAME_MAX_LENGTH"
                    >
                  </div>
                  <div class="field field--full">
                    <label for="register-student-no">学号 <span class="field-required">*</span></label>
                    <input
                      id="register-student-no"
                      v-model.trim="registerForm.studentNo"
                      type="text"
                      placeholder="请输入学号"
                      :maxlength="STUDENT_NO_MAX_LENGTH"
                    >
                  </div>
                </div>
              </section>

              <p v-if="registerError" class="error-text">{{ registerError }}</p>
            </div>

            <div class="auth-form__actions">
              <button class="submit-btn" type="submit" :disabled="loading.register">
                <span class="submit-btn__label">{{ loading.register ? '注册中…' : '创建账号' }}</span>
              </button>
              <p class="auth-switch-hint">
                已有账号？
                <button type="button" class="inline-link" @click="goLogin">返回登录</button>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>

    <p v-if="successMessage" class="success-toast">{{ successMessage }}</p>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi, registerApi } from '@/api/account'
import BrandLogo from '@/components/BrandLogo.vue'
import { setPermissionContext } from '@/utils/roleHome'
import { getHomePath } from '@/stores/permissionStore'
import { BRAND_FULL_NAME, BRAND_NAME, BRAND_SLOGAN, BRAND_TAGLINE } from '@/constants/brand'
import {
  NICK_NAME_MAX_LENGTH,
  PASSWORD_MAX_LENGTH,
  REAL_NAME_MAX_LENGTH,
  STUDENT_NO_MAX_LENGTH,
  USERNAME_MAX_LENGTH,
} from '@/constants/fieldLimits'

const router = useRouter()
const AUTH_STORAGE_KEY = 'authorization'
const USER_STORAGE_KEY = 'user'

const currentTab = ref('login')
const loading = reactive({ login: false, register: false })
const loginError = ref('')
const registerError = ref('')
const successMessage = ref('')

/** Rising square tiles for the auth page background */
const floatTiles = Array.from({ length: 28 }, (_, i) => {
  const size = 6 + ((i * 5) % 14)
  const left = ((i * 37) % 96) + 2
  const duration = 10 + ((i * 3) % 14)
  const delay = -((i * 1.7) % 16)
  const drift = ((i % 5) - 2) * 18
  const opacity = 0.18 + ((i % 6) * 0.06)
  return {
    id: i,
    style: {
      '--tile-size': `${size}px`,
      '--tile-left': `${left}%`,
      '--tile-duration': `${duration}s`,
      '--tile-delay': `${delay}s`,
      '--tile-drift': `${drift}px`,
      '--tile-opacity': String(opacity),
    },
  }
})

const loginForm = reactive({ username: '', password: '', remember: true })
const registerForm = reactive({
  username: '',
  password: '',
  role: 'STUDENT',
  studentNo: '',
  nickName: '',
  realName: '',
})

const submitLogin = async () => {
  loginError.value = ''
  successMessage.value = ''
  if (!loginForm.username || !loginForm.password) {
    loginError.value = '请输入用户名和密码'
    return
  }
  loading.login = true
  try {
    const data = await loginApi({ username: loginForm.username, password: loginForm.password })
    if (data?.authorization) {
      const storage = loginForm.remember ? localStorage : sessionStorage
      storage.setItem(AUTH_STORAGE_KEY, data.authorization)
      storage.setItem(USER_STORAGE_KEY, JSON.stringify(data.user || {}))
      await setPermissionContext(data, { remember: loginForm.remember })
    }
    successMessage.value = `登录成功，正在进入${BRAND_NAME}。`
    await router.push(data?.home || getHomePath())
  } catch (error) {
    loginError.value = error.message || '登录失败'
  } finally {
    loading.login = false
  }
}

const submitRegister = async () => {
  registerError.value = ''
  successMessage.value = ''
  if (!registerForm.username || !registerForm.password || !registerForm.studentNo) {
    registerError.value = '用户名、密码和学号不能为空'
    return
  }
  loading.register = true
  try {
    await registerApi(registerForm)
    successMessage.value = '注册成功，请返回登录。'
    currentTab.value = 'login'
  } catch (error) {
    registerError.value = error.message || '注册失败'
  } finally {
    loading.register = false
  }
}

const goRegister = () => {
  currentTab.value = 'register'
}

const goLogin = () => {
  currentTab.value = 'login'
}

const goReset = () => {
  router.push('/auth/reset')
}
</script>

<style lang="scss" scoped>
@use '@/styles/mixins' as *;

.auth-page {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: 28px;
  color-scheme: dark;
  background: #12101a;
  color: var(--wb-text-primary);
}

/* —— Floating square background —— */
.auth-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.auth-bg__base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 55% 45% at 18% 22%, rgba(139, 92, 246, 0.28), transparent 58%),
    radial-gradient(ellipse 50% 40% at 82% 78%, rgba(124, 58, 237, 0.22), transparent 55%),
    linear-gradient(160deg, #0c0a14 0%, #16122a 45%, #12101a 100%);
}

.auth-bg__tile {
  position: absolute;
  bottom: -40px;
  left: var(--tile-left);
  width: var(--tile-size);
  height: var(--tile-size);
  border-radius: 2px;
  background: linear-gradient(145deg, rgba(196, 181, 253, 0.75), rgba(139, 92, 246, 0.35));
  box-shadow: 0 0 12px rgba(139, 92, 246, 0.35);
  opacity: 0;
  will-change: transform, opacity;
  animation: auth-tile-rise var(--tile-duration) linear infinite;
  animation-delay: var(--tile-delay);
}

@keyframes auth-tile-rise {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
    opacity: 0;
  }

  12% {
    opacity: var(--tile-opacity);
  }

  78% {
    opacity: var(--tile-opacity);
  }

  100% {
    transform: translate3d(var(--tile-drift), calc(-100vh - 80px), 0) rotate(120deg);
    opacity: 0;
  }
}

@keyframes auth-orbit-spin {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }

  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

.auth-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 1060px);
  min-height: 700px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 520px);
  overflow: hidden;
  border: 1px solid rgba(196, 181, 253, 0.16);
  border-radius: 28px;
  background: var(--wb-card-bg);
  box-shadow:
    0 28px 80px rgba(0, 0, 0, 0.45),
    0 0 0 1px rgba(139, 92, 246, 0.12) inset;
}

.auth-visual {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 28% 22%, rgba(167, 139, 250, 0.28), transparent 34%),
    radial-gradient(circle at 72% 78%, rgba(124, 58, 237, 0.22), transparent 30%),
    linear-gradient(165deg, #0f0d16 0%, #1a1630 48%, #2a2150 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.visual-orbit {
  position: absolute;
  left: 50%;
  top: 42%;
  border-radius: 50%;
  border: 1px solid rgba(196, 181, 253, 0.22);
  transform: translate(-50%, -50%);
  will-change: transform;

  &::after {
    content: '';
    position: absolute;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #c4b5fd;
    box-shadow: 0 0 10px rgba(167, 139, 250, 0.8);
    top: 0;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  &--one {
    width: 200px;
    height: 200px;
    animation: auth-orbit-spin 12s linear infinite;
  }

  &--two {
    width: 280px;
    height: 280px;
    border-color: rgba(167, 139, 250, 0.16);
    animation: auth-orbit-spin 18s linear infinite reverse;

    &::after {
      width: 5px;
      height: 5px;
    }
  }

  &--three {
    width: 360px;
    height: 360px;
    border-color: rgba(139, 92, 246, 0.1);
    animation: auth-orbit-spin 26s linear infinite;

    &::after {
      width: 4px;
      height: 4px;
      opacity: 0.7;
    }
  }
}

.visual-mark {
  position: absolute;
  left: 50%;
  top: 42%;
  z-index: 1;
  transform: translate(-50%, -50%);
  display: grid;
  place-items: center;
  width: 108px;
  height: 108px;
  border-radius: 28px;
  background: rgba(31, 28, 43, 0.92);
  border: 1px solid rgba(196, 181, 253, 0.28);
  box-shadow:
    0 20px 44px rgba(0, 0, 0, 0.35),
    0 0 32px rgba(139, 92, 246, 0.25);
}

.auth-brand-copy {
  position: absolute;
  left: 50%;
  bottom: 72px;
  z-index: 1;
  transform: translateX(-50%);
  width: min(100%, 320px);
  text-align: center;
  padding: 0 24px;
}

.auth-brand-name,
.auth-brand-slogan,
.auth-brand-tagline {
  margin: 0;
}

.auth-brand-name {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #f4f2fa;
}

.auth-brand-slogan {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #c4b5fd;
}

.auth-brand-tagline {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(226, 232, 240, 0.72);
}

.auth-panel-wrap {
  position: relative;
  perspective: 1800px;
  min-height: 700px;

  &.flipped {
    .auth-panel--front {
      transform: rotateY(-180deg);
    }

    .auth-panel--back {
      transform: rotateY(0deg);
    }
  }
}

.auth-panel {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  padding: 28px 32px 32px;
  backface-visibility: hidden;
  transform-style: preserve-3d;
  background: #1f1c2b;
  transition: transform 520ms cubic-bezier(0.2, 0.8, 0.2, 1);
  overflow: hidden;

  &--front {
    transform: rotateY(0deg);
  }

  &--back {
    transform: rotateY(180deg);
  }
}

.auth-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  padding: 4px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: #2a2640;
  border: 1px solid #3d3a52;
  flex-shrink: 0;
}

.auth-tab {
  height: 40px;
  border-radius: var(--wb-radius-sm);
  background: transparent;
  color: #a8a4bc;
  font-size: 14px;
  font-weight: 600;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;

  &.active {
    background: #16141f;
    color: #c4b5fd;
    box-shadow: 0 2px 8px rgba(139, 92, 246, 0.2);
  }
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
  min-height: 0;

  &__body {
    display: grid;
    gap: 14px;
    min-height: 0;
  }

  &--register {
    .auth-form__body {
      flex: 1;
      min-height: 0;
    }

    .auth-form__actions {
      background: #1f1c2b;
    }
  }

  &__sections {
    padding-bottom: 0;
    display: grid;
    gap: 14px;
  }

  &__section {
    display: grid;
    gap: 12px;
  }

  &__section-title {
    margin: 0;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: #a78bfa;
  }

  &__actions {
    display: grid;
    gap: 12px;
    margin-top: auto;
    padding-top: 10px;
    flex-shrink: 0;
  }
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;

  &--register .field--full {
    grid-column: 1 / -1;
  }
}

.field {
  display: grid;
  gap: 8px;

  label {
    font-size: 13px;
    font-weight: 600;
    color: #f4f2fa;
  }

  input,
  textarea,
  select {
    width: 100%;
    border: 1px solid #3d3a52;
    border-radius: var(--wb-radius-sm);
    padding: 12px 14px;
    background: #2a2640;
    color: #f4f2fa;
    transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
  }

  input {
    &::placeholder {
      color: #7a7690;
    }

    &:hover {
      border-color: #5b4b8a;
      background: #252238;
    }

    &:focus {
      @include focus-ring($color: #a78bfa, $width: 4px);
      background: #252238;
    }
  }
}

.field-required {
  color: #fdba74;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #a8a4bc;
  font-size: 13px;
  cursor: pointer;

  input {
    width: 16px;
    height: 16px;
    accent-color: #8b5cf6;
  }
}

.link-btn,
.inline-link {
  background: transparent;
  color: #a78bfa;
  padding: 0;
  font-weight: 600;
  font-size: 13px;

  &:hover {
    color: #c4b5fd;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--wb-radius-sm);
  background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 42%, #7c3aed 100%);
  color: #f8f5ff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: 0 12px 28px rgba(124, 58, 237, 0.38);
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 16px 32px rgba(124, 58, 237, 0.48);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
    box-shadow: 0 8px 18px rgba(124, 58, 237, 0.32);
  }

  &:disabled {
    opacity: 0.72;
    cursor: not-allowed;
    transform: none;
  }

  &__label {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 4em;
  }
}

.auth-switch-hint {
  margin: 0;
  text-align: center;
  font-size: 13px;
  color: #a8a4bc;
}

.error-text,
.success-toast {
  margin: 0;
  font-size: 13px;
}

.error-text {
  padding: 10px 12px;
  border-radius: var(--wb-radius-sm);
  background: rgba(220, 38, 38, 0.12);
  border: 1px solid rgba(252, 165, 165, 0.45);
  color: #fca5a5;
}

.success-toast {
  position: fixed;
  left: 50%;
  top: 28px;
  z-index: 2;
  transform: translateX(-50%);
  padding: 12px 18px;
  border-radius: var(--wb-radius-pill);
  background: #1f1c2b;
  border: 1px solid rgba(134, 239, 172, 0.45);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
  color: #86efac;
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .auth-visual {
    min-height: 260px;
    border-right: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  .auth-panel-wrap {
    min-height: 680px;
  }

  .field-grid,
  .field-grid--register {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-panel,
  .submit-btn {
    transition: none;
  }

  .auth-bg__tile,
  .visual-orbit {
    animation: none !important;
  }

  .auth-bg__tile {
    opacity: 0.22;
    bottom: auto;
    top: var(--tile-left);
    transform: none;
  }
}
</style>
