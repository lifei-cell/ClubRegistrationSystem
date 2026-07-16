package com.twt.club.registration.controller;

import com.twt.club.registration.common.Result;
import com.twt.club.registration.dto.ActivityCreateRequest;
import com.twt.club.registration.dto.ActivityUpdateRequest;
import com.twt.club.registration.service.ActivityService;
import com.twt.club.registration.service.CategoryService;
import com.twt.club.registration.service.OperationLogService;
import com.twt.club.registration.service.RegistrationService;
import com.twt.club.registration.util.SecurityUtils;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.RegistrationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ActivityService activityService;
    private final CategoryService categoryService;
    private final RegistrationService registrationService;
    private final OperationLogService operationLogService;

    //活动管理

    @PostMapping("/activities")
    public Result<ActivityDetailVO> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ActivityDetailVO activity = activityService.create(request, userId);
        operationLogService.log(userId, SecurityUtils.getCurrentUsername(),
                "CREATE_ACTIVITY", "activity", activity.getId(), "创建活动: " + activity.getTitle());
        return Result.success("创建成功", activity);
    }

    @PutMapping("/activities/{id}")
    public Result<ActivityDetailVO> updateActivity(@PathVariable Long id,
                                                   @RequestBody ActivityUpdateRequest request) {
        ActivityDetailVO activity = activityService.update(id, request);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                "UPDATE_ACTIVITY", "activity", id, "修改活动: " + activity.getTitle());
        return Result.success("修改成功", activity);
    }

    @DeleteMapping("/activities/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityService.delete(id);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                "DELETE_ACTIVITY", "activity", id, "删除活动 ID: " + id);
        return Result.success("删除成功");
    }

    @GetMapping("/activities/{id}/registrations")
    public Result<List<RegistrationVO>> activityRegistrations(@PathVariable Long id) {
        List<RegistrationVO> registrations = registrationService.getByActivityId(id);
        return Result.success(registrations);
    }
}
