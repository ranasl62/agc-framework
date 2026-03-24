package com.framework.agent.mcp;

import com.framework.agent.core.McpToolExecutor;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationResult;

import java.time.Duration;

public class EchoMcpToolExecutor implements McpToolExecutor {

    @Override
    public ToolInvocationResult execute(ToolInvocationContext ctx) throws ToolExecutionException {
        return new ToolInvocationResult(true, "echo:" + ctx.toolName(), Duration.ofMillis(1));
    }
}
