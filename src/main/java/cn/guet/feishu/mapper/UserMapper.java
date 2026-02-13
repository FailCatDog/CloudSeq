package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    
    User selectByUserId(@Param("userId") String userId);
    
    User selectByUsername(@Param("username") String username);
    
    User selectByStudentId(@Param("studentId") String studentId);
    
    User selectByTeacherId(@Param("teacherId") String teacherId);
    
    int insert(User user);
    
    int updateByUserId(User user);
}


