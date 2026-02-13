package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.ProjectGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectGroupMapper {
    
    ProjectGroup selectByGroupId(@Param("groupId") String groupId);
    
    ProjectGroup selectByGroupCode(@Param("projectId") String projectId, @Param("groupCode") String groupCode);
    
    List<ProjectGroup> selectByProjectId(@Param("projectId") String projectId);
    
    int countByProjectId(@Param("projectId") String projectId);
    
    int insert(ProjectGroup projectGroup);
    
    int updateByGroupId(ProjectGroup projectGroup);
    
    int deleteByGroupId(@Param("groupId") String groupId);
}

