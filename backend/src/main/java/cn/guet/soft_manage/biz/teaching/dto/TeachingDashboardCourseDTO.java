package cn.guet.soft_manage.biz.teaching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教学工作台课号摘要
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingDashboardCourseDTO {

    private Long id;

    private String courseCode;

    private String courseName;

    private Integer termYear;

    private String termSeason;
}
