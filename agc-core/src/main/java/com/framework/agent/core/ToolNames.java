package com.framework.agent.core;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tool identity for governance: optional {@code :vN} version suffix (e.g. {@code search:v1}).
 * Policy, registry, and guardrails compare {@linkplain #logicalName(String) logical names} for backward-compatible upgrades.
 */
public final class ToolNames {

    private static final Pattern VERSION_SUFFIX = Pattern.compile("^(.+):v([0-9]+)$", Pattern.CASE_INSENSITIVE);

    private ToolNames() {
    }

    /**
     * Returns the canonical tool id without a {@code :vN} version suffix, lowercased.
     * Non-versioned names are lowercased as-is (colons that are not {@code :v\d+} are kept in the string).
     */
    public static String logicalName(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            return "";
        }
        String t = toolName.trim();
        var m = VERSION_SUFFIX.matcher(t);
        if (m.matches()) {
            return m.group(1).toLowerCase(Locale.ROOT);
        }
        return t.toLowerCase(Locale.ROOT);
    }
}
