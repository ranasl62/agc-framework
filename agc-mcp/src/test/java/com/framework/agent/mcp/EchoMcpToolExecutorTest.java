package com.framework.agent.mcp;

import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.mcp.internal.EchoMcpToolExecutor;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EchoMcpToolExecutorTest {

    @Test
    void directExecutionWithoutGatewayScopeIsForbidden() {
        var ex = assertThrows(IllegalStateException.class, () ->
                new EchoMcpToolExecutor().execute(ctx("search"))
        );
        assertTrue(ex.getMessage().contains("Direct execution forbidden"));
    }

    @Test
    void executionInsideGatewayScopeSucceeds() throws ToolExecutionException {
        var ctx = ctx("search");
        GatewayContextHolder.enterGateway();
        try {
            var r = new EchoMcpToolExecutor().execute(ctx);
            assertTrue(r.success());
            assertEquals("echo:search", r.outcomeSummary());
        } finally {
            GatewayContextHolder.exitGateway();
        }
    }

    private static ToolInvocationContext ctx(String tool) {
        return new ToolInvocationContext(
                "t",
                "c",
                "",
                "",
                Set.of("user"),
                tool,
                Map.of(),
                null
        );
    }
}
