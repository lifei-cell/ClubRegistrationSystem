package com.twt.club.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.twt.club.registration.common.ErrorCode;
import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.entity.Activity;
import com.twt.club.registration.entity.Registration;
import com.twt.club.registration.entity.User;
import com.twt.club.registration.exception.BusinessException;
import com.twt.club.registration.mapper.ActivityMapper;
import com.twt.club.registration.mapper.RegistrationMapper;
import com.twt.club.registration.mapper.UserMapper;
import com.twt.club.registration.service.RegistrationService;
import com.twt.club.registration.vo.RegistrationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void register(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        // 检查报名截止时间
        if (LocalDateTime.now().isAfter(activity.getRegistrationDeadline())) {
            throw new BusinessException(ErrorCode.REGISTRATION_DEADLINE_PASSED);
        }

        // 检查活动是否已开始
        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw new BusinessException(ErrorCode.ACTIVITY_STARTED, "活动已开始，无法报名");
        }

        // 先创建报名记录（利用数据库 uk_user_activity 唯一约束防止并发重复报名）
        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setActivityId(activityId);
        registration.setStatus("REGISTERED");
        registration.setRegisteredAt(LocalDateTime.now());
        try {
            registrationMapper.insert(registration);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED);
        }

        // 再扣减名额（乐观锁）
        int updated = activityMapper.updateCurrentParticipants(activityId, 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        // 活动开始后不允许取消
        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_STARTED);
        }

        // 原子更新：将状态从 REGISTERED 改为 CANCELLED，防止并发重复扣减名额
        LambdaUpdateWrapper<Registration> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Registration::getUserId, userId)
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getStatus, "REGISTERED")
                .set(Registration::getStatus, "CANCELLED")
                .set(Registration::getCancelledAt, LocalDateTime.now());
        int updated = registrationMapper.update(updateWrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }

        // 减少报名人数
        int participantUpdated = activityMapper.updateCurrentParticipants(activityId, -1);
        if (participantUpdated == 0) {
            log.warn("取消报名时扣减名额失败: activityId={}, userId={}", activityId, userId);
        }
    }

    @Override
    public PageResult<RegistrationVO> getMyRegistrations(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .orderByDesc(Registration::getRegisteredAt);

        Page<Registration> p = new Page<>(page, size);
        p = registrationMapper.selectPage(p, wrapper);

        List<RegistrationVO> records = enrichWithActivityInfo(p.getRecords());

        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), records);
    }

    @Override
    public List<RegistrationVO> getByActivityId(Long activityId) {
        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getActivityId, activityId)
                        .eq(Registration::getStatus, "REGISTERED")
                        .orderByAsc(Registration::getRegisteredAt));
        return enrichWithActivityInfo(registrations);
    }

    private List<RegistrationVO> enrichWithActivityInfo(List<Registration> registrations) {
        if (registrations.isEmpty()) {
            return List.of();
        }

        // 批量获取用户信息
        List<Long> userIds = registrations.stream()
                .map(Registration::getUserId)
                .distinct()
                .toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量获取活动信息
        List<Long> activityIds = registrations.stream()
                .map(Registration::getActivityId)
                .distinct()
                .toList();
        Map<Long, Activity> activityMap = activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, a -> a));

        return registrations.stream().map(r -> {
            RegistrationVO vo = new RegistrationVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setActivityId(r.getActivityId());
            vo.setStatus(r.getStatus());
            vo.setRegisteredAt(r.getRegisteredAt());
            vo.setCancelledAt(r.getCancelledAt());

            User user = userMap.get(r.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setEmail(user.getEmail());
            }

            Activity activity = activityMap.get(r.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }

            return vo;
        }).toList();
    }
}
