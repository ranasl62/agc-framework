package com.framework.agent.demo.backend;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Simulated downstream services behind governed tools. Exposed as HTTP via {@link DemoBackendController}.
 */
@Service
public class DemoToolBackend {

    public Map<String, Object> search(String query, String requestedVersion) {
        String q = query == null || query.isBlank() ? "*" : query.trim();
        String v = requestedVersion != null ? requestedVersion : "default";
        return Map.of(
                "service", "demo-search-api",
                "version", v,
                "query", q,
                "hits", List.of(
                        Map.of("id", "doc-1", "title", "AGC overview", "snippet", "Governance for tool calls."),
                        Map.of("id", "doc-2", "title", "Audit trail", "snippet", "Append-only events by traceId.")
                )
        );
    }

    public Map<String, Object> readDocument(String resourceId) {
        String id = resourceId == null || resourceId.isBlank() ? "doc-42" : resourceId.trim();
        return Map.of(
                "service", "demo-read-api",
                "resourceId", id,
                "content", "Synthetic document body for " + id + " (PII-safe demo text)."
        );
    }

    public Map<String, Object> adminPurge() {
        return Map.of(
                "service", "demo-admin-api",
                "ack", true,
                "detail", "Destructive operation simulated — no data was deleted."
        );
    }

    public Map<String, Object> queuePayment(int amountCents) {
        int amt = Math.max(0, amountCents);
        return Map.of(
                "service", "demo-payments-api",
                "status", "queued",
                "amountCents", amt,
                "reference", "pay-demo-" + System.currentTimeMillis()
        );
    }
}
