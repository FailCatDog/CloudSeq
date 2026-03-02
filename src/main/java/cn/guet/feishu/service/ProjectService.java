package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.CreateProjectRequestDTO;
import cn.guet.feishu.controller.dto.ProjectListItemDTO;
import cn.guet.feishu.controller.dto.ProjectMemberDTO;
import cn.guet.feishu.controller.dto.UpdateProjectRequestDTO;
import cn.guet.feishu.entity.Project;

import java.util.List;

public interface ProjectService {
    
    /**
     * 创建项目
     * 
     * @param creatorId 创建者ID（教师）
     * @param request 创建请求，包含项目名称、描述、时间等
     * @return 创建的项目
     */
    Project createProject(String creatorId, CreateProjectRequestDTO request);
    
    /**
     * 更新项目信息
     * 
     * @param projectId 项目ID
     * @param request 更新请求，包含项目名称、描述、状态等
     */
    void updateProject(String projectId, UpdateProjectRequestDTO request);
    
    /**
     * 删除项目
     * 
     * @param projectId 项目ID
     */
    void deleteProject(String projectId);
    
    /**
     * 根据ID获取项目详情
     * 
     * @param projectId 项目ID
     * @return 项目详情
     */
    Project getProjectById(String projectId);
    
    /**
     * 根据项目编码获取项目详情
     * 
     * @param projectCode 项目编码
     * @return 项目详情
     */
    Project getProjectByCode(String projectCode);
    
    /**
     * 获取我参与的项目列表
     * 
     * @param userId 用户ID
     * @return 项目列表
     */
    List<ProjectListItemDTO> getMyProjects(String userId);
    
    /**
     * 获取所有项目列表
     * 
     * @return 项目列表
     */
    List<Project> getAllProjects();
    
    /**
     * 加入项目
     * 
     * @param userId 用户ID
     * @param projectCode 项目编码
     */
    void joinProject(String userId, String projectCode);
    
    /**
     * 退出项目
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     */
    void quitProject(String userId, String projectId);
    
    /**
     * 获取项目成员列表
     * 
     * @param projectId 项目ID
     * @return 成员列表
     */
    List<ProjectMemberDTO> getProjectMembers(String projectId);
}

