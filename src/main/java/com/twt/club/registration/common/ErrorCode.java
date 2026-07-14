package com.twt.club.registration.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用错误
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 无效"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 用户相关 1xxx
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已被注册"),
    USER_NOT_FOUND(1003, "用户不存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    INVALID_TOKEN(1005, "Token 无效或已过期"),

    // 活动相关 2xxx
    ACTIVITY_NOT_FOUND(2001, "活动不存在"),
    ACTIVITY_DELETED(2002, "活动已删除"),
    REGISTRATION_DEADLINE_PASSED(2003, "报名已截止"),
    ACTIVITY_FULL(2004, "活动名额已满"),
    ACTIVITY_STARTED(2005, "活动已开始，无法报名"),
    INVALID_ACTIVITY_TIME(2006, "活动时间不合法"),
    ACTIVITY_HAS_REGISTRATIONS(2007, "活动已有报名记录，无法删除"),

    // 报名相关 3xxx
    ALREADY_REGISTERED(3001, "已报名该活动"),
    REGISTRATION_NOT_FOUND(3002, "未找到报名记录"),
    CANNOT_CANCEL_OTHERS(3003, "只能取消自己的报名"),
    CANNOT_CANCEL_STARTED(3004, "活动已开始，无法取消报名"),
    CAPACITY_LESS_THAN_CURRENT(3005, "最大报名人数不能小于当前报名人数"),

    // 分类相关 4xxx
    CATEGORY_NOT_FOUND(4001, "分类不存在"),
    CATEGORY_NAME_EXISTS(4002, "分类名称已存在"),
    CATEGORY_HAS_ACTIVITIES(4003, "分类下存在活动，无法删除"),

    // 参数校验
    VALIDATION_ERROR(4000, "参数校验失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
