package com.framework.agent.orchestrator;

import com.framework.agent.core.LlmClient;
import com.framework.agent.core.LlmException;
import com.framework.agent.core.ToolInvocationContext;

public class EchoLlmClient implements LlmClient {

    private final String toolName;

    public EchoLlmClient(String toolName) {
        this.toolName = toolName;
    }

    @Override
    public String complete(String userMessage, ToolInvocationContext context) throws LlmException {
        return toolName;
    }
}
