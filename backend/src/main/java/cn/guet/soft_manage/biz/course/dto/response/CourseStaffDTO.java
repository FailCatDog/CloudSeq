package cn.guet.soft_manage.biz.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStaffDTO {

    private Long id;

    private Long courseId;

    private Long userId;

    private String userName;

    private String staffRole;

    private String staffStatus;
}
