package cn.guet.soft_manage.biz.teaching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教学工作台需关注小组项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingDashboardAtRiskTeamDTO {

    private Long id;

    private String teamLabel;

    private String topicTitle;

    private String riskHint;

    private Integer progressPercent;
}
