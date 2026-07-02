package cn.guet.soft_manage.biz.team.dto;

import cn.guet.soft_manage.biz.team.entity.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小组成员查询结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMembersResponseDTO {

    /** 小组成员关系记录 */
    private List<TeamMember> members;

    /** 组内所有成员的用户信息 */
    private List<TeamMemberInfoDTO> memberList;
}
