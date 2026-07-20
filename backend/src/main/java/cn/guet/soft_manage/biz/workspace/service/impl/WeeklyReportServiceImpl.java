package cn.guet.soft_manage.biz.workspace.service.impl;

import cn.guet.soft_manage.biz.course.dao.CourseDao;
import cn.guet.soft_manage.biz.course.entity.Course;
import cn.guet.soft_manage.biz.rbac.service.IDataScopeService;
import cn.guet.soft_manage.biz.team.dao.TeamDao;
import cn.guet.soft_manage.biz.team.entity.Team;
import cn.guet.soft_manage.biz.user.dao.UserDao;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.biz.workspace.dao.WeeklyReportDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.workspace.dto.TeacherWeeklyReportItemDTO;
import cn.guet.soft_manage.biz.workspace.dto.TeacherWeeklyReviewResponseDTO;
import cn.guet.soft_manage.biz.workspace.dto.TeacherWeeklyReviewTeamDTO;
import cn.guet.soft_manage.biz.workspace.dto.WeeklyReviewWeekOptionDTO;
import cn.guet.soft_manage.biz.workspace.entity.WeeklyReport;
import cn.guet.soft_manage.biz.workspace.entity.Workspace;
import cn.guet.soft_manage.biz.workspace.service.WeeklyReportService;
import cn.guet.soft_manage.biz.workspace.util.IsoWeekUtil;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
 * @Author: 黄光宇
 * @CreateTime: 2026-06-09
 * @Description: 周报服务实现
 */
@Service
public class WeeklyReportServiceImpl implements WeeklyReportService {

    @Resource
    private WeeklyReportDao weeklyReportDao;

    @Resource
    private TeamDao teamDao;

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private CourseDao courseDao;

    @Resource
    private IDataScopeService dataScopeService;

