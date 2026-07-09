package cn.guet.soft_manage.biz.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师周报审阅可选周次
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReviewWeekOptionDTO {

    private Integer reportYear;

    private Integer reportWeek;

    private String label;
}
