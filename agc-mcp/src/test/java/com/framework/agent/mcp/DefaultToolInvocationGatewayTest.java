package com.framework.agent.mcp;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.audit.AuditMode;
import com.framework.agent.core.AuditPersistenceException;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernedPathAuditException;
import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolRegistry;
import com.framework.agent.mcp.internal.McpToolExecutor;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultToolInvocationGatewayTest {

    private static ToolInvocationContext ctx(String tool) {
        return new ToolInvocationContext(
                "trace-1",
                "corr-1",
                "",
                "p1",
                Set.of("user"),
                tool,
                Map.of(),
                null
        );
    }

    private static DefaultToolInvocationGateway gateway(
            GovernancePipeline pipeline,
            McpToolExecutor executor,
            AuditRecorder recorder,
            AuditMode auditMode,
            AgcRuntimeProperties runtime,
            ToolRegistry registry
    ) {
        AgcAuditProperties audit = new AgcAuditProperties();
        audit.setMode(auditMode);
        return new DefaultToolInvocationGateway(
                pipeline, executor, recorder, audit, runtime, registry, null, null
        );
    }

    @Test
    void killSwitchDeniesWithoutCallingExecutor() throws Exception {
        GovernancePipeline pipeline = mock(GovernancePipeline.class);
        McpToolExecutor executor = mock(McpToolExecutor.class);
        AuditRecorder recorder = mock(AuditRecorder.class);
        AgcRuntimeProperties runtime = new AgcRuntimeProperties();
        runtime.setEnabled(false);
        var g = gateway(pipeline, executor, recorder, AuditMode.STRICT, runtime, t -> true);
        ToolInvocationDeniedException ex = assertThrows(
                ToolInvocationDeniedException.class,
                () -> g.invoke(ctx("search"))
        );
        assertEquals("AGC_DISABLED", ex.getDecision().reasonCode());
        verify(executor, never()).execute(any());
        verify(pipeline, never()).evaluatePreInvocation(any());
    }

    @Test
    void registryDeniesWhenToolNotAllowlisted() throws Exception {
        GovernancePipeline pipeline = mock(GovernancePipeline.class);
        when(pipeline.evaluatePreInvocation(any())).thenReturn(GovernanceDecision.allow());
        McpToolExecutor executor = mock(McpToolExecutor.class);
        AuditRecorder recorder = mock(AuditRecorder.class);
        AgcRuntimeProperties runtime = new AgcRuntimeProperties();
        runtime.getTools().setAllowed(java.util.List.of("search"));
        var g = gateway(pipeline, executor, recorder, AuditMode.STRICT, runtime, new DefaultToolRegistry(runtime));
        ToolInvocationDeniedException ex = assertThrows(
                ToolInvocationDeniedException.class,
                () -> g.invoke(ctx("other"))
        );
        assertEquals("TOOL_NOT_REGISTERED", ex.getDecision().reasonCode());
        verify(executor, never()).execute(any());
        verify(pipeline, never()).evaluatePreInvocation(any());
    }

    @Test
    void strictAuditFailureOnGovernedPathFailsClosed() throws Exception {
        GovernancePipeline pipeline = mock(GovernancePipeline.class);
        when(pipeline.evaluatePreInvocation(any())).thenReturn(GovernanceDecision.allow());
        McpToolExecutor executor = mock(McpToolExecutor.class);
        AuditRecorder recorder = mock(AuditRecorder.class);
        doThrow(new AuditPersistenceException("x", null)).when(recorder).record(any());
        AgcRuntimeProperties runtime = new AgcRuntimeProperties();
        var g = gateway(pipeline, executor, recorder, AuditMode.STRICT, runtime, t -> true);
        assertThrows(GovernedPathAuditException.class, () -> g.invoke(ctx("search")));
        verify(executor, never()).execute(any());
    }

    @Test
    void bestEffortSwallowsAuditPersistErrors() throws Exception {
        GovernancePipeline pipeline = mock(GovernancePipeline.class);
        when(pipeline.evaluatePreInvocation(any())).thenReturn(GovernanceDecision.allow());
        McpToolExecutor executor = mock(McpToolExecutor.class);
        when(executor.execute(any())).thenReturn(new com.framework.agent.core.ToolInvocationResult(true, "ok", Duration.ZERO));
        AuditRecorder recorder = mock(AuditRecorder.class);
        doThrow(new AuditPersistenceException("x", null)).when(recorder).record(any());
        AgcRuntimeProperties runtime = new AgcRuntimeProperties();
        var g = gateway(pipeline, executor, recorder, AuditMode.BEST_EFFORT, runtime, t -> true);
        var r = g.invoke(ctx("search"));
        assertEquals("ok", r.outcomeSummary());
        verify(executor).execute(any());
    }
}
