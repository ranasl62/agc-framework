package com.framework.agent.api.web;

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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
public class AgentExecuteController {

    private final AgentOrchestrator orchestrator;
    private final AuditEventRepository auditEventRepository;

    public AgentExecuteController(AgentOrchestrator orchestrator, AuditEventRepository auditEventRepository) {
        this.orchestrator = orchestrator;
        this.auditEventRepository = auditEventRepository;
    }

    @PostMapping("/agent/execute")
    public ResponseEntity<?> execute(@RequestBody ExecuteRequest request) {
        String traceId = request.traceId() != null ? request.traceId() : UUID.randomUUID().toString();
        try {
            ToolInvocationResult result = orchestrator.runUserTurn(
                    traceId,
                    request.correlationId() != null ? request.correlationId() : traceId,
                    request.tenantId() != null ? request.tenantId() : "",
                    request.principalId() != null ? request.principalId() : "anonymous",
                    request.roles() != null ? Set.copyOf(request.roles()) : Set.of(),
                    request.message() != null ? request.message() : "",
                    null
            );
            return ResponseEntity.ok(new ExecuteResponse(traceId, result.outcomeSummary(), result.success()));
        } catch (com.framework.agent.core.ToolInvocationDeniedException e) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getDecision().reasonCode());
            pd.setTitle("Tool invocation denied");
            pd.setProperty("reasonCode", e.getDecision().reasonCode());
            pd.setProperty("matchedRuleIds", e.getDecision().matchedRuleIds());
            pd.setProperty("traceId", traceId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
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
