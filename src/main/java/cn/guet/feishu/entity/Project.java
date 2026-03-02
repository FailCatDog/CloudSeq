package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Project {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目编码（自动生成）
     */
    private String projectCode;
    /**
     * 项目描述
     */
    private String description;
    /**
     * 创建者ID（教师）
     */
    private String creatorId;
    /**
     * 项目开始时间
     */
    private LocalDateTime startTime;
    /**
     * 项目结束时间
     */
    private LocalDateTime endTime;
    /**
     * 状态：1-准备中，2-进行中，3-已结束，4-已取消
     */
    private Integer status;
    /**
     * 最大小组人数
     */
    private Integer maxGroupSize;
    /**
     * 最小小组人数
     */
    private Integer minGroupSize;
    /**
     * 小组创建截止时间
     */
    private LocalDateTime groupDeadline;
    /**
     * 是否允许学生创建小组：1-允许，0-不允许
     */
    private Integer allowStudentCreateGroup;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

