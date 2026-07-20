package cn.guet.soft_manage.biz.teaching.service.impl;

import cn.guet.soft_manage.biz.course.dao.CourseDao;
import cn.guet.soft_manage.biz.course.entity.Course;
import cn.guet.soft_manage.biz.rbac.service.IDataScopeService;
import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardAtRiskTeamDTO;
import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardCourseDTO;
import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardPendingItemDTO;
import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardResponseDTO;
import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardStatsDTO;
import cn.guet.soft_manage.biz.teaching.service.TeachingDashboardService;
import cn.guet.soft_manage.biz.team.dao.TeamDao;
import cn.guet.soft_manage.biz.team.dao.TeamMemberDao;
import cn.guet.soft_manage.biz.team.dao.TopicApprovalDao;
import cn.guet.soft_manage.biz.team.entity.Team;
import cn.guet.soft_manage.biz.team.entity.TeamMember;
import cn.guet.soft_manage.biz.team.entity.TopicApproval;
import cn.guet.soft_manage.biz.user.dao.UserDao;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.biz.workspace.dao.PlanTaskDao;
import cn.guet.soft_manage.biz.workspace.dao.WeeklyReportDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.workspace.entity.PlanTask;
import cn.guet.soft_manage.biz.workspace.entity.WeeklyReport;
import cn.guet.soft_manage.biz.workspace.entity.Workspace;
import cn.guet.soft_manage.biz.workspace.util.IsoWeekUtil;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教学工作台服务实现
 */
@Service
public class TeachingDashboardServiceImpl implements TeachingDashboardService {

    private static final int PENDING_LIMIT = 5;
    private static final int AT_RISK_LIMIT = 3;

    @Resource
    private CourseDao courseDao;

    @Resource
    private TeamDao teamDao;

    @Resource
    private TeamMemberDao teamMemberDao;

    @Resource
    private TopicApprovalDao topicApprovalDao;

    @Resource
    private UserDao userDao;

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private WeeklyReportDao weeklyReportDao;

    @Resource
    private PlanTaskDao planTaskDao;

    @Resource
    private IDataScopeService dataScopeService;

