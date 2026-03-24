package com.framework.agent.mcp.internal;

import com.framework.agent.mcp.GatewayContextHolder;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationResult;

import java.time.Duration;

public final class EchoMcpToolExecutor implements McpToolExecutor {

    @Override
    public ToolInvocationResult execute(ToolInvocationContext ctx) throws ToolExecutionException {
        if (!GatewayContextHolder.isGatewayCall()) {
            throw new IllegalStateException("Direct execution forbidden");
        }
        return new ToolInvocationResult(true, "echo:" + ctx.toolName(), Duration.ofMillis(1));
    }
}
