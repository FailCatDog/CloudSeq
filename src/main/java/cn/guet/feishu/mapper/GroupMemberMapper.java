package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMemberMapper {
    
    GroupMember selectByGroupMemberId(@Param("groupMemberId") String groupMemberId);
    
    GroupMember selectByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
    
    GroupMember selectByProjectIdAndUserId(@Param("projectId") String projectId, @Param("userId") String userId);
    
    List<GroupMember> selectByGroupId(@Param("groupId") String groupId);
    
    List<GroupMember> selectByUserId(@Param("userId") String userId);
    
    int countByGroupId(@Param("groupId") String groupId);
    
    int insert(GroupMember groupMember);
    
    int updateByGroupMemberId(GroupMember groupMember);
    
    int deleteByGroupMemberId(@Param("groupMemberId") String groupMemberId);
    
    int deleteByGroupId(@Param("groupId") String groupId);
}

