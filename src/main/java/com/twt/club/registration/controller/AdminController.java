package com.twt.club.registration.controller;

import com.twt.club.registration.common.Constants;
import com.twt.club.registration.common.Result;
import com.twt.club.registration.dto.ActivityCreateRequest;
import com.twt.club.registration.dto.ActivityUpdateRequest;
import com.twt.club.registration.dto.CategoryRequest;
import com.twt.club.registration.service.ActivityService;
import com.twt.club.registration.service.CategoryService;
import com.twt.club.registration.service.OperationLogService;
import com.twt.club.registration.service.RegistrationService;
import com.twt.club.registration.util.SecurityUtils;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.CategoryVO;
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
                Constants.ACTION_CREATE_ACTIVITY, Constants.TARGET_TYPE_ACTIVITY, activity.getId(), "创建活动: " + activity.getTitle());
        return Result.success("创建成功", activity);
    }

    @PutMapping("/activities/{id}")
    public Result<ActivityDetailVO> updateActivity(@PathVariable Long id,
                                                   @Valid @RequestBody ActivityUpdateRequest request) {
        ActivityDetailVO activity = activityService.update(id, request);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                Constants.ACTION_UPDATE_ACTIVITY, Constants.TARGET_TYPE_ACTIVITY, id, "修改活动: " + activity.getTitle());
        return Result.success("修改成功", activity);
    }

    @DeleteMapping("/activities/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityService.delete(id);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                Constants.ACTION_DELETE_ACTIVITY, Constants.TARGET_TYPE_ACTIVITY, id, "删除活动 ID: " + id);
        return Result.success("删除成功");
    }

    // 根据活动 id 获取报名记录

    @GetMapping("/activities/{id}/registrations")
    public Result<List<RegistrationVO>> activityRegistrations(@PathVariable Long id) {
        List<RegistrationVO> registrations = registrationService.getByActivityId(id);
        return Result.success(registrations);
    }

    //分类管理

    @PostMapping("/categories")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryVO category = categoryService.create(request);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                Constants.ACTION_CREATE_CATEGORY, Constants.TARGET_TYPE_CATEGORY, category.getId(), "创建分类: " + category.getName());
        return Result.success("创建成功", category);
    }

    @PutMapping("/categories/{id}")
    public Result<CategoryVO> updateCategory(@PathVariable Long id,
                                             @Valid @RequestBody CategoryRequest request) {
        CategoryVO category = categoryService.update(id, request);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                Constants.ACTION_UPDATE_CATEGORY, Constants.TARGET_TYPE_CATEGORY, id, "修改分类: " + category.getName());
        return Result.success("修改成功", category);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        operationLogService.log(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername(),
                Constants.ACTION_DELETE_CATEGORY, Constants.TARGET_TYPE_CATEGORY, id, "删除分类 ID: " + id);
        return Result.success("删除成功");
    }
}
