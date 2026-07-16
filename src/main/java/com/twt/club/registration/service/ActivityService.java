package com.twt.club.registration.service;

import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.dto.ActivityCreateRequest;
import com.twt.club.registration.dto.ActivityQueryRequest;
import com.twt.club.registration.dto.ActivityUpdateRequest;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.ActivityVO;

public interface ActivityService {
    PageResult<ActivityVO> list(ActivityQueryRequest request);

    ActivityDetailVO getById(Long id);

    ActivityDetailVO create(ActivityCreateRequest request, Long userId);

    ActivityDetailVO update(Long id, ActivityUpdateRequest request);

    void delete(Long id);
}
