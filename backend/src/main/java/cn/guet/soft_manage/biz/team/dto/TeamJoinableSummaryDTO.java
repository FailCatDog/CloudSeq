package cn.guet.soft_manage.biz.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamJoinableSummaryDTO {

    private Long id;

    private String teamName;

    private String leaderName;

    private Integer memberCount;

    private Integer maxTeamSize;

    private Boolean full;

    private String status;
}
