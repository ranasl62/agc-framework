package com.framework.agent.api.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
}
