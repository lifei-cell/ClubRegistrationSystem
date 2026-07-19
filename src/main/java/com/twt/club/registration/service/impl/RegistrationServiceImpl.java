package com.twt.club.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.twt.club.registration.common.Constants;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
            throw new BusinessException(ErrorCode.ACTIVITY_STARTED);
        }

        // 查找已有记录（可能是 CANCELLED 状态，也可能是还没记录）
        Registration existing = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getActivityId, activityId));

        if (existing != null && Constants.REGISTRATION_STATUS_REGISTERED.equals(existing.getStatus())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED);
        }

        // 给当前报名人数加 1
        int updated = activityMapper.updateCurrentParticipants(activityId, 1);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }

        if (existing != null) {
            // 之前取消过，重新激活原记录
            existing.setStatus(Constants.REGISTRATION_STATUS_REGISTERED);
            existing.setRegisteredAt(LocalDateTime.now());
            existing.setCancelledAt(null);
            registrationMapper.updateById(existing);
        } else {
            Registration registration = new Registration();
            registration.setUserId(userId);
            registration.setActivityId(activityId);
            registration.setStatus(Constants.REGISTRATION_STATUS_REGISTERED);
            registration.setRegisteredAt(LocalDateTime.now());
            registrationMapper.insert(registration);
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

        // 原子更新：只修改状态和取消时间，不软删除
        LambdaUpdateWrapper<Registration> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Registration::getUserId, userId)
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getStatus, Constants.REGISTRATION_STATUS_REGISTERED)
                .set(Registration::getStatus, Constants.REGISTRATION_STATUS_CANCELLED)
                .set(Registration::getCancelledAt, LocalDateTime.now());
        int updated = registrationMapper.update(updateWrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }

        // 将报名人数减 1
        int participantUpdated = activityMapper.updateCurrentParticipants(activityId, -1);
        if (participantUpdated == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "取消报名时扣减名额失败，请稍后重试");
        }
    }

    @Override
    public PageResult<RegistrationVO> getMyRegistrations(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getStatus, Constants.REGISTRATION_STATUS_REGISTERED)
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
                        .eq(Registration::getStatus, Constants.REGISTRATION_STATUS_REGISTERED)
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
