package com.framework.agent.api.web;

import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.orchestrator.AgentOrchestrator;
import com.framework.agent.storage.AuditEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentExecuteControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void productionUnauthenticatedRequestIsRejectedWith401() throws Exception {
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        AuditEventRepository auditRepo = mock(AuditEventRepository.class);
        AgentExecuteController controller = new AgentExecuteController(orchestrator, auditRepo, "PRODUCTION");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/agent/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traceId":"t-1",
                                  "correlationId":"c-1",
                                  "principalId":"user-from-body",
                                  "roles":["admin"],
                                  "message":"hello"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orchestrator);
    }

    @Test
    void deniedResponseIncludesDecisionAndReasonCode() throws Exception {
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        when(orchestrator.runUserTurn(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ToolInvocationDeniedException(
                        GovernanceDecision.deny("POLICY_TOOL_FORBIDDEN", List.of("policy"), Map.of())
                ));
        AuditEventRepository auditRepo = mock(AuditEventRepository.class);
        AgentExecuteController controller = new AgentExecuteController(orchestrator, auditRepo, "DEVELOPMENT");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/agent/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traceId":"t-2",
                                  "correlationId":"c-2",
                                  "principalId":"user-from-body",
                                  "roles":["user"],
                                  "message":"hello"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.reasonCode").value("POLICY_TOOL_FORBIDDEN"))
                .andExpect(jsonPath("$.decision").value("DENY"));
    }

    @Test
    void authenticatedRolesComeFromSecurityContextNotRequestBody() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice",
                        "n/a",
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                )
        );
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        when(orchestrator.runUserTurn(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new com.framework.agent.core.ToolInvocationResult(true, "ok", java.time.Duration.ZERO));
        AuditEventRepository auditRepo = mock(AuditEventRepository.class);
        AgentExecuteController controller = new AgentExecuteController(orchestrator, auditRepo, "DEVELOPMENT");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/agent/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traceId":"t-3",
                                  "correlationId":"c-3",
                                  "principalId":"user-from-body",
                                  "roles":["user"],
                                  "message":"hello"
                                }
                                """))
                .andExpect(status().isOk());

        verify(orchestrator).runUserTurn(
                eq("t-3"),
                eq("c-3"),
                any(),
                eq("alice"),
                eq(Set.of("admin")),
                any(),
                any()
        );
    }
}
