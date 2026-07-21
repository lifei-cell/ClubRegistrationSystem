package com.twt.club.registration.common;

public final class Constants {
    private Constants() {}

    // 角色常量
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // JWT 常量
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // 活动状态常量
    public static final String ACTIVITY_STATUS_UPCOMING = "UPCOMING";
    public static final String ACTIVITY_STATUS_ONGOING = "ONGOING";
    public static final String ACTIVITY_STATUS_ENDED = "ENDED";
    public static final String ACTIVITY_STATUS_CANCELLED = "CANCELLED";

    // 报名状态常量
    public static final String REGISTRATION_STATUS_REGISTERED = "REGISTERED";
    public static final String REGISTRATION_STATUS_CANCELLED = "CANCELLED";

    // 操作日志常量
    public static final String ACTION_CREATE_ACTIVITY = "CREATE_ACTIVITY";
    public static final String ACTION_UPDATE_ACTIVITY = "UPDATE_ACTIVITY";
    public static final String ACTION_DELETE_ACTIVITY = "DELETE_ACTIVITY";
    public static final String ACTION_CREATE_CATEGORY = "CREATE_CATEGORY";
    public static final String ACTION_UPDATE_CATEGORY = "UPDATE_CATEGORY";
    public static final String ACTION_DELETE_CATEGORY = "DELETE_CATEGORY";

    // 目标类型常量
    public static final String TARGET_TYPE_ACTIVITY = "activity";
    public static final String TARGET_TYPE_CATEGORY = "category";

    // 数据库限制常量
    public static final int ACTIVITY_TITLE_MAX_LENGTH = 100;
}
