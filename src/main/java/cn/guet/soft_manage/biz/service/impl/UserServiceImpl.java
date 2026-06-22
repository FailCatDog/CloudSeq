package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.UserDao;
import cn.guet.soft_manage.biz.pojo.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.LoginResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.pojo.param.UserParam;
import cn.guet.soft_manage.biz.service.UserService;
import cn.guet.soft_manage.biz.utils.JwtUtil;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.UserRole;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getIsActive, 1));
        if (Objects.isNull(user)) {
            throw new BusinessException(BizResponseCode.LOGIN_FAILED);
        }
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BizResponseCode.LOGIN_FAILED);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userDao.updateById(user);

        String token = JwtUtil.generateToken(user);
        return LoginResponseDTO.builder().user(user).authorization(token).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequestDTO request) {
        if (request.getRole().equals(UserRole.STUDENT.getCode()) && (Objects.isNull(request.getStudentNo()) || request.getStudentNo().isBlank())) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "学生注册时学号不能为空");
        }

        boolean exists = userDao.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (exists) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR.getCode(), "用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setRole(UserRole.STUDENT.getCode());
        user.setCreateUser(1L);
        user.setUpdateUser(1L);
        userDao.insert(user);
    }

    @Override
    public User getProfile(Long userId) {
        User user = userDao.selectById(userId);
        if (Objects.isNull(user)) throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserParam param) {
        User user = userDao.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        user.setNickName(param.getNickName());
        user.setRealName(param.getRealName());
        user.setBio(param.getBio());
        user.setAvatarUrl(param.getAvatarUrl());
        userDao.updateById(user);
    }
}
