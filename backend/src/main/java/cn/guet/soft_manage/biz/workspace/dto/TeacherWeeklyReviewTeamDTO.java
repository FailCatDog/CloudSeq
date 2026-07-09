package cn.guet.soft_manage.biz.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教师周报审阅小组项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherWeeklyReviewTeamDTO {

    private Long teamId;

    private String teamLabel;

    private String topicTitle;

    private Long workspaceId;

    private List<TeacherWeeklyReportItemDTO> reports;
}
