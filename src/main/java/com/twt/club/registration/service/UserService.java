package com.twt.club.registration.service;

import com.twt.club.registration.dto.LoginRequest;
import com.twt.club.registration.dto.RegisterRequest;
import com.twt.club.registration.vo.LoginVO;
import com.twt.club.registration.vo.UserVO;

public interface UserService {
    UserVO register(RegisterRequest registerRequest);

    LoginVO login(LoginRequest loginRequest);
}
