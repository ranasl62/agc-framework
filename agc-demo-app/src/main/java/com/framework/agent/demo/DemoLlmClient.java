package com.framework.agent.demo;

import com.framework.agent.core.LlmClient;
import com.framework.agent.core.LlmException;
import com.framework.agent.core.ToolInvocationContext;
import com.framework.agent.orchestrator.LlmStubProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demo LLM: {@link DemoToolOverride} wins, then {@code [[tool:name]]} in the user message, then {@code agc.llm.planned-tool-name}.
 */
public final class DemoLlmClient implements LlmClient {

    private static final Pattern TOOL_TOKEN = Pattern.compile("\\[\\[tool:([a-zA-Z0-9_\\-]+)\\]\\]");

    private final LlmStubProperties stubProperties;

    public DemoLlmClient(LlmStubProperties stubProperties) {
        this.stubProperties = stubProperties;
    }

    @Override
    public String complete(String userMessage, ToolInvocationContext context) throws LlmException {
        String fromThread = DemoToolOverride.getPlannedTool();
        if (fromThread != null && !fromThread.isBlank()) {
            return fromThread;
        }
        if (userMessage != null) {
            Matcher m = TOOL_TOKEN.matcher(userMessage);
            if (m.find()) {
                return m.group(1);
            }
        }
        String planned = stubProperties.getPlannedToolName();
        return planned != null && !planned.isBlank() ? planned : "search";
    }
}
