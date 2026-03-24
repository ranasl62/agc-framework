package com.framework.agent.orchestrator;

import com.framework.agent.core.AuditEvent;
import com.framework.agent.core.AuditEventType;
import com.framework.agent.core.AuditPersistenceException;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.LlmClient;
import com.framework.agent.core.LlmException;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.core.ToolInvocationResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
public class AgentOrchestrator {

    private final LlmClient llmClient;
    private final ToolInvocationGateway toolInvocationGateway;
    private final AuditRecorder auditRecorder;

    public AgentOrchestrator(
            LlmClient llmClient,
            ToolInvocationGateway toolInvocationGateway,
            AuditRecorder auditRecorder
    ) {
        this.llmClient = llmClient;
        this.toolInvocationGateway = toolInvocationGateway;
        this.auditRecorder = auditRecorder;
    }

    public ToolInvocationResult runUserTurn(
            String traceId,
            String correlationId,
            String tenantId,
            String principalId,
            Set<String> roles,
            String userMessage,
            Instant deadline
    ) throws LlmException, ToolInvocationDeniedException, ToolExecutionException, AuditPersistenceException {
        var ctxForLlm = new ToolInvocationContext(
                traceId,
                correlationId,
                tenantId,
                principalId,
                roles,
                "",
                Map.of(),
                deadline
        );
        auditRecorder.record(AuditEvent.pending(
                traceId,
                AuditEventType.REQUEST_RECEIVED,
                null,
                "",
                userMessage != null ? userMessage : "",
                ""
        ));
        String toolName = llmClient.complete(userMessage, ctxForLlm).trim();
        auditRecorder.record(AuditEvent.pending(
                traceId,
                AuditEventType.LLM_INVOCATION,
                null,
                "",
                "plannedTool=" + toolName,
                ""
        ));
        var toolCtx = new ToolInvocationContext(
                traceId,
                correlationId,
                tenantId,
                principalId,
                roles,
                toolName,
                Map.of(),
                deadline
        );
        return toolInvocationGateway.invoke(toolCtx);
    }
}
