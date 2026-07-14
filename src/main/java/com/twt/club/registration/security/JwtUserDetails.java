package com.twt.club.registration.security;

import lombok.Data;

@Data
public class JwtUserDetails {
    private final Long userId;
    private final String username;
    public JwtUserDetails(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
