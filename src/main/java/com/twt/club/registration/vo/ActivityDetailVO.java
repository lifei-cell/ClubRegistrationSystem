package com.twt.club.registration.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityDetailVO {
    private Long id;

    private String title;

    private String description;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private String status;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime createdAt;
}
