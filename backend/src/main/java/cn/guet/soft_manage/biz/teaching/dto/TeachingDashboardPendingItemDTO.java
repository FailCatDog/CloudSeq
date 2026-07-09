package cn.guet.soft_manage.biz.teaching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教学工作台待审选题项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingDashboardPendingItemDTO {

    private Long id;

    private String teamLabel;

    private String topicTitle;

    private String leaderName;

    private Integer memberCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitDate;
}
