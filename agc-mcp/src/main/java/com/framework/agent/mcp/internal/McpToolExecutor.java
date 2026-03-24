package com.framework.agent.mcp.internal;

import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationResult;

/**
 * Internal adapter invoked exclusively by {@link com.framework.agent.mcp.DefaultToolInvocationGateway}.
 * Application code must use {@link com.framework.agent.core.ToolInvocationGateway} only.
 */
@FunctionalInterface
public interface McpToolExecutor {

    ToolInvocationResult execute(ToolInvocationContext ctx) throws ToolExecutionException;
}
