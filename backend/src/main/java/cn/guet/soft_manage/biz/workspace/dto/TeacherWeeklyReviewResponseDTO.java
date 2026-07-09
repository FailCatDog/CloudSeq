package cn.guet.soft_manage.biz.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教师周报审阅响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherWeeklyReviewResponseDTO {

    private List<WeeklyReviewWeekOptionDTO> weekOptions;

    private List<TeacherWeeklyReviewTeamDTO> teams;
}
