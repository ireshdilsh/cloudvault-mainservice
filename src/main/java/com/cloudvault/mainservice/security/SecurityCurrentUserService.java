package com.cloudvault.mainservice.security;

import com.cloudvault.mainservice.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityCurrentUserService implements CurrentUserService {

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication is required");
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
            throw new UnauthorizedException("Authentication is required");
        }

        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("Authenticated principal must resolve to a numeric user id");
        }
    }
}
