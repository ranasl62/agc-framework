package com.framework.agent.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoScenarioIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void scenariosListsAll() throws Exception {
        mockMvc.perform(get("/demo/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(DemoScenario.values().length)));
    }

    @Test
    void allowSearchVersioned_succeeds() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"allow_search_versioned\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.scenario").value("allow_search_versioned"))
                .andExpect(jsonPath("$.outcomeSummary").value("echo:search:v2"));
    }

    @Test
    void unknownTool_notRegistered() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"unknown_tool_not_registered\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.denialReasonCode").value("TOOL_NOT_REGISTERED"));
    }

    @Test
    void allowSearch_succeeds() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"allow_search\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.scenario").value("allow_search"))
                .andExpect(jsonPath("$.outcomeSummary").value("echo:search"))
                .andExpect(jsonPath("$.auditUrl").exists());
    }

    @Test
    void readTool_emitsWarnAndStillSucceeds() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"allow_read_with_warn\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.outcomeSummary").value("echo:read"));
    }

    @Test
    void policyDenyForbiddenTool() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"policy_deny_forbidden_tool\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.denialReasonCode").value("POLICY_TOOL_FORBIDDEN"));
    }

    @Test
    void policyDenyNoRoles() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"policy_deny_no_roles\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.denialReasonCode").value("POLICY_NO_ROLES"));
    }

    @Test
    void guardrailDenyPayment() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"guardrail_deny_payment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.denialReasonCode").value("GUARDRAIL_block-payment"));
    }

    @Test
    void unknownScenario_returns400() throws Exception {
        mockMvc.perform(post("/demo/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"nope\"}"))
                .andExpect(status().isBadRequest());
    }
}
