package com.framework.agent.core;

import java.time.Duration;
import java.util.Objects;

public record ToolInvocationResult(
        boolean success,
        String outcomeSummary,
        Duration latency
) {
    public ToolInvocationResult {
        Objects.requireNonNull(outcomeSummary, "outcomeSummary");
        Objects.requireNonNull(latency, "latency");
    }
}
