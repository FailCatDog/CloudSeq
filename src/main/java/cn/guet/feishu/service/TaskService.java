package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.ProjectTask;

import java.util.List;

public interface TaskService {
    
    /**
     * 创建任务
     * 
     * @param userId 用户ID
     * @param request 创建请求，包含任务标题、描述、优先级等
     * @return 创建的任务
     */
    ProjectTask createTask(String userId, CreateTaskRequestDTO request);
    
    /**
     * 更新任务
     * 
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param request 更新请求，包含任务标题、描述、状态等
     */
    void updateTask(String userId, String taskId, UpdateTaskRequestDTO request);
    
    /**
     * 删除任务
     * 
     * @param userId 用户ID
     * @param taskId 任务ID
     */
    void deleteTask(String userId, String taskId);
    
    /**
     * 获取任务详情
     * 
     * @param taskId 任务ID
     * @return 任务详情，包含分配、子任务、依赖等
     */
    TaskDetailDTO getTaskDetail(String taskId);
    
    /**
     * 获取项目任务列表
     * 
     * @param projectId 项目ID
     * @param status 状态筛选（可选）
     * @param priority 优先级筛选（可选）
     * @return 任务列表
     */
    List<ProjectTask> getProjectTasks(String projectId, Integer status, Integer priority);
    
    /**
     * 获取小组任务列表
     * 
     * @param groupId 小组ID
     * @return 任务列表
     */
    List<ProjectTask> getGroupTasks(String groupId);
    
    /**
     * 获取我的任务列表
     * 
     * @param userId 用户ID
     * @return 任务列表
     */
    List<ProjectTask> getMyTasks(String userId);
    
    /**
     * 分配任务
     * 
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param userIds 被分配的用户ID列表
     */
    void assignTask(String userId, String taskId, List<String> userIds);
    
    /**
     * 更新任务分配状态
     * 
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param request 更新请求，包含状态、进度、工时
     */
    void updateAssignment(String userId, String taskId, UpdateAssignmentRequestDTO request);
    
    /**
     * 添加任务依赖
     * 
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param dependOnTaskId 依赖的任务ID（前置任务）
     */
    void addDependency(String userId, String taskId, String dependOnTaskId);
    
    /**
     * 移除任务依赖
     * 
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param dependOnTaskId 依赖的任务ID
     */
    void removeDependency(String userId, String taskId, String dependOnTaskId);

    /**
     * 获取项目任务统计
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 任务统计信息
     */
    TaskStatisticsDTO getProjectTaskStatistics(String userId, String projectId);

    /**
     * 获取我的任务统计
     * 
     * @param userId 用户ID
     * @return 任务统计信息
     */
    TaskStatisticsDTO getMyTaskStatistics(String userId);
}

