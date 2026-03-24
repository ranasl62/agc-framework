package com.framework.agent.mcp;

import com.framework.agent.core.AuditEvent;
import com.framework.agent.core.AuditEventType;
import com.framework.agent.core.AuditPersistenceException;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.McpToolExecutor;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.core.ToolInvocationResult;

import java.time.Duration;
import java.time.Instant;

public class DefaultToolInvocationGateway implements ToolInvocationGateway {

    private final GovernancePipeline pipeline;
    private final McpToolExecutor executor;
    private final AuditRecorder auditRecorder;

    public DefaultToolInvocationGateway(
            GovernancePipeline pipeline,
            McpToolExecutor executor,
            AuditRecorder auditRecorder
    ) {
        this.pipeline = pipeline;
        this.executor = executor;
        this.auditRecorder = auditRecorder;
    }

    @Override
    public ToolInvocationResult invoke(ToolInvocationContext ctx)
            throws ToolInvocationDeniedException, ToolExecutionException {
        GovernanceDecision decision = pipeline.evaluatePreInvocation(ctx);
        boolean governanceAuditOk = persistAudit(AuditEvent.pending(
                ctx.traceId(),
                AuditEventType.GOVERNANCE_DECISION,
                decision,
                ctx.toolName(),
                summarizeDecision(decision),
                ""
        ));
        if (decision.blocksExecution()) {
            throw new ToolInvocationDeniedException(decision);
        }
        if (!governanceAuditOk) {
            throw new IllegalStateException("Audit persistence failed before tool invocation; failing closed");
        }
        persistAuditOrThrow(AuditEvent.pending(
                ctx.traceId(),
                AuditEventType.TOOL_INVOCATION_REQUEST,
                null,
                ctx.toolName(),
                "invoke",
                ""
        ));
        Instant start = Instant.now();
        try {
            ToolInvocationResult result = executor.execute(ctx);
            Duration latency = Duration.between(start, Instant.now());
            persistAuditOrThrow(AuditEvent.pending(
                    ctx.traceId(),
                    AuditEventType.TOOL_INVOCATION_RESPONSE,
                    decision,
                    ctx.toolName(),
                    result.outcomeSummary(),
                    ""
            ));
            return new ToolInvocationResult(result.success(), result.outcomeSummary(), latency);
        } catch (ToolExecutionException ex) {
            persistAuditQuietly(AuditEvent.pending(
                    ctx.traceId(),
                    AuditEventType.SYSTEM_ERROR,
                    decision,
                    ctx.toolName(),
                    "tool_error:" + ex.getMessage(),
                    ""
            ));
            throw ex;
        }
    }

    private boolean persistAudit(AuditEvent event) {
        try {
            auditRecorder.record(event);
            return true;
        } catch (AuditPersistenceException e) {
            return false;
        }
    }

    private void persistAuditOrThrow(AuditEvent event) {
        try {
            auditRecorder.record(event);
        } catch (AuditPersistenceException e) {
            throw new IllegalStateException("Audit persistence failed on governed path", e);
        }
    }

    private void persistAuditQuietly(AuditEvent event) {
        try {
            auditRecorder.record(event);
        } catch (AuditPersistenceException ignored) {
            // Metrics recorded by JpaAuditRecorder; preserve primary ToolExecutionException.
        }
    }

    private static String summarizeDecision(GovernanceDecision d) {
        return d.type() + ":" + d.reasonCode() + ":" + String.join(",", d.matchedRuleIds());
    }
}
