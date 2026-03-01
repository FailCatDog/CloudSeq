package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.ProjectTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectTaskMapper {
    
    int insert(ProjectTask task);
    
    int updateByTaskId(ProjectTask task);
    
    int deleteByTaskId(@Param("taskId") String taskId);
    
    ProjectTask selectByTaskId(@Param("taskId") String taskId);
    
    List<ProjectTask> selectByProjectId(@Param("projectId") String projectId);
    
    List<ProjectTask> selectByGroupId(@Param("groupId") String groupId);
    
    List<ProjectTask> selectByParentTaskId(@Param("parentTaskId") String parentTaskId);
    
    List<ProjectTask> selectByCreatorId(@Param("creatorId") String creatorId);
    
    String selectMaxTaskCode(@Param("projectId") String projectId);
    
    List<ProjectTask> selectByCondition(@Param("projectId") String projectId,
                                        @Param("groupId") String groupId,
                                        @Param("status") Integer status,
                                        @Param("priority") Integer priority);
}

