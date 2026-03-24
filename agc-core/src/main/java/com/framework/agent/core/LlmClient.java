package com.framework.agent.core;

@FunctionalInterface
public interface LlmClient {

    String complete(String userMessage, ToolInvocationContext context) throws LlmException;
}
