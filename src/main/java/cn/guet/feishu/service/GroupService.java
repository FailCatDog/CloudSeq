package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.CreateGroupRequestDTO;
import cn.guet.feishu.controller.dto.GroupMemberDTO;
import cn.guet.feishu.entity.ProjectGroup;

import java.util.List;

public interface GroupService {
    
    ProjectGroup createGroup(String userId, CreateGroupRequestDTO request);
    
    ProjectGroup getGroupById(String groupId);
    
    List<ProjectGroup> getProjectGroups(String projectId);
    
    ProjectGroup getMyGroup(String userId, String projectId);
    
    void joinGroup(String userId, String groupId);
    
    void quitGroup(String userId, String groupId);
    
    List<GroupMemberDTO> getGroupMembers(String groupId);
    
    void dissolveGroup(String groupId);
    
    void transferLeader(String groupId, String newLeaderId);
}

