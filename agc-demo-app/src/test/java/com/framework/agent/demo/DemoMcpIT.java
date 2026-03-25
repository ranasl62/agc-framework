package com.framework.agent.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoMcpIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void backendSearch_isPublicJson() throws Exception {
        mockMvc.perform(get("/demo/backend/search").param("q", "audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("demo-search-api"))
                .andExpect(jsonPath("$.hits").isArray());
    }

    @Test
    void mcpToolsList() throws Exception {
        mockMvc.perform(post("/demo/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools", hasSize(4)))
                .andExpect(jsonPath("$.result.tools[0].name").exists());
    }

    @Test
    void mcpToolsCall_search_governedAllow() throws Exception {
        String body = """
                {
                  "jsonrpc": "2.0",
                  "id": "r1",
                  "method": "tools/call",
                  "params": {
                    "name": "search",
                    "arguments": { "q": "governance" },
                    "context": {
                      "traceId": "mcp-trace-1",
                      "correlationId": "mcp-trace-1",
                      "tenantId": "demo",
                      "principalId": "u1",
                      "roles": ["user"]
                    }
                  }
                }
                """;
        mockMvc.perform(post("/demo/mcp").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].text", containsString("demo-backend.search")));
    }

    @Test
    void mcpToolsCall_payment_guardrailDeny() throws Exception {
        String body = """
                {
                  "jsonrpc": "2.0",
                  "id": 2,
                  "method": "tools/call",
                  "params": {
                    "name": "payment_api",
                    "arguments": {},
                    "context": {
                      "traceId": "mcp-trace-pay",
                      "correlationId": "mcp-trace-pay",
                      "principalId": "admin",
                      "roles": ["admin"]
                    }
                  }
                }
                """;
        mockMvc.perform(post("/demo/mcp").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(403))
                .andExpect(jsonPath("$.error.data.reasonCode").value("GUARDRAIL_block-payment"));
    }
}
