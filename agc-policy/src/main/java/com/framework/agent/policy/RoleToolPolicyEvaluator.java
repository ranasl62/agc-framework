package com.framework.agent.policy;

import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.core.ToolInvocationContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Static role-to-tools map from configuration.
 */
public class RoleToolPolicyEvaluator implements PolicyEvaluator {

    private final Map<String, List<String>> roleToTools;

    public RoleToolPolicyEvaluator(Map<String, List<String>> roleToTools) {
        Map<String, List<String>> norm = new LinkedHashMap<>();
        roleToTools.forEach((k, v) -> norm.put(k.toLowerCase(Locale.ROOT), v));
        this.roleToTools = Map.copyOf(norm);
    }

    @Override
    public GovernanceDecision evaluate(ToolInvocationContext ctx) {
        if (ctx.roles().isEmpty()) {
            return GovernanceDecision.deny(
                    "POLICY_NO_ROLES",
                    List.of("policy"),
                    Map.of("message", "No roles on context")
            );
        }
        String tool = ctx.toolName();
        for (String role : ctx.roles()) {
            List<String> allowed = roleToTools.get(role.toLowerCase(Locale.ROOT));
            if (allowed == null) {
                continue;
            }
            if (allowed.contains("*")) {
                return GovernanceDecision.allow();
            }
            if (allowed.stream().anyMatch(t -> t.equalsIgnoreCase(tool))) {
                return GovernanceDecision.allow();
            }
        }
        return GovernanceDecision.deny(
                "POLICY_TOOL_FORBIDDEN",
                List.of("policy"),
                Map.of("tool", tool, "roles", String.join(",", ctx.roles()))
        );
    }
}
