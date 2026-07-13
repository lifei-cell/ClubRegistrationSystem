package com.twt.club.registration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private Long categoryId;

    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
