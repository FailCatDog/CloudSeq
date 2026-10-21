package cn.guet.soft_manage.biz.workspace.dao;

import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区文档正文 Mapper
 */
@Mapper
public interface WorkspaceContentDao extends BaseMapper<WorkspaceContent> {
}
