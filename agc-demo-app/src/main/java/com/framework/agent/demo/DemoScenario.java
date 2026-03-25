package com.framework.agent.demo;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Built-in scenarios exercising registry, policy, guardrails (DENY/WARN), and successful tool invocation.
 */
public enum DemoScenario {
    ALLOW_SEARCH(
            DemoScenarioGroup.SUCCESS,
            "allow_search",
            "Role `user` calls allowed tool `search` — expect success and full audit trail.",
            "demo-user",
            Set.of("user"),
            "Hello, please use [[tool:search]]",
            "search"
    ),
    ALLOW_SEARCH_VERSIONED(
            DemoScenarioGroup.SUCCESS,
            "allow_search_versioned",
            "Planned tool `search:v2` — registry uses logical name `search`; demo backend returns `version=v2`.",
            "demo-user",
            Set.of("user"),
            "Versioned search (override)",
            "search:v2"
    ),
    ALLOW_READ_WITH_WARN(
            DemoScenarioGroup.SUCCESS,
            "allow_read_with_warn",
            "Role `user` calls `read` — guardrail emits WARN; tool still runs.",
            "demo-user",
            Set.of("user"),
            "Summarize doc via [[tool:read]]",
            "read"
    ),
    UNKNOWN_TOOL_NOT_REGISTERED(
            DemoScenarioGroup.REGISTRY,
            "unknown_tool_not_registered",
            "Plans `exfil` — not in `agc.tools.allowed` → DENY `TOOL_NOT_REGISTERED` before policy.",
            "demo-user",
            Set.of("user"),
            "Run [[tool:exfil]]",
            "exfil"
    ),
    POLICY_DENY_FORBIDDEN_TOOL(
            DemoScenarioGroup.POLICY,
            "policy_deny_forbidden_tool",
            "Role `user` plans `delete_db` — policy DENY (tool not in role allowlist).",
            "demo-user",
            Set.of("user"),
            "Maintenance [[tool:delete_db]]",
            "delete_db"
    ),
    POLICY_DENY_NO_ROLES(
            DemoScenarioGroup.POLICY,
            "policy_deny_no_roles",
            "Empty roles — policy DENY (`POLICY_NO_ROLES`).",
            "anonymous",
            Set.of(),
            "[[tool:search]]",
            "search"
    ),
    GUARDRAIL_DENY_PAYMENT(
            DemoScenarioGroup.GUARDRAIL,
            "guardrail_deny_payment",
            "Role `admin` (`*` tools) plans `payment_api` — guardrail DENY blocks MCP.",
            "demo-admin",
            Set.of("admin"),
            "Pay invoice [[tool:payment_api]]",
            "payment_api"
    );

    private final DemoScenarioGroup group;
    private final String id;
    private final String description;
    private final String principalId;
    private final Set<String> roles;
    private final String userMessage;
    private final String plannedTool;

    DemoScenario(
            DemoScenarioGroup group,
            String id,
            String description,
            String principalId,
            Set<String> roles,
            String userMessage,
            String plannedTool
    ) {
        this.group = group;
        this.id = id;
        this.description = description;
        this.principalId = principalId;
        this.roles = roles;
        this.userMessage = userMessage;
        this.plannedTool = plannedTool;
    }

    public DemoScenarioGroup group() {
        return group;
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
                .map(s -> new DemoScenarioInfo(s.id, s.description, s.group.name().toLowerCase(Locale.ROOT)))
                .toList();
    }

    public record DemoScenarioInfo(String id, String description, String group) {
    }
}
