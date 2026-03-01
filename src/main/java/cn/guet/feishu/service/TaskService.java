package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.ProjectTask;

import java.util.List;

public interface TaskService {
    
    ProjectTask createTask(String userId, CreateTaskRequestDTO request);
    
    void updateTask(String userId, String taskId, UpdateTaskRequestDTO request);
    
    void deleteTask(String userId, String taskId);
    
    TaskDetailDTO getTaskDetail(String taskId);
    
    List<ProjectTask> getProjectTasks(String projectId, Integer status, Integer priority);
    
    List<ProjectTask> getGroupTasks(String groupId);
    
    List<ProjectTask> getMyTasks(String userId);
    
    void assignTask(String userId, String taskId, List<String> userIds);
    
    void updateAssignment(String userId, String taskId, UpdateAssignmentRequestDTO request);
    
    void addDependency(String userId, String taskId, String dependOnTaskId);
    
    void removeDependency(String userId, String taskId, String dependOnTaskId);
}

