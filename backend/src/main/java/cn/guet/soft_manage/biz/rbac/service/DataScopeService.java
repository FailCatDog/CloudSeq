package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.DataScopeResult;

import java.util.List;

/**
 * RBAC 数据范围：SELF / COURSE / ALL
 */
public interface DataScopeService {

    DataScopeResult resolve(Long userId);

    String resolveDataScope(Long userId);

    List<Long> resolveCourseIds(Long userId);
}
