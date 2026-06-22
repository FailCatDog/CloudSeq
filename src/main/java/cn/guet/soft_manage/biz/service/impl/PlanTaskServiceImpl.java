package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.PlanTaskDao;
import cn.guet.soft_manage.biz.pojo.entity.PlanTask;
import cn.guet.soft_manage.biz.service.PlanTaskService;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 计划任务服务实现
 */
@Service
public class PlanTaskServiceImpl implements PlanTaskService {

    @Resource
    private PlanTaskDao planTaskDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanTask create(PlanTask task) {
        validateDates(task.getStartDate(), task.getEndDate());
        validateActualDates(task.getActualStartDate(), task.getActualEndDate());
        fillDefaultFields(task, true);
        normalizeDuration(task);
        planTaskDao.insert(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanTask update(PlanTask task) {
        if (Objects.isNull(task.getId())) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "任务ID不能为空");
        }
        validateDates(task.getStartDate(), task.getEndDate());
        validateActualDates(task.getActualStartDate(), task.getActualEndDate());
        fillDefaultFields(task, false);
        normalizeDuration(task);
        int rows = planTaskDao.updateById(task);
        if (rows == 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "任务不存在或已被修改");
        }
        return planTaskDao.selectById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        int rows = planTaskDao.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "任务不存在");
        }
    }

    @Override
    public PlanTask getById(Long id) {
        return planTaskDao.selectById(id);
    }

    @Override
    public List<PlanTask> listByWorkspace(Long workspaceId) {
        return planTaskDao.selectList(new LambdaQueryWrapper<PlanTask>()
                .eq(PlanTask::getWorkspaceId, workspaceId)
                .eq(PlanTask::getDelFlag, 0)
                .orderByAsc(PlanTask::getParentTaskId)
                .orderByAsc(PlanTask::getSortOrder)
                .orderByAsc(PlanTask::getId));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "开始日期不能晚于结束日期");
        }
    }

    private void validateActualDates(LocalDate actualStartDate, LocalDate actualEndDate) {
        if (actualStartDate != null && actualEndDate != null && actualStartDate.isAfter(actualEndDate)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "实际开始日期不能晚于实际结束日期");
        }
    }

    private void normalizeDuration(PlanTask task) {
        if (task.getDuration() != null) {
            task.setDuration(task.getDuration().setScale(1, RoundingMode.HALF_UP));
            return;
        }
        if (task.getStartDate() == null || task.getEndDate() == null) {
            return;
        }
        long days = ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) + 1;
        if (days > 0) {
            task.setDuration(BigDecimal.valueOf(days).setScale(1, RoundingMode.HALF_UP));
        }
    }

    private void fillDefaultFields(PlanTask task, boolean isCreate) {
        Long userId = UserContext.getUserId();
        if (isCreate) {
            task.setCreateUser(userId);
        }
        task.setUpdateUser(userId);
        if (task.getTaskStatus() == null) {
            task.setTaskStatus(1);
        }
        if (task.getSortOrder() == null) {
            task.setSortOrder(0);
        }
    }
}
