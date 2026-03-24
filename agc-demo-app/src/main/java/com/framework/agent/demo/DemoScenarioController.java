package com.framework.agent.demo;

import com.framework.agent.core.AuditPersistenceException;
import com.framework.agent.core.GovernedPathAuditException;
import com.framework.agent.core.InvalidGovernanceContextException;
import com.framework.agent.core.LlmException;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolInvocationResult;
import com.framework.agent.orchestrator.AgentOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class DemoScenarioController {

    private final AgentOrchestrator orchestrator;

    public DemoScenarioController(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/demo/scenarios")
    public List<DemoScenario.DemoScenarioInfo> scenarios() {
        return DemoScenario.catalog();
    }

    @PostMapping("/demo/run")
    public ResponseEntity<DemoRunResponse> run(@RequestBody DemoRunRequest request) {
        DemoScenario scenario;
        try {
            scenario = DemoScenario.fromId(request.scenario());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DemoRunResponse.error(null, "BAD_REQUEST", e.getMessage()));
        }

        String traceId = request.traceId() != null && !request.traceId().isBlank()
                ? request.traceId().trim()
                : UUID.randomUUID().toString();
        String correlationId = traceId;

        try (DemoToolOverride.Scope ignored = DemoToolOverride.scope(scenario.plannedTool())) {
            ToolInvocationResult result = orchestrator.runUserTurn(
                    traceId,
                    correlationId,
                    "demo-tenant",
                    scenario.principalId(),
                    scenario.roles(),
                    scenario.userMessage(),
                    null
            );
            return ResponseEntity.ok(DemoRunResponse.ok(
                    scenario.id(),
                    traceId,
                    result.outcomeSummary(),
                    result.success()
            ));
        } catch (ToolInvocationDeniedException e) {
            return ResponseEntity.ok(DemoRunResponse.denied(
                    scenario.id(),
                    traceId,
                    e.getDecision().reasonCode(),
                    e.getDecision().matchedRuleIds(),
                    e.getDecision().type().name()
            ));
        } catch (InvalidGovernanceContextException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DemoRunResponse.error(traceId, "INVALID_CONTEXT", e.getMessage()));
        } catch (GovernedPathAuditException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(DemoRunResponse.error(traceId, "AUDIT_FAILURE", e.getMessage()));
        } catch (LlmException | ToolExecutionException | AuditPersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DemoRunResponse.error(traceId, "EXECUTION_ERROR", e.getMessage()));
        } finally {
            DemoToolOverride.clear();
        }
    }

    public record DemoRunRequest(String scenario, String traceId) {
    }

    public record DemoRunResponse(
            String scenario,
            String traceId,
            boolean success,
            String outcomeSummary,
            String denialReasonCode,
            List<String> denialMatchedRuleIds,
            String denialDecisionType,
            String errorCode,
            String errorMessage,
            String auditUrl
    ) {
        static DemoRunResponse ok(String scenario, String traceId, String summary, boolean toolSuccess) {
            return new DemoRunResponse(
                    scenario,
                    traceId,
                    toolSuccess,
                    summary,
                    null,
                    null,
                    null,
                    null,
                    null,
                    auditUrl(traceId)
            );
        }

        static DemoRunResponse denied(
                String scenario,
                String traceId,
                String reasonCode,
                List<String> matchedRuleIds,
                String decisionType
        ) {
            return new DemoRunResponse(
                    scenario,
                    traceId,
                    false,
                    null,
                    reasonCode,
                    matchedRuleIds,
                    decisionType,
                    null,
                    null,
                    auditUrl(traceId)
            );
        }

        static DemoRunResponse error(String traceId, String code, String message) {
            return new DemoRunResponse(
                    null,
                    traceId,
                    false,
                    null,
                    null,
                    null,
                    null,
                    code,
                    message,
                    traceId != null ? auditUrl(traceId) : null
            );
        }

        private static String auditUrl(String traceId) {
            return traceId == null ? null : "/audit/" + traceId;
        }
    }
}
