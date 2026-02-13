package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.CreateProjectRequestDTO;
import cn.guet.feishu.controller.dto.ProjectListItemDTO;
import cn.guet.feishu.controller.dto.ProjectMemberDTO;
import cn.guet.feishu.controller.dto.UpdateProjectRequestDTO;
import cn.guet.feishu.entity.Project;

import java.util.List;

public interface ProjectService {
    
    Project createProject(String creatorId, CreateProjectRequestDTO request);
    
    void updateProject(String projectId, UpdateProjectRequestDTO request);
    
    void deleteProject(String projectId);
    
    Project getProjectById(String projectId);
    
    Project getProjectByCode(String projectCode);
    
    List<ProjectListItemDTO> getMyProjects(String userId);
    
    List<Project> getAllProjects();
    
    void joinProject(String userId, String projectCode);
    
    void quitProject(String userId, String projectId);
    
    List<ProjectMemberDTO> getProjectMembers(String projectId);
}

