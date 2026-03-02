package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectGroup {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String groupId;
    /**
     * 所属项目ID
     */
    private String projectId;
    /**
     * 小组名称
     */
    private String groupName;
    /**
     * 小组编码（自动生成）
     */
    private String groupCode;
    /**
     * 小组描述
     */
    private String description;
    /**
     * 组长ID
     */
    private String leaderId;
    /**
     * 创建者ID
     */
    private String creatorId;
    /**
     * 当前成员数
     */
    private Integer currentMembers;
    /**
     * 最大成员数
     */
    private Integer maxMembers;
    /**
     * 状态：1-正常，2-已解散
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

