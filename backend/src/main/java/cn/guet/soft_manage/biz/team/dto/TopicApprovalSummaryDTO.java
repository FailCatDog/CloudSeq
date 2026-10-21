package cn.guet.soft_manage.biz.team.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师端选题审批列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicApprovalSummaryDTO {

    private Long id;

    private Long teamId;

    private Long courseId;

    private String courseCode;

    private String teamLabel;

    private String topicTitle;

    private String topicDesc;

    private String approvalStatus;

    private String rejectReason;

    private String leaderName;

    private String leaderNo;

    private Integer memberCount;

    private String members;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitDate;
}
