package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.*;
import cn.guet.feishu.mapper.*;
import cn.guet.feishu.service.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ProjectTaskMapper projectTaskMapper;
    private final TaskAssignmentMapper taskAssignmentMapper;
    private final TaskDependencyMapper taskDependencyMapper;
    private final ProjectMapper projectMapper;
    private final ProjectGroupMapper projectGroupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProjectTask createTask(String userId, CreateTaskRequestDTO request) {
        Project project = projectMapper.selectByProjectId(request.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        User user = userMapper.selectByUserId(userId);
        boolean isTeacher = "TEACHER".equals(user.getRole());

        if (request.getGroupId() != null) {
            ProjectGroup group = projectGroupMapper.selectByGroupId(request.getGroupId());
            if (group == null) {
                throw new BusinessException("小组不存在");
            }
            if (!isTeacher) {
                GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(request.getGroupId(), userId);
                if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
                    throw new BusinessException("只有组长可以创建小组任务");
                }
            }
        }

        if (request.getParentTaskId() != null) {
            ProjectTask parentTask = projectTaskMapper.selectByTaskId(request.getParentTaskId());
            if (parentTask == null) {
                throw new BusinessException("父任务不存在");
            }
        }

        String taskCode = generateTaskCode(request.getProjectId());

        ProjectTask task = new ProjectTask();
        task.setTaskId(UUID.randomUUID().toString());
        task.setProjectId(request.getProjectId());
        task.setGroupId(request.getGroupId());
        task.setTaskTitle(request.getTaskTitle());
        task.setTaskDescription(request.getTaskDescription());
        task.setTaskCode(taskCode);
        task.setPriority(request.getPriority());
        task.setStatus(0);
        task.setProgress(0);
        task.setParentTaskId(request.getParentTaskId());
        task.setCreatorId(userId);
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setActualHours(BigDecimal.ZERO);
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                task.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("标签格式错误");
            }
        }

        projectTaskMapper.insert(task);

        if (request.getAssignToUserIds() != null && !request.getAssignToUserIds().isEmpty()) {
            for (String assignUserId : request.getAssignToUserIds()) {
                TaskAssignment assignment = new TaskAssignment();
                assignment.setAssignmentId(UUID.randomUUID().toString());
                assignment.setTaskId(task.getTaskId());
                assignment.setUserId(assignUserId);
                assignment.setAssignedBy(userId);
                assignment.setStatus(0);
                assignment.setProgress(0);
                assignment.setActualHours(BigDecimal.ZERO);
                taskAssignmentMapper.insert(assignment);
            }
        }

        return task;
    }

    @Override
    @Transactional
    public void updateTask(String userId, String taskId, UpdateTaskRequestDTO request) {
        ProjectTask task = projectTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        User user = userMapper.selectByUserId(userId);
        boolean isTeacher = "TEACHER".equals(user.getRole());

        if (!isTeacher && !task.getCreatorId().equals(userId)) {
            if (task.getGroupId() != null) {
                GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(task.getGroupId(), userId);
                if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
                    throw new BusinessException("无权限修改该任务");
                }
            } else {
                throw new BusinessException("无权限修改该任务");
            }
        }

        if (request.getTaskTitle() != null) {
            task.setTaskTitle(request.getTaskTitle());
        }
        if (request.getTaskDescription() != null) {
            task.setTaskDescription(request.getTaskDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getStartTime() != null) {
            task.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            task.setEndTime(request.getEndTime());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getTags() != null) {
            try {
                task.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("标签格式错误");
            }
        }

        projectTaskMapper.updateByTaskId(task);
    }

    @Override
    @Transactional
    public void deleteTask(String userId, String taskId) {
        ProjectTask task = projectTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        User user = userMapper.selectByUserId(userId);
        boolean isTeacher = "TEACHER".equals(user.getRole());

        if (!isTeacher && !task.getCreatorId().equals(userId)) {
            throw new BusinessException("无权限删除该任务");
        }

        List<ProjectTask> subTasks = projectTaskMapper.selectByParentTaskId(taskId);
        if (!subTasks.isEmpty()) {
            throw new BusinessException("请先删除子任务");
        }

        taskDependencyMapper.deleteByTaskId(taskId);
        taskAssignmentMapper.deleteByTaskId(taskId);
        projectTaskMapper.deleteByTaskId(taskId);
    }

    @Override
    public TaskDetailDTO getTaskDetail(String taskId) {
        ProjectTask task = projectTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskDetailDTO dto = new TaskDetailDTO();
        dto.setTaskId(task.getTaskId());
        dto.setProjectId(task.getProjectId());
        dto.setGroupId(task.getGroupId());
        dto.setTaskTitle(task.getTaskTitle());
        dto.setTaskDescription(task.getTaskDescription());
        dto.setTaskCode(task.getTaskCode());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setProgress(task.getProgress());
        dto.setParentTaskId(task.getParentTaskId());
        dto.setCreatorId(task.getCreatorId());
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setActualHours(task.getActualHours());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());

        if (task.getTags() != null) {
            try {
                List<String> tags = objectMapper.readValue(task.getTags(), List.class);
                dto.setTags(tags);
            } catch (JsonProcessingException e) {
                dto.setTags(new ArrayList<>());
            }
        }

        List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
        List<TaskAssignmentDTO> assignmentDTOs = assignments.stream().map(assignment -> {
            TaskAssignmentDTO assignmentDTO = new TaskAssignmentDTO();
            assignmentDTO.setAssignmentId(assignment.getAssignmentId());
            assignmentDTO.setTaskId(assignment.getTaskId());
            assignmentDTO.setUserId(assignment.getUserId());
            assignmentDTO.setAssignedBy(assignment.getAssignedBy());
            assignmentDTO.setStatus(assignment.getStatus());
            assignmentDTO.setProgress(assignment.getProgress());
            assignmentDTO.setActualHours(assignment.getActualHours());
            assignmentDTO.setAssignTime(assignment.getAssignTime());
            assignmentDTO.setStartTime(assignment.getStartTime());
            assignmentDTO.setFinishTime(assignment.getFinishTime());

            User user = userMapper.selectByUserId(assignment.getUserId());
            if (user != null) {
                assignmentDTO.setUsername(user.getUsername());
                assignmentDTO.setRealName(user.getRealName());
            }

            return assignmentDTO;
        }).collect(Collectors.toList());
        dto.setAssignments(assignmentDTOs);

        List<ProjectTask> subTasks = projectTaskMapper.selectByParentTaskId(taskId);
        List<TaskDetailDTO> subTaskDTOs = subTasks.stream().map(subTask -> {
            TaskDetailDTO subTaskDTO = new TaskDetailDTO();
            subTaskDTO.setTaskId(subTask.getTaskId());
            subTaskDTO.setTaskTitle(subTask.getTaskTitle());
            subTaskDTO.setStatus(subTask.getStatus());
            subTaskDTO.setProgress(subTask.getProgress());
            return subTaskDTO;
        }).collect(Collectors.toList());
        dto.setSubTasks(subTaskDTOs);

        List<TaskDependency> dependencies = taskDependencyMapper.selectByTaskId(taskId);
        List<String> dependOnTaskIds = dependencies.stream()
                .map(TaskDependency::getDependOnTaskId)
                .collect(Collectors.toList());
        dto.setDependOnTaskIds(dependOnTaskIds);

        return dto;
    }

    @Override
    public List<ProjectTask> getProjectTasks(String projectId, Integer status, Integer priority) {
        return projectTaskMapper.selectByCondition(projectId, null, status, priority);
    }

    @Override
    public List<ProjectTask> getGroupTasks(String groupId) {
        return projectTaskMapper.selectByGroupId(groupId);
    }

    @Override
    public List<ProjectTask> getMyTasks(String userId) {
        List<TaskAssignment> assignments = taskAssignmentMapper.selectByUserId(userId);
        return assignments.stream()
                .map(assignment -> projectTaskMapper.selectByTaskId(assignment.getTaskId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignTask(String userId, String taskId, List<String> userIds) {
        ProjectTask task = projectTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        User user = userMapper.selectByUserId(userId);
        boolean isTeacher = "TEACHER".equals(user.getRole());

        if (!isTeacher && !task.getCreatorId().equals(userId)) {
            if (task.getGroupId() != null) {
                GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(task.getGroupId(), userId);
                if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
                    throw new BusinessException("无权限分配该任务");
                }
            } else {
                throw new BusinessException("无权限分配该任务");
            }
        }

        for (String assignUserId : userIds) {
            TaskAssignment existing = taskAssignmentMapper.selectByTaskIdAndUserId(taskId, assignUserId);
            if (existing != null) {
                continue;
            }

            TaskAssignment assignment = new TaskAssignment();
            assignment.setAssignmentId(UUID.randomUUID().toString());
            assignment.setTaskId(taskId);
            assignment.setUserId(assignUserId);
            assignment.setAssignedBy(userId);
            assignment.setStatus(0);
            assignment.setProgress(0);
            assignment.setActualHours(BigDecimal.ZERO);
            taskAssignmentMapper.insert(assignment);
        }
    }

    @Override
    @Transactional
    public void updateAssignment(String userId, String taskId, UpdateAssignmentRequestDTO request) {
        TaskAssignment assignment = taskAssignmentMapper.selectByTaskIdAndUserId(taskId, userId);
        if (assignment == null) {
            throw new BusinessException("未找到任务分配记录");
        }

        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
            if (request.getStatus() == 1 && assignment.getStartTime() == null) {
                assignment.setStartTime(LocalDateTime.now());
            }
            if (request.getStatus() == 2) {
                assignment.setFinishTime(LocalDateTime.now());
            }
        }
        if (request.getProgress() != null) {
            assignment.setProgress(request.getProgress());
        }
        if (request.getActualHours() != null) {
            assignment.setActualHours(request.getActualHours());
        }

        taskAssignmentMapper.updateByAssignmentId(assignment);

        updateTaskProgress(taskId);
    }

    @Override
    @Transactional
    public void addDependency(String userId, String taskId, String dependOnTaskId) {
        ProjectTask task = projectTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        ProjectTask dependOnTask = projectTaskMapper.selectByTaskId(dependOnTaskId);
        if (dependOnTask == null) {
            throw new BusinessException("依赖任务不存在");
        }

        if (taskId.equals(dependOnTaskId)) {
            throw new BusinessException("任务不能依赖自己");
        }

        TaskDependency existing = taskDependencyMapper.selectByTaskIdAndDependOnTaskId(taskId, dependOnTaskId);
        if (existing != null) {
            throw new BusinessException("依赖关系已存在");
        }

        if (hasCircularDependency(taskId, dependOnTaskId)) {
            throw new BusinessException("存在循环依赖");
        }

        TaskDependency dependency = new TaskDependency();
        dependency.setDependencyId(UUID.randomUUID().toString());
        dependency.setTaskId(taskId);
        dependency.setDependOnTaskId(dependOnTaskId);
        dependency.setDependencyType(0);
        taskDependencyMapper.insert(dependency);
    }

    @Override
    @Transactional
    public void removeDependency(String userId, String taskId, String dependOnTaskId) {
        TaskDependency dependency = taskDependencyMapper.selectByTaskIdAndDependOnTaskId(taskId, dependOnTaskId);
        if (dependency == null) {
            throw new BusinessException("依赖关系不存在");
        }

        taskDependencyMapper.deleteByDependencyId(dependency.getDependencyId());
    }

    private String generateTaskCode(String projectId) {
        String maxCode = projectTaskMapper.selectMaxTaskCode(projectId);
        if (maxCode == null) {
            return "TASK001";
        }
        int num = Integer.parseInt(maxCode.substring(4)) + 1;
        return String.format("TASK%03d", num);
    }

    private void updateTaskProgress(String taskId) {
        List<TaskAssignment> assignments = taskAssignmentMapper.selectByTaskId(taskId);
        if (assignments.isEmpty()) {
            return;
        }

        int totalProgress = assignments.stream()
                .mapToInt(TaskAssignment::getProgress)
                .sum();
        int avgProgress = totalProgress / assignments.size();

        BigDecimal totalHours = assignments.stream()
                .map(TaskAssignment::getActualHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ProjectTask task = projectTaskMapper.selectByTaskId(taskId);
        task.setProgress(avgProgress);
        task.setActualHours(totalHours);

        boolean allCompleted = assignments.stream().allMatch(a -> a.getStatus() == 2);
        if (allCompleted) {
            task.setStatus(2);
        } else {
            boolean anyInProgress = assignments.stream().anyMatch(a -> a.getStatus() == 1);
            if (anyInProgress && task.getStatus() == 0) {
                task.setStatus(1);
            }
        }

        projectTaskMapper.updateByTaskId(task);
    }

    private boolean hasCircularDependency(String taskId, String dependOnTaskId) {
        List<TaskDependency> dependencies = taskDependencyMapper.selectByTaskId(dependOnTaskId);
        for (TaskDependency dep : dependencies) {
            if (dep.getDependOnTaskId().equals(taskId)) {
                return true;
            }
            if (hasCircularDependency(taskId, dep.getDependOnTaskId())) {
                return true;
            }
        }
        return false;
    }
}

