package cn.guet.soft_manage.biz.teaching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教学工作台统计指标
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingDashboardStatsDTO {

    private Integer pendingApprovalCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime earliestPendingSubmitDate;

    private Integer activeTeamCount;

    private Integer weeklySubmittedCount;

    private Integer weeklyTotalCount;

    private Integer overdueTaskCount;

    private Integer overdueTeamCount;
}
