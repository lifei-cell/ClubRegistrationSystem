package com.twt.club.registration.service;

import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.dto.ActivityQueryRequest;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.ActivityVO;

public interface ActivityService {
    PageResult<ActivityVO> list(ActivityQueryRequest request);

    ActivityDetailVO getById(Long id);
}
