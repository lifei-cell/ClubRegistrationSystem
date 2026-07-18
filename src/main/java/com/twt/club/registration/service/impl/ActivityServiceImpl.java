package com.twt.club.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.twt.club.registration.common.ErrorCode;
import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.dto.ActivityCreateRequest;
import com.twt.club.registration.dto.ActivityQueryRequest;
import com.twt.club.registration.dto.ActivityUpdateRequest;
import com.twt.club.registration.entity.Activity;
import com.twt.club.registration.entity.Category;
import com.twt.club.registration.entity.Registration;
import com.twt.club.registration.exception.BusinessException;
import com.twt.club.registration.mapper.ActivityMapper;
import com.twt.club.registration.mapper.CategoryMapper;
import com.twt.club.registration.mapper.RegistrationMapper;
import com.twt.club.registration.service.ActivityService;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final CategoryMapper categoryMapper;
    private final RegistrationMapper registrationMapper;

    @Override
    public PageResult<ActivityVO> list(ActivityQueryRequest request) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(Activity::getTitle, request.getKeyword())
                    .or()
                    .like(Activity::getLocation, request.getKeyword()));
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Activity::getCategoryId, request.getCategoryId());
        }

        // 状态筛选
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Activity::getStatus, request.getStatus());
        }

        // 按开始时间倒序
        wrapper.orderByDesc(Activity::getStartTime);

        int pageNum = request.getPage();
        int pageSize = request.getSize();
        Page<Activity> page = new Page<>(pageNum, pageSize);
        page = activityMapper.selectPage(page, wrapper);

        // 获取分类名称映射
        List<Activity> activities = page.getRecords();
        List<Long> categoryIds = activities.stream()
                .map(Activity::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> categoryMap = Map.of();
        if (!categoryIds.isEmpty()) {
            categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        Map<Long, String> finalCategoryMap = categoryMap;
        List<ActivityVO> records = activities.stream()
                .map(a -> toVO(a, finalCategoryMap.get(a.getCategoryId())))
                .toList();

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public ActivityDetailVO getById(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        String categoryName = null;
        if (activity.getCategoryId() != null) {
            Category category = categoryMapper.selectById(activity.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        return toDetailVO(activity, categoryName);
    }

    @Override
    @Transactional
    public ActivityDetailVO create(ActivityCreateRequest request, Long userId) {
        validateActivityTime(request.getStartTime(), request.getEndTime(), request.getRegistrationDeadline());

        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setLocation(request.getLocation());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setRegistrationDeadline(request.getRegistrationDeadline());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCurrentParticipants(0);
        activity.setCategoryId(request.getCategoryId());
        activity.setStatus("UPCOMING");
        activity.setCreatedBy(userId);

        try {
            activityMapper.insert(activity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ACTIVITY_NAME_EXISTS);
        }

        return getById(activity.getId());
    }

    @Override
    @Transactional
    public ActivityDetailVO update(Long id, ActivityUpdateRequest request) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        if (request.getTitle() != null) {
            activity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            activity.setLocation(request.getLocation());
        }

        // 校验最大报名人数变更
        if (request.getMaxParticipants() != null) {
            if (request.getMaxParticipants() < activity.getCurrentParticipants()) {
                throw new BusinessException(ErrorCode.CAPACITY_LESS_THAN_CURRENT);
            }
            activity.setMaxParticipants(request.getMaxParticipants());
        }

        // 更新时间字段
        LocalDateTime newStart = request.getStartTime() != null ? request.getStartTime() : activity.getStartTime();
        LocalDateTime newEnd = request.getEndTime() != null ? request.getEndTime() : activity.getEndTime();
        LocalDateTime newDeadline = request.getRegistrationDeadline() != null
                ? request.getRegistrationDeadline() : activity.getRegistrationDeadline();
        validateActivityTime(newStart, newEnd, newDeadline);

        activity.setStartTime(newStart);
        activity.setEndTime(newEnd);
        activity.setRegistrationDeadline(newDeadline);

        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            activity.setCategoryId(request.getCategoryId());
        }

        activityMapper.updateById(activity);
        return getById(activity.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        // 检查是否有有效报名记录
        Long count = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, id)
                .eq(Registration::getStatus, "REGISTERED"));
        if (count > 0) {
            throw new BusinessException(ErrorCode.ACTIVITY_HAS_REGISTRATIONS);
        }

        activityMapper.deleteById(id);
    }

    private void validateActivityTime(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime deadline) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_ACTIVITY_TIME, "活动开始时间必须早于结束时间");
        }
        if (!deadline.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.INVALID_ACTIVITY_TIME, "报名截止时间必须早于活动开始时间");
        }
    }

    private ActivityVO toVO(Activity activity, String categoryName) {
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setLocation(activity.getLocation());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setRegistrationDeadline(activity.getRegistrationDeadline());
        vo.setMaxParticipants(activity.getMaxParticipants());
        vo.setCurrentParticipants(activity.getCurrentParticipants());
        vo.setStatus(activity.getStatus());
        vo.setCategoryName(categoryName);
        return vo;
    }

    private ActivityDetailVO toDetailVO(Activity activity, String categoryName) {
        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setLocation(activity.getLocation());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setRegistrationDeadline(activity.getRegistrationDeadline());
        vo.setMaxParticipants(activity.getMaxParticipants());
        vo.setCurrentParticipants(activity.getCurrentParticipants());
        vo.setStatus(activity.getStatus());
        vo.setCategoryId(activity.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setCreatedAt(activity.getCreatedAt());
        return vo;
    }
}
