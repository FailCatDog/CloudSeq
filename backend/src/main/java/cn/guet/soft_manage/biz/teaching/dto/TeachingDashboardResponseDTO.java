package cn.guet.soft_manage.biz.teaching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教学工作台响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingDashboardResponseDTO {

    private TeachingDashboardCourseDTO course;

    private TeachingDashboardStatsDTO stats;

    private List<TeachingDashboardPendingItemDTO> pendingApprovals;

    private List<TeachingDashboardAtRiskTeamDTO> atRiskTeams;
}
