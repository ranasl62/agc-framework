package com.framework.agent.mcp;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.audit.AuditMode;
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
import com.framework.agent.core.ToolRegistry;
import com.framework.agent.mcp.internal.McpToolExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Enforced flow: {@code Gateway → ToolRegistry → Policy/Guardrails → Executor}.
 */
public class DefaultToolInvocationGateway implements ToolInvocationGateway {

    private static final Logger log = LoggerFactory.getLogger(DefaultToolInvocationGateway.class);

    private final GovernancePipeline pipeline;
    private final McpToolExecutor executor;
    private final AuditRecorder auditRecorder;
    private final AgcAuditProperties auditProperties;
    private final AgcRuntimeProperties runtimeProperties;
    private final ToolRegistry toolRegistry;
    private final Executor auditAsyncExecutor;
    private final MeterRegistry meterRegistry;

    public DefaultToolInvocationGateway(
            GovernancePipeline pipeline,
            McpToolExecutor executor,
            AuditRecorder auditRecorder,
            AgcAuditProperties auditProperties,
            AgcRuntimeProperties runtimeProperties,
            ToolRegistry toolRegistry,
            Executor auditAsyncExecutor,
            MeterRegistry meterRegistry
    ) {
        this.pipeline = pipeline;
        this.executor = executor;
        this.auditRecorder = auditRecorder;
        this.auditProperties = auditProperties;
        this.runtimeProperties = runtimeProperties;
        this.toolRegistry = toolRegistry;
        this.auditAsyncExecutor = auditAsyncExecutor;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ToolInvocationResult invoke(ToolInvocationContext ctx)
            throws ToolInvocationDeniedException, ToolExecutionException {
        GatewayInvocationConstraints.validate(ctx);
        GatewayContextHolder.enterGateway();
        putMdc(ctx);
        Instant invokeStart = Instant.now();
        String metricOutcome = "error";
        try {
            if (!runtimeProperties.isEnabled()) {
                MDC.put("decision", DecisionType.DENY.name());
                MDC.put("reasonCode", "AGC_DISABLED");
                GovernanceDecision kill = GovernanceDecision.deny(
                        "AGC_DISABLED",
                        List.of("agc"),
                        Map.of("message", "AGC disabled (agc.enabled=false)")
                );
                log.warn("agc.decision=DENY reason=AGC_DISABLED traceId={} tool={}", ctx.traceId(), ctx.toolName());
                persistGovernedAudit(ctx, kill, AuditEventType.GOVERNANCE_DECISION, summarizeDecision(kill));
                metricOutcome = "denied";
                throw new ToolInvocationDeniedException(kill);
            }

            if (!toolRegistry.isAllowed(ctx.toolName())) {
                GovernanceDecision regDeny = GovernanceDecision.deny(
                        "TOOL_NOT_REGISTERED",
                        List.of("registry"),
                        Map.of("tool", ctx.toolName())
                );
                MDC.put("decision", DecisionType.DENY.name());
                MDC.put("reasonCode", "TOOL_NOT_REGISTERED");
                persistGovernedAudit(ctx, regDeny, AuditEventType.GOVERNANCE_DECISION, summarizeDecision(regDeny));
                log.info("agc.decision=DENY reasonCode=TOOL_NOT_REGISTERED traceId={} tool={}",
                        ctx.traceId(), ctx.toolName());
                metricOutcome = "denied";
                throw new ToolInvocationDeniedException(regDeny);
            }

            GovernanceDecision decision = pipeline.evaluatePreInvocation(ctx);
            if (decision == null) {
                throw new IllegalStateException("Governance pipeline returned null decision");
            }

            persistGovernedAudit(ctx, decision, AuditEventType.GOVERNANCE_DECISION, summarizeDecision(decision));
            MDC.put("decision", decision.type().name());
            MDC.put("reasonCode", decision.reasonCode());
            log.info("agc.decision={} reasonCode={} traceId={} correlationId={} tool={}",
                    decision.type(), decision.reasonCode(), ctx.traceId(), ctx.correlationId(), ctx.toolName());

            if (decision.type() == DecisionType.DENY) {
                metricOutcome = "denied";
                throw new ToolInvocationDeniedException(decision);
            }

            persistToolRequest(ctx);

            Instant execStart = Instant.now();
            try {
                ToolInvocationResult result = executor.execute(ctx);
                Duration latency = Duration.between(execStart, Instant.now());
                persistToolResponse(ctx, decision, result.outcomeSummary());
                metricOutcome = result.success() ? "success" : "tool_failed";
                return new ToolInvocationResult(result.success(), result.outcomeSummary(), latency);
            } catch (ToolExecutionException ex) {
                recordSystemErrorAfterToolFailure(ctx, decision, ex);
                metricOutcome = "tool_error";
                throw ex;
            }
        } catch (GovernedPathAuditException e) {
            metricOutcome = "audit_failure";
            throw e;
        } finally {
            recordLatencyAndMetrics(invokeStart, metricOutcome);
            clearMdc();
            GatewayContextHolder.exitGateway();
        }
    }

    private void recordLatencyAndMetrics(Instant invokeStart, String outcome) {
        long ms = Duration.between(invokeStart, Instant.now()).toMillis();
        MDC.put("executionTimeMs", Long.toString(ms));
        if (meterRegistry != null) {
            meterRegistry.timer("agc.gateway.invoke.latency", "outcome", outcome).record(Duration.ofMillis(ms));
            meterRegistry.counter("agc.gateway.invoke.outcomes", "outcome", outcome).increment();
        }
    }

    private void putMdc(ToolInvocationContext ctx) {
        MDC.put("traceId", ctx.traceId());
        MDC.put("correlationId", ctx.correlationId());
        MDC.put("principalId", ctx.principalId());
        MDC.put("toolName", ctx.toolName());
    }

    private void clearMdc() {
        MDC.remove("traceId");
        MDC.remove("correlationId");
        MDC.remove("principalId");
        MDC.remove("toolName");
        MDC.remove("decision");
        MDC.remove("reasonCode");
        MDC.remove("executionTimeMs");
    }

    private void persistGovernedAudit(
            ToolInvocationContext ctx,
            GovernanceDecision decision,
            AuditEventType type,
            String summary
    ) {
        AuditEvent event = AuditEvent.pending(
                ctx.traceId(),
                type,
                decision,
                ctx.toolName(),
                summary,
                ""
        );
        persistGoverned(event, "GOVERNANCE_DECISION");
    }

    private void persistToolRequest(ToolInvocationContext ctx) {
        AuditEvent event = AuditEvent.pending(
                ctx.traceId(),
                AuditEventType.TOOL_INVOCATION_REQUEST,
                null,
                ctx.toolName(),
                "invoke",
                ""
        );
        persistGoverned(event, "TOOL_INVOCATION_REQUEST");
    }

    private void persistToolResponse(
            ToolInvocationContext ctx,
            GovernanceDecision decision,
            String outcomeSummary
    ) {
        AuditEvent event = AuditEvent.pending(
                ctx.traceId(),
                AuditEventType.TOOL_INVOCATION_RESPONSE,
                decision,
                ctx.toolName(),
                outcomeSummary,
                ""
        );
        persistGoverned(event, "TOOL_INVOCATION_RESPONSE");
    }

    private void persistGoverned(AuditEvent event, String phase) {
        AuditMode mode = auditProperties.getMode();
        if (mode == AuditMode.ASYNC) {
            if (auditAsyncExecutor == null) {
                throw new IllegalStateException("agc.audit.mode=ASYNC requires agcAuditAsyncExecutor bean");
            }
            auditAsyncExecutor.execute(() -> {
                try {
                    auditRecorder.record(event);
                } catch (AuditPersistenceException e) {
                    log.error("agc.audit mode=ASYNC: persist failed phase={} traceId={}", phase, event.traceId(), e);
                }
            });
            return;
        }
        if (mode == AuditMode.BEST_EFFORT) {
            try {
                auditRecorder.record(event);
            } catch (AuditPersistenceException e) {
                log.warn("agc.audit mode=BEST_EFFORT: persist failed phase={} traceId={}", phase, event.traceId(), e);
            }
            return;
        }
        try {
            auditRecorder.record(event);
        } catch (AuditPersistenceException e) {
            throw new GovernedPathAuditException(
                    "Governed audit write failed phase=" + phase + "; failing closed",
                    e
            );
        }
    }

    private void recordSystemErrorAfterToolFailure(
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
        boolean strictSecondary =
                auditProperties.getMode() == AuditMode.STRICT && auditProperties.isStrictSecondaryAudit();
        if (strictSecondary) {
            try {
                auditRecorder.record(event);
            } catch (AuditPersistenceException e) {
                GovernedPathAuditException gpe = new GovernedPathAuditException(
                        "SYSTEM_ERROR audit failed while handling tool failure",
                        e
                );
                gpe.addSuppressed(ex);
                throw gpe;
            }
            return;
        }
        if (auditProperties.getMode() == AuditMode.ASYNC && auditAsyncExecutor != null) {
            auditAsyncExecutor.execute(() -> {
                try {
                    auditRecorder.record(event);
                } catch (AuditPersistenceException e) {
                    log.warn("agc.audit mode=ASYNC: SYSTEM_ERROR persist failed traceId={}", ctx.traceId(), e);
                }
            });
            return;
        }
        try {
            auditRecorder.record(event);
        } catch (AuditPersistenceException e) {
            log.warn("agc.audit: secondary SYSTEM_ERROR persist failed traceId={}", ctx.traceId(), e);
        }
    }

    private static String summarizeDecision(GovernanceDecision d) {
        return d.type() + ":" + d.reasonCode() + ":" + String.join(",", d.matchedRuleIds());
    }
}
