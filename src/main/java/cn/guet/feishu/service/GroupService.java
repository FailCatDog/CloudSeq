package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.CreateGroupRequestDTO;
import cn.guet.feishu.controller.dto.GroupMemberDTO;
import cn.guet.feishu.entity.ProjectGroup;

import java.util.List;

public interface GroupService {
    
    /**
     * 创建小组
     * 
     * @param userId 用户ID
     * @param request 创建请求，包含项目ID、小组名称、描述
     * @return 创建的小组
     */
    ProjectGroup createGroup(String userId, CreateGroupRequestDTO request);
    
    /**
     * 获取小组详情
     * 
     * @param groupId 小组ID
     * @return 小组详情
     */
    ProjectGroup getGroupById(String groupId);
    
    /**
     * 获取项目下的所有小组
     * 
     * @param projectId 项目ID
     * @return 小组列表
     */
    List<ProjectGroup> getProjectGroups(String projectId);
    
    /**
     * 获取我在项目中的小组
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 小组信息
     */
    ProjectGroup getMyGroup(String userId, String projectId);
    
    /**
     * 加入小组
     * 
     * @param userId 用户ID
     * @param groupId 小组ID
     */
    void joinGroup(String userId, String groupId);
    
    /**
     * 退出小组
     * 
     * @param userId 用户ID
     * @param groupId 小组ID
     */
    void quitGroup(String userId, String groupId);
    
    /**
     * 获取小组成员列表
     * 
     * @param groupId 小组ID
     * @return 成员列表
     */
    List<GroupMemberDTO> getGroupMembers(String groupId);
    
    /**
     * 解散小组
     * 
     * @param groupId 小组ID
     */
    void dissolveGroup(String groupId);
    
    /**
     * 转让组长
     * 
     * @param groupId 小组ID
     * @param newLeaderId 新组长ID
     */
    void transferLeader(String groupId, String newLeaderId);
}

