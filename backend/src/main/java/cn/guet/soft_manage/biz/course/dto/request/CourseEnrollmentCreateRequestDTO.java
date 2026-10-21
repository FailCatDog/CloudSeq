package cn.guet.soft_manage.biz.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseEnrollmentCreateRequestDTO {

    @NotNull(message = "学生用户ID不能为空")
    private Long userId;

    /** 可选；不传则使用用户当前学号快照 */
    private String studentNo;
}
