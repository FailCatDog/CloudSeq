package cn.guet.soft_manage.biz.course.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDTO {

    private Long id;

    private String courseCode;

    private String courseName;

    private Integer termYear;

    private String termSeason;

    private Long primaryTeacherId;

    private String primaryTeacherName;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topicDeadline;
}
