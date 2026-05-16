package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.pojo.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.UpdateProfileRequest;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.dao.UserDao;
import cn.guet.soft_manage.biz.service.UserService;
import cn.guet.soft_manage.biz.pojo.vo.LoginVO;
import cn.guet.soft_manage.biz.pojo.vo.UserProfileVO;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.UserRole;
import cn.guet.soft_manage.frame.exception.BusinessException;
import cn.guet.soft_manage.biz.utils.JwtUtil;
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
    public LoginVO login(LoginRequestDTO request) {
        User user = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getIsActive, 1));
        if (user == null) {
            throw new BusinessException(BizResponseCode.LOGIN_FAILED);
        }
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BizResponseCode.LOGIN_FAILED);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userDao.updateById(user);

        String token = JwtUtil.generateToken(user);
        return new LoginVO(user.getId(), user.getUsername(), user.getRole(), user.getNickName(), user.getRealName(), user.getAvatarUrl(), token);
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
        userDao.insert(user);
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userDao.selectById(userId);
        if (Objects.isNull(user)) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        return new UserProfileVO(user.getId(), user.getUsername(), user.getRole(), user.getStudentNo(), user.getNickName(), user.getRealName(), user.getBio(), user.getAvatarUrl(), user.getLastLoginAt(), user.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        user.setNickName(request.getNickName());
        user.setRealName(request.getRealName());
        user.setBio(request.getBio());
        user.setAvatarUrl(request.getAvatarUrl());
        userDao.updateById(user);
    }
}
