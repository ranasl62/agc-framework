package com.framework.agent.core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record ToolInvocationContext(
        String traceId,
        String correlationId,
        String tenantId,
        String principalId,
        Set<String> roles,
        String toolName,
        Map<String, Object> arguments,
        Instant deadline
) {
    public ToolInvocationContext {
        Objects.requireNonNull(traceId, "traceId");
        Objects.requireNonNull(toolName, "toolName");
        correlationId = correlationId != null ? correlationId : traceId;
        tenantId = tenantId != null ? tenantId : "";
        principalId = principalId != null ? principalId : "";
        roles = roles != null ? Set.copyOf(roles) : Set.of();
        arguments = arguments != null ? Map.copyOf(arguments) : Map.of();
    }

    public boolean isPastDeadline() {
        return deadline != null && Instant.now().isAfter(deadline);
    }

    public static ToolInvocationContext minimal(String traceId, String toolName, Set<String> roles) {
        return new ToolInvocationContext(traceId, traceId, "", "", roles, toolName, Map.of(), null);
    }
}
