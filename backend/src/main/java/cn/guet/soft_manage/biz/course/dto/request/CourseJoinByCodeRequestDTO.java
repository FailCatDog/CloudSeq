package cn.guet.soft_manage.biz.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseJoinByCodeRequestDTO {

    @NotBlank(message = "课号不能为空")
    private String courseCode;
}
