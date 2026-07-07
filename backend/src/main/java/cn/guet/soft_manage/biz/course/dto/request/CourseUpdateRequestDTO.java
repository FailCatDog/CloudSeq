package cn.guet.soft_manage.biz.course.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseUpdateRequestDTO {

    @Size(max = 128, message = "课程名称长度不能超过128")
    private String courseName;

    @Size(max = 2000, message = "说明长度不能超过2000")
    private String description;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topicDeadline;

    @Min(value = 1, message = "最小组人数至少为1")
    private Integer minTeamSize;

    @Min(value = 1, message = "最大组人数至少为1")
    @Max(value = 20, message = "最大组人数不能超过20")
    private Integer maxTeamSize;

    private Integer weeklyRequired;
}
