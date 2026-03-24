package com.framework.agent.mcp;

import com.framework.agent.core.ToolNames;
import com.framework.agent.core.ToolRegistry;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Allowlist implementation: empty configured list means no registry filter.
 */
public final class DefaultToolRegistry implements ToolRegistry {

    private final Set<String> allowedNormalized;

    public DefaultToolRegistry(AgcRuntimeProperties properties) {
        var list = properties.getTools().getAllowed();
        if (list == null || list.isEmpty()) {
            this.allowedNormalized = Set.of();
        } else {
            this.allowedNormalized = list.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(ToolNames::logicalName)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    @Override
    public boolean isAllowed(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            return false;
        }
        if (allowedNormalized.isEmpty()) {
            return true;
        }
        return allowedNormalized.contains(ToolNames.logicalName(toolName));
    }

    public boolean isRegistryActive() {
        return !allowedNormalized.isEmpty();
    }
}
