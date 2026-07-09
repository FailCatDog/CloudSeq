package cn.guet.soft_manage.biz.teaching.dto;

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
public class TeachingStudentItemDTO {

    private Long enrollmentId;

    private Long userId;

    private String userName;

    private String studentNo;

    private Long courseId;

    private String courseCode;

    private String courseName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrollDate;
}
