import { createRouter, createWebHistory } from 'vue-router'
import { buildPageTitle } from '@/constants/brand'
import { resolveNavigationGuard } from '@/utils/roleHome'
import AppLayout from '../layout/AppLayout.vue'
import StudentLayout from '../views/student/index.vue'
import StudentDashboard from '../views/student/dashboard/index.vue'
import ProjectLayout from '../views/student/project/ProjectLayout.vue'
import ProjectBoard from '../views/student/project/board/index.vue'
import ProjectDocPage from '../views/student/project/doc/index.vue'
import StudentGantt from '../views/student/gantt/index.vue'
import StudentWeekly from '../views/student/weekly/index.vue'
import TeacherLayout from '../views/teacher/index.vue'
import TeachingDashboard from '../views/teacher/dashboard/index.vue'
import TeachingApprovals from '../views/teacher/approvals/index.vue'
import TeachingTeams from '../views/teacher/teams/index.vue'
import TeachingReports from '../views/teacher/reports/index.vue'
import TeachingCourses from '../views/teacher/courses/index.vue'
import TeachingStudents from '../views/teacher/students/index.vue'
import AdminLayout from '../views/admin/index.vue'
import AdminDashboard from '../views/admin/dashboard/index.vue'
import MessagesView from '../views/common/messages/index.vue'
import AuthView from '../views/common/auth/index.vue'
import ProfileView from '../views/common/profile/index.vue'
import PrepareView from '../views/student/prepare/index.vue'

const routes = [
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', redirect: '/workspace' },
      {
        path: 'workspace',
        component: StudentLayout,
        children: [
          { path: '', redirect: '/workspace/dashboard' },
          { path: 'dashboard', component: StudentDashboard, meta: { title: '工作台' } },
          {
            path: 'project',
            component: ProjectLayout,
            meta: { title: '项目空间' },
            redirect: '/workspace/project/board',
            children: [
              { path: 'board', name: 'project-board', component: ProjectBoard, meta: { title: '数据看板' } },
              { path: 'gantt', name: 'project-gantt', component: StudentGantt, meta: { title: '任务甘特图' } },
              { path: 'weekly', name: 'project-weekly', component: StudentWeekly, meta: { title: '周报' } },
              {
                path: 'doc/:nodeId',
                name: 'project-doc',
                component: ProjectDocPage,
                meta: { title: '项目文档' },
              },
              {
                path: 'sheet/:nodeId',
                name: 'project-sheet',
                component: () => import('../views/student/project/sheet/index.vue'),
                meta: { title: '项目表格' },
              },
            ],
          },
          { path: 'gantt', redirect: '/workspace/project/gantt' },
          { path: 'weekly', redirect: '/workspace/project/weekly' },
        ],
      },
      {
        path: 'teaching',
        component: TeacherLayout,
        meta: { title: '教师端' },
        children: [
          { path: '', redirect: '/teaching/dashboard' },
          { path: 'dashboard', component: TeachingDashboard, meta: { title: '教学工作台' } },
          { path: 'courses', component: TeachingCourses, meta: { title: '课号管理' } },
          { path: 'students', component: TeachingStudents, meta: { title: '学生管理' } },
          { path: 'approvals', component: TeachingApprovals, meta: { title: '选题审批' } },
          { path: 'teams', component: TeachingTeams, meta: { title: '小组总览' } },
          { path: 'reports', component: TeachingReports, meta: { title: '周报审阅' } },
        ],
      },
      {
        path: 'admin',
        component: AdminLayout,
        meta: { title: '管理端' },
        children: [
          { path: '', redirect: '/admin/dashboard' },
          { path: 'dashboard', component: AdminDashboard, meta: { title: '管理台' } },
        ],
      },
      { path: 'messages', component: MessagesView, meta: { title: '消息' } },
      { path: 'prepare', component: PrepareView, meta: { title: '课程准备' } },
      {
        path: 'profile',
        children: [
          { path: '', component: ProfileView, meta: { title: '个人中心' } },
          {
            path: 'team',
            redirect: () => ({ path: '/prepare' }),
          },
          {
            path: 'security',
            redirect: () => ({ path: '/profile' }),
          },
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

router.beforeEach((to) => {
  const redirect = resolveNavigationGuard(to.path)
  if (redirect && redirect !== to.path) {
    return redirect
  }
})

router.afterEach((to) => {
  let title = to.meta.title
  if (to.path === '/profile') {
    if (to.query.tab === 'team') title = '我的小组'
    else title = '个人中心'
  }
  document.title = buildPageTitle(title)
})

export default router
