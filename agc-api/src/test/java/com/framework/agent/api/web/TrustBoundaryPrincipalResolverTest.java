package com.framework.agent.api.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustBoundaryPrincipalResolverTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedPrincipalAndRolesComeFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice",
                        "n/a",
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ops")
                )
        );

        assertTrue(TrustBoundaryPrincipalResolver.isAuthenticated());
        assertEquals("alice", TrustBoundaryPrincipalResolver.resolvePrincipalId("body-user"));
        assertEquals(java.util.Set.of("admin", "ops"),
                TrustBoundaryPrincipalResolver.resolveRoles(List.of("user")));
    }

    @Test
    void unauthenticatedFallsBackToRequestIdentity() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("k", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
        );

        assertFalse(TrustBoundaryPrincipalResolver.isAuthenticated());
        assertEquals("body-user", TrustBoundaryPrincipalResolver.resolvePrincipalId("body-user"));
        assertEquals(java.util.Set.of("user"),
                TrustBoundaryPrincipalResolver.resolveRoles(List.of("user")));
    }
}
