package com.framework.agent.demo;

import com.framework.agent.core.ToolExecutionException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.core.ToolInvocationResult;
import com.framework.agent.core.ToolNames;
import com.framework.agent.demo.backend.DemoToolBackend;
import com.framework.agent.mcp.GatewayContextHolder;
import com.framework.agent.mcp.internal.McpToolExecutor;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demo {@link McpToolExecutor}: runs after governance and calls {@link DemoToolBackend}
 * (same logic as {@code /demo/backend/*} REST). Outcome strings are short summaries for UI and audit.
 */
public final class DemoMcpToolExecutor implements McpToolExecutor {

    private static final Pattern VERSION_SUFFIX = Pattern.compile("^(.+):v([0-9]+)$", Pattern.CASE_INSENSITIVE);

    private final DemoToolBackend backend;

    public DemoMcpToolExecutor(DemoToolBackend backend) {
        this.backend = backend;
    }

    @Override
    public ToolInvocationResult execute(ToolInvocationContext ctx) throws ToolExecutionException {
        if (!GatewayContextHolder.isGatewayCall()) {
            throw new IllegalStateException("Direct execution forbidden");
        }
        String rawName = ctx.toolName();
        String logical = ToolNames.logicalName(rawName);
        String versionLabel = versionLabel(rawName);
        try {
            return switch (logical) {
                case "search" -> {
                    String q = stringArg(ctx.arguments(), "q", "*");
                    var body = backend.search(q, versionLabel);
                    yield ok(body, summary("demo-backend.search", rawName, body));
                }
                case "read" -> {
                    String rid = stringArg(ctx.arguments(), "resourceId", "doc-42");
                    var body = backend.readDocument(rid);
                    yield ok(body, summary("demo-backend.read", rawName, body));
                }
                case "delete_db" -> {
                    var body = backend.adminPurge();
                    yield ok(body, summary("demo-backend.admin.purge", rawName, body));
                }
                case "payment_api" -> {
                    int cents = intArg(ctx.arguments(), "amountCents", 100);
                    var body = backend.queuePayment(cents);
                    yield ok(body, summary("demo-backend.payments", rawName, body));
                }
                default -> throw new ToolExecutionException("Demo has no backend mapping for tool: " + logical);
            };
        } catch (ToolExecutionException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ToolExecutionException("Demo tool execution failed: " + e.getMessage(), e);
        }
    }

    private static ToolInvocationResult ok(Map<String, Object> body, String summary) {
        return new ToolInvocationResult(true, summary, Duration.ofMillis(1));
    }

    private static String summary(String apiKey, String rawTool, Map<String, Object> body) {
        Object svc = body.get("service");
        return apiKey + " tool=" + rawTool + " service=" + svc;
    }

    private static String stringArg(Map<String, Object> args, String key, String defaultValue) {
        if (args == null) {
            return defaultValue;
        }
        Object v = args.get(key);
        if (v == null) {
            return defaultValue;
        }
        return String.valueOf(v);
    }

    private static int intArg(Map<String, Object> args, String key, int defaultValue) {
        if (args == null) {
            return defaultValue;
        }
        Object v = args.get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            return Integer.parseInt(s.trim());
        }
        return defaultValue;
    }

    private static String versionLabel(String toolName) {
        if (toolName == null) {
            return "default";
        }
        Matcher m = VERSION_SUFFIX.matcher(toolName.trim());
        if (m.matches()) {
            return "v" + m.group(2).toLowerCase(Locale.ROOT);
        }
        return "default";
    }
}
