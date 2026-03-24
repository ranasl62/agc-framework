package com.framework.agent.mcp;

import com.framework.agent.core.InvalidGovernanceContextException;
import com.framework.agent.core.ToolInvocationContext;

final class GatewayInvocationConstraints {

    private GatewayInvocationConstraints() {
    }

    static void validate(ToolInvocationContext ctx) {
        if (ctx == null) {
            throw new InvalidGovernanceContextException("ToolInvocationContext is required");
        }
        if (ctx.traceId() == null || ctx.traceId().isBlank()) {
            throw new InvalidGovernanceContextException("traceId is required for governed tool invocation");
        }
        if (ctx.correlationId() == null || ctx.correlationId().isBlank()) {
            throw new InvalidGovernanceContextException("correlationId is required for governed tool invocation");
        }
        if (ctx.toolName() == null || ctx.toolName().isBlank()) {
            throw new InvalidGovernanceContextException("toolName is required before gateway invocation");
        }
    }
}
