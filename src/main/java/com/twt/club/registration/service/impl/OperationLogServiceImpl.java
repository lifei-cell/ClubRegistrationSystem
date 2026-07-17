package com.twt.club.registration.service.impl;

import com.twt.club.registration.entity.OperationLog;
import com.twt.club.registration.mapper.OperationLogMapper;
import com.twt.club.registration.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {
    private final OperationLogMapper operationLogMapper;

    @Override
    @Async
    public void log(Long userId, String username, String action, String targetType, Long targetId, String detail) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setAction(action);
            operationLog.setTargetType(targetType);
            operationLog.setTargetId(targetId);
            operationLog.setDetail(detail);
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("操作日志写入失败: userId={}, action={}, targetType={}, targetId={}",
                    userId, action, targetType, targetId, e);
        }
    }
}
