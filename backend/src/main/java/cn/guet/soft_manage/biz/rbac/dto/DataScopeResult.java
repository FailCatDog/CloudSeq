package cn.guet.soft_manage.biz.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 数据范围解析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeResult {

    /** dict:data_scope */
    private String dataScope;

    /** true 表示不限课号（ALL） */
    private boolean allCourses;

    @Builder.Default
    private List<Long> courseIds = Collections.emptyList();
}
