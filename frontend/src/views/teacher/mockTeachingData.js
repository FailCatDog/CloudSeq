/** 教师端静态演示数据，后续接入 /api/teaching/* */

export const APPROVAL_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
}

export const TEAM_OVERVIEW_STATUS = {
  ACTIVE: 'active',
  PENDING: 'pending',
  AT_RISK: 'at_risk',
}

export const MOCK_APPROVALS = [
  {
    id: 1,
    teamLabel: '第 3 组',
    topicTitle: '校园社团协作系统',
    topicDesc:
      '面向校内社团的活动发布、成员招募与物资借用管理，支持移动端访问。系统需包含社团认证、活动日历、消息通知三个核心模块，并与学校统一身份认证对接。',
    leaderName: '李明',
    leaderNo: '2023010101',
    memberCount: 4,
    members: '李明、张婷、周凯、孙悦',
    submittedAt: '今天 11:20',
    submittedAtFull: '2026-07-07 11:20',
    status: APPROVAL_STATUS.PENDING,
  },
  {
    id: 2,
    teamLabel: '第 7 组',
    topicTitle: '在线考试防作弊方案',
    topicDesc:
      '基于行为分析与设备指纹的在线考试监考辅助方案，支持切屏检测、异常操作告警与事后回放审计。',
    leaderName: '王芳',
    leaderNo: '2023010208',
    memberCount: 5,
    members: '王芳、刘畅、何静、马超、林溪',
    submittedAt: '昨天 18:32',
    submittedAtFull: '2026-07-06 18:32',
    status: APPROVAL_STATUS.PENDING,
  },
  {
    id: 3,
    teamLabel: '第 11 组',
    topicTitle: '实验室设备预约平台',
    topicDesc:
      '面向学院实验室的设备在线预约与使用记录管理，含审批流、冲突检测与使用统计报表。',
    leaderName: '陈浩',
    leaderNo: '2023010315',
    memberCount: 4,
    members: '陈浩、黄悦、宋琪、唐磊',
    submittedAt: '昨天 09:15',
    submittedAtFull: '2026-07-06 09:15',
    status: APPROVAL_STATUS.PENDING,
  },
  {
    id: 4,
    teamLabel: '第 1 组',
    topicTitle: '课程资源分享社区',
    topicDesc: '课程资料上传、检索与评论社区，支持多格式预览与权限分级。',
    leaderName: '赵磊',
    leaderNo: '2023010103',
    memberCount: 4,
    members: '赵磊、吴敏、郑强、钱程',
    submittedAt: '03-28 14:00',
    submittedAtFull: '2026-03-28 14:00',
    status: APPROVAL_STATUS.APPROVED,
  },
  {
    id: 5,
    teamLabel: '第 6 组',
    topicTitle: '校园失物招领小程序',
    topicDesc: '失物发布、认领与消息通知，对接校园统一身份认证。',
    leaderName: '刘洋',
    leaderNo: '2023010112',
    memberCount: 3,
    members: '刘洋、孙浩、周敏',
    submittedAt: '03-25 10:42',
    submittedAtFull: '2026-03-25 10:42',
    status: APPROVAL_STATUS.REJECTED,
    rejectReason: '选题范围与课程要求偏差较大，建议聚焦软件项目管理流程本身。',
  },
]

export const MOCK_TEAMS = [
  {
    id: 1,
    teamLabel: '第 1 组',
    topicTitle: '课程资源分享社区',
    status: TEAM_OVERVIEW_STATUS.ACTIVE,
    memberCount: 4,
    progressPercent: 72,
    lastWeeklyLabel: '第 12 周',
    actionLabel: '巡查项目',
    actionTo: '/teaching/teams',
  },
  {
    id: 2,
    teamLabel: '第 2 组',
    topicTitle: '课程问答社区',
    status: TEAM_OVERVIEW_STATUS.ACTIVE,
    memberCount: 4,
    progressPercent: 68,
    lastWeeklyLabel: '第 12 周',
    actionLabel: '巡查项目',
    actionTo: '/teaching/teams',
  },
  {
    id: 3,
    teamLabel: '第 3 组',
    topicTitle: '校园社团协作系统',
    status: TEAM_OVERVIEW_STATUS.PENDING,
    memberCount: 4,
    progressPercent: null,
    lastWeeklyLabel: '—',
    actionLabel: '去审批',
    actionTo: '/teaching/approvals',
  },
  {
    id: 5,
    teamLabel: '第 5 组',
    topicTitle: '智慧校园导航',
    status: TEAM_OVERVIEW_STATUS.AT_RISK,
    memberCount: 4,
    progressPercent: 35,
    lastWeeklyLabel: '第 10 周',
    actionLabel: '巡查项目',
    actionTo: '/teaching/teams',
  },
  {
    id: 7,
    teamLabel: '第 7 组',
    topicTitle: '在线考试防作弊方案',
    status: TEAM_OVERVIEW_STATUS.PENDING,
    memberCount: 5,
    progressPercent: null,
    lastWeeklyLabel: '—',
    actionLabel: '去审批',
    actionTo: '/teaching/approvals',
  },
  {
    id: 8,
    teamLabel: '第 8 组',
    topicTitle: '二手教材交易平台',
    status: TEAM_OVERVIEW_STATUS.AT_RISK,
    memberCount: 4,
    progressPercent: 52,
    lastWeeklyLabel: '第 10 周',
    actionLabel: '巡查项目',
    actionTo: '/teaching/teams',
  },
  {
    id: 11,
    teamLabel: '第 11 组',
    topicTitle: '实验室设备预约平台',
    status: TEAM_OVERVIEW_STATUS.PENDING,
    memberCount: 4,
    progressPercent: null,
    lastWeeklyLabel: '—',
    actionLabel: '去审批',
    actionTo: '/teaching/approvals',
  },
]

