package com.framework.agent.mcp;

/**
 * Marks the current thread as executing inside {@link com.framework.agent.mcp.DefaultToolInvocationGateway#invoke}.
 * The internal {@link com.framework.agent.mcp.internal.McpToolExecutor} must only run under this scope.
 */
public final class GatewayContextHolder {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private GatewayContextHolder() {
    }

    public static void enterGateway() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void exitGateway() {
        int d = DEPTH.get() - 1;
        if (d <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(d);
        }
    }

    public static boolean isGatewayCall() {
        return DEPTH.get() > 0;
    }
}
