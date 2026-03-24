package com.framework.agent.core;

@FunctionalInterface
public interface ToolInvocationGateway {

    ToolInvocationResult invoke(ToolInvocationContext ctx)
            throws ToolInvocationDeniedException, ToolExecutionException;
}
