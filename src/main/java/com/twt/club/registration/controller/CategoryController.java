package com.twt.club.registration.controller;

import com.twt.club.registration.common.Result;
import com.twt.club.registration.service.CategoryService;
import com.twt.club.registration.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> categories = categoryService.list();
        return Result.success(categories);
    }
}
