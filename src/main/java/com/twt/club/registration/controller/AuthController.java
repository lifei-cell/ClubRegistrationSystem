package com.twt.club.registration.controller;

import com.twt.club.registration.common.Result;
import com.twt.club.registration.dto.LoginRequest;
import com.twt.club.registration.dto.RegisterRequest;
import com.twt.club.registration.service.UserService;
import com.twt.club.registration.vo.LoginVO;
import com.twt.club.registration.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserVO userVO = userService.register(registerRequest);
        return Result.success("注册成功", userVO);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginVO loginVO = userService.login(loginRequest);
        return Result.success("登录成功", loginVO);
    }

}
