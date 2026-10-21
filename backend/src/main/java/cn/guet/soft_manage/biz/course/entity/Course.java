package cn.guet.soft_manage.biz.course.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教学班（课号）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("fs_course")
public class Course {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String courseCode;

    private String courseName;

    private Integer termYear;

    private String termSeason;

    private String description;

    private Long primaryTeacherId;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topicDeadline;

    private Integer minTeamSize;

    private Integer maxTeamSize;

    private Integer weeklyRequired;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Version
    private Long version;
}
