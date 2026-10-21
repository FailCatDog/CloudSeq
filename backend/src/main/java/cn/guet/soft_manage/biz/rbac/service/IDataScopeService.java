package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.DataScopeResult;

import java.util.List;

/**
 * RBAC 数据范围：SELF / COURSE / ALL
 */
public interface IDataScopeService {

    DataScopeResult resolve(Long userId);

    String resolveDataScope(Long userId);

    List<Long> resolveCourseIds(Long userId);

    boolean canAccessCourse(Long userId, Long courseId);

    void requireCourseAccess(Long userId, Long courseId);

    boolean isStaffDataScope(Long userId);
}
