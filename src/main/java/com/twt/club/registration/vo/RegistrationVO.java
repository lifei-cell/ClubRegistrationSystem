package com.twt.club.registration.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationVO {
    private Long id;

    private Long userId;

    private String username;

    private String email;

    private Long activityId;

    private String activityTitle;

    private String status;

    private LocalDateTime registeredAt;

    private LocalDateTime cancelledAt;
}
