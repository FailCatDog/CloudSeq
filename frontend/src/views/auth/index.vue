<template>
  <section class="auth-page">
    <div class="auth-shell">
      <div class="auth-visual" aria-hidden="true">
        <div class="visual-orbit visual-orbit--one"></div>
        <div class="visual-orbit visual-orbit--two"></div>
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
          <form class="auth-form" @submit.prevent="submitLogin">
            <h1>登录</h1>
            <p class="auth-lead">登录{{ BRAND_NAME }}，开始你的项目协作。</p>

            <div class="field">
              <label for="login-username">用户名</label>
              <input id="login-username" v-model.trim="loginForm.username" type="text" autocomplete="username" />
            </div>
            <div class="field">
              <label for="login-password">密码</label>
              <input id="login-password" v-model="loginForm.password" type="password" autocomplete="current-password" />
            </div>
            <div class="form-row">
              <label class="check">
                <input v-model="loginForm.remember" type="checkbox" />
                <span>记住我</span>
              </label>
              <button type="button" class="link-btn" @click="goRegister">去注册</button>
            </div>
            <p v-if="loginError" class="error-text">{{ loginError }}</p>
            <button class="submit-btn" type="submit" :disabled="loading.login">
              {{ loading.login ? '登录中...' : '登录' }}
            </button>
            <button type="button" class="text-link" @click="goReset">忘记密码</button>
          </form>
        </div>

        <div class="auth-panel auth-panel--back">
          <form class="auth-form" @submit.prevent="submitRegister">
            <h1>注册</h1>
            <p class="auth-lead">加入{{ BRAND_NAME }}，开启你的软件项目管理之旅。</p>
            <div class="field-grid">
              <div class="field">
                <label for="register-username">用户名</label>
                <input id="register-username" v-model.trim="registerForm.username" type="text" autocomplete="username" />
              </div>
              <div class="field">
                <label for="register-password">密码</label>
                <input id="register-password" v-model="registerForm.password" type="password" autocomplete="new-password" />
              </div>
              <div class="field">
                <label for="register-real-name">真实姓名</label>
                <input id="register-real-name" v-model.trim="registerForm.realName" type="text" autocomplete="name" />
              </div>
              <div class="field">
                <label for="register-nick-name">昵称</label>
                <input id="register-nick-name" v-model.trim="registerForm.nickName" type="text" />
              </div>
              <div class="field">
                <label for="register-student-no">学号</label>
                <input id="register-student-no" v-model.trim="registerForm.studentNo" type="text" />
              </div>
            </div>
            <p v-if="registerError" class="error-text">{{ registerError }}</p>
            <button class="submit-btn" type="submit" :disabled="loading.register">
              {{ loading.register ? '注册中...' : '注册' }}
            </button>
            <button type="button" class="text-link" @click="goLogin">返回登录</button>
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
import { BRAND_FULL_NAME, BRAND_NAME, BRAND_SLOGAN, BRAND_TAGLINE } from '@/constants/brand'

const router = useRouter()
const AUTH_STORAGE_KEY = 'authorization'
const USER_STORAGE_KEY = 'user'

const currentTab = ref('login')
const loading = reactive({ login: false, register: false })
const loginError = ref('')
const registerError = ref('')
const successMessage = ref('')

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
    }
    successMessage.value = `登录成功，正在进入${BRAND_NAME}。`
    await router.push('/workspace/dashboard')
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

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
}

.auth-shell {
  width: min(100%, 1060px);
  min-height: 700px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 520px);
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: var(--shadow);
}

.auth-visual {
  position: relative;
  background:
    radial-gradient(circle at 30% 20%, rgba(48, 75, 140, 0.24), transparent 30%),
    radial-gradient(circle at 70% 80%, rgba(15, 118, 110, 0.16), transparent 28%),
    linear-gradient(160deg, #f8fbff 0%, #e8eef8 100%);
  border-right: 1px solid var(--line);
}

.visual-orbit {
  position: absolute;
  left: 50%;
  top: 50%;
  border-radius: 999px;
  border: 1px solid rgba(48, 75, 140, 0.16);
  transform: translate(-50%, -50%);
}

.visual-orbit--one {
  width: 220px;
  height: 220px;
}

.visual-orbit--two {
  width: 320px;
  height: 320px;
}

.visual-mark {
  position: absolute;
  left: 50%;
  top: 42%;
  transform: translate(-50%, -50%);
  display: grid;
  place-items: center;
  width: 108px;
  height: 108px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 18px 40px rgba(124, 58, 237, 0.16);
}

.auth-brand-copy {
  position: absolute;
  left: 50%;
  bottom: 72px;
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
  color: #1a1a2e;
}

.auth-brand-slogan {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #7c3aed;
}

.auth-brand-tagline {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #66758f;
}

.auth-panel-wrap {
  position: relative;
  perspective: 1800px;
}

.auth-panel {
  position: absolute;
  inset: 0;
  padding: 34px;
  backface-visibility: hidden;
  transform-style: preserve-3d;
  background: rgba(255, 255, 255, 0.96);
  transition: transform 520ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

.auth-panel--front {
  transform: rotateY(0deg);
}

.auth-panel--back {
  transform: rotateY(180deg);
}

.auth-panel-wrap.flipped .auth-panel--front {
  transform: rotateY(-180deg);
}

.auth-panel-wrap.flipped .auth-panel--back {
  transform: rotateY(0deg);
}

.auth-form {
  display: grid;
  gap: 14px;
  align-content: center;
  height: 100%;
}

.auth-form h1 {
  margin: 0 0 4px;
  font-size: 32px;
  line-height: 1.1;
}

.auth-lead {
  margin: 0 0 8px;
  color: var(--muted);
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
}

.field label {
  font-size: 13px;
  font-weight: 600;
}

.field input,
.field textarea,
.field select {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 12px 14px;
  background: var(--surface);
  color: var(--text);
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
  color: var(--muted);
}

.link-btn,
.text-link {
  background: transparent;
  color: var(--primary);
  padding: 0;
  font-weight: 600;
}

.submit-btn {
  height: 44px;
  border-radius: 14px;
  background: var(--primary);
  color: white;
  font-weight: 700;
}

.text-link {
  justify-self: start;
}

.error-text,
.success-toast {
  margin: 0;
  font-size: 13px;
}

.error-text {
  color: oklch(52% 0.16 25);
}

.success-toast {
  position: fixed;
  left: 50%;
  top: 28px;
  transform: translateX(-50%);
  padding: 12px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  color: oklch(50% 0.12 150);
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .auth-visual {
    min-height: 260px;
    border-right: 0;
    border-bottom: 1px solid var(--line);
  }

  .auth-panel-wrap {
    min-height: 680px;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-panel {
    transition: none;
  }
}
</style>
