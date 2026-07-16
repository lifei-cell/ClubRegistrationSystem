package com.twt.club.registration.controller;

import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.common.Result;
import com.twt.club.registration.service.RegistrationService;
import com.twt.club.registration.util.SecurityUtils;
import com.twt.club.registration.vo.RegistrationVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping("/activities/{id}/register")
    public Result<Void> register(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        registrationService.register(userId, id);
        return Result.success("报名成功");
    }

    @DeleteMapping("/activities/{id}/register")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        registrationService.cancel(userId, id);
        return Result.success("取消报名成功");
    }

    @GetMapping("/user/registrations")
    public Result<PageResult<RegistrationVO>> myRegistrations(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数不能小于1") @Max(value = 100, message = "每页条数不能超过100") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResult<RegistrationVO> result = registrationService.getMyRegistrations(userId, page, size);
        return Result.success(result);
    }
}
