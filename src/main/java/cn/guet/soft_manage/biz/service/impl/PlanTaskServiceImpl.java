package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.PlanTaskDao;
import cn.guet.soft_manage.biz.pojo.entity.PlanTask;
import cn.guet.soft_manage.biz.service.PlanTaskService;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if (task.getStartDate() != null && task.getEndDate() != null && task.getStartDate().isAfter(task.getEndDate())) throw new BusinessException(BizResponseCode.PLAN_TASK_START_AFTER_END);

        if (task.getActualStartDate() != null && task.getActualEndDate() != null && task.getActualStartDate().isAfter(task.getActualEndDate())) throw new BusinessException(BizResponseCode.PLAN_TASK_ACTUAL_START_AFTER_END);
        if (task.getTaskStatus() == null) task.setTaskStatus(1);
        if (task.getSortOrder() == null) task.setSortOrder(0);
        if (task.getDuration() != null) {
            task.setDuration(task.getDuration().setScale(1, RoundingMode.HALF_UP));
        } else if (task.getStartDate() != null && task.getEndDate() != null && ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) >= 0) {
            task.setDuration(BigDecimal.valueOf(ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) + 1).setScale(1, RoundingMode.HALF_UP));
        }
        planTaskDao.insert(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanTask update(PlanTask task) {
        if (Objects.isNull(task.getId())) throw new BusinessException(BizResponseCode.PLAN_TASK_ID_REQUIRED);
        if (task.getStartDate() != null && task.getEndDate() != null && task.getStartDate().isAfter(task.getEndDate())) throw new BusinessException(BizResponseCode.PLAN_TASK_START_AFTER_END);
        if (task.getActualStartDate() != null && task.getActualEndDate() != null && task.getActualStartDate().isAfter(task.getActualEndDate())) throw new BusinessException(BizResponseCode.PLAN_TASK_ACTUAL_START_AFTER_END);
        if (task.getTaskStatus() == null) task.setTaskStatus(1);
        if (task.getSortOrder() == null) task.setSortOrder(0);
        if (task.getDuration() != null) {
            task.setDuration(task.getDuration().setScale(1, RoundingMode.HALF_UP));
        } else if (task.getStartDate() != null && task.getEndDate() != null && ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) >= 0) {
            task.setDuration(BigDecimal.valueOf(ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) + 1).setScale(1, RoundingMode.HALF_UP));
        }

        int rows = planTaskDao.updateById(task);
        if (rows == 0) throw new BusinessException(BizResponseCode.PLAN_TASK_STALE);
        return planTaskDao.selectById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        int rows = planTaskDao.deleteById(id);
        if (rows == 0) throw new BusinessException(BizResponseCode.PLAN_TASK_NOT_FOUND);
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
}