    @Resource
    private UserDao userDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport create(WeeklyReport report) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        IsoWeekUtil.IsoWeek isoWeek;
        if (report.getWeekStartDate() != null) {
            isoWeek = IsoWeekUtil.of(report.getWeekStartDate());
        } else if (report.getReportYear() != null && report.getReportWeek() != null) {
            isoWeek = IsoWeekUtil.of(report.getReportYear(), report.getReportWeek());
        } else {
            isoWeek = IsoWeekUtil.current();
        }
        report.setReportYear(isoWeek.year());
        report.setReportWeek(isoWeek.week());
        report.setWeekStartDate(isoWeek.weekStartDate());
        Long count = weeklyReportDao.selectCount(new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, report.getWorkspaceId())
                .eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getReportYear, report.getReportYear())
                .eq(WeeklyReport::getReportWeek, report.getReportWeek()));
        if (count != null && count > 0) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_DUPLICATE);
        report.setUserId(userId);
        if (report.getReportStatus() == null) report.setReportStatus(CacheCode.WEEKLY_REPORT_STATUS_DRAFT.getCode());
        if (!StringUtils.hasText(report.getTitle())) {
            var loginUser = UserContext.get();
            String name = loginUser != null ? loginUser.displayNameOr("我的") : "我的";
            report.setTitle(String.format("%s的周报 %d年第%d周", name, report.getReportYear(), report.getReportWeek()));
        }
        weeklyReportDao.insert(report);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport update(WeeklyReport report) {
        if (Objects.isNull(report.getId())) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_ID_REQUIRED);
        WeeklyReport existing = weeklyReportDao.selectById(report.getId());
        if (existing == null) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_NOT_FOUND);
        Long userId = UserContext.getUserId();
        if (userId == null || !userId.equals(existing.getUserId())) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        if (!Objects.equals(existing.getReportStatus(), CacheCode.WEEKLY_REPORT_STATUS_DRAFT.getCode())) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_SUBMITTED);
        IsoWeekUtil.IsoWeek isoWeek;
        if (report.getWeekStartDate() != null) {
            isoWeek = IsoWeekUtil.of(report.getWeekStartDate());
        } else if (report.getReportYear() != null && report.getReportWeek() != null) {
            isoWeek = IsoWeekUtil.of(report.getReportYear(), report.getReportWeek());
        } else {
            isoWeek = IsoWeekUtil.current();
        }
        report.setReportYear(isoWeek.year());
        report.setReportWeek(isoWeek.week());
        report.setWeekStartDate(isoWeek.weekStartDate());
        Long count = weeklyReportDao.selectCount(new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, report.getWorkspaceId())
                .eq(WeeklyReport::getUserId, existing.getUserId())
                .eq(WeeklyReport::getReportYear, report.getReportYear())
                .eq(WeeklyReport::getReportWeek, report.getReportWeek())
                .ne(WeeklyReport::getId, report.getId()));
        if (count != null && count > 0) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_DUPLICATE);
        if (!StringUtils.hasText(report.getTitle())) {
            var loginUser = UserContext.get();
            String name = loginUser != null ? loginUser.displayNameOr("我的") : "我的";
            report.setTitle(String.format("%s的周报 %d年第%d周", name, report.getReportYear(), report.getReportWeek()));
        }
        report.setUserId(existing.getUserId());
        report.setReportStatus(existing.getReportStatus());
        int rows = weeklyReportDao.updateById(report);
        if (rows == 0) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_STALE);
        return weeklyReportDao.selectById(report.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport submit(Long id) {
        WeeklyReport existing = weeklyReportDao.selectById(id);
        if (existing == null) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_NOT_FOUND);
        Long userId = UserContext.getUserId();
        if (userId == null || !userId.equals(existing.getUserId())) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        if (!Objects.equals(existing.getReportStatus(), CacheCode.WEEKLY_REPORT_STATUS_DRAFT.getCode())) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_SUBMITTED);
        if (!StringUtils.hasText(existing.getWeeklyProgress())) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_PROGRESS_REQUIRED);
        existing.setReportStatus(CacheCode.WEEKLY_REPORT_STATUS_SUBMITTED.getCode());
        existing.setSubmitUserId(userId);
        existing.setSubmitDate(LocalDateTime.now());
        int rows = weeklyReportDao.updateById(existing);
        if (rows == 0) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_STALE);
        return weeklyReportDao.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WeeklyReport existing = weeklyReportDao.selectById(id);
        if (existing == null) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_NOT_FOUND);
        Long userId = UserContext.getUserId();
        if (userId == null || !userId.equals(existing.getUserId())) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        if (!Objects.equals(existing.getReportStatus(), CacheCode.WEEKLY_REPORT_STATUS_DRAFT.getCode())) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_SUBMITTED);
        int rows = weeklyReportDao.deleteById(id);
        if (rows == 0) throw new BusinessException(BizResponseCode.WEEKLY_REPORT_NOT_FOUND);
    }

    @Override
    public WeeklyReport getById(Long id) {
        WeeklyReport report = weeklyReportDao.selectById(id);
        if (report == null) return null;
        Long userId = UserContext.getUserId();
        if (userId == null || !userId.equals(report.getUserId())) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        return report;
    }

    @Override
    public WeeklyReport getByWeek(Long workspaceId, Integer reportYear, Integer reportWeek) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        IsoWeekUtil.IsoWeek isoWeek;
        if (reportYear != null && reportWeek != null) {
            isoWeek = IsoWeekUtil.of(reportYear, reportWeek);
        } else {
            isoWeek = IsoWeekUtil.current();
        }
        return weeklyReportDao.selectOne(new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, workspaceId)
                .eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getReportYear, isoWeek.year())
                .eq(WeeklyReport::getReportWeek, isoWeek.week())
                .last("LIMIT 1"));
    }

    @Override
    public List<WeeklyReport> listMine(Long workspaceId) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        return weeklyReportDao.selectList(new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, workspaceId)
                .eq(WeeklyReport::getUserId, userId)
                .orderByDesc(WeeklyReport::getReportYear)
                .orderByDesc(WeeklyReport::getReportWeek));
    }

    @Override
    public TeacherWeeklyReviewResponseDTO getTeacherWeeklyReview(Long courseId, Integer reportYear, Integer reportWeek) {
        Long teacherId = UserContext.getUserId();
        if (teacherId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }
        if (courseId == null) {
            throw new BusinessException(BizResponseCode.COURSE_NOT_FOUND);
        }

        Course course = courseDao.selectById(courseId);
        if (course == null) {
            throw new BusinessException(BizResponseCode.COURSE_NOT_FOUND);
        }

        List<Long> accessibleCourseIds = dataScopeService.resolveCourseIds(teacherId);
        if (!accessibleCourseIds.contains(courseId)) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }

        List<Team> teams = teamDao.selectList(new LambdaQueryWrapper<Team>()
                .eq(Team::getCourseId, courseId)
                .orderByAsc(Team::getTeamName)
                .orderByAsc(Team::getId));
        if (teams.isEmpty()) {
            return TeacherWeeklyReviewResponseDTO.builder()
                    .weekOptions(Collections.emptyList())
                    .teams(Collections.emptyList())
                    .build();
        }

        List<Long> teamIds = teams.stream().map(Team::getId).filter(Objects::nonNull).toList();
        List<Workspace> workspaces = workspaceDao.selectList(new LambdaQueryWrapper<Workspace>()
                .in(Workspace::getTeamId, teamIds));
        Map<Long, Workspace> workspaceByTeam = workspaces.stream()
                .collect(Collectors.toMap(Workspace::getTeamId, Function.identity(), (left, right) -> left));

        List<Long> workspaceIds = workspaces.stream().map(Workspace::getId).filter(Objects::nonNull).toList();
        List<WeeklyReport> submittedReports = workspaceIds.isEmpty()
                ? Collections.emptyList()
                : weeklyReportDao.selectList(new LambdaQueryWrapper<WeeklyReport>()
                        .in(WeeklyReport::getWorkspaceId, workspaceIds)
                        .eq(WeeklyReport::getReportStatus, CacheCode.WEEKLY_REPORT_STATUS_SUBMITTED.getCode())
                        .orderByDesc(WeeklyReport::getReportYear)
                        .orderByDesc(WeeklyReport::getReportWeek)
                        .orderByAsc(WeeklyReport::getUserId));

        List<WeeklyReviewWeekOptionDTO> weekOptions = buildWeekOptions(submittedReports);

        Set<Long> userIds = submittedReports.stream()
                .map(WeeklyReport::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userDao.selectBatchIds(new ArrayList<>(userIds)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));

        Map<Long, List<WeeklyReport>> reportsByWorkspace = submittedReports.stream()
                .collect(Collectors.groupingBy(WeeklyReport::getWorkspaceId));

        boolean filterByWeek = reportYear != null && reportWeek != null;
        List<TeacherWeeklyReviewTeamDTO> teamItems = teams.stream()
                .map(team -> {
                    Workspace workspace = workspaceByTeam.get(team.getId());
                    List<TeacherWeeklyReportItemDTO> reports = Collections.emptyList();
                    if (workspace != null && filterByWeek) {
                        reports = reportsByWorkspace.getOrDefault(workspace.getId(), Collections.emptyList()).stream()
                                .filter(report -> Objects.equals(report.getReportYear(), reportYear)
                                        && Objects.equals(report.getReportWeek(), reportWeek))
                                .map(report -> toTeacherReportItem(report, userMap))
                                .toList();
                    }
                    return TeacherWeeklyReviewTeamDTO.builder()
                            .teamId(team.getId())
                            .teamLabel(StringUtils.hasText(team.getTeamName()) ? team.getTeamName() : "未命名小组")
                            .topicTitle(team.getTopicTitle())
                            .workspaceId(workspace != null ? workspace.getId() : null)
                            .reports(reports)
                            .build();
                })
                .toList();

        return TeacherWeeklyReviewResponseDTO.builder()
                .weekOptions(weekOptions)
                .teams(teamItems)
                .build();
    }

    private List<WeeklyReviewWeekOptionDTO> buildWeekOptions(List<WeeklyReport> reports) {
        return reports.stream()
                .filter(report -> report.getReportYear() != null && report.getReportWeek() != null)
                .collect(Collectors.toMap(
                        report -> report.getReportYear() + ":" + report.getReportWeek(),
                        report -> report,
                        (left, right) -> left))
                .values().stream()
                .sorted(Comparator.comparing(WeeklyReport::getReportYear, Comparator.nullsFirst(Integer::compareTo)).reversed()
                        .thenComparing(WeeklyReport::getReportWeek, Comparator.nullsFirst(Integer::compareTo)).reversed())
                .map(report -> WeeklyReviewWeekOptionDTO.builder()
                        .reportYear(report.getReportYear())
                        .reportWeek(report.getReportWeek())
                        .label(String.format("%d年 第%d周", report.getReportYear(), report.getReportWeek()))
                        .build())
                .toList();
    }

    private TeacherWeeklyReportItemDTO toTeacherReportItem(WeeklyReport report, Map<Long, User> userMap) {
        return TeacherWeeklyReportItemDTO.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .memberName(resolveDisplayName(userMap.get(report.getUserId()), report.getUserId()))
                .reportYear(report.getReportYear())
                .reportWeek(report.getReportWeek())
                .title(report.getTitle())
                .weeklyProgress(report.getWeeklyProgress())
                .problems(report.getProblems())
                .nextPlan(report.getNextPlan())
                .reportStatus(report.getReportStatus())
                .submitDate(report.getSubmitDate())
                .build();
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
}
