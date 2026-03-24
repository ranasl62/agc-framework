package com.framework.agent.demo;

/**
 * Per-request planned tool for the demo LLM stub (same thread as HTTP request).
 */
public final class DemoToolOverride {

    private static final ThreadLocal<String> PLANNED_TOOL = new ThreadLocal<>();

    private DemoToolOverride() {
    }

    public static void setPlannedTool(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            PLANNED_TOOL.remove();
        } else {
            PLANNED_TOOL.set(toolName.trim());
        }
    }

    public static String getPlannedTool() {
        return PLANNED_TOOL.get();
    }

    public static void clear() {
        PLANNED_TOOL.remove();
    }

    public static Scope scope(String toolName) {
        return new Scope(toolName);
    }

    public static final class Scope implements AutoCloseable {
        private final String previous;

        private Scope(String toolName) {
            this.previous = PLANNED_TOOL.get();
            setPlannedTool(toolName);
        }

        @Override
        public void close() {
            if (previous == null) {
                PLANNED_TOOL.remove();
            } else {
                PLANNED_TOOL.set(previous);
            }
        }
    }
}
