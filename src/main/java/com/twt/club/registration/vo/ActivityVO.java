package com.twt.club.registration.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVO {
    private Long id;

    private String title;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private String status;

    private String categoryName;
}
