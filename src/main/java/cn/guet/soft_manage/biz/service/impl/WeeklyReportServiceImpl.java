package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WeeklyReportDao;
import cn.guet.soft_manage.biz.pojo.entity.WeeklyReport;
import cn.guet.soft_manage.biz.service.WeeklyReportService;
import cn.guet.soft_manage.biz.utils.IsoWeekUtil;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-09
 * @Description: 周报服务实现
 */
@Service
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private static final int STATUS_DRAFT = 1;
    private static final int STATUS_SUBMITTED = 2;

    @Resource
    private WeeklyReportDao weeklyReportDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport create(WeeklyReport report) {
        Long userId = requireUserId();
        normalizeWeek(report);
        ensureNotDuplicate(report.getWorkspaceId(), userId, report.getReportYear(), report.getReportWeek(), null);
        fillFields(report, userId, true);
        weeklyReportDao.insert(report);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport update(WeeklyReport report) {
        if (Objects.isNull(report.getId())) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "周报ID不能为空");
        }
        WeeklyReport existing = requireOwnedReport(report.getId());
        ensureDraft(existing);
        normalizeWeek(report);
        ensureNotDuplicate(
                report.getWorkspaceId(),
                existing.getUserId(),
                report.getReportYear(),
                report.getReportWeek(),
                report.getId()
        );
        fillFields(report, existing.getUserId(), false);
        report.setUserId(existing.getUserId());
        report.setReportStatus(existing.getReportStatus());
        int rows = weeklyReportDao.updateById(report);
        if (rows == 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "周报不存在或已被修改");
        }
        return weeklyReportDao.selectById(report.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport submit(Long id) {
        WeeklyReport existing = requireOwnedReport(id);
        ensureDraft(existing);
        if (!StringUtils.hasText(existing.getWeeklyProgress())) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "请填写本周工作进展后再提交");
        }
        Long userId = requireUserId();
        existing.setReportStatus(STATUS_SUBMITTED);
        existing.setSubmitUserId(userId);
        existing.setSubmitDate(LocalDateTime.now());
        existing.setUpdateUser(userId);
        int rows = weeklyReportDao.updateById(existing);
        if (rows == 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "周报不存在或已被修改");
        }
        return weeklyReportDao.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WeeklyReport existing = requireOwnedReport(id);
        ensureDraft(existing);
        int rows = weeklyReportDao.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "周报不存在");
        }
    }

    @Override
    public WeeklyReport getById(Long id) {
        WeeklyReport report = weeklyReportDao.selectById(id);
        if (report == null) {
            return null;
        }
        assertOwner(report);
        return report;
    }

    @Override
    public WeeklyReport getByWeek(Long workspaceId, Integer reportYear, Integer reportWeek) {
        Long userId = requireUserId();
        IsoWeekUtil.IsoWeek isoWeek = resolveWeek(reportYear, reportWeek);
        return weeklyReportDao.selectOne(new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, workspaceId)
                .eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getReportYear, isoWeek.year())
                .eq(WeeklyReport::getReportWeek, isoWeek.week())
                .eq(WeeklyReport::getDelFlag, 0)
                .last("LIMIT 1"));
    }

    @Override
    public List<WeeklyReport> listMine(Long workspaceId) {
        Long userId = requireUserId();
        return weeklyReportDao.selectList(new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, workspaceId)
                .eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getDelFlag, 0)
                .orderByDesc(WeeklyReport::getReportYear)
                .orderByDesc(WeeklyReport::getReportWeek));
    }

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }
        return userId;
    }

    private WeeklyReport requireOwnedReport(Long id) {
        WeeklyReport report = weeklyReportDao.selectById(id);
        if (report == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "周报不存在");
        }
        assertOwner(report);
        return report;
    }

    private void assertOwner(WeeklyReport report) {
        Long userId = UserContext.getUserId();
        if (userId == null || !userId.equals(report.getUserId())) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }
    }

    private void ensureDraft(WeeklyReport report) {
        if (!Objects.equals(report.getReportStatus(), STATUS_DRAFT)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "已提交的周报不可修改");
        }
    }

    private void ensureNotDuplicate(Long workspaceId, Long userId, Integer year, Integer week, Long excludeId) {
        LambdaQueryWrapper<WeeklyReport> wrapper = new LambdaQueryWrapper<WeeklyReport>()
                .eq(WeeklyReport::getWorkspaceId, workspaceId)
                .eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getReportYear, year)
                .eq(WeeklyReport::getReportWeek, week)
                .eq(WeeklyReport::getDelFlag, 0);
        if (excludeId != null) {
            wrapper.ne(WeeklyReport::getId, excludeId);
        }
        Long count = weeklyReportDao.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "该周周报已存在");
        }
    }

    private IsoWeekUtil.IsoWeek resolveWeek(Integer reportYear, Integer reportWeek) {
        if (reportYear != null && reportWeek != null) {
            return IsoWeekUtil.of(reportYear, reportWeek);
        }
        return IsoWeekUtil.current();
    }

    private void normalizeWeek(WeeklyReport report) {
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
    }

    private void fillFields(WeeklyReport report, Long userId, boolean isCreate) {
        if (isCreate) {
            report.setUserId(userId);
            report.setCreateUser(userId);
            if (report.getReportStatus() == null) {
                report.setReportStatus(STATUS_DRAFT);
            }
        }
        report.setUpdateUser(userId);
        if (!StringUtils.hasText(report.getTitle())) {
            report.setTitle(buildDefaultTitle(report.getReportYear(), report.getReportWeek()));
        }
    }

    private String buildDefaultTitle(Integer year, Integer week) {
        var user = UserContext.get();
        String name = user != null && StringUtils.hasText(user.getRealName())
                ? user.getRealName()
                : (user != null && StringUtils.hasText(user.getNickName()) ? user.getNickName() : "我的");
        return String.format("%s的周报 %d年第%d周", name, year, week);
    }
}
