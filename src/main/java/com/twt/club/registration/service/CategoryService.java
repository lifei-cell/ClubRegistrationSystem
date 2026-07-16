package com.twt.club.registration.service;

import com.twt.club.registration.dto.CategoryRequest;
import com.twt.club.registration.vo.CategoryVO;
import java.util.List;

public interface CategoryService {
    List<CategoryVO> list();

    CategoryVO create(CategoryRequest request);

    CategoryVO update(Long id, CategoryRequest request);

    void delete(Long id);
}
