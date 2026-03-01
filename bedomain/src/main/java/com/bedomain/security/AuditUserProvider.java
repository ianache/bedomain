package com.bedomain.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Provides the current auditor for JPA auditing based on JWT claims.
 * Extracts user identity from the JWT token's "preferred_username" claim,
 * falling back to "sub" claim if preferred_username is not present.
 */
public class AuditUserProvider implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("system");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt jwt) {
            // Try preferred_username first (typical Keycloak claim)
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null && !username.isBlank()) {
                return Optional.of(username);
            }
            // Fall back to "sub" claim (subject/unique ID)
            String subject = jwt.getSubject();
            if (subject != null && !subject.isBlank()) {
                return Optional.of(subject);
            }
        }
        
        // Fallback to principal's toString if not a JWT
        return Optional.ofNullable(principal)
                .map(Object::toString)
                .filter(s -> !s.isBlank() && !"anonymousUser".equals(s))
                .map(Optional::of)
                .orElse(Optional.of("system"));
    }
}
