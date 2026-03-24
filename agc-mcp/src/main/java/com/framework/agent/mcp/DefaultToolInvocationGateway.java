package com.framework.agent.mcp;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.core.AuditEvent;
import com.framework.agent.core.AuditEventType;
import com.framework.agent.core.AuditPersistenceException;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.DecisionType;
import com.framework.agent.core.GovernedPathAuditException;
import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.core.ToolInvocationResult;
import com.framework.agent.mcp.internal.McpToolExecutor;

import java.time.Duration;
import java.time.Instant;

public class DefaultToolInvocationGateway implements ToolInvocationGateway {

    private final GovernancePipeline pipeline;
    private final McpToolExecutor executor;
    private final AuditRecorder auditRecorder;
    private final AgcAuditProperties auditProperties;

    public DefaultToolInvocationGateway(
            GovernancePipeline pipeline,
            McpToolExecutor executor,
            AuditRecorder auditRecorder,
            AgcAuditProperties auditProperties
    ) {
        this.pipeline = pipeline;
        this.executor = executor;
        this.auditRecorder = auditRecorder;
        this.auditProperties = auditProperties;
    }

    @Override
    public ToolInvocationResult invoke(ToolInvocationContext ctx)
            throws ToolInvocationDeniedException, ToolExecutionException {
        GatewayInvocationConstraints.validate(ctx);

        GovernanceDecision decision = pipeline.evaluatePreInvocation(ctx);
        if (decision == null) {
            throw new IllegalStateException("Governance pipeline returned null decision");
        }

        recordGovernanceDecisionOrThrow(ctx, decision);

        if (decision.type() == DecisionType.DENY) {
            throw new ToolInvocationDeniedException(decision);
        }

        recordToolRequestOrThrow(ctx);

        Instant start = Instant.now();
        try {
            ToolInvocationResult result = executor.execute(ctx);
            Duration latency = Duration.between(start, Instant.now());
            recordToolResponseOrThrow(ctx, decision, result.outcomeSummary());
            return new ToolInvocationResult(result.success(), result.outcomeSummary(), latency);
        } catch (ToolExecutionException ex) {
            recordSystemErrorAfterToolFailureOrThrow(ctx, decision, ex);
            throw ex;
        }
    }

    private void recordGovernanceDecisionOrThrow(ToolInvocationContext ctx, GovernanceDecision decision) {
        try {
            auditRecorder.record(AuditEvent.pending(
                    ctx.traceId(),
                    AuditEventType.GOVERNANCE_DECISION,
                    decision,
                    ctx.toolName(),
                    summarizeDecision(decision),
                    ""
            ));
        } catch (AuditPersistenceException e) {
            throw new GovernedPathAuditException(
                    "Governance decision could not be audited; failing closed (no tool execution)",
                    e
            );
        }
    }

    private void recordToolRequestOrThrow(ToolInvocationContext ctx) {
        try {
            auditRecorder.record(AuditEvent.pending(
                    ctx.traceId(),
                    AuditEventType.TOOL_INVOCATION_REQUEST,
                    null,
                    ctx.toolName(),
                    "invoke",
                    ""
            ));
        } catch (AuditPersistenceException e) {
            throw new GovernedPathAuditException(
                    "Tool invocation request could not be audited; failing closed",
                    e
            );
        }
    }

    private void recordToolResponseOrThrow(
            ToolInvocationContext ctx,
            GovernanceDecision decision,
            String outcomeSummary
    ) {
        try {
            auditRecorder.record(AuditEvent.pending(
                    ctx.traceId(),
                    AuditEventType.TOOL_INVOCATION_RESPONSE,
                    decision,
                    ctx.toolName(),
                    outcomeSummary,
                    ""
            ));
        } catch (AuditPersistenceException e) {
            throw new GovernedPathAuditException(
                    "Tool invocation response could not be audited; failing closed",
                    e
            );
        }
    }

    private void recordSystemErrorAfterToolFailureOrThrow(
            ToolInvocationContext ctx,
            GovernanceDecision decision,
            ToolExecutionException ex
    ) {
        AuditEvent event = AuditEvent.pending(
                ctx.traceId(),
                AuditEventType.SYSTEM_ERROR,
                decision,
                ctx.toolName(),
                "tool_error:" + ex.getMessage(),
                ""
        );
        if (auditProperties.isStrictSecondaryAudit()) {
            try {
                auditRecorder.record(event);
            } catch (AuditPersistenceException e) {
                GovernedPathAuditException gpe = new GovernedPathAuditException(
                        "SYSTEM_ERROR audit failed while handling tool failure (strict secondary audit)",
                        e
                );
                gpe.addSuppressed(ex);
                throw gpe;
            }
        } else {
            try {
                auditRecorder.record(event);
            } catch (AuditPersistenceException ignored) {
                // Relaxed mode only; not recommended for production governance posture.
            }
        }
    }

    private static String summarizeDecision(GovernanceDecision d) {
        return d.type() + ":" + d.reasonCode() + ":" + String.join(",", d.matchedRuleIds());
    }
}
