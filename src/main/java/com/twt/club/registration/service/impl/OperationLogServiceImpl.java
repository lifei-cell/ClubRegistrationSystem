package com.twt.club.registration.service.impl;

import com.twt.club.registration.entity.OperationLog;
import com.twt.club.registration.mapper.OperationLogMapper;
import com.twt.club.registration.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {
    private final OperationLogMapper operationLogMapper;

    @Override
    @Async
    public void log(Long userId, String username, String action, String targetType, Long targetId, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        operationLogMapper.insert(log);
    }
}
