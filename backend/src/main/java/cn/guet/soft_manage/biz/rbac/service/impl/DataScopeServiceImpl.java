package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.course.dao.CourseDao;
import cn.guet.soft_manage.biz.course.dao.CourseEnrollmentDao;
import cn.guet.soft_manage.biz.course.dao.CourseStaffDao;
import cn.guet.soft_manage.biz.course.entity.Course;
import cn.guet.soft_manage.biz.course.entity.CourseEnrollment;
import cn.guet.soft_manage.biz.course.entity.CourseStaff;
import cn.guet.soft_manage.biz.rbac.dto.DataScopeResult;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.service.IDataScopeService;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DataScopeServiceImpl implements IDataScopeService {

    @Resource
    private IRoleService roleService;

    @Resource
    private CourseDao courseDao;

    @Resource
    private CourseStaffDao courseStaffDao;

    @Resource
    private CourseEnrollmentDao courseEnrollmentDao;

    @Override
    public DataScopeResult resolve(Long userId) {
        String dataScope = resolveDataScope(userId);
        if (CacheCode.DATA_SCOPE_ALL.getCode().equals(dataScope)) {
            return DataScopeResult.builder()
                    .dataScope(dataScope)
                    .allCourses(true)
                    .courseIds(Collections.emptyList())
                    .build();
        }
        return DataScopeResult.builder()
                .dataScope(dataScope)
                .allCourses(false)
                .courseIds(resolveCourseIds(userId))
                .build();
    }

    @Override
    public String resolveDataScope(Long userId) {
        List<SysRole> roles = roleService.listActiveRolesByUserId(userId);
        if (roles.isEmpty()) {
            return CacheCode.DATA_SCOPE_SELF.getCode();
        }
        boolean hasAll = roles.stream()
                .anyMatch(role -> Objects.equals(role.getDataScope(), CacheCode.DATA_SCOPE_ALL.getCode()));
        if (hasAll) {
            return CacheCode.DATA_SCOPE_ALL.getCode();
        }
        boolean hasCourse = roles.stream()
                .anyMatch(role -> Objects.equals(role.getDataScope(), CacheCode.DATA_SCOPE_COURSE.getCode()));
        if (hasCourse) {
            return CacheCode.DATA_SCOPE_COURSE.getCode();
        }
        return CacheCode.DATA_SCOPE_SELF.getCode();
    }

    @Override
    public List<Long> resolveCourseIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        String dataScope = resolveDataScope(userId);
        if (CacheCode.DATA_SCOPE_ALL.getCode().equals(dataScope)) {
            return courseDao.selectList(new LambdaQueryWrapper<Course>().select(Course::getId)).stream()
                    .map(Course::getId)
                    .toList();
        }
        if (CacheCode.DATA_SCOPE_COURSE.getCode().equals(dataScope)) {
            return findCourseIdsByStaff(userId);
        }
        return findCourseIdsByEnrollment(userId);
    }

    @Override
    public boolean canAccessCourse(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        DataScopeResult scope = resolve(userId);
        if (scope.isAllCourses()) {
            return true;
        }
        return scope.getCourseIds().contains(courseId);
    }

    @Override
    public void requireCourseAccess(Long userId, Long courseId) {
        if (!canAccessCourse(userId, courseId)) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }
    }

    @Override
    public boolean isStaffDataScope(Long userId) {
        String dataScope = resolveDataScope(userId);
        return CacheCode.DATA_SCOPE_COURSE.getCode().equals(dataScope)
                || CacheCode.DATA_SCOPE_ALL.getCode().equals(dataScope);
    }

    private List<Long> findCourseIdsByStaff(Long userId) {
        Set<Long> courseIds = new LinkedHashSet<>();

        courseDao.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getPrimaryTeacherId, userId)
                        .select(Course::getId))
                .forEach(course -> courseIds.add(course.getId()));

        courseStaffDao.selectList(new LambdaQueryWrapper<CourseStaff>()
                        .eq(CourseStaff::getUserId, userId)
                        .eq(CourseStaff::getStaffStatus, CacheCode.COURSE_STAFF_STATUS_ACTIVE.getCode())
                        .select(CourseStaff::getCourseId))
                .forEach(staff -> courseIds.add(staff.getCourseId()));

        return new ArrayList<>(courseIds);
    }

    private List<Long> findCourseIdsByEnrollment(Long userId) {
        return courseEnrollmentDao.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                        .eq(CourseEnrollment::getUserId, userId)
                        .eq(CourseEnrollment::getEnrollStatus, CacheCode.ENROLL_STATUS_ENROLLED.getCode())
                        .select(CourseEnrollment::getCourseId))
                .stream()
                .map(CourseEnrollment::getCourseId)
                .distinct()
                .toList();
    }
}
