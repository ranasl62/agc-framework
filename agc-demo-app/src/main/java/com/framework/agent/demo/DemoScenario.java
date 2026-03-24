package com.framework.agent.demo;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Built-in scenarios exercising policy, guardrails (DENY/WARN), and successful tool invocation.
 */
public enum DemoScenario {
    ALLOW_SEARCH(
            "allow_search",
            "Role `user` calls allowed tool `search` — expect success and full audit trail.",
            "demo-user",
            Set.of("user"),
            "Hello, please use [[tool:search]]",
            "search"
    ),
    ALLOW_READ_WITH_WARN(
            "allow_read_with_warn",
            "Role `user` calls `read` — guardrail emits WARN; tool still runs.",
            "demo-user",
            Set.of("user"),
            "Summarize doc via [[tool:read]]",
            "read"
    ),
    POLICY_DENY_FORBIDDEN_TOOL(
            "policy_deny_forbidden_tool",
            "Role `user` plans `delete_db` — policy DENY (tool not in role allowlist).",
            "demo-user",
            Set.of("user"),
            "Maintenance [[tool:delete_db]]",
            "delete_db"
    ),
    POLICY_DENY_NO_ROLES(
            "policy_deny_no_roles",
            "Empty roles — policy DENY (`POLICY_NO_ROLES`).",
            "anonymous",
            Set.of(),
            "[[tool:search]]",
            "search"
    ),
    GUARDRAIL_DENY_PAYMENT(
            "guardrail_deny_payment",
            "Role `admin` (`*` tools) plans `payment_api` — guardrail DENY blocks MCP.",
            "demo-admin",
            Set.of("admin"),
            "Pay invoice [[tool:payment_api]]",
            "payment_api"
    );

    private final String id;
    private final String description;
    private final String principalId;
    private final Set<String> roles;
    private final String userMessage;
    private final String plannedTool;

    DemoScenario(
            String id,
            String description,
            String principalId,
            Set<String> roles,
            String userMessage,
            String plannedTool
    ) {
        this.id = id;
        this.description = description;
        this.principalId = principalId;
        this.roles = roles;
        this.userMessage = userMessage;
        this.plannedTool = plannedTool;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String principalId() {
        return principalId;
    }

    public Set<String> roles() {
        return roles;
    }

    public String userMessage() {
        return userMessage;
    }

    public String plannedTool() {
        return plannedTool;
    }

    public static DemoScenario fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("scenario id required");
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        for (DemoScenario s : values()) {
            if (s.id.equals(key)) {
                return s;
            }
        }
        throw new IllegalArgumentException("unknown scenario: " + raw);
    }

    public static List<DemoScenarioInfo> catalog() {
        return java.util.Arrays.stream(values())
                .map(s -> new DemoScenarioInfo(s.id, s.description))
                .toList();
    }

    public record DemoScenarioInfo(String id, String description) {
    }
}
