package com.twt.club.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50位")
    private String name;

    @Size(max = 255, message = "分类描述长度不能超过255位")
    private String description;

    private Integer sortOrder;
}
