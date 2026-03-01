package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskDependencyMapper {
    
    int insert(TaskDependency dependency);
    
    int deleteByDependencyId(@Param("dependencyId") String dependencyId);
    
    int deleteByTaskId(@Param("taskId") String taskId);
    
    TaskDependency selectByTaskIdAndDependOnTaskId(@Param("taskId") String taskId,
                                                    @Param("dependOnTaskId") String dependOnTaskId);
    
    List<TaskDependency> selectByTaskId(@Param("taskId") String taskId);
    
    List<TaskDependency> selectByDependOnTaskId(@Param("dependOnTaskId") String dependOnTaskId);
}

