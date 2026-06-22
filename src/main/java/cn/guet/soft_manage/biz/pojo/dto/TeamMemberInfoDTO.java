package cn.guet.soft_manage.biz.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小组成员用户信息（用于展示与下拉选择）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberInfoDTO {

    private Long teamMemberId;

    private Long userId;

    private String name;

    private String username;

    private String studentNo;

    private String avatarUrl;

    private Integer isLeader;

    private Integer memberStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinDate;
}
