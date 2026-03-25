package com.framework.agent.api.web;

import com.framework.agent.core.GovernanceMode;
import com.framework.agent.core.GovernedPathAuditException;
import com.framework.agent.core.InvalidGovernanceContextException;
import com.framework.agent.core.ToolInvocationResult;
import com.framework.agent.orchestrator.AgentOrchestrator;
import com.framework.agent.storage.AuditEventEntity;
import com.framework.agent.storage.AuditEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Locale;
@RestController
public class AgentExecuteController {

    private final AgentOrchestrator orchestrator;
    private final AuditEventRepository auditEventRepository;
    private final GovernanceMode governanceMode;

    public AgentExecuteController(
            AgentOrchestrator orchestrator,
            AuditEventRepository auditEventRepository,
            @Value("${agc.governance.mode:DEVELOPMENT}") String governanceModeRaw
    ) {
        this.orchestrator = orchestrator;
        this.auditEventRepository = auditEventRepository;
        this.governanceMode = parseGovernanceMode(governanceModeRaw);
    }

    private static GovernanceMode parseGovernanceMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return GovernanceMode.DEVELOPMENT;
        }
        try {
            return GovernanceMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return GovernanceMode.DEVELOPMENT;
        }
    }

    @PostMapping("/agent/execute")
    public ResponseEntity<?> execute(@RequestBody ExecuteRequest request) {
        if (request.traceId() == null || request.traceId().isBlank()) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "traceId is required");
            pd.setTitle("Invalid governance context");
            pd.setProperty("decision", "DENY");
            pd.setProperty("reasonCode", "INVALID_CONTEXT");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
        }
        if (request.correlationId() == null || request.correlationId().isBlank()) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "correlationId is required");
            pd.setTitle("Invalid governance context");
            pd.setProperty("decision", "DENY");
            pd.setProperty("reasonCode", "INVALID_CONTEXT");
            pd.setProperty("traceId", request.traceId().trim());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
        }
        String traceId = request.traceId().trim();
        if (governanceMode == GovernanceMode.PRODUCTION && !TrustBoundaryPrincipalResolver.isAuthenticated()) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required when agc.governance.mode=PRODUCTION"
            );
            pd.setTitle("Authentication required");
            pd.setProperty("decision", "DENY");
            pd.setProperty("reasonCode", "AUTH_REQUIRED");
            pd.setProperty("traceId", traceId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
        }
        String principalId = TrustBoundaryPrincipalResolver.resolvePrincipalId(
                request.principalId() != null ? request.principalId() : "anonymous"
        );
        try {
            ToolInvocationResult result = orchestrator.runUserTurn(
                    traceId,
                    request.correlationId().trim(),
                    request.tenantId() != null ? request.tenantId() : "",
                    principalId,
                    TrustBoundaryPrincipalResolver.resolveRoles(request.roles()),
                    request.message() != null ? request.message() : "",
                    null
            );
            return ResponseEntity.ok(new ExecuteResponse(traceId, result.outcomeSummary(), result.success()));
        } catch (com.framework.agent.core.ToolInvocationDeniedException e) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getDecision().reasonCode());
            pd.setTitle("Tool invocation denied");
            pd.setProperty("decision", e.getDecision().type().name());
            pd.setProperty("reasonCode", e.getDecision().reasonCode());
            pd.setProperty("matchedRuleIds", e.getDecision().matchedRuleIds());
            pd.setProperty("traceId", traceId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
        } catch (InvalidGovernanceContextException e) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            pd.setTitle("Invalid governance context");
            pd.setProperty("decision", "DENY");
            pd.setProperty("reasonCode", "INVALID_CONTEXT");
            pd.setProperty("traceId", traceId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
        } catch (GovernedPathAuditException e) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Audit persistence failed on governed path"
            );
            pd.setTitle("Governed path audit failure");
            pd.setProperty("decision", "DENY");
            pd.setProperty("reasonCode", "AUDIT_FAILURE");
            pd.setProperty("traceId", traceId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(pd);
        } catch (Exception e) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            pd.setType(URI.create("about:blank"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
        }
    }

    @GetMapping("/audit/{traceId}")
    public List<AuditEventEntity> audit(@PathVariable String traceId) {
        return auditEventRepository.findByTraceIdOrderBySequenceNumAsc(traceId);
    }

    public record ExecuteRequest(
            String traceId,
            String correlationId,
            String tenantId,
            String principalId,
            List<String> roles,
            String message
    ) {
    }

    public record ExecuteResponse(String traceId, String outcomeSummary, boolean success) {
    }
}
