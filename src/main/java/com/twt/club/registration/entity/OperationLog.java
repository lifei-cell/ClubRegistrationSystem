package com.twt.club.registration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String action;

    private String targetType;

    private Long targetId;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
