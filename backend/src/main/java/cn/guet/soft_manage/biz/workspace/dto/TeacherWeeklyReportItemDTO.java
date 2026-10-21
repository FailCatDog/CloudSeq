package cn.guet.soft_manage.biz.workspace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师周报审阅列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherWeeklyReportItemDTO {

    private Long id;

    private Long userId;

    private String memberName;

    private Integer reportYear;

    private Integer reportWeek;

    private String title;

    private String weeklyProgress;

    private String problems;

    private String nextPlan;

    private String reportStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitDate;
}
