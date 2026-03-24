package com.framework.agent.core;

@FunctionalInterface
public interface McpToolExecutor {

    ToolInvocationResult execute(ToolInvocationContext ctx) throws ToolExecutionException;
}
