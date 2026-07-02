package cn.guet.soft_manage.biz.workspace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区文档编辑锁实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sm_workspace_lock")
public class WorkspaceLock {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long nodeId;

    private Long holderUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    @TableLogic
    private Integer delFlag;

    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Version
    private Long version;
}
