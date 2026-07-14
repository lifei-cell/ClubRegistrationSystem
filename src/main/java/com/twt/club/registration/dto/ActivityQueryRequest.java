package com.twt.club.registration.dto;

import lombok.Data;

@Data
public class ActivityQueryRequest {
    private String keyword;

    private Long categoryId;

    private String status;

    private Integer page = 1;

    private Integer size = 10;
}
