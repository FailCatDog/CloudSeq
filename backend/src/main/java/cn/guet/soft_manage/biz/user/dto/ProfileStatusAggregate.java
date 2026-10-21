package cn.guet.soft_manage.biz.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 个人中心「我的状态」聚合视图（扁平字段，不含实体引用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileStatusAggregate {

    /** 学生里程碑状态，字典 student_milestone_status */
    private String milestoneStatus;

    private Long userId;
    private String username;
    private String realName;
    private String nickName;
    private String studentNo;
    private String role;

    private Long enrollmentId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer termYear;
    private String termSeason;
    private String enrollStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrollDate;

    private Long teamId;
    private String teamName;
    private Long teamCourseId;
    private Long leaderUserId;
    private String teamStatus;
    private String topicTitle;
    private String topicDesc;
    private Integer isLeader;
    private Integer memberCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime teamCreateDate;

    private Long latestApprovalId;
    private String approvalStatus;
    private String rejectReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalSubmitDate;

    private Long workspaceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime workspaceUnlockAt;
}
