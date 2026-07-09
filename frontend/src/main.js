import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { appPromptPlugin } from '@/plugins/appPrompt'
import { naiveUi } from '@/plugins/naiveUi'
import { initDictStore } from '@/stores/dictStore'
import 'jordium-gantt-vue3/dist/assets/jordium-gantt-vue3.css'
import './style.css'
import './styles/workbench.css'
import './styles/teaching.css'
import './styles/console.css'
import './styles/scrollbar.css'
import './styles/project-space.css'

const bootstrap = async () => {
  await initDictStore()
  const app = createApp(App)
  app.use(router)
  app.use(naiveUi)
  app.use(appPromptPlugin)
  app.mount('#app')
}

bootstrap().catch((error) => {
  console.error('应用初始化失败', error)
})