    @Override
    public TeachingDashboardResponseDTO getDashboard(Long courseId) {
        Long teacherId = UserContext.getUserId();
        if (teacherId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        List<Long> accessibleCourseIds = dataScopeService.resolveCourseIds(teacherId);
        if (accessibleCourseIds.isEmpty()) {
            return emptyDashboard();
        }

        List<Long> scopeCourseIds;
        Course heroCourse;
        if (courseId != null) {
            if (!accessibleCourseIds.contains(courseId)) {
                throw new BusinessException(BizResponseCode.FORBIDDEN);
            }
            scopeCourseIds = List.of(courseId);
            heroCourse = courseDao.selectById(courseId);
        } else {
            scopeCourseIds = accessibleCourseIds;
            heroCourse = courseDao.selectById(accessibleCourseIds.get(0));
        }

        if (heroCourse == null) {
            return emptyDashboard();
        }

        List<Team> teams = teamDao.selectList(new LambdaQueryWrapper<Team>()
                .in(Team::getCourseId, scopeCourseIds));
        if (teams.isEmpty()) {
            return TeachingDashboardResponseDTO.builder()
                    .course(toCourseDto(heroCourse))
                    .stats(emptyStats())
                    .pendingApprovals(Collections.emptyList())
                    .atRiskTeams(Collections.emptyList())
                    .build();
        }

        List<Long> teamIds = teams.stream().map(Team::getId).filter(Objects::nonNull).toList();
        Map<Long, Team> teamMap = teams.stream()
                .collect(Collectors.toMap(Team::getId, Function.identity(), (left, right) -> left));

        Map<Long, Long> memberCountByTeam = teamMemberDao.selectList(new LambdaQueryWrapper<TeamMember>()
                        .in(TeamMember::getTeamId, teamIds)
                        .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode()))
                .stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        Set<Long> leaderIds = teams.stream()
                .map(Team::getLeaderUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> leaderMap = leaderIds.isEmpty()
                ? Collections.emptyMap()
                : userDao.selectBatchIds(new ArrayList<>(leaderIds)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));

        List<TopicApproval> pendingApprovals = teamIds.isEmpty()
                ? Collections.emptyList()
                : topicApprovalDao.selectList(new LambdaQueryWrapper<TopicApproval>()
                        .in(TopicApproval::getTeamId, teamIds)
                        .eq(TopicApproval::getApprovalStatus, CacheCode.APPROVAL_STATUS_PENDING.getCode())
                        .orderByDesc(TopicApproval::getSubmitDate));

        List<Team> unlockedTeams = teams.stream()
                .filter(team -> Objects.equals(team.getStatus(), CacheCode.TEAM_STATUS_UNLOCKED.getCode()))
                .toList();
        List<Long> unlockedTeamIds = unlockedTeams.stream().map(Team::getId).toList();

        List<Workspace> workspaces = unlockedTeamIds.isEmpty()
                ? Collections.emptyList()
                : workspaceDao.selectList(new LambdaQueryWrapper<Workspace>()
                        .in(Workspace::getTeamId, unlockedTeamIds));
        Map<Long, Workspace> workspaceByTeam = workspaces.stream()
                .collect(Collectors.toMap(Workspace::getTeamId, Function.identity(), (left, right) -> left));
        List<Long> workspaceIds = workspaces.stream().map(Workspace::getId).filter(Objects::nonNull).toList();

        IsoWeekUtil.IsoWeek currentWeek = IsoWeekUtil.current();
        List<WeeklyReport> currentWeekReports = workspaceIds.isEmpty()
                ? Collections.emptyList()
                : weeklyReportDao.selectList(new LambdaQueryWrapper<WeeklyReport>()
                        .in(WeeklyReport::getWorkspaceId, workspaceIds)
                        .eq(WeeklyReport::getReportYear, currentWeek.year())
                        .eq(WeeklyReport::getReportWeek, currentWeek.week())
                        .eq(WeeklyReport::getReportStatus, CacheCode.WEEKLY_REPORT_STATUS_SUBMITTED.getCode()));

        int weeklySubmittedCount = currentWeekReports.size();
        int weeklyTotalCount = unlockedTeams.stream()
                .mapToInt(team -> memberCountByTeam.getOrDefault(team.getId(), 0L).intValue())
                .sum();

        List<WeeklyReport> allSubmittedReports = workspaceIds.isEmpty()
                ? Collections.emptyList()
                : weeklyReportDao.selectList(new LambdaQueryWrapper<WeeklyReport>()
                        .in(WeeklyReport::getWorkspaceId, workspaceIds)
                        .eq(WeeklyReport::getReportStatus, CacheCode.WEEKLY_REPORT_STATUS_SUBMITTED.getCode()));
        Map<Long, List<WeeklyReport>> reportsByWorkspace = allSubmittedReports.stream()
                .collect(Collectors.groupingBy(WeeklyReport::getWorkspaceId));

        List<PlanTask> allTasks = workspaceIds.isEmpty()
                ? Collections.emptyList()
                : planTaskDao.selectList(new LambdaQueryWrapper<PlanTask>()
                        .in(PlanTask::getWorkspaceId, workspaceIds));
        Map<Long, List<PlanTask>> tasksByWorkspace = allTasks.stream()
                .collect(Collectors.groupingBy(PlanTask::getWorkspaceId));

        LocalDate today = LocalDate.now();
        List<PlanTask> overdueTasks = allTasks.stream()
                .filter(task -> task.getEndDate() != null
                        && task.getEndDate().isBefore(today)
                        && !Objects.equals(task.getTaskStatus(), CacheCode.PLAN_TASK_STATUS_COMPLETED.getCode())
                        && !Objects.equals(task.getTaskStatus(), CacheCode.PLAN_TASK_STATUS_CANCELLED.getCode()))
                .toList();

        Set<Long> overdueTeamIds = overdueTasks.stream()
                .map(PlanTask::getWorkspaceId)
                .map(workspaceId -> workspaces.stream()
                        .filter(workspace -> Objects.equals(workspace.getId(), workspaceId))
                        .findFirst()
                        .map(Workspace::getTeamId)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDateTime earliestPendingSubmitDate = pendingApprovals.stream()
                .map(TopicApproval::getSubmitDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        TeachingDashboardStatsDTO stats = TeachingDashboardStatsDTO.builder()
                .pendingApprovalCount(pendingApprovals.size())
                .earliestPendingSubmitDate(earliestPendingSubmitDate)
                .activeTeamCount(unlockedTeams.size())
                .weeklySubmittedCount(weeklySubmittedCount)
                .weeklyTotalCount(weeklyTotalCount)
                .overdueTaskCount(overdueTasks.size())
                .overdueTeamCount(overdueTeamIds.size())
                .build();

        List<TeachingDashboardPendingItemDTO> pendingItems = pendingApprovals.stream()
                .limit(PENDING_LIMIT)
                .map(approval -> {
                    Team team = teamMap.get(approval.getTeamId());
                    User leader = team != null ? leaderMap.get(team.getLeaderUserId()) : null;
                    return TeachingDashboardPendingItemDTO.builder()
                            .id(approval.getId())
                            .teamLabel(resolveTeamLabel(team))
                            .topicTitle(approval.getTopicTitle())
                            .leaderName(resolveDisplayName(leader, team != null ? team.getLeaderUserId() : null))
                            .memberCount(memberCountByTeam.getOrDefault(approval.getTeamId(), 0L).intValue())
                            .submitDate(approval.getSubmitDate())
                            .build();
                })
                .toList();

        List<TeachingDashboardAtRiskTeamDTO> atRiskTeams = buildAtRiskTeams(
                unlockedTeams,
                workspaceByTeam,
                memberCountByTeam,
                reportsByWorkspace,
                tasksByWorkspace,
                currentWeek,
                currentWeekReports);

        return TeachingDashboardResponseDTO.builder()
                .course(toCourseDto(heroCourse))
                .stats(stats)
                .pendingApprovals(pendingItems)
                .atRiskTeams(atRiskTeams)
                .build();
    }

    private List<TeachingDashboardAtRiskTeamDTO> buildAtRiskTeams(
            List<Team> unlockedTeams,
            Map<Long, Workspace> workspaceByTeam,
            Map<Long, Long> memberCountByTeam,
            Map<Long, List<WeeklyReport>> reportsByWorkspace,
            Map<Long, List<PlanTask>> tasksByWorkspace,
            IsoWeekUtil.IsoWeek currentWeek,
            List<WeeklyReport> currentWeekReports) {

        Map<Long, Long> currentWeekSubmittedByWorkspace = currentWeekReports.stream()
                .collect(Collectors.groupingBy(WeeklyReport::getWorkspaceId, Collectors.counting()));

        LocalDate today = LocalDate.now();
        List<RiskCandidate> candidates = new ArrayList<>();

        for (Team team : unlockedTeams) {
            Workspace workspace = workspaceByTeam.get(team.getId());
            if (workspace == null) {
                continue;
            }

            int memberCount = memberCountByTeam.getOrDefault(team.getId(), 0L).intValue();
            List<PlanTask> tasks = tasksByWorkspace.getOrDefault(workspace.getId(), Collections.emptyList());
            List<WeeklyReport> reports = reportsByWorkspace.getOrDefault(workspace.getId(), Collections.emptyList());

            int totalTasks = tasks.size();
            long completedTasks = tasks.stream()
                    .filter(task -> Objects.equals(task.getTaskStatus(), CacheCode.PLAN_TASK_STATUS_COMPLETED.getCode()))
                    .count();
            int progressPercent = totalTasks == 0 ? 100 : (int) Math.round(completedTasks * 100.0 / totalTasks);

            long overdueCount = tasks.stream()
                    .filter(task -> task.getEndDate() != null
                            && task.getEndDate().isBefore(today)
                            && !Objects.equals(task.getTaskStatus(), CacheCode.PLAN_TASK_STATUS_COMPLETED.getCode())
                            && !Objects.equals(task.getTaskStatus(), CacheCode.PLAN_TASK_STATUS_CANCELLED.getCode()))
                    .count();

            int submittedThisWeek = currentWeekSubmittedByWorkspace.getOrDefault(workspace.getId(), 0L).intValue();
            WeeklyReport latestReport = reports.stream()
                    .max(Comparator.comparing(WeeklyReport::getReportYear, Comparator.nullsFirst(Integer::compareTo))
                            .thenComparing(WeeklyReport::getReportWeek, Comparator.nullsFirst(Integer::compareTo)))
                    .orElse(null);

            List<String> hints = new ArrayList<>();
            int riskScore = 0;

            if (overdueCount > 0) {
                riskScore += (int) overdueCount * 10;
                hints.add(overdueCount + " 项任务逾期");
            }

            if (memberCount > 0 && submittedThisWeek < memberCount) {
                int rate = submittedThisWeek * 100 / memberCount;
                riskScore += Math.max(1, (100 - rate) / 2);
                hints.add("本周周报提交率 " + rate + "%（" + submittedThisWeek + "/" + memberCount + " 人）");
            }

            if (latestReport == null) {
                riskScore += 25;
                if (hints.stream().noneMatch(hint -> hint.contains("周报"))) {
                    hints.add("暂无已提交周报");
                }
            } else {
                int weekGap = weekGap(currentWeek, latestReport.getReportYear(), latestReport.getReportWeek());
                if (weekGap >= 2) {
                    riskScore += 20;
                    hints.add(0, "连续 " + weekGap + " 周未交周报");
                } else if (weekGap == 1 && submittedThisWeek < memberCount) {
                    hints.add("最近周报 第 " + latestReport.getReportWeek() + " 周");
                }
            }

            if (progressPercent < 60) {
                riskScore += (60 - progressPercent);
                if (totalTasks > 0) {
                    hints.add("任务完成率 " + progressPercent + "%");
                }
            }

            if (riskScore <= 0) {
                continue;
            }

            candidates.add(new RiskCandidate(
                    team,
                    String.join(" · ", hints),
                    progressPercent,
                    riskScore));
        }

        return candidates.stream()
                .sorted(Comparator.comparingInt(RiskCandidate::riskScore).reversed())
                .limit(AT_RISK_LIMIT)
                .map(candidate -> TeachingDashboardAtRiskTeamDTO.builder()
                        .id(candidate.team().getId())
                        .teamLabel(resolveTeamLabel(candidate.team()))
                        .topicTitle(candidate.team().getTopicTitle())
                        .riskHint(candidate.riskHint())
                        .progressPercent(candidate.progressPercent())
                        .build())
                .toList();
    }

    private int weekGap(IsoWeekUtil.IsoWeek current, Integer reportYear, Integer reportWeek) {
        if (reportYear == null || reportWeek == null) {
            return Integer.MAX_VALUE;
        }
        if (Objects.equals(current.year(), reportYear)) {
            return Math.max(0, current.week() - reportWeek);
        }
        return current.week() + Math.max(0, 52 - reportWeek);
    }

    private TeachingDashboardCourseDTO toCourseDto(Course course) {
        return TeachingDashboardCourseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .termYear(course.getTermYear())
                .termSeason(course.getTermSeason())
                .build();
    }

    private TeachingDashboardStatsDTO emptyStats() {
        return TeachingDashboardStatsDTO.builder()
                .pendingApprovalCount(0)
                .activeTeamCount(0)
                .weeklySubmittedCount(0)
                .weeklyTotalCount(0)
                .overdueTaskCount(0)
                .overdueTeamCount(0)
                .build();
    }

    private TeachingDashboardResponseDTO emptyDashboard() {
        return TeachingDashboardResponseDTO.builder()
                .course(null)
                .stats(emptyStats())
                .pendingApprovals(Collections.emptyList())
                .atRiskTeams(Collections.emptyList())
                .build();
    }

    private String resolveTeamLabel(Team team) {
        if (team == null || !StringUtils.hasText(team.getTeamName())) {
            return "未命名小组";
        }
        return team.getTeamName().trim();
    }

    private String resolveDisplayName(User user, Long userId) {
        if (user == null) {
            return userId == null ? "未知成员" : "用户" + userId;
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName().trim();
        }
        if (user.getNickName() != null && !user.getNickName().isBlank()) {
            return user.getNickName().trim();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        return "用户" + user.getId();
    }

    private record RiskCandidate(Team team, String riskHint, int progressPercent, int riskScore) {
    }
}
