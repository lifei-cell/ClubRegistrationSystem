package com.twt.club.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.twt.club.registration.common.ErrorCode;
import com.twt.club.registration.common.PageResult;
import com.twt.club.registration.dto.ActivityQueryRequest;
import com.twt.club.registration.entity.Activity;
import com.twt.club.registration.entity.Category;
import com.twt.club.registration.exception.BusinessException;
import com.twt.club.registration.mapper.ActivityMapper;
import com.twt.club.registration.mapper.CategoryMapper;
import com.twt.club.registration.service.ActivityService;
import com.twt.club.registration.vo.ActivityDetailVO;
import com.twt.club.registration.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activityMapper;
    private final CategoryMapper categoryMapper;

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

        int pageNum = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int pageSize = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
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
