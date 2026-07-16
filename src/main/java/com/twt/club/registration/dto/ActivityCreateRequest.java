package com.twt.club.registration.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityCreateRequest {
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100位")
    private String title;

    @NotBlank(message = "活动介绍不能为空")
    private String description;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 200, message = "地点长度不能超过200位")
    private String location;

    @NotNull(message = "活动开始时间不能为空")
    @Future(message = "活动开始时间必须在将来")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime registrationDeadline;

    @NotNull(message = "最大报名人数不能为空")
    @Min(value = 1, message = "最大报名人数必须大于0")
    private Integer maxParticipants;

    private Long categoryId;
}
