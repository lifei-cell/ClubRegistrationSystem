package com.twt.club.registration.util;

import com.twt.club.registration.common.ErrorCode;
import com.twt.club.registration.exception.BusinessException;
import com.twt.club.registration.security.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    private SecurityUtils() {}

    private static JwtUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails userDetails) {
            return userDetails;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

    public static String getCurrentUsername() {
        return getCurrentUserDetails().getUsername();
    }

}
