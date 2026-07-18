package com.twt.club.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.twt.club.registration.common.ErrorCode;
import com.twt.club.registration.dto.CategoryRequest;
import com.twt.club.registration.entity.Activity;
import com.twt.club.registration.entity.Category;
import com.twt.club.registration.exception.BusinessException;
import com.twt.club.registration.mapper.ActivityMapper;
import com.twt.club.registration.mapper.CategoryMapper;
import com.twt.club.registration.service.CategoryService;
import com.twt.club.registration.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final ActivityMapper activityMapper;

    @Override
    public List<CategoryVO> list() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
        return categories.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public CategoryVO create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
        }

        return toVO(category);
    }

    @Override
    @Transactional
    public CategoryVO update(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        categoryMapper.updateById(category);

        return toVO(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 检查分类下是否有活动
        Long activityCount = activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCategoryId, id));
        if (activityCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_ACTIVITIES);
        }

        categoryMapper.deleteById(id);
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        vo.setCreatedAt(category.getCreatedAt());
        return vo;
    }

}
