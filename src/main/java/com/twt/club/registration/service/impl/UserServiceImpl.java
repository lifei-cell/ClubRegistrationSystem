package com.twt.club.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.twt.club.registration.common.Constants;
import com.twt.club.registration.common.ErrorCode;
import com.twt.club.registration.dto.LoginRequest;
import com.twt.club.registration.dto.RegisterRequest;
import com.twt.club.registration.entity.User;
import com.twt.club.registration.exception.BusinessException;
import com.twt.club.registration.mapper.UserMapper;
import com.twt.club.registration.security.JwtUtils;
import com.twt.club.registration.service.UserService;
import com.twt.club.registration.vo.LoginVO;
import com.twt.club.registration.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public UserVO register(RegisterRequest registerRequest) {
        // 检查用户名重复
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerRequest.getUsername())) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 检查邮箱重复
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, registerRequest.getEmail())) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 构建用户实体并保存
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Constants.ROLE_USER);
        userMapper.insert(user);

        return toUserVO(user);
    }

    @Override
    public LoginVO login(LoginRequest loginRequest) {
        // 查找用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginRequest.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 校验密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginVO(token, user.getId(), user.getUsername(), user.getRole());
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
