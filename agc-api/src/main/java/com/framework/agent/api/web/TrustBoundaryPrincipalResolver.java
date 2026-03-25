package com.framework.agent.api.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Derives {@code principalId} from Spring Security when an authenticated principal exists;
 * otherwise falls back to the untrusted request value (e.g. demo / headless use).
 */
public final class TrustBoundaryPrincipalResolver {

    private TrustBoundaryPrincipalResolver() {
    }

    /** True when a non-anonymous authenticated principal is present. */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * @param untrustedPrincipalId value from the HTTP request body (never trusted alone in production)
     */
    public static String resolvePrincipalId(String untrustedPrincipalId) {
        if (isAuthenticated()) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return untrustedPrincipalId != null ? untrustedPrincipalId : "";
    }

    /**
     * Derives roles from trusted authorities when authenticated; otherwise falls back to request roles.
     */
    public static Set<String> resolveRoles(List<String> untrustedRoles) {
        if (isAuthenticated()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Set<String> trusted = new LinkedHashSet<>();
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if (authority == null || authority.getAuthority() == null) {
                    continue;
                }
                String role = authority.getAuthority().trim();
                if (role.isEmpty()) {
                    continue;
                }
                if (role.startsWith("ROLE_") && role.length() > 5) {
                    role = role.substring(5);
                }
                trusted.add(role.toLowerCase());
            }
            return Set.copyOf(trusted);
        }
        if (untrustedRoles == null || untrustedRoles.isEmpty()) {
            return Set.of();
        }
        Set<String> fallback = new LinkedHashSet<>();
        for (String role : untrustedRoles) {
            if (role != null && !role.isBlank()) {
                fallback.add(role.trim());
            }
        }
        return Set.copyOf(fallback);
    }
}