export const MOCK_REPORT_WEEKS = [12, 11, 10]

export const MOCK_REPORT_TREE = [
  {
    id: 'team-1',
    teamLabel: '第 1 组',
    topicTitle: '课程资源分享社区',
    expanded: true,
    reports: [
      {
        id: 'r-1-12-zl',
        memberName: '赵磊',
        week: 12,
        submittedAt: '2026-07-06 21:34',
        status: 'submitted',
        sections: {
          done: '完成了资源上传模块的前端页面与后端接口联调，支持 PDF、PPT 两种格式的预览；修复了分页列表在 Safari 下的样式问题；与第 2 组同学讨论了统一认证对接方案。',
          plan: '实现资源评论与点赞功能；编写单元测试覆盖上传校验逻辑；准备中期演示 PPT 初稿。',
          risk: '对象存储 CDN 缓存策略尚未与运维确认，可能影响大文件预览速度。已与组长同步，计划周三前拿到结论。',
        },
      },
      {
        id: 'r-1-12-wm',
        memberName: '吴敏',
        week: 12,
        submittedAt: '2026-07-06 20:18',
        status: 'submitted',
        sections: {
          done: '完成资源检索接口与 Elasticsearch 索引结构设计；编写检索性能测试脚本。',
          plan: '优化热门资源缓存策略；补充 API 文档。',
          risk: '暂无。',
        },
      },
      {
        id: 'r-1-12-zq',
        memberName: '郑强',
        week: 12,
        submittedAt: '2026-07-06 19:45',
        status: 'submitted',
        sections: {
          done: '完成用户权限模块 RBAC 设计与实现；对接统一身份认证 OAuth 流程。',
          plan: '编写权限相关集成测试；协助前端联调登录态。',
          risk: 'OAuth 回调域名白名单申请中，可能延迟 1 天。',
        },
      },
    ],
  },
  {
    id: 'team-2',
    teamLabel: '第 2 组',
    topicTitle: '课程问答社区',
    expanded: false,
    reports: [
      {
        id: 'r-2-12-lm',
        memberName: '李萌',
        week: 12,
        submittedAt: '2026-07-06 22:10',
        status: 'submitted',
        sections: {
          done: '问答列表分页与搜索功能上线；修复移动端适配问题。',
          plan: '实现回答采纳与积分机制。',
          risk: '暂无。',
        },
      },
    ],
  },
  {
    id: 'team-5',
    teamLabel: '第 5 组',
    topicTitle: '智慧校园导航',
    expanded: false,
    reports: [
      {
        id: 'r-5-12-cx',
        memberName: '陈欣',
        week: 12,
        submittedAt: '2026-07-05 18:20',
        status: 'submitted',
        sections: {
          done: '完成室内地图 POI 标注模块。',
          plan: '路径规划算法优化。',
          risk: '组内 2 人连续未提交周报，已私下提醒。',
        },
      },
    ],
  },
  {
    id: 'team-8',
    teamLabel: '第 8 组',
    topicTitle: '二手教材交易平台',
    expanded: false,
    reports: [
      {
        id: 'r-8-12-hy',
        memberName: '韩宇',
        week: 12,
        submittedAt: '2026-07-06 17:55',
        status: 'submitted',
        sections: {
          done: '交易订单状态机重构；支付回调幂等处理。',
          plan: '消息通知模块开发。',
          risk: '3 项任务逾期，需组内重新排期。',
        },
      },
    ],
  },
]

export const approvalStatusLabel = (status) => {
  if (status === APPROVAL_STATUS.APPROVED) return '已通过'
  if (status === APPROVAL_STATUS.REJECTED) return '已驳回'
  return '待审'
}

export const approvalStatusTagClass = (status) => {
  if (status === APPROVAL_STATUS.APPROVED) return 'tch-tag--success'
  if (status === APPROVAL_STATUS.REJECTED) return 'tch-tag--danger'
  return 'tch-tag--pending'
}

export const teamStatusLabel = (status) => {
  if (status === TEAM_OVERVIEW_STATUS.ACTIVE) return '在研'
  if (status === TEAM_OVERVIEW_STATUS.AT_RISK) return '需关注'
  return '待审'
}

export const teamStatusTagClass = (status) => {
  if (status === TEAM_OVERVIEW_STATUS.ACTIVE) return 'tch-tag--success'
  if (status === TEAM_OVERVIEW_STATUS.AT_RISK) return 'tch-tag--warning'
  return 'tch-tag--pending'
}

export const progressFillClass = (percent) => {
  if (percent == null) return ''
  if (percent < 40) return 'wb-progress-fill--danger'
  if (percent < 60) return 'wb-progress-fill--warning'
  return ''
}
