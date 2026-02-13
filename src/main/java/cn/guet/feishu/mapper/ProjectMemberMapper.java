package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMemberMapper {
    
    ProjectMember selectByProjectMemberId(@Param("projectMemberId") String projectMemberId);
    
    ProjectMember selectByProjectIdAndUserId(@Param("projectId") String projectId, @Param("userId") String userId);
    
    List<ProjectMember> selectByProjectId(@Param("projectId") String projectId);
    
    List<ProjectMember> selectByUserId(@Param("userId") String userId);
    
    int insert(ProjectMember projectMember);
    
    int updateByProjectMemberId(ProjectMember projectMember);
    
    int deleteByProjectMemberId(@Param("projectMemberId") String projectMemberId);
}

