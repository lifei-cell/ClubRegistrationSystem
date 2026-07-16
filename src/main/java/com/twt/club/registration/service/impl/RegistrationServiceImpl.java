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
import org.springframework.dao.DuplicateKeyException;
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
        activityMapper.updateCurrentParticipants(activityId, -1);
    }

    @Override
    public PageResult<RegistrationVO> getMyRegistrations(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .orderByDesc(Registration::getRegisteredAt);

        Page<Registration> p = new Page<>(page, size);
        p = registrationMapper.selectPage(p, wrapper);

        // 查询当前用户信息（所有记录属于同一用户，只需查一次）
        User user = userMapper.selectById(userId);

        List<RegistrationVO> records = enrichWithActivityInfo(p.getRecords(), user);

        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), records);
    }

    private List<RegistrationVO> enrichWithActivityInfo(List<Registration> registrations, User user) {
        if (registrations.isEmpty()) {
            return List.of();
        }

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
