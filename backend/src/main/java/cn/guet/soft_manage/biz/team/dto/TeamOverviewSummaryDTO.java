package cn.guet.soft_manage.biz.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师端小组总览列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamOverviewSummaryDTO {

    private Long id;

    private Long courseId;

    private String courseCode;

    private String teamLabel;

    private String topicTitle;

    private String status;

    private String leaderName;

    private Integer memberCount;

    private Integer lastReportYear;

    private Integer lastReportWeek;
}
