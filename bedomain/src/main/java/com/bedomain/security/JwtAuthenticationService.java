package com.bedomain.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JwtAuthenticationService {

    public Optional<String> getCurrentUserId() {
        return getJwt()
            .map(jwt -> jwt.getClaimAsString("sub"));
    }

    public Optional<String> getCurrentUsername() {
        return getJwt()
            .map(jwt -> jwt.getClaimAsString("preferred_username"));
    }

    public Optional<String> getCurrentUserEmail() {
        return getJwt()
            .map(jwt -> jwt.getClaimAsString("email"));
    }

    private Optional<Jwt> getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    public String getRequiredUserId() {
        return getCurrentUserId()
            .orElseThrow(() -> new IllegalStateException("No user ID found in JWT"));
    }

    public String getRequiredUsername() {
        return getCurrentUsername()
            .orElse(getRequiredUserId());
    }
}
