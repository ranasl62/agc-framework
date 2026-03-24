package com.framework.agent.mcp;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.audit.AuditMode;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolRegistry;
import com.framework.agent.mcp.internal.EchoMcpToolExecutor;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Simulated abuse cases: bypass attempts, unknown tools, policy denial.
 */
class GovernanceAttackSimulationTest {

    private static ToolInvocationContext ctx(String tool) {
        return new ToolInvocationContext(
                "t-attack",
                "c-attack",
                "",
                "attacker",
                Set.of("user"),
                tool,
                Map.of(),
                null
        );
    }

    private static DefaultToolInvocationGateway gateway(
            GovernancePipeline pipeline,
            ToolRegistry registry
    ) {
        AgcAuditProperties audit = new AgcAuditProperties();
        audit.setMode(AuditMode.STRICT);
        AgcRuntimeProperties runtime = new AgcRuntimeProperties();
        return new DefaultToolInvocationGateway(
                pipeline,
                new EchoMcpToolExecutor(),
                mock(AuditRecorder.class),
                audit,
                runtime,
                registry,
                null,
                null
        );
    }

    @Test
    void directMcpExecutorCallIsBlocked() {
        assertTrue(assertThrows(IllegalStateException.class, () ->
                new EchoMcpToolExecutor().execute(ctx("any"))
        ).getMessage().contains("Direct execution forbidden"));
    }

    @Test
    void unknownToolRejectedBeforePolicy() throws Exception {
        GovernancePipeline pipeline = mock(GovernancePipeline.class);
        when(pipeline.evaluatePreInvocation(any())).thenReturn(GovernanceDecision.allow());
        AgcRuntimeProperties runtime = new AgcRuntimeProperties();
        runtime.getTools().setAllowed(java.util.List.of("search"));
        var g = gateway(pipeline, new DefaultToolRegistry(runtime));
        ToolInvocationDeniedException ex = assertThrows(
                ToolInvocationDeniedException.class,
                () -> g.invoke(ctx("malicious_tool"))
        );
        assertEquals("TOOL_NOT_REGISTERED", ex.getDecision().reasonCode());
        verify(pipeline, never()).evaluatePreInvocation(any());
    }

    @Test
    void unauthorizedRoleDeniedByPolicy() throws Exception {
        GovernancePipeline pipeline = mock(GovernancePipeline.class);
        when(pipeline.evaluatePreInvocation(any())).thenReturn(
                GovernanceDecision.deny("POLICY_TOOL_FORBIDDEN", java.util.List.of("policy"), Map.of())
        );
        var g = gateway(pipeline, t -> true);
        ToolInvocationDeniedException ex = assertThrows(
                ToolInvocationDeniedException.class,
                () -> g.invoke(ctx("search"))
        );
        assertEquals("POLICY_TOOL_FORBIDDEN", ex.getDecision().reasonCode());
    }
}
