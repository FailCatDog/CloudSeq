import { createRouter, createWebHistory } from 'vue-router'
import { buildPageTitle } from '@/constants/brand'
import AppLayout from '../layout/AppLayout.vue'
import WorkspaceLayout from '../views/workspace/index.vue'
import WorkspaceDashboard from '../views/workspace/dashboard/index.vue'
import ProjectLayout from '../views/workspace/project/ProjectLayout.vue'
import ProjectBoard from '../views/workspace/project/board/index.vue'
import ProjectDocPage from '../views/workspace/project/doc/index.vue'
import WorkspaceGantt from '../views/workspace/gantt/index.vue'
import WorkspaceWeekly from '../views/workspace/weekly/index.vue'
import MessagesView from '../views/messages/index.vue'
import AuthView from '../views/auth/index.vue'
import ProfileView from '../views/profile/index.vue'
import ProfileSecurityView from '../views/profile/security.vue'
import ProfileTeamView from '../views/profile/team.vue'

const routes = [
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', redirect: '/workspace' },
      {
        path: 'workspace',
        component: WorkspaceLayout,
        children: [
          { path: '', redirect: '/workspace/dashboard' },
          { path: 'dashboard', component: WorkspaceDashboard, meta: { title: '工作台' } },
          {
            path: 'project',
            component: ProjectLayout,
            meta: { title: '项目空间' },
            redirect: '/workspace/project/board',
            children: [
              { path: 'board', name: 'project-board', component: ProjectBoard, meta: { title: '数据看板' } },
              { path: 'gantt', name: 'project-gantt', component: WorkspaceGantt, meta: { title: '任务甘特图' } },
              { path: 'weekly', name: 'project-weekly', component: WorkspaceWeekly, meta: { title: '周报' } },
              {
                path: 'doc/:nodeId',
                name: 'project-doc',
                component: ProjectDocPage,
                meta: { title: '项目文档' },
              },
            ],
          },
          { path: 'gantt', redirect: '/workspace/project/gantt' },
          { path: 'weekly', redirect: '/workspace/project/weekly' },
        ],
      },
      { path: 'messages', component: MessagesView, meta: { title: '消息' } },
      {
        path: 'profile',
        children: [
          { path: '', component: ProfileView, meta: { title: '个人中心' } },
          { path: 'security', component: ProfileSecurityView, meta: { title: '安全设置' } },
          { path: 'team', component: ProfileTeamView, meta: { title: '我的小组' } },
        ],
      },
    ],
  },
  { path: '/auth', component: AuthView, meta: { title: '登录' } },
  { path: '/auth/register', redirect: '/auth' },
  { path: '/auth/reset', redirect: '/auth' },
  { path: '/login', redirect: '/auth' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  document.title = buildPageTitle(to.meta.title)
})

export default router
