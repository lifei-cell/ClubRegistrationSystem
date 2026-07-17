package com.twt.club.registration.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityUpdateRequest {
    @Size(max = 100, message = "标题长度不能超过100位")
    private String title;

    private String description;

    @Size(max = 200, message = "地点长度不能超过200位")
    private String location;

    @Future(message = "活动开始时间必须在将来")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    @Min(value = 1, message = "最大报名人数必须大于0")
    private Integer maxParticipants;

    private Long categoryId;
}
