package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WeeklyReportDao;
import cn.guet.soft_manage.biz.pojo.entity.WeeklyReport;
import cn.guet.soft_manage.biz.service.WeeklyReportService;
import cn.guet.soft_manage.biz.utils.IsoWeekUtil;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    @Resource
    private WeeklyReportDao weeklyReportDao;

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
            var user = UserContext.get();
            String name = user != null && StringUtils.hasText(user.getRealName()) ? user.getRealName() : (user != null && StringUtils.hasText(user.getNickName()) ? user.getNickName() : "我的");
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
            var user = UserContext.get();
            String name = user != null && StringUtils.hasText(user.getRealName()) ? user.getRealName() : (user != null && StringUtils.hasText(user.getNickName()) ? user.getNickName() : "我的");
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
}
