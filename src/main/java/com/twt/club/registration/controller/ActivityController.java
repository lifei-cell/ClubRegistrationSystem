package com.twt.club.registration.controller;

import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.common.Result;
import com.twt.club.registration.service.ActivityService;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.ActivityVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Validated
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping
    public Result<PageResult<ActivityVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不能小于1") @Max(value = 100, message = "每页条数不能超过100") Integer size) {
        PageResult<ActivityVO> result = activityService.list(keyword, categoryId, status, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ActivityDetailVO> detail(@PathVariable Long id) {
        ActivityDetailVO activity = activityService.getById(id);
        return Result.success(activity);
    }


}
