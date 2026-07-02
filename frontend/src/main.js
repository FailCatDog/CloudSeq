import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { appPromptPlugin } from '@/plugins/appPrompt'
import 'jordium-gantt-vue3/dist/assets/jordium-gantt-vue3.css'
import './style.css'
import './styles/workbench.css'
import './styles/scrollbar.css'
import './styles/project-space.css'

const app = createApp(App)
app.use(router)
app.use(appPromptPlugin)
app.mount('#app')
