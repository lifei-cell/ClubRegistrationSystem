package com.twt.club.registration.controller;

import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.common.Result;
import com.twt.club.registration.dto.ActivityQueryRequest;
import com.twt.club.registration.service.ActivityService;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.ActivityVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping
    public Result<PageResult<ActivityVO>> list(@Valid ActivityQueryRequest request) {
        PageResult<ActivityVO> result = activityService.list(request);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ActivityDetailVO> detail(@PathVariable Long id) {
        ActivityDetailVO activity = activityService.getById(id);
        return Result.success(activity);
    }


}
