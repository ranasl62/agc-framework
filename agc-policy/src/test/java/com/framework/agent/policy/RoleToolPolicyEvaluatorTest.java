package com.framework.agent.policy;

import com.framework.agent.core.DecisionType;
import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.ToolInvocationContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleToolPolicyEvaluatorTest {

    private static ToolInvocationContext ctx(Set<String> roles, String tool) {
        return new ToolInvocationContext("t", "c", "", "", roles, tool, Map.of(), null);
    }

    @Test
    void allowWhenRoleMatchesTool() {
        var p = new RoleToolPolicyEvaluator(Map.of("user", List.of("search", "read")));
        GovernanceDecision d = p.evaluate(ctx(Set.of("user"), "search"));
        assertEquals(DecisionType.ALLOW, d.type());
    }

    @Test
    void denyWhenToolNotInRoleAllowlist() {
        var p = new RoleToolPolicyEvaluator(Map.of("user", List.of("search")));
        GovernanceDecision d = p.evaluate(ctx(Set.of("user"), "delete_db"));
        assertEquals(DecisionType.DENY, d.type());
        assertEquals("POLICY_TOOL_FORBIDDEN", d.reasonCode());
    }

    @Test
    void denyWhenNoRoles() {
        var p = new RoleToolPolicyEvaluator(Map.of("user", List.of("search")));
        GovernanceDecision d = p.evaluate(ctx(Set.of(), "search"));
        assertEquals(DecisionType.DENY, d.type());
        assertEquals("POLICY_NO_ROLES", d.reasonCode());
    }

    @Test
    void starRoleAllowsAnyTool() {
        var p = new RoleToolPolicyEvaluator(Map.of("admin", List.of("*")));
        GovernanceDecision d = p.evaluate(ctx(Set.of("admin"), "anything"));
        assertEquals(DecisionType.ALLOW, d.type());
    }

    @Test
    void versionedToolMatchesConfiguredLogicalName() {
        var p = new RoleToolPolicyEvaluator(Map.of("user", List.of("search")));
        GovernanceDecision d = p.evaluate(ctx(Set.of("user"), "search:v2"));
        assertEquals(DecisionType.ALLOW, d.type());
    }
}
