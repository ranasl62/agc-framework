package com.framework.agent.core;

/**
 * Allowlist of tool names that may be executed after policy/guardrails pass.
 * When the configured list is empty, all non-blank names are accepted (policy remains authoritative).
 */
@FunctionalInterface
public interface ToolRegistry {

    boolean isAllowed(String toolName);
}
