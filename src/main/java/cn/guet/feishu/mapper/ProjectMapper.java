package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {
    
    Project selectByProjectId(@Param("projectId") String projectId);
    
    Project selectByProjectCode(@Param("projectCode") String projectCode);
    
    List<Project> selectByCreatorId(@Param("creatorId") String creatorId);
    
    List<Project> selectAll();
    
    int insert(Project project);
    
    int updateByProjectId(Project project);
    
    int deleteByProjectId(@Param("projectId") String projectId);
}

