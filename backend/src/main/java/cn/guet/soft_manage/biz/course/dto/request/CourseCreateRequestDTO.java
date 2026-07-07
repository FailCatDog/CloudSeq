package cn.guet.soft_manage.biz.course.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseCreateRequestDTO {

    @NotBlank(message = "课号不能为空")
    @Size(max = 32, message = "课号长度不能超过32")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 128, message = "课程名称长度不能超过128")
    private String courseName;

    @NotNull(message = "学年不能为空")
    private Integer termYear;

    @NotBlank(message = "学期不能为空")
    private String termSeason;

    @Size(max = 2000, message = "说明长度不能超过2000")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topicDeadline;

    @Min(value = 1, message = "最小组人数至少为1")
    private Integer minTeamSize;

    @Min(value = 1, message = "最大组人数至少为1")
    @Max(value = 20, message = "最大组人数不能超过20")
    private Integer maxTeamSize;

    private Integer weeklyRequired;
}
