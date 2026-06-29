package cn.guet.soft_manage.biz.pojo.entity;

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
 * @Description: 工作区文档正文实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sm_workspace_content")
public class WorkspaceContent {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long nodeId;

    private String contentMd;

    private byte[] yjsState;

    /** 正文字符数 */
    private Integer charCount;

    /** Markdown 字节数（UTF-8） */
    private Integer contentBytes;

    /** Yjs 快照字节数 */
    private Integer yjsBytes;

    /** 正文摘要，供列表/导航展示 */
    private String summary;

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
