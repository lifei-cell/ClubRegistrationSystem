package com.twt.club.registration.service;

public interface OperationLogService {
    void log(Long userId, String username, String action, String targetType, Long targetId, String detail);
}

