package com.twt.club.registration.service;

import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.vo.RegistrationVO;

public interface RegistrationService {
    void register(Long userId, Long activityId);

    void cancel(Long userId, Long activityId);

    PageResult<RegistrationVO> getMyRegistrations(Long userId, Integer page, Integer size);
}
