package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.TaskAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskAssignmentMapper {
    
    int insert(TaskAssignment assignment);
    
    int updateByAssignmentId(TaskAssignment assignment);
    
    int deleteByAssignmentId(@Param("assignmentId") String assignmentId);
    
    int deleteByTaskId(@Param("taskId") String taskId);
    
    TaskAssignment selectByAssignmentId(@Param("assignmentId") String assignmentId);
    
    TaskAssignment selectByTaskIdAndUserId(@Param("taskId") String taskId, 
                                           @Param("userId") String userId);
    
    List<TaskAssignment> selectByTaskId(@Param("taskId") String taskId);
    
    List<TaskAssignment> selectByUserId(@Param("userId") String userId);
    
    int countCompletedByUserId(@Param("userId") String userId);
}

