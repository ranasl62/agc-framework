package com.framework.agent.demo.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.framework.agent.core.GovernedPathAuditException;
import com.framework.agent.core.InvalidGovernanceContextException;
import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationDeniedException;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.core.ToolInvocationResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal JSON-RPC-style MCP bridge: {@code tools/list} and {@code tools/call}.
 * Each {@code tools/call} goes through {@link ToolInvocationGateway} (full governance + audit).
 */
@RestController
public class DemoMcpController {

    private final ToolInvocationGateway gateway;
    private final ObjectMapper mapper;

    public DemoMcpController(ToolInvocationGateway gateway, ObjectMapper mapper) {
        this.gateway = gateway;
        this.mapper = mapper;
    }

    @PostMapping(value = "/demo/mcp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> handle(@RequestBody JsonNode body) {
        if (body == null || !body.isObject()) {
            return ResponseEntity.badRequest().body(rpcError(null, -32700, "Parse error", null));
        }
        String jsonrpc = text(body, "jsonrpc");
        JsonNode idNode = body.get("id");
        if (!"2.0".equals(jsonrpc)) {
            return ResponseEntity.badRequest().body(rpcError(idNode, -32600, "Invalid Request", null));
        }
        String method = text(body, "method");
        JsonNode params = body.get("params");
        if (method == null || method.isBlank()) {
            return ResponseEntity.badRequest().body(rpcError(idNode, -32600, "method required", null));
        }
        try {
            return switch (method) {
                case "initialize" -> ResponseEntity.ok(rpcResult(idNode, initializeResult()));
                case "tools/list" -> ResponseEntity.ok(rpcResult(idNode, toolsListResult()));
                case "tools/call" -> toolsCall(idNode, params != null && params.isObject() ? (ObjectNode) params : mapper.createObjectNode());
                default -> ResponseEntity.ok(rpcError(idNode, -32601, "Method not found: " + method, null));
            };
        } catch (Exception e) {
            return ResponseEntity.ok(rpcError(idNode, -32603, e.getMessage(), null));
        }
    }

    private ResponseEntity<JsonNode> toolsCall(JsonNode idNode, ObjectNode params) {
        String name = text(params, "name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.ok(rpcError(idNode, -32602, "params.name required", null));
        }
        JsonNode ctxNode = params.get("context");
        if (ctxNode == null || !ctxNode.isObject()) {
            return ResponseEntity.ok(rpcError(idNode, -32602, "params.context object required (traceId, correlationId, principalId, roles[])", null));
        }
        String traceId = text(ctxNode, "traceId");
        String correlationId = text(ctxNode, "correlationId");
        String tenantId = text(ctxNode, "tenantId");
        String principalId = text(ctxNode, "principalId");
        if (traceId == null || traceId.isBlank()) {
            return ResponseEntity.ok(rpcError(idNode, -32602, "context.traceId required", null));
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = traceId;
        }
        if (tenantId == null) {
            tenantId = "";
        }
        if (principalId == null) {
            principalId = "";
        }
        Set<String> roles = new HashSet<>();
        JsonNode rolesNode = ctxNode.get("roles");
        if (rolesNode != null && rolesNode.isArray()) {
            for (JsonNode r : rolesNode) {
                if (r != null && r.isTextual()) {
                    roles.add(r.asText());
                }
            }
        }
        Map<String, Object> arguments = Map.of();
        JsonNode argsNode = params.get("arguments");
        if (argsNode != null && argsNode.isObject()) {
            Map<String, Object> m = mapper.convertValue(argsNode, new TypeReference<>() {});
            arguments = m != null ? Map.copyOf(m) : Map.of();
        }
        var ctx = new ToolInvocationContext(
                traceId,
                correlationId,
                tenantId,
                principalId,
                roles,
                name,
                arguments,
                null
        );
        try {
            ToolInvocationResult result = gateway.invoke(ctx);
            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.set("content", mapper.valueToTree(List.of(Map.of(
                    "type", "text",
                    "text", result.outcomeSummary()
            ))));
            return ResponseEntity.ok(rpcResult(idNode, resultNode));
        } catch (ToolInvocationDeniedException e) {
            ObjectNode data = mapper.createObjectNode();
            data.put("decision", e.getDecision().type().name());
            data.put("reasonCode", e.getDecision().reasonCode());
            data.set("matchedRuleIds", mapper.valueToTree(e.getDecision().matchedRuleIds()));
            return ResponseEntity.ok(rpcError(idNode, 403, "Tool invocation denied", data));
        } catch (InvalidGovernanceContextException e) {
            return ResponseEntity.ok(rpcError(idNode, -32602, e.getMessage(), null));
        } catch (ToolExecutionException e) {
            return ResponseEntity.ok(rpcError(idNode, -32000, e.getMessage(), null));
        } catch (GovernedPathAuditException e) {
            return ResponseEntity.ok(rpcError(idNode, -32001, "Audit failure (fail-closed)", mapper.valueToTree(Map.of("detail", e.getMessage()))));
        }
    }

    private ObjectNode initializeResult() {
        ObjectNode n = mapper.createObjectNode();
        n.put("protocolVersion", "2024-11-05");
        ObjectNode info = mapper.createObjectNode();
        info.put("name", "agc-demo-mcp");
        info.put("version", "1.0.0");
        n.set("serverInfo", info);
        n.put("note", "This is a minimal HTTP bridge for the demo; each tools/call uses ToolInvocationGateway.");
        return n;
    }

    private ObjectNode toolsListResult() {
        ObjectNode root = mapper.createObjectNode();
        root.set("tools", mapper.valueToTree(List.of(
                toolSchema("search", "Search internal demo corpus", Map.of(
                        "type", "object",
                        "properties", Map.of("q", Map.of("type", "string", "description", "Query string"))
                )),
                toolSchema("read", "Read a synthetic document", Map.of(
                        "type", "object",
                        "properties", Map.of("resourceId", Map.of("type", "string"))
                )),
                toolSchema("delete_db", "Simulated destructive admin operation", Map.of("type", "object", "properties", Map.of())),
                toolSchema("payment_api", "Queue a demo payment", Map.of(
                        "type", "object",
                        "properties", Map.of("amountCents", Map.of("type", "integer"))
                ))
        )));
        return root;
    }

    private static Map<String, Object> toolSchema(String name, String description, Map<String, Object> inputSchema) {
        return Map.of(
                "name", name,
                "description", description,
                "inputSchema", inputSchema
        );
    }

    private ObjectNode rpcResult(JsonNode id, ObjectNode result) {
        ObjectNode out = mapper.createObjectNode();
        out.put("jsonrpc", "2.0");
        out.set("id", id == null || id.isNull() ? mapper.nullNode() : id);
        out.set("result", result);
        return out;
    }

    private ObjectNode rpcError(JsonNode id, int code, String message, JsonNode data) {
        ObjectNode out = mapper.createObjectNode();
        out.put("jsonrpc", "2.0");
        out.set("id", id == null || id.isNull() ? mapper.nullNode() : id);
        ObjectNode err = mapper.createObjectNode();
        err.put("code", code);
        err.put("message", message);
        if (data != null) {
            err.set("data", data);
        }
        out.set("error", err);
        return out;
    }

    private static String text(JsonNode parent, String field) {
        if (parent == null || !parent.has(field) || parent.get(field).isNull()) {
            return null;
        }
        JsonNode n = parent.get(field);
        return n.isTextual() ? n.asText() : n.toString();
    }
}
